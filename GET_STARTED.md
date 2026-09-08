# Getting Started — Impostor Fridays

**A complete, start-to-finish guide.** No programming knowledge needed. If you can install a
Minecraft mod, you can do this.

> **In a hurry?**
> - **Just playing?** → [Part 2](#part-2--joining-as-a-player) only. Takes about 5 minutes.
> - **Hosting the server?** → [Part 1](#part-1--hosting-the-server), then Part 2 for yourself.

---

## Contents

1. [What you're installing](#what-youre-installing)
2. [Get the files](#get-the-files)
3. [Part 1 — Hosting the server](#part-1--hosting-the-server)
4. [Part 2 — Joining as a player](#part-2--joining-as-a-player)
5. [Your first game](#your-first-game)
6. [Command reference](#command-reference)
7. [Is my stuff safe?](#is-my-stuff-safe)
8. [Troubleshooting](#troubleshooting)
9. [Building from source](#building-from-source)

---

## What you're installing

Impostor Fridays turns a normal Minecraft world into a social deduction game. One player is
secretly the Impostor. Everyone else shares one real Minecraft objective — activate a beacon, breed
a panda and a mooshroom and a goat, get everyone into diamond armour — and must finish it before
the timer runs out. The Impostor tries to stop them without being identified.

Nobody is ever eliminated. Dying just means a short respawn wait.

**Requirements:**

| | |
|---|---|
| Minecraft | **1.21.11** (exactly this version) |
| Mod loader | **Fabric** |
| Java | **21 or newer** — [get it here](https://adoptium.net) if you don't have it |
| Players | At least **2**. The Sniffer role needs **3+**. Best with 5-10. |

---

## Get the files

You need **three** `.jar` files. Everyone — the server and every single player — needs the same three.

### 1. Impostor Fridays (this mod)

Download `impostor-fridays-1.0.0.jar` from the
**[Releases page](https://github.com/UtkarshTewari24/imposter-friday-mod/releases)**.

> Or grab it from the [`dist/`](dist/) folder of this repository — it's the same file.

### 2. Fabric API

Download from **<https://modrinth.com/mod/fabric-api>**

Pick the version for **1.21.11** and **Fabric**. It'll be named something like
`fabric-api-0.141.6+1.21.11.jar`.

### 3. Simple Voice Chat *(optional but recommended)*

Download from **<https://modrinth.com/mod/simple-voice-chat>**

Pick **1.21.11** and **Fabric** — `voicechat-fabric-1.21.11-2.6.23.jar` or newer.

This adds proximity voice chat, which makes the game *much* better. The mod works fine without it.

> ### ⚠ The one rule that breaks everything if you get it wrong
>
> The server and every player must use the **same Fabric API file** and the **same Impostor Fridays
> file**. Not just "both for 1.21.11" — literally the same downloads.
>
> **The easy way:** whoever hosts downloads all three files once, then sends those exact files to
> everyone in a Discord chat. Everyone uses those.
>
> The Fabric *Loader* version does **not** need to match. The mod works on **Fabric Loader 0.17.3
> or newer**, which is deliberately loose so it still runs if your launcher ships an older loader —
> Lunar Client's Fabric add-on often lags a little behind. Fabric API and the mod jar **do** need
> to match exactly.

---

## Part 1 — Hosting the server

One person does this. It needs to be a computer that can stay on while you play, with Java 21+.

### Step 1 — Download the server launcher

1. Go to **<https://fabricmc.net/use/server>**
2. Set **Minecraft version** to **1.21.11**
3. Leave **Loader version** on whatever it shows as latest — anything recent is fine
4. Click **Download server jar**
5. Make a new empty folder, e.g. `ImpostorServer`, and put the file inside

### Step 2 — Run it once to generate the files

Open a terminal (macOS/Linux) or Command Prompt (Windows) **inside that folder**, then run:

```bash
java -Xmx4G -jar fabric-server-launch.jar nogui
```

> **Tip:** on Windows you can hold **Shift**, right-click inside the folder, and choose
> *"Open PowerShell window here"*.
>
> If your downloaded file has a longer name, use that name instead.

It will stop immediately and complain about the EULA. **That's supposed to happen.**

### Step 3 — Accept the EULA

A file called `eula.txt` has appeared. Open it in any text editor, change:

```
eula=false
```

to:

```
eula=true
```

Save and close. (This is you agreeing to
[Minecraft's EULA](https://www.minecraft.net/en-us/eula) — read it if you like.)

### Step 4 — Install the mods

1. There's now a folder called `mods`. If not, create one.
2. Put your **three** jars inside it:
   - `fabric-api-0.141.6+1.21.11.jar`
   - `voicechat-fabric-1.21.11-2.6.23.jar`
   - `impostor-fridays-1.0.0.jar`
3. Nothing else goes in there.

Your folder should look like:

```
ImpostorServer/
├── fabric-server-launch.jar
├── eula.txt
├── server.properties
└── mods/
    ├── fabric-api-0.141.6+1.21.11.jar
    ├── voicechat-fabric-1.21.11-2.6.23.jar
    └── impostor-fridays-1.0.0.jar
```

### Step 5 — Start it properly

```bash
java -Xmx4G -jar fabric-server-launch.jar nogui
```

Wait for this line:

```
Done (5.254s)! For help, type "help"
```

**You're running.** 🎉

> **Always stop the server by typing `stop` and pressing Enter** — never by closing the window.
> That's what makes sure your world is saved properly.

### Step 6 — Let friends connect

**Same house / same Wi-Fi?** They connect to your local IP, something like `192.168.1.42`.

Find it with `ipconfig` (Windows) or `ifconfig | grep inet` (Mac/Linux).

**Friends elsewhere?** You need to open two ports on your router:

| Port | Protocol | What for |
|---|---|---|
| **25565** | TCP **and** UDP | Minecraft itself |
| **24454** | UDP | Simple Voice Chat |

1. Open your router's admin page (usually `192.168.1.1` in a browser)
2. Find **Port Forwarding**
3. Forward both ports to the hosting computer's local IP
4. Friends connect to your **public** IP (google "what is my IP")

> **Forget port 24454 and everything works except voice chat** — that's the single most common
> setup mistake.
>
> **Router being difficult?** Some ISPs block this entirely. [playit.gg](https://playit.gg) or
> [Tailscale](https://tailscale.com) give you a shareable address with no router changes.

### Step 7 — Set up admins

`MrBoombox840` and `SpeedTellyYT` are **already admins** — built into the mod, nothing to do.

To add anyone else, type this into the server console:

```
op TheirMinecraftUsername
```

---

## Part 2 — Joining as a player

**Everyone does this, including the host.**

### On Lunar Client

1. Open **Lunar Client**
2. Select version **1.21.11**
3. Enable the **Fabric** add-on for that version
4. Open the Fabric add-on's **Mods** tab
5. Drag in the same three jars the server is running
6. Launch, then **Multiplayer → Add Server** and enter the host's address

### On the normal Minecraft launcher

1. Get the Fabric installer from **<https://fabricmc.net/use/installer>**
2. Run it, choose **1.21.11**, click Install
3. Put the three jars in your `mods` folder:
   - **Windows:** `%appdata%\.minecraft\mods`
   - **Mac:** `~/Library/Application Support/minecraft/mods`
   - **Linux:** `~/.minecraft/mods`
4. Launch Minecraft using the **fabric-loader-1.21.11** profile

> Create the `mods` folder yourself if it isn't there.

### On PrismLauncher / MultiMC

Create a 1.21.11 instance, add Fabric, then drag the three jars into the instance's mods folder.

---

## Your first game

1. **Everyone joins.** You need at least 2 players (3+ if you want the Sniffer).
2. An admin types `/amongussetup` — a settings panel appears in chat with clickable buttons.
3. Click through the settings. **First time, just click `[ Save Settings ]` — the defaults are good.**
4. An admin types `/start`.
5. **Everyone sees their own role** in big letters in the middle of their screen.
   **Don't say it out loud.** Nobody else is told your role, ever.
6. A **timer** appears at the top, with the **shared task** underneath it.
7. Play Minecraft. Do the task. Watch each other.
8. When you're done, an admin types `/end`.

Run `/start` again for another round whenever you like. **Everyone keeps everything.**

### Choosing what to do

In `/amongussetup` you pick either a **preset set** of three objectives, or **Random** (one task
drawn from the difficulty pool).

| Set | Feel | Rough length |
|---|---|---|
| **Set 1 — First Light** | Gentle. Good for a first game. | ~50 min |
| **Set 2 — Groundwork** | The standard game. | ~70 min |
| **Set 3 — Deep Cuts** | Heavier — mining and a village project. | ~80 min |
| **Set 4 — The Long Haul** | Hard. One big objective at its centre. | ~90 min |
| **Set 5 — Endgame** | Very hard. The Ender Dragon. | ~90 min |
| **Set 6 — Scattered** | Everyone ends up somewhere different. | ~75 min |

**Start with Set 1.** Each set is three objectives and the Innocents must finish **all three**,
with progress shown on everyone's HUD.

Each one mixes a hard anchor, something that forces the group to split across biomes, and a quick
win so a bad start never feels hopeless — that mix is what keeps it fair for both sides.

### Suggested first-time settings

Real games are 90 minutes. For your first test, try:

| Setting | Try |
|---|---|
| Game length | **15 minutes** |
| Task set | **Set 1 — First Light** |
| Ability cooldown | **60 seconds** |

That way you see the whole loop quickly. Bump them back up once everyone knows the rules.

---

## Command reference

### Everyone

| Command | Who | What it does |
|---|---|---|
| `/sniff` | Sniffer | Opens the suspect picker. Right guess strips one of the Impostor's abilities permanently. |
| `/steal <player>` | Impostor | Opens their inventory. You may take **one** stack. |
| `/swap <p1> <p2>` | Impostor | Swaps two players' locations, even across dimensions. |
| `/blind` | Impostor | Blinds **every** Innocent at once. |
| `/gravity` | Impostor | Flips everyone else's world upside down. |
| `/invis` | Impostor | Total invisibility — no body, armour, held item or nametag. |

**All five Impostor abilities share one cooldown** (5 minutes by default). Using any one locks all
of them. Dying does **not** cost you your role or abilities.

Right-click the **Tracking Compass** (the Impostor gets one automatically) to pick someone to track.
The needle follows them across dimensions.

### Admins

| Command | What it does |
|---|---|
| `/start` | Begins a match. Needs 2+ players. |
| `/end` | Ends it and clears everything. Safe to run when nothing is running. |
| `/amongussetup` | The settings panel. |
| `/amongusreset` | **Destructive** — wipes everyone's items and XP. Asks you to confirm first. |

---

## Is my stuff safe?

**Yes.** This matters enough to be blunt about:

- ✅ `/start` and `/end` **never** touch your items, XP, or anything you've built
- ✅ Your world and seed persist across matches **indefinitely**
- ✅ Keep-inventory is enforced — you never drop your items when you die
- ⚠️ The **only** exception: the Impostor's Tracking Compass is handed out at `/start` and taken
  back at `/end`. Nothing else.

### The one command that deletes things

`/amongusreset` wipes **everyone's** inventory, ender chest and XP.

- It's admin-only
- It warns you first and does nothing until you run `/amongusreset confirm` within 30 seconds
- It does **not** touch the world, your builds, or the seed

To start a genuinely fresh world, run `/amongusreset world` — it prints the exact steps. It
deliberately doesn't do it for you, because a running server can't safely delete its own world files.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| **"Incompatible mod set!"** when joining | Someone's Fabric API or mod jar is different from the server's. Re-copy the host's exact files. This is the #1 cause of join failures. |
| Server won't start, says something about Java | You need **Java 21+**. Get it from [adoptium.net](https://adoptium.net). Check with `java -version`. |
| Voice chat is silent | Port **24454 UDP** isn't forwarded, or someone is missing the Simple Voice Chat mod. |
| Compass is a purple/black checkerboard | The mod jar didn't load. Make sure it's in `mods` on **both** the server and your client. |
| `/start` says it needs 2 players | It does. Get a second person in. |
| Sniffer never gets assigned | The Sniffer needs **3+** players, and must be enabled in `/amongussetup`. |
| An admin command says "Unknown command" | You're not an admin. Get `op`'d, or ask MrBoombox840 / SpeedTellyYT. |
| Crash **only** on Lunar Client | Switch to Lunar's **Vanilla Addon** module instead of the normal Fabric one. It runs Fabric without Lunar's own bundled mods (Sodium, Iris), which sidesteps the conflict. |
| Camera stuck upside down | Shouldn't be possible — it resets on death, respawn, disconnect and `/end`. If it happens, please [open an issue](https://github.com/UtkarshTewari24/imposter-friday-mod/issues) and say exactly what you were doing. |
| Can't connect from outside your network | Port forwarding isn't set up, or your ISP blocks it. Try [playit.gg](https://playit.gg). |

### Checking the mod is healthy after an update

If you update Minecraft or Fabric and want to confirm the mod still works, start the server with:

```bash
java -Dimpostorfridays.mixinAudit=true -Xmx4G -jar fabric-server-launch.jar nogui
```

Look for two `[mixin-audit] OK:` lines near the top of the log. If either says `FAILED`, the mod
needs rebuilding for the new version — don't run a game night on it.

### Still stuck?

[Open an issue](https://github.com/UtkarshTewari24/imposter-friday-mod/issues) with:

- What you were trying to do
- What happened instead
- The last ~30 lines of the server console

---

## Building from source

Only needed if you want to change the mod. You need **JDK 21**.

```bash
git clone https://github.com/UtkarshTewari24/imposter-friday-mod.git
cd imposter-friday-mod

./gradlew build          # jar appears in build/libs/
./gradlew test           # run the unit tests
./gradlew runServer      # local test server
./gradlew runClient      # local test client
```

On Windows use `gradlew.bat` instead of `./gradlew`.

If Gradle can't find Java 21:

```bash
# macOS (Homebrew)
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
```

> **Heads up:** the first build downloads Minecraft, mappings and Gradle — roughly **1 GB** and
> several minutes. After that it's cached and fast.
>
> If the build fails on `libraries.minecraft.net`, that host is being blocked — a VPN or DNS
> filter is the usual cause. Turn it off for the first build.

### Project layout

```
src/main/java/com/impostorfridays/
├── game/       roles, state, abilities, tracking, death, sniffer
├── task/       the data-driven task system (25 tasks)
├── command/    /start /end /amongussetup /amongusreset, permissions
├── net/        client/server packets
├── screen/     the /steal container
├── item/       Tracking Compass
├── voice/      Simple Voice Chat integration
└── mixin/      death message anonymising, respawn gating

src/client/java/com/impostorfridays/client/
├── hud/        timer, cooldown, role reveal, sniff cue
├── screen/     the shared player picker
└── mixin/      camera flip, death screen countdown
```

**Adding a task** is one entry in `TaskPools.java` — no core game logic to touch.

More detail in [IMPLEMENTATION_NOTES.md](IMPLEMENTATION_NOTES.md).

---

## Before your first real game night

Please run through **[MANUAL_TEST_CHECKLIST.md](MANUAL_TEST_CHECKLIST.md)** — at minimum Test 1,
which takes two players about 15 minutes and catches the obvious problems before they ruin a
90-minute session.

Have fun. Trust no one. 🔪
