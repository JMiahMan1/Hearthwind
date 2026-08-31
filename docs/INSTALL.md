# Installing & packaging Hearthwind - Server and Client

Target: Minecraft **26.2**, Fabric loader 0.19.3+, Java 25. One repo builds
**both** sides: server is required, client companion is optional (see
`docs/PROJECT_DIRECTION.md#distribution-model`).

## Players: joining a server

**Server is authoritative - vanilla works.** A vanilla 26.2 client connects
like any other server with no mods:

    Multiplayer -> Add Server -> <host>:25565

You will see survival warnings via action-bar/chat (thirst, diet,
temperature, skill gates) - fully playable.

**Optional: install the companion client for full HUD:**

- **Modrinth App:** open `HearthwindClient-<ver>-mc26.2.mrpack` → Create instance → launch. The client mod `hearthwind-client` renders HUD bars (thirst/diet/temp/skill/job), instruction toasts (Genesis-style), and water-motion preview. Server syncs via `hearthwind:*` payloads; without the client you simply see action-bar fallbacks.
- **Prism Launcher:** create a vanilla 26.2 instance → Add mods → drop `hearthwind-client-*.jar` into `mods/` (keep Fabric API). Or import the `.mrpack` as instance.

The client never grants authority - the server enforces all gates, XP,
spoilage, and mob scaling. Installing or omitting the client does not
change gameplay, only presentation.

**Fresh Animations (client pack only):** the client `.mrpack` ships
`FreshAnimations_v1.10.5.zip` under `resourcepacks/` plus its required
client mods (`entity_model_features`, `entity_texture_features`). It is
NOT auto-enabled - after launching, go to `Options > Resource Packs`
and move Fresh Animations to the active column. Server-only players
see vanilla mob models either way.

## Server admins: installing a built pack

### Option A - Modrinth pack files (recommended)

Two files are published per release / CI artifact (`mod-jars` on
[Actions](../../actions)):

- `HearthwindServer-<ver>-mc26.2.mrpack` - **required** (server mods + `world/datapacks/hearthwind/`)
- `HearthwindClient-<ver>-mc26.2.mrpack` - **optional** (players who want HUD/companion visuals)

Server install:

1. Grab `HearthwindServer-*.mrpack` from
   [Releases](../../releases) or CI.
