# Live testing playbook

## Test protocol (per user directive)

Before ANY live visual test:

1. `execute as TestPlayer run hearthwind hydration set 20`
2. Body temp good: `execute as TestPlayer run hearthwind temp set 0`
   (or stand in a temperate biome; verify on Temp tab = green).
3. Clear inventory: `execute as TestPlayer run clear @s`
4. THEN add exactly the items needed for the test
   (`item replace entity TestPlayer hotbar.0 with <id> <n>`).
5. Give immunity so thirst doesn't kill mid-test:
   `effect give TestPlayer minecraft:resistance 100000 3 true`.

TestPlayer keeps dying of thirst mid-session — the death screen eats
all clicks and looks like "input not registering". Always run the
protocol above first; respawn button is at `c:960,644`.

## Reusable test site

Build a sky platform above the terrain so tests never depend on
worldgen (no grass blocking view, no thirst walking):

- Forceload area is already marked: `-16,-16 .. 47,47`.
- Plan: fill a ~16x6x16 platform at `y≈200` (`fill x1 199 z1 x2 199 z2
  smooth_stone`), 2-block walls on the work edge, glowstone for light,
  then `tp TestPlayer x y z yaw pitch` on top. Build script goes in
  `.tmp/testsite.sh` (idempotent — always fill first, then set blocks).
- Current ground-level test coords (savanna pit): sieve `-6 76 -1`,
  crafting rock `-8 75 -1`, player hover `tp TestPlayer -5.7 78.5 -0.7
  0 85`.

## Client-gametest adoption plan (fabric-client-gametest-api-v1)

Headless server gametests are green (115/115), but visual/UX behavior
needs a real client. Fabric's first-party `fabric-client-gametest-api-v1`
drives a REAL client (movement, clicks, inventory, screenshots,
assertions) — supported in CI under xvfb per AGENTS.md. Adoption plan:

1. Add the module to the loom cache / build classpath
   (it ships inside fabric-api but must be enabled explicitly).
2. Port the highest-value flows first, in order:
   a. crafting rock: place, insert item via right-click, rock-hit,
      verify result slot + client render state,
   b. sieve: insert dirt, 4 taps, verify drop spawn,
   c. tab strip navigation + jobs join/leave round trip,
   d. thirst droplets shift-up while underwater.
3. Run on GH runners (xvfb, ~4 GB) once scenarios exist locally.

## Session cheatsheet

- Client: `python3 custom-mods/tools/client_harness.py launch
  --quickplay --host localhost --port 25565 --username TestPlayer`
  (accessibility Continue at `cliclick c:960,1012`; focus via
  `bash .tmp/front.sh`).
- RCON: `python3 custom-mods/tools/rcon.py 127.0.0.1 25575 agedtest
  '<cmd>'` from repo root.
- Server relaunch must use the python3 Popen `start_new_session`
  pattern (plain `nohup &` dies with the tool command).
- Aged-ref server PID 49171: LEAVE RUNNING.
- Kill dev client with `pkill -f XstartOnFirstThread`.
- Screenshots: `screencapture -x .tmp/shots/N.png` then Read tool.
- `data modify block` writes the BE NBT but does NOT trigger the
  client-sync packet — real right-clicks are the only valid sync test.
- `execute as TestPlayer run tp ~ ~ ~` does NOT use the executor pos —
  use absolute `tp TestPlayer x y z yaw pitch`.
