# Setting up Impostor Fridays

Written for people who play Minecraft, not people who write it. No programming needed.

There are two halves:

- **Part 1** — for whoever is hosting the server (one person does this once)
- **Part 2** — for everyone who is playing, including the host (each player does this on their own PC)

Everyone needs to do Part 2. Only one person does Part 1.

---

## What you need to download

Get these four things first. Keep them all in one folder so you can find them.

| # | What | Where |
|---|---|---|
| 1 | **Fabric server launcher** | <https://fabricmc.net/use/server> |
| 2 | **Fabric API** (for 1.21.11) | <https://modrinth.com/mod/fabric-api> |
| 3 | **Simple Voice Chat** (Fabric, 1.21.11) | <https://modrinth.com/mod/simple-voice-chat> |
| 4 | **Impostor Fridays** — `impostor-fridays-1.0.0.jar` | this project's `build/libs/` folder |

> **The single most important rule:** the server and every player must use the **same Fabric API
> file** and the **same Impostor Fridays file**. Not "both 1.21.11" — literally the same downloads.
> The easiest way is for the host to send everyone the exact files they used.
>
> The Fabric *Loader* version does not need to match exactly. Fabric API and the mod jar do.

---

## Part 1 — Hosting the server

Do this on the computer that will run the server. It needs **Java 21 or newer**
(<https://adoptium.net> if you don't have it).

### Step 1: Get the server files

1. Go to <https://fabricmc.net/use/server>.
2. Set **Minecraft version** to **1.21.11**.
3. Leave **Loader version** on whatever it offers as latest — that's fine.
4. Click **Download server jar**.
5. Make a new empty folder, e.g. `ImpostorServer`, and put the downloaded file in it.

### Step 2: Run it once, then accept the EULA

1. Open a terminal / command prompt **in that folder**.
2. Run:
   ```
   java -Xmx4G -jar fabric-server-launch.jar nogui
   ```
   (The file may have a slightly longer name — use whatever you downloaded.)
3. It will stop almost immediately and complain about the EULA. **This is expected.**
4. A file called `eula.txt` has appeared. Open it, change `eula=false` to `eula=true`, and save.

### Step 3: Add the mods

1. There is now a folder called `mods`. If there isn't, make one.
2. Put these **three** files into `mods`:
   - `fabric-api-....jar`
   - `voicechat-fabric-1.21.11-....jar`
   - `impostor-fridays-1.0.0.jar`
3. Nothing else goes in there.

### Step 4: Start it for real

```
java -Xmx4G -jar fabric-server-launch.jar nogui
```

Wait for `Done (…)! For help, type "help"`. The server is up.

To stop it safely, type `stop` and press Enter. **Always** stop it this way rather than closing the
window — that's what makes sure the world is saved.

### Step 5: Let your friends connect

Everyone on the **same Wi-Fi** can join using the host's local IP address (something like
`192.168.1.42`).

For friends **not** on your Wi-Fi, you need to open port **25565** to the internet:

1. Find your router's admin page (usually `192.168.1.1` in a browser).
2. Find **Port Forwarding**.
3. Forward port **25565**, both TCP and UDP, to the hosting computer's local IP.
4. Also forward port **24454 (UDP)** — that's the one Simple Voice Chat uses. Without it, the game
   works but voice chat won't.
5. Friends then connect to your **public** IP (search "what is my IP").

> Port forwarding varies by router and some ISPs block it. If it's a hassle, a service like
> [playit.gg](https://playit.gg) gives you an address to share with no router changes.

### Step 6: Make yourself an admin

`MrBoombox840` and `SpeedTellyYT` are already admins and don't need this.

For anyone else, type this in the server console:

```
op YourMinecraftUsername
```

---

## Part 2 — Joining with Lunar Client

Every player does this, including the host.

1. Open **Lunar Client**.
2. Set the version to **1.21.11**.
3. Turn on the **Fabric** add-on for that version.
4. Open the Fabric add-on's **Mods** tab.
5. Drag in the same three jars the server uses:
   - `fabric-api-....jar`
   - `voicechat-fabric-1.21.11-....jar`
   - `impostor-fridays-1.0.0.jar`
6. Launch the game, go to **Multiplayer → Add Server**, and enter the host's address.

### Not using Lunar Client?

Install Fabric from <https://fabricmc.net/use/installer>, pick 1.21.11, then drop the same three
jars into your `.minecraft/mods` folder.

### If Lunar Client throws an error

Lunar ships its own mods (Sodium, Iris and others) which can occasionally clash. If you get a crash
or a "mixin" error **only on Lunar Client**:

> Switch to Lunar's **Vanilla Addon** module instead of the normal Fabric one. It runs Fabric
> without Lunar's bundled mods, which sidesteps the conflict.

If it still fails, double-check that your Fabric API and Impostor Fridays jars are byte-for-byte the
ones the server is running.

---

## Playing your first game

1. Everyone joins. You need **at least 2 players**.
2. An admin types `/amongussetup` and clicks through the settings. Press **Save Settings**.
3. An admin types `/start`.
4. Everyone sees their own role in big letters in the middle of the screen. **Don't say it out loud.**
5. The timer appears at the top. The shared task appears under it.
6. Play. Finish the task to win, or run out of time and the Impostor wins.
7. An admin types `/end` when you're done.

Run `/start` again whenever you want another round. Everyone keeps all their stuff.

---

## Your stuff is safe between rounds

This is worth being clear about, because it's the thing people worry about:

- `/start` and `/end` **never** delete your items, XP, or anything you built. Ever.
- Your world and seed stay exactly as they are, round after round, indefinitely.
- The one exception: the Impostor's Tracking Compass is handed out at `/start` and taken back at
  `/end`. Nothing else.

### The one command that does delete things

`/amongusreset` wipes **everyone's** inventory, ender chest and XP. It is admin-only, it warns you
first, and it does nothing until you run `/amongusreset confirm` within 30 seconds.

It does **not** touch the world, your builds, or the seed.

To start a genuinely fresh world, run `/amongusreset world` — it prints the exact steps. It
deliberately doesn't do it automatically, because a running server can't safely delete its own world
files.

---

## Troubleshooting

| Problem | Fix |
|---|---|
| "Incompatible mod set" when joining | Someone's Fabric API or mod jar differs from the server's. Re-copy the host's exact files. |
| Server won't start, mentions Java | You need Java 21+. Get it from <https://adoptium.net>. |
| Voice chat silent | Port **24454 UDP** isn't forwarded, or a player is missing the Simple Voice Chat mod. |
| `/start` says it needs 2 players | It does. Get a second person in. |
| Crash only on Lunar Client | Use Lunar's **Vanilla Addon** module (see above). |
| Compass shows as a purple/black checkerboard | The mod jar didn't load properly. Make sure it's in `mods` on **both** the server and your client. |
| An admin command says "Unknown command" | You aren't an admin. Get opped, or ask MrBoombox840 / SpeedTellyYT. |

### After a Minecraft or Fabric update

If you update anything and want to check the mod is still healthy, start the server with this extra
flag:

```
java -Dimpostorfridays.mixinAudit=true -Xmx4G -jar fabric-server-launch.jar nogui
```

Look for two `[mixin-audit] OK:` lines near the top of the log. If you see `FAILED` instead, the mod
needs rebuilding against the new version — don't run a game night on it.
