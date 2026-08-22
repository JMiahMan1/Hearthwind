# Aged
Aged is a challenging medieval based modpack where realism & wisdom rule your life.  

You awaken in the **Tales of Aged** wilderness.  
You quickly realize that you need to **gather supplies** & craft some **flint tools**.  
**Fresh water** and **body temperature** become important pretty soon.  
Your **character** is still **weak**, so you go an adventure to **gather experience** to increase your **skills** and attributes.  
*That's where your story begins.*  

### Installation
Aged is a modpack hosted on the modrinth website and can be found [here](https://modrinth.com/modpack/aged). To install Aged, visit modrinths [documentation](https://docs.modrinth.com/docs/modpacks/playing_modpacks/) which provides instructions on using [Modrinth Launcher](https://modrinth.com/app), [ATLauncher](https://atlauncher.com/about), [MultiMC](https://multimc.org/), and [Prism Launcher](https://prismlauncher.org/).

### Server conversion (this fork)
This fork rebuilds Aged as a **server-focused pack for modern Minecraft
releases** (baseline `26.2`, snapshot-friendly). See
[docs/CONVERSION.md](docs/CONVERSION.md) for the feasibility study, the curated
mod manifest, and the build toolchain:

```bash
python3 conversion/scripts/resolve_deps.py          # readiness report for target MC
python3 conversion/scripts/build_pack.py --server-dir
```

Custom replacement mods for the core survival mechanics live in `custom-mods/`.

### Branches
To [1.21.1 branch](https://github.com/xR4YM0ND/Aged/tree/1.21.1)
To [1.20.1 branch](https://github.com/xR4YM0ND/Aged/tree/1.20.1)
You can find older versions source code in the [backup branch](https://github.com/xR4YM0ND/Aged/tree/backup).

### License
Aged is licensed under [MIT](https://github.com/xR4YM0ND/Aged/tree/master/LICENSE.md).
