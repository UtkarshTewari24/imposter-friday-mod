# Manual Test Checklist

Everything in this list needs **real people on a real server**. None of it can be verified without
players, so please work through it before your first proper game night.

You don't need to be technical. Each check is "do this, expect that".

**Reporting a problem:** note which numbered check failed, what you expected, what happened, and
grab the server console text if anything went red.

---

## Test 1 — Two players, 15 minutes

Just you and one friend. This catches the big, obvious breakages.

**Setup**
- [ ] 1.1 Both players join the server successfully
- [ ] 1.2 Admin runs `/amongussetup` — the settings panel appears with clickable buttons
- [ ] 1.3 Click `[+]` and `[-]` on Game length — the number changes
- [ ] 1.4 Set game length to **5 minutes** (faster testing), click **Save Settings**
- [ ] 1.5 Run `/amongussetup` again — it still shows 5 minutes (it saved)

**Starting**
- [ ] 1.6 With only ONE player online, `/start` refuses and says it needs 2 players
- [ ] 1.7 With both online, `/start` works
- [ ] 1.8 Each player sees a big role announcement in the centre of their own screen
- [ ] 1.9 The announcement fades away after a few seconds on its own
- [ ] 1.10 **Neither player is told the other's role anywhere**
- [ ] 1.11 A timer appears at the top of the screen for both players
- [ ] 1.12 The shared task appears under the timer, and it's the same text for both
- [ ] 1.13 The timer counts down at the same rate on both screens (compare out loud)

**Roles**
- [ ] 1.14 The Impostor has a **Tracking Compass** in their inventory
- [ ] 1.15 The compass looks like a real compass — **not** a purple/black checkerboard
- [ ] 1.16 It still looks right in the hotbar and in the hand, not just the inventory
- [ ] 1.17 Only the Impostor sees the cooldown readout on the right side of the screen
- [ ] 1.18 The Innocent sees **no** cooldown readout at all

**Compass**
- [ ] 1.19 Impostor right-clicks the compass — a dark player-picker menu opens
- [ ] 1.20 The other player's head and name are shown, and the heads look correct
- [ ] 1.21 There's a **Nearest Player** entry at the top
- [ ] 1.22 Clicking a player closes the menu and confirms who is being tracked
- [ ] 1.23 The needle points toward that player — walk around and confirm it follows
- [ ] 1.24 Have the target go to the Nether. The needle still points sensibly, not spinning randomly