2. Import it into the [Modrinth App](https://modrinth.com/app)
   (Create instance -> From file) or run it headlessly with
   [Modrinth's CLI](https://github.com/modrinth/code) / your launcher.
3. Accept the EULA in the instance's `eula.txt`, start.

Hand the `HearthwindClient-*.mrpack` link to players - they install it
as a separate instance (or drop the `hearthwind-client` jar into their
Prism `mods/`). Vanilla players without it still join fine.

> **Prism Launcher users**: as a PLAYER you don't need any of this -
> create any vanilla 26.2 instance in Prism and connect to the server;
> the whole pack runs server-side. If you want to HOST via Prism, an
> `.mrpack` import technically loads, but Prism has no dedicated
> dedicated-server workflow - prefer the Modrinth App, a `mrpack` CLI,
> or Option B below for hosting.

### Option B - plain server directory

1. Get the `dist/server/` directory (CI artifact or build it yourself):
   contains `fabric-server.jar` (Fabric launcher), `mods/` (all resolved
   third-party mods), and `world/datapacks/hearthwind/` (the migrated
   tuning datapack, renamed from `aged-server` to `hearthwind`).
2. Copy our custom mods from the `mod-jars` artifact into `mods/`
   (`hearthwind-survival`, `hearthwind-skills`, `hearthwind-jobs`,
   `hearthwind-primitive`, `hearthwind-world`). For the client pack,
   `hearthwind-client` goes into the **player's** `mods/` not the server.
3. First-boot checklist:
   - `echo "eula=true" > eula.txt`
   - RCON if you want remote admin:
     `enable-rcon=true`, `rcon.port=25575`, `rcon.password=<secret>`
     (vanilla property names; fabric-style `rcon.enabled` is ignored)
4. `java -Xmx3G -jar fabric-server.jar nogui`

Server configs are created on first boot with sane defaults:
`config/hearthwind_survival.json` (thirst/temperature/diet/spoilage),
`config/hearthwind_skills.json` (skills/mob scaling/gates),
`config/hearthwind_jobs.json` (job XP curve) and
`config/hearthwind_world.json` (seasons). The client has no server
config - its HUD reads `hearthwind:*` sync payloads.

### Option C - Linux host as systemd service (long-running)

For a dedicated Linux box (Ubuntu/Debian) that survives reboots:

```bash
# On the Linux host, as root:
git clone https://github.com/JMiahMan1/Hearthwind.git /opt/Hearthwind
cd /opt/Hearthwind

# Build or download a release zip into /opt/hearthwind
sudo bash deploy/install.sh /opt/hearthwind
#  → creates user hearthwind, copies conversion/build/dist/server + hearthwind-* jars,
#    fabric-server.jar, eula.txt and server.properties (edit rcon.password!)

sudo systemctl start hearthwind
sudo systemctl enable hearthwind
sudo journalctl -u hearthwind -f   # logs
sudo systemctl status hearthwind
```

Unit file is `deploy/systemd/hearthwind.service` (`gh`):

- `User=hearthwind`, `WorkingDirectory=/opt/hearthwind`, `ExecStart=/usr/lib/jvm/java-25-openjdk-amd64/bin/java -Xmx3G -Xms1G -jar fabric-server.jar nogui`
- `Restart=on-failure`, `RestartSec=10`, `SuccessExitStatus=0 1 143`
- Tune heap via drop-in: `sudo systemctl edit hearthwind` → override `ExecStart` (see `deploy/systemd/override.conf.example` for 6G example)
- Logs to journald (`SyslogIdentifier=hearthwind`), RCON same as plain dir (`custom-mods/tools/rcon.py 127.0.0.1 25575 <password> list`)

Local dev equivalent (macOS, no systemd): `bash tools/run_local_server.sh` → starts `dev-server/` on `*:25565` (Prism `Hearthwind-Dev-Client` connects to `localhost:25565`), `bash custom-mods/tools/setup_prism_dev.sh` syncs Prism instance.


## Developers: building everything from source

Prerequisites: JDK 25, Python 3.10+, ~4 GB free RAM for builds.

```bash
git clone https://github.com/JMiahMan1/Hearthwind.git && cd Hearthwind
git checkout server-26.2

# 1. resolve third-party mods against MC 26.2 (writes conversion/build/resolved.json)
python3 conversion/scripts/resolve_deps.py

# 2. migrate the original 1.20.1 tuning corpus into the native datapack
python3 conversion/scripts/migrate_datapack.py   # see script header

# 3. regenerate skill-gate definitions from the migrated levelz data
python3 conversion/scripts/generate_skill_gates.py

# 4. package: Modrinth index + .mrpack (+ plain server dir with --server-dir)
python3 conversion/scripts/build_pack.py --server-dir

# 5. build our custom mods (jars land in custom-mods/*/build/libs/)
cd custom-mods && ./gradlew build --no-daemon --max-workers=2
```

Low-memory hosts: cap Gradle with `-Xmx512m` and use
`--max-workers=1` (already configured in `gradle.properties`);
test servers boot with `-Xmx768M`. Roomier machines can drop these limits.

### Testing your build

```bash
cd custom-mods
bash tools/run_gametests.sh          # headless gametests (fast, no client)
# builds all modules + boots throwaway server, expect 19 tests green

# when hearthwind-client ships HUD, run client gametests under xvfb:
bash tools/run_gametests.sh --client   # boots real client, drives inventory/HUD assertions (heavy, ~4GB)
```

Pushing to GitHub runs the same suites plus artifact uploads automatically
(`.github/workflows/build-and-test.yml`): headless `build-gametest` on every
push, optional `client-gametest` + `boot-smoke` on dispatch. See
`AGENTS.md` ("CI") for how far automated testing goes, including scripted
real-client tests and interactive tunnel sessions.

## Packaging notes (maintainers)

- `build_pack.py` pins exact versions + sha1 hashes from
  `conversion/build/resolved.json`; rerun `resolve_deps.py` after
  editing `conversion/curated/mods-manifest.json`.
- Our custom mods are NOT in the Modrinth index - they are built from
  this repo. Release flow = tag -> GHA artifacts -> attach to a GitHub
  release together with the `.mrpack`.
