# Versioning discipline

Two independent version lines. Do not mix them.

## Pack version (`conversion/build.conf.json` -> `pack.version`)

This is the release number players see: `Hearthwind-0.1.1-mc26.2.mrpack`,
GitHub Release tag `v0.1.1`, `modrinth.index.json` `versionId`.

- Bump it for every player-facing release (new mods, parity fixes, tuning).
- The release workflow (`build-and-test.yml`, `release` job) publishes the
  `.mrpack` files built from this version when a `v*` tag is pushed.
- This is the ONLY version key the pack script reads. There is no
  `pack_version` key; if you see one documented anywhere, it is stale.

## Mod versions (individual `fabric.mod.json` / gradle properties)

Only four in-house modules carry their own version:

- `hearthwind-survival`, `hearthwind-skills`, `hearthwind-jobs`, `smallships`

All other in-house modules report loader-based versions and have no version
field to bump. Mod jars keep version `0.1.0` across pack releases unless the
mod itself changed in a player-visible way.

## Checklist for a release

1. Update `pack.version` in `conversion/build.conf.json`.
2. Rebuild packs: `python3 conversion/scripts/build_pack.py --server-dir`.
3. Verify overrides contain the in-house jars
   (`overrides/mods/passable-foliage-*.jar`, `hearthwind-client` only in the
   Client pack, never in the Server pack).
4. Commit, push, tag `v<pack.version>`, let CI publish the release.
5. Confirm the GitHub Release has all 3 `.mrpack` assets.