**Ending and restarting** *(this cycle is important — please do it twice)*
- [ ] 1.25 `/end` — the timer and task disappear from both screens immediately
- [ ] 1.26 The Impostor's Tracking Compass is gone from their inventory
- [ ] 1.27 `/end` again — says "No game is running", doesn't error
- [ ] 1.28 `/start` immediately after — a brand new game begins normally
- [ ] 1.29 Roles were re-rolled (play a few rounds; they shouldn't always be the same)
- [ ] 1.30 The timer restarted from full, not from where the last one stopped

**Your stuff survives**
- [ ] 1.31 Before `/start`, note exactly what's in your inventory and your XP level
- [ ] 1.32 Run `/start`, then `/end`. Inventory and XP are **completely unchanged**
- [ ] 1.33 Build something. `/start`, `/end`. It's still there

---

## Test 2 — Death and respawn (2 players)

- [ ] 2.1 During a game, have one player kill the other
- [ ] 2.2 Chat says **"Player killed by Player"** — the killer's name does **not** appear
- [ ] 2.3 **On the victim's own death screen**, the killer's name does **not** appear either
- [ ] 2.4 The Respawn button is greyed out and shows a countdown **on the button**, e.g. "Respawn in 7 seconds"
- [ ] 2.5 The countdown ticks down
- [ ] 2.6 Clicking the button early does nothing
- [ ] 2.7 When it hits zero, the button becomes clickable and says "Respawn"
- [ ] 2.8 After respawning, **nothing was lost from the inventory**
- [ ] 2.9 The timer at the top kept running the whole time and is still correct
- [ ] 2.10 If the Impostor died: they are **still the Impostor**, still have the compass, and their cooldown is unchanged
- [ ] 2.11 Now die naturally (fall, lava, drowning). The message is the **normal** Minecraft one, with the real cause
- [ ] 2.12 Run a few commands right after dying — nothing is broken

---

## Test 3 — Impostor abilities (2 players minimum, 3+ better)

Set the ability cooldown to something short (30 seconds) in `/amongussetup` while testing.

**Shared cooldown**
- [ ] 3.1 Use any ability. The cooldown readout starts counting down
- [ ] 3.2 Immediately try a **different** ability — it's refused, because all five share one cooldown
- [ ] 3.3 Wait for it to reach zero. It says "Ability Ready"
- [ ] 3.4 An Innocent typing `/blind` gets a clear refusal, not a crash

**/swap**
- [ ] 3.5 `/swap PlayerA PlayerB` — they trade places
- [ ] 3.6 `/swap PlayerA PlayerA` — refused with a clear message
- [ ] 3.7 Put one player in the Nether, then swap. Both end up in the right place and don't fall through the world

**/blind**
- [ ] 3.8 `/blind` — **every** Innocent goes near-totally blind at the same time
- [ ] 3.9 The Impostor is **not** affected
- [ ] 3.10 No particle effects or status-effect icons appear that would reveal who is affected
- [ ] 3.11 Nametags are hidden while it lasts
- [ ] 3.12 It wears off cleanly at the configured duration

**/invis**
- [ ] 3.13 `/invis` — the Impostor is completely invisible to others
- [ ] 3.14 **No armour is visible** (put armour on first and check)
- [ ] 3.15 **No held item is visible** (hold a torch and check)
- [ ] 3.16 No nametag, no particles
- [ ] 3.17 Everything comes back correctly when it ends — armour, held item, nametag

**/steal**
- [ ] 3.18 `/steal <player>` opens a chest-style window titled with their name
- [ ] 3.19 You can see their hotbar, main inventory, and armour
- [ ] 3.20 It behaves like a real chest — pick items up, move them, put them down
- [ ] 3.21 Take **one** stack into your inventory. A message confirms it
- [ ] 3.22 Try to take a **second** stack — refused
- [ ] 3.23 Close the window. The target has lost **exactly** the one stack, nothing more
- [ ] 3.24 The target's other items are all still there and correct

**/gravity** *(please test with 2+ affected players at once)*
- [ ] 3.25 `/gravity` — everyone except the Impostor sees the world flip upside down
- [ ] 3.26 The Impostor's view is normal
- [ ] 3.27 You can still move and look around while flipped — it's playable, not frozen
- [ ] 3.28 Multiple affected players at once all work correctly
- [ ] 3.29 When it ends, the camera returns to normal **by itself**
- [ ] 3.30 **Die while flipped.** After respawning, the camera is the right way up
- [ ] 3.31 **Disconnect and reconnect while flipped.** Camera is normal
- [ ] 3.32 Run `/end` while it's active. Camera returns to normal immediately
- [ ] 3.33 Nobody takes fall damage from the ability itself

---

## Test 4 — The Sniffer (3+ players)

Turn the Sniffer on in `/amongussetup` first. It needs at least 3 players.

- [ ] 4.1 With 3+ players, `/start` assigns exactly one Sniffer
- [ ] 4.2 The Sniffer sees their **own** cooldown readout
- [ ] 4.3 `/sniff` opens a picker that looks **the same** as the compass picker
- [ ] 4.4 There is **no** "Nearest Player" entry here (sniffing needs a deliberate choice)
- [ ] 4.5 Sniff an Innocent — you're told they're not the Impostor
- [ ] 4.6 Sniff the actual Impostor — you're told you found them, and which ability was stripped
- [ ] 4.7 The Impostor is told one of their abilities is gone
- [ ] 4.8 The Impostor tries that ability — it's refused, permanently
- [ ] 4.9 It's **still** gone after the Impostor dies and respawns
- [ ] 4.10 The sniffed player gets an obvious cue (sound + on-screen)
- [ ] 4.11 **A third player standing right next to them sees and hears nothing.** This is the important one — no particles, no sound, no way to tell a sniff happened
- [ ] 4.12 Sniffing goes on cooldown afterwards
- [ ] 4.13 An Innocent typing `/sniff` is refused

---

## Test 5 — Voice chat (3+ players, needs Simple Voice Chat)

- [ ] 5.1 Proximity voice chat works normally before starting a game
- [ ] 5.2 Turn **Mute dead players** ON in `/amongussetup`
- [ ] 5.3 During a game, a dead player **cannot** be heard by living players
- [ ] 5.4 A dead player **cannot** hear living players
- [ ] 5.5 After respawning, they can hear and be heard again
- [ ] 5.6 Turn the setting OFF — dead players can talk normally
- [ ] 5.7 Voice chat still works while `/invis` is active
- [ ] 5.8 Voice chat still works while `/gravity` is active
- [ ] 5.9 Voice chat still works after a `/swap`
- [ ] 5.10 **Also test with Simple Voice Chat NOT installed** — the mod should load and play perfectly normally, just with no voice features

---

## Test 6 — Tasks

- [ ] 6.1 Set difficulty to **Easy**, `/start`. The task shown is an easy one
- [ ] 6.2 Set difficulty to **Hard**, `/start`. The task is a hard one
- [ ] 6.3 Run `/start` several times — you don't always get the same task
- [ ] 6.4 Actually complete a task. A completion message is broadcast and the Innocents win
- [ ] 6.5 **Important:** if a task mentions an advancement you already earned in a previous round on this world (e.g. "enter the Nether"), it must **not** complete instantly. It has to be done again this match
- [ ] 6.6 Let the timer run to zero without finishing. The Impostor wins and the game ends cleanly

---

## Test 7 — The reset command

⚠ **This deletes items. Test it before you care about your stuff, or on a throwaway world.**

- [ ] 7.1 A non-admin, non-op runs `/amongusreset` — refused
- [ ] 7.2 An admin runs `/amongusreset` — a red warning appears, **nothing is deleted yet**
- [ ] 7.3 Wait more than 30 seconds, then `/amongusreset confirm` — refused as expired
- [ ] 7.4 `/amongusreset` then `/amongusreset confirm` promptly — inventories, ender chests and XP are cleared for everyone
- [ ] 7.5 **Your builds and the world are untouched**
- [ ] 7.6 `/amongusreset world` prints manual instructions and does **not** delete anything

---

## Test 8 — Full playtest (6+ players, a real 90-minute game)

This is about whether it's *fun*, not whether it works.

- [ ] 8.1 Play a full game at default settings, Standard difficulty
- [ ] 8.2 Is 90 minutes about right, or too long/short?
- [ ] 8.3 Is the 5-minute ability cooldown well judged? Too punishing? Too generous?
- [ ] 8.4 Does the task push people to split up and regroup, or does everyone clump together?
- [ ] 8.5 Was the Impostor's job possible? Too easy?
- [ ] 8.6 Did the Sniffer feel meaningful, or overpowered?
- [ ] 8.7 Did the 10-second respawn feel right?
- [ ] 8.8 Did anything reveal a role that shouldn't have?
- [ ] 8.9 Any lag or stutter with 6+ players?
- [ ] 8.10 Run 3+ games back to back with `/end` and `/start` — no degradation, no leftovers between rounds

**The single most important question:** did anything at any point accidentally tell you who the
Impostor was? If so, write down exactly what happened — that's the thing most worth fixing.
