# Implementation Notes

Judgment calls, ambiguities, and known tradeoffs. Kept current as the build progresses.

## Verified toolchain versions (checked live, 2026-09-06)

All of these were resolved against the real registries rather than assumed from memory,
per Section 0 of the spec.

| Component | Version | Source of truth |
|---|---|---|
| Minecraft | `1.21.11` | `meta.fabricmc.net/v2/versions/game` — confirmed present and stable |
| Fabric Loader | `0.19.5` | `meta.fabricmc.net/v2/versions/loader` — current stable |
| Yarn mappings | `1.21.11+build.6` | `meta.fabricmc.net/v2/versions/yarn/1.21.11` — latest stable build |
| Fabric Loom | `1.17.20` | `maven.fabricmc.net` — latest **stable** (1.18.0-alpha.* exist but are alphas) |
| Fabric API | `0.141.6+1.21.11` | `maven.fabricmc.net` — latest for 1.21.11; matches the version named in the spec |
| Gradle | `9.5.1` | matches the official `fabric-example-mod` 1.21.11 branch |
| Simple Voice Chat API | `2.6.20` | `maven.maxhenkel.de` — latest published `voicechat-api` artifact |
| Simple Voice Chat (runtime jar) | `fabric-1.21.11-2.6.23` | Modrinth, for 1.21.11 Fabric |

### Loom plugin id changed
The official 1.21.11 template uses the plugin id **`net.fabricmc.fabric-loom-remap`**, not the
historical `fabric-loom`. This is easy to get wrong from memory. Its plugin marker lives at
`net/fabricmc/fabric-loom-remap/net.fabricmc.fabric-loom-remap.gradle.plugin/`.
We pin `1.17.20` rather than the template's `1.17-SNAPSHOT` so builds are reproducible.

### Mappings: Yarn, not Mojang
The current official template defaults to `loom.officialMojangMappings()`. The spec explicitly
asks for Yarn, and the class names the spec references (e.g. `net.minecraft.client.render.Camera`)
are Yarn names, so we use Yarn `1.21.11+build.6`. **Consequence:** every mixin target must be
verified against Yarn 1.21.11 mappings specifically. Under Mojang mappings that camera class
would instead be `net.minecraft.client.Camera` — the two naming schemes are not interchangeable,
and no mixin target in this project is written from memory.

### Simple Voice Chat is a soft dependency
Declared as `compileOnly` and listed under `suggests` (not `depends`) in `fabric.mod.json`, so the
mod loads and plays normally on servers without SVC installed. All SVC calls are guarded behind a
runtime presence check.

## Open items
- Nothing implemented beyond project scaffolding yet; this section grows as features land.
