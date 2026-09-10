# Impostor Fridays

An Among Us-style social deduction game mode for Minecraft — where **Minecraft itself is the game board**.

Everyone joins a normal Minecraft world for a timed match (90 minutes by default). One player is
secretly the Impostor. Everyone else shares one real Minecraft objective — activate a beacon, breed
a panda and a mooshroom and a goat, get the whole crew into diamond armour — and has to finish it
before the clock runs out.

The Impostor's job is to stop them, using abilities that sow confusion without ever making it
obvious who they are. Nobody is ever eliminated: death is a short respawn timer, never the end of
your game.

- **Minecraft version:** 1.21.11
- **Mod loader:** Fabric
- **Requires:** Fabric API
- **Optional:** [Simple Voice Chat](https://modrinth.com/mod/simple-voice-chat)

## ▶ Start here

**[📖 GET_STARTED.md](GET_STARTED.md) — the complete step-by-step guide.**
Hosting a server, joining as a player, your first game, and troubleshooting. No programming needed.

**[⬇ Download the mod](https://github.com/UtkarshTewari24/imposter-friday-mod/releases)** from the
Releases page.

---

## Roles

| Role | Who | What they do |
|---|---|---|
| **Innocent** | Everyone else | Work on the shared task. Can be tracked, blinded, robbed and flipped upside down. |
| **Impostor** | One random player | Gets a Tracking Compass. Has five abilities on one shared cooldown. Wants the clock to run out. |
| **Sniffer** | One random player (optional) | Can test one suspect at a time. Guessing right permanently strips one of the Impostor's abilities. |

You only ever see **your own** role. Nobody is told anyone else's, ever.

## The Impostor's abilities

All five share **one cooldown** (5 minutes by default) — using any one locks all of them.

| Command | Effect |
|---|---|
| `/steal <player>` | Opens a real chest-style view of that player's inventory. You may take **one** stack. |
| `/swap <player1> <player2>` | Silently swaps two players' locations, even across dimensions. |
| `/blind` | Blinds and weakens **every** Innocent at once. |
| `/gravity` | Flips gravity for **everyone on the server**, the Impostor included. Anyone in a **boat** is immune. |
| `/hunt` | The kill window: total invisibility **plus Strength and Speed**, all for the same duration. |

Dying does **not** cost you the Impostor role or any of your abilities.

## The Sniffer

`/sniff` opens a player picker. Pick the real Impostor and one of their abilities is gone for the
rest of the match. Pick wrong and you have simply learned that person is innocent.

The person you sniffed is told it happened — but **nobody else can tell**. There are no particles
or sounds a bystander could use to work out who was tested.

## Commands

### Anyone
| Command | Description |
|---|---|
| `/sniff` | Sniffer only — opens the suspect picker. |
| `/steal`, `/swap`, `/blind`, `/gravity`, `/hunt` | Impostor only. |

### Admins
Admins are the usernames hardcoded in `Permissions.ADMIN_USERNAMES` (`MrBoombox840`,
`SpeedTellyYT`) **or** anyone with server operator level 2+.

| Command | Description |
|---|---|
| `/start` | Begins a match. Needs at least 2 players. |
| `/end` | Ends the match and clears all state. Safe to run when nothing is running. |
| `/amongussetup` | Opens the clickable settings panel. |
| `/amongusreset` | **Destructive.** Wipes everyone's inventory, ender chest and XP. Requires confirmation. |

## Your world is never wiped by accident

This matters enough to state plainly:

- **`/start` and `/end` never touch your items, XP, builds, or the world.** Everything you find and
  build persists across matches, indefinitely.
- The only exception is the Tracking Compass: it is given to the Impostor at `/start` and removed
  from everyone at `/end`, so a previous round's Impostor isn't left holding one.
- **The only thing that wipes anything is `/amongusreset`**, which is admin-only and asks you to
  confirm first.

## Settings

`/amongussetup` opens a clickable panel covering match length, respawn delay, the Impostor
cooldown, which abilities are enabled and how long each lasts, the Sniffer and its cooldown, task
difficulty, and voice chat muting. Press **Save Settings** to write them to
`config/amongusgame.properties`.

### Objectives

Every match randomly picks **one of three formats**:

| Format | What it means |
|---|---|
| **3 Objectives** | Three independent sub-tasks, always from three *different* categories |
| **Major Objective** | One large objective that contains substantial work by itself |
| **5 Advancements** | Five specific vanilla advancements |

And **one of five difficulties**:

| | Intended feeling |
|---|---|
| 🟢 **Beginner** | "We can definitely do this." |
| 🔵 **Standard** | "We'll need to split up." |
| 🟠 **Advanced** | "Okay, we need a real plan." |
| 🔴 **Expert** | "This is going to take most of the game." |
| 🟣 **Master** | "We need basically everyone contributing efficiently." |

There are **275 tasks and 98 advancement entries** across the tiers, so games rarely repeat.
Objectives are always objectively measurable — never "build a nice house".

Anything needing the whole group says **"(excluding Impostor)"**, so the Impostor can't sabotage
it by simply refusing to take part.

## Building from source

```bash
./gradlew build          # jar lands in build/libs/
./gradlew test           # unit tests
./gradlew runServer      # local dev server
./gradlew runClient      # local dev client
```

Requires JDK 21.

## Documentation

- **[GET_STARTED.md](GET_STARTED.md) — the full tutorial. Start here.**
- [SETUP.md](SETUP.md) — condensed install reference
- [MANUAL_TEST_CHECKLIST.md](MANUAL_TEST_CHECKLIST.md) — what to test with real players
- [IMPLEMENTATION_NOTES.md](IMPLEMENTATION_NOTES.md) — judgment calls and known tradeoffs
