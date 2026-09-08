# Implementation Notes

Judgment calls, ambiguities in the spec, and known tradeoffs.

---

## 1. Verified toolchain versions

Everything below was resolved against the live registries at build time rather than assumed,
per Section 0 of the spec.

| Component | Version | How it was confirmed |
|---|---|---|
| Minecraft | `1.21.11` | present and stable in `meta.fabricmc.net/v2/versions/game` |
| Fabric Loader | built against `0.19.5`, **requires `>=0.17.3`** | current stable from `meta.fabricmc.net/v2/versions/loader`; the floor matches Fabric API's own requirement (see below) |
| Yarn mappings | `1.21.11+build.6` | latest stable build for 1.21.11 |
| Fabric Loom | `1.17.20` | latest **stable** on `maven.fabricmc.net` (1.18.0-alpha.* exist but are alphas) |
| Fabric API | `0.141.6+1.21.11` | latest for 1.21.11; matches the version named in the spec |
| Gradle | `9.5.1` | matches the official `fabric-example-mod` 1.21.11 branch |
| Simple Voice Chat API | `2.6.20` | latest `voicechat-api`; confirmed to be exactly what SVC 2.6.23 bundles |
| Simple Voice Chat (runtime) | `fabric-1.21.11-2.6.23` | Modrinth, for 1.21.11 Fabric |

### The Loom plugin id changed
The 1.21.11 template uses **`net.fabricmc.fabric-loom-remap`**, not the historical `fabric-loom`.
Its plugin marker lives at `net/fabricmc/fabric-loom-remap/net.fabricmc.fabric-loom-remap.gradle.plugin/`.
We pin `1.17.20` rather than the template's `1.17-SNAPSHOT` so builds are reproducible.

### Mappings: Yarn, not Mojang
The current official template defaults to `loom.officialMojangMappings()`. The spec asks for Yarn,
and the class names it references (e.g. `net.minecraft.client.render.Camera`) are Yarn names, so we
use Yarn `1.21.11+build.6`. Under Mojang mappings that class would be `net.minecraft.client.Camera` —
the two schemes are not interchangeable.

---

## 2. 1.21.11 API changes that would have broken this mod

Each of these was found by checking real mappings or bytecode. Every one compiles fine if you guess
wrong from an older version's memory — and then fails.

| What changed | Old (from memory) | Actual 1.21.11 |
|---|---|---|
| Command permissions | `source.hasPermissionLevel(int)` | Gone. `PermissionPredicate.hasPermission(Permission)`; use `new Permission.Level(PermissionLevel.GAMEMASTERS)` |
| Game rules | `net.minecraft.world.GameRules`, mutable | `net.minecraft.world.rule.GameRules`, an immutable value map; no `server.getGameRules()` — it's on `ServerWorld` |
| Player name | `GameProfile.getName()` | `GameProfile.name()` — it's a record in authlib 7.x |
| Entity world | `entity.getWorld()` | `entity.getEntityWorld()` |
| Chat events | `new ClickEvent(Action.RUN_COMMAND, str)` | Sealed interface: `new ClickEvent.RunCommand(str)`, `new HoverEvent.ShowText(text)` |
| GUI clicks | `mouseClicked(double, double, int)` | `mouseClicked(Click, boolean)` |
| **Camera** | `update(BlockView, …)` | `update(World, Entity, boolean, boolean, float)` |

### The camera mixin specifically
The spec flagged this as a historical failure point, so it was built from the actual bytecode of
`Camera.setRotation`, not from recall:

```java
this.rotation.rotationYXZ((float)Math.PI - yaw * 0.017453292f, -pitch * 0.017453292f, 0.0f);
HORIZONTAL.rotate(this.rotation, this.horizontalPlane);
VERTICAL.rotate(this.rotation, this.verticalPlane);
DIAGONAL.rotate(this.rotation, this.diagonalPlane);
```

The third argument to `rotationYXZ` is **roll**. The flip overrides it from `0` to `PI`. The three
derived basis vectors must then be recomputed — skipping that leaves movement and projection out of
sync with what is drawn.

