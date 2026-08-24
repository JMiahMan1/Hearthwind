# Installing & packaging Aged Server

Three routes depending on who you are. Target: Minecraft **26.2**,
Fabric loader 0.19.3+, Java 25.

## Players: joining a server

Nothing to install — the pack is **server-side only**. A vanilla
client of the matching version connects like any other server:

    Multiplayer -> Add Server -> <host>:25565

Optional client-side enhancements (HUD bars, shaders) are planned as a
companion mod; until then every gameplay feature works without any
client download.

## Server admins: installing a built pack

### Option A — Modrinth pack file (recommended)

1. Grab `AgedServer-<ver>-mc26.2.mrpack` from
   [Releases](../../releases) or a CI artifact (`mod-jars` /
   pack artifacts on the [Actions tab](../../actions)).
2. Import it into the [Modrinth App](https://modrinth.com/app)
   (Create instance -> From file) or run it headlessly with
   [Modrinth's CLI](https://github.com/modrinth/code) (`mrpack` support)
   / your launcher of choice.
3. Accept the EULA in the instance's `eula.txt`, start.

> **Prism Launcher users**: as a PLAYER you don't need any of this —
> create any vanilla 26.2 instance in Prism and connect to the server;
> the whole pack runs server-side. If you want to HOST via Prism, an
> `.mrpack` import technically loads, but Prism has no dedicated
> dedicated-server workflow — prefer the Modrinth App, a `mrpack` CLI,
> or Option B below for hosting.

### Option B — plain server directory

1. Get the `dist/server/` directory (CI artifact or build it yourself):
   contains `fabric-server.jar` (Fabric launcher), `mods/` (all resolved
   third-party mods), and `world/datapacks/aged-server/` (the migrated
   tuning datapack).
2. Copy our custom mods from the `mod-jars` artifact into `mods/`
   (`aged-survival`, `aged-skills`, ...).
3. First-boot checklist:
   - `echo "eula=true" > eula.txt`
   - RCON if you want remote admin:
     `enable-rcon=true`, `rcon.port=25575`, `rcon.password=<secret>`
     (vanilla property names; fabric-style `rcon.enabled` is ignored)
4. `java -Xmx3G -jar fabric-server.jar nogui`

Config files are created on first boot with sane defaults:
`config/aged_survival.json` (thirst/temperature/diet/spoilage) and
`config/aged_skills.json` (skills/mob scaling/gates).

## Developers: building everything from source

Prerequisites: JDK 25, Python 3.10+, ~4 GB free RAM for builds.

```bash
git clone https://github.com/JMiahMan1/Aged.git && cd Aged

# 1. resolve third-party mods against MC 26.2 (writes conversion/build/resolved.json)
python3 conversion/scripts/resolve_deps.py

# 2. migrate the original 1.20.1 tuning corpus into the native datapack
python3 conversion/scripts/migrate_datapack.py   # see script header

# 3. regenerate skill-gate definitions from the migrated levelz data
python3 conversion/scripts/generate_skill_gates.py

# 4. package: Modrinth index + .mrpack (+ plain server dir with --server-dir)
python3 conversion/scripts/build_pack.py --server-dir

# 5. build our custom mods (jars land in custom-mods/*/build/libs/)
cd custom-mods && ./gradlew build --no-daemon
```

Low-memory hosts: cap Gradle with `-Xmx512m` and use
`--max-workers=1` (already configured here); test servers boot with
`-Xmx768M`. Roomier machines can drop these limits.

### Testing your build

```bash
cd custom-mods
bash tools/run_gametests.sh          # headless gametests (fast, no client)
```

Pushing to GitHub runs the same suite plus artifact uploads automatically
(`.github/workflows/build-and-test.yml`). See AGENTS.md ("CI") for how
far automated testing goes, including scripted real-client tests.

## Packaging notes (maintainers)

- `build_pack.py` pins exact versions + sha1 hashes from
  `conversion/build/resolved.json`; rerun `resolve_deps.py` after
  editing `conversion/curated/mods-manifest.json`.
- Our custom mods are NOT in the Modrinth index — they are built from
  this repo. Release flow = tag -> GHA artifacts -> attach to a GitHub
  release together with the `.mrpack`.