### MixinAudit, and why dev-only testing is not enough
Mixins apply lazily, when their target class is first loaded. `DeathScreen` and `Camera` are not
loaded during a normal startup, so a broken injection would stay invisible until someone actually
died or `/gravity` fired mid-session. `MixinAudit` force-loads every mixin target at boot, turning
that into an immediate, obvious log failure. It runs automatically in dev, and can be enabled on a
real server with `-Dimpostorfridays.mixinAudit=true`.

**The dev environment and a real server do not run the same class names.** Dev uses named (Yarn)
mappings; a built jar is remapped to intermediary, so at runtime on a real server
`DamageTracker` is actually `net.minecraft.class_1283`. The first version of this audit used
`Class.forName("net.minecraft.entity.damage.DamageTracker")`, which passed in dev and then threw
`ClassNotFoundException` on a production server — reporting a mixin failure that did not exist.

The fix is that targets are **class literals behind suppliers**, not name strings: Loom rewrites
class literals to the correct intermediary name at build time, but it cannot rewrite the contents
of a string. The suppliers keep resolution inside the try block rather than during the audit
class's own initialisation.

This was only caught by running the packaged jar on a genuine standalone Fabric server, which is
worth doing after any Minecraft or Fabric update.

---

## 3. Design decisions where the spec left room

### `/amongussetup` is a chat panel, not a chest GUI
The spec says "config UI" without specifying a form. It's built as an interactive chat panel with
clickable `[-] / [+] / [ON|OFF]` controls and a **Save Settings** button.

*Why:* it needs no client-side screen, works on vanilla clients, and every control is a real click.
A container GUI would have needed a custom `ScreenHandler` with click interception for what is an
admin-only settings screen. The complexity budget went to the player picker and the steal container
instead, which players actually interact with. Settings are also reachable non-interactively via
`/amongussetup set <key> <value>`, which is what made the end-to-end console test possible.

### Anonymised death messages hide the victim too
The spec's example is literally `"Player killed by Player"`, so that is what is used. Note this
hides **who died** as well as who killed them. If you'd rather name the victim, it's one line in
`DamageTrackerMixin`.

The injection is on `DamageTracker.getDeathMessage()` rather than at the broadcast site, because the
chat broadcast and the packet that fills the victim's own death screen both read that one method —
so a single override closes both leaks. Natural deaths (fall, lava, mobs) are untouched.

### The locked compass slot in `/steal`
The spec asks for the Tracking Compass to be "greyed out / not clickable". A vanilla client cannot
render a greyed-out slot in a standard container, so the slot shows a **barrier item named
"Tracking Compass (locked)"** and every click type on it is dropped server-side. It reads as
deliberately locked rather than merely absent.

In practice this is defensive: only the Impostor is given a compass, and they are the one stealing.

### `/steal` consumes the cooldown on **open**, not on taking an item
Otherwise the Impostor could open everyone's inventory for free reconnaissance and simply never take
anything.

### `/gravity` uses levitation
Minecraft has no gravity-inversion API. Levitation is the closest playable approximation, paired
with the 180° client camera flip. Slow-falling is applied when it expires so the ability is never
lethal on its own.

### The Sniffer needs 3+ players
With 2 players, assigning a Sniffer would make both roles known immediately. Below 3 players the
game runs Impostor vs Innocent regardless of the config setting.

### Baby-animal scanning instead of a breeding event
Fabric has no breeding event. Breeding tasks are satisfied by periodically scanning loaded entities
for baby animals of the required types. Simpler, and it naturally covers any route the players find
to a baby animal.

### `/amongusreset world` prints instructions rather than acting
A running server cannot safely delete its own save directory — that means unlinking files out from
under open handles, risking a corrupted or half-deleted world. The spec explicitly asked for world
regeneration to be "a clearly separate, explicitly confirmed step", so this command prints the exact
four-step manual procedure and deliberately does not act. **This is the one part of the spec that is
documented rather than automated**, and it is called out here and in SETUP.md.

---

## 4. Task sets and how they are balanced

A set is three objectives that the Innocents must ALL complete. Every set is built to the same
shape, and the shape is the balance:

- **One anchor** — the hard objective that actually decides the match
- **One spread** — needs several biomes or dimensions, so the group must split up and people end
  up alone together. This is what generates suspicion; a set everyone can do in one place produces
  no social gameplay at all.
- **One light** — a quick win, so a bad start never feels hopeless and the Impostor can't win by
  simply stalling the opening twenty minutes.

Two constraints are enforced by unit test rather than by care:

- **No set stacks two boss-tier objectives** (Wither plus Dragon). That cannot be finished inside
  a match, which is an automatic Impostor win.
- **Every set's task ids must resolve.** `TaskSet.resolve()` silently skips ids it cannot find, so
  a single typo would quietly drop an objective and nobody would notice until a match ran short.

Estimates assume 4-8 players with an Impostor actively disrupting, and are asserted to sit between
30 and 90 minutes.

### Item objectives measure the increase, not the amount held

The world persists indefinitely between matches, which quietly broke eight tasks: "obtain a Goat
Horn" completed the instant anyone still had one from a previous round, and by the third match
several objectives would win the game immediately.

Item checks now use `TaskContext.gained(item)`, which compares against a baseline captured at
`/start` — and, like the advancement baseline, that baseline is per player and captured on first
sight so a late joiner's inventory cannot satisfy the objective for everyone.

Two tasks were also re-tiered after checking what they actually require: filling a shulker box
needs End City shells, and a woodland mansion can be thousands of blocks away. Both moved from
STANDARD to HARD.

## 5. Known tradeoffs

### Admins are identified by username, not UUID
Per the spec, `Permissions.ADMIN_USERNAMES` holds `MrBoombox840` and `SpeedTellyYT` as **usernames**.
**If either of them changes their Mojang username, their admin access silently stops working** — the
check will simply stop matching, with no error explaining why.

Server operator level 2+ works as an alternative path, so this is recoverable, but it is a real
sharp edge. Switching to UUIDs would fix it permanently and is a small change to one constant.

### `/amongusreset` only affects players who are online
Offline players keep their items until they log in and are reset manually. The command says so when
it runs.

### The compass needle across dimensions
A lodestone target in another dimension just makes the vanilla needle spin. Targets are therefore
projected into the holder's own dimension, with the 8:1 Nether scale applied, so the needle gives a
meaningful bearing. It points toward where the target *would* be, which is the useful behaviour, but
it is an approximation rather than a true 3D bearing.

### `/start` enables `keepInventory`
Required by the spec (players must never lose items on death). This is a world-state change that
persists after the match ends — it is not reverted on `/end`, since reverting it would risk item
loss in the gap between rounds.

---

## 6. What has and has not been verified

**Verified automatically:**
- Compiles cleanly; `runServer` and `runClient` both launch with zero errors
- All four mixins confirmed applying at runtime via MixinAudit
- 17 unit tests: config round-trip, clamping, malformed-file recovery, shared-cooldown semantics,
  timer expiry, role lookup, death timers, task pool integrity
- End-to-end console test: `/amongussetup` renders and mutates, `save` writes correct values to
  disk, `/start` refuses below 2 players, `/end` no-ops safely, `/amongusreset` warns
- The Tracking Compass model resolves with no missing-model or missing-texture errors
- **The packaged jar was tested on a real standalone Fabric server** (official 1.21.11 server
  launcher + Fabric API 0.141.6, no dev environment): it boots cleanly, and both server-side mixin
  targets load correctly under intermediary mappings
- The mod loads and runs correctly with Simple Voice Chat **absent**, confirming the soft dependency

**NOT verified — needs real players.** See [MANUAL_TEST_CHECKLIST.md](MANUAL_TEST_CHECKLIST.md).
Nothing involving two or more simultaneous human players has been exercised: role assignment in a
real match, the abilities actually firing, the gravity flip on screen, the steal container being
dragged in, the death screen countdown, or voice chat muting.
