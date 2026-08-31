# Server-authority audit: HUD + menus, end-to-end (2026-08-29)

Scope: every client-rendered surface (HUDs, panel screens, toasts) and every
client→server interaction in the `hearthwind-*` modules.

## Data flow model (verified in code)

All gameplay state is computed server-side and pushed S2C; the client is
presentation-only. No serverbound (C2S) gameplay payloads exist anywhere —
grep for `ServerNetworking`/serverbound registrations returns nothing.

| Surface | Client source | Server producer |
|---|---|---|
| ThirstHud droplets | `ClientThirstData` ← `ThirstSyncPayload` | `HearthwindSurvivalThirst` tick (2 s) + on JOIN |
| Diet panel (NutrientsScreen) | `ClientDietData` ← `DietSyncPayload` | hash-gated tick (4 s) + on JOIN |
| TempHud + temp panel | `ClientTempData` ← `TempSyncPayload` | hash-gated tick (4 s) + on JOIN |
| JobHud + jobs panel | `ClientJobData` ← `JobSyncPayload` | hash-gated tick (4 s) + on JOIN |
| Skills panel | `ClientSkillData` ← `SkillsSyncPayload` (full sync) | JOIN + after every `SkillXp.addXp` |
| Skill-up toast | `SkillUpPayload` | server level-up event |
| SeasonHud | `ClientSeasonData` ← `SeasonSyncPayload` | JOIN + 5 s tick, change-gated broadcast |

Client-side math is limited to presentation derivations on synced values
(bar widths, `trendDirection()`, freeze/overheat threshold labels,
`xpProgress()`); the server still enforces all effects/damage/gates.
Vanilla field reads (`airSupply`, creative/spectator) mirror vanilla HUD
behavior.

## Interactions (server-authoritative by construction)

- Jobs Join/Leave buttons send vanilla chat commands (`/job join|leave <id>`);
  server validates job id + state and mutates the attachment. No client trust.
- Skill gates (block break/use) enforced in `SkillEvents`/`SkillGates` on the
  server; client never pre-approves.
- Debug command is the only client-triggerable mutator.

## Findings fixed in this audit

1. **`/hearthwind hydration set|get`, `temperature set`, `test *`, `move` were
   open to every player** (`requires(src -> true)` despite the "OP only"
   comment). Fixed: `.requires(src -> Commands.LEVEL_MODERATORS.check(
   src.permissions()))` (26.2 permission model: `PermissionCheck` +
   `PermissionSet`, the old `hasPermission(int)` is gone).
2. **JOIN sync could be skipped when the hash is 0** (e.g. temperature exactly
   `0.0f` → `floatToIntBits == 0` matched the `getOrDefault(id, 0)` sentinel).
   Fixed: presence-aware `Integer prev = map.get(id); if (prev == null ||
   prev != hash)` for diet/temp/job syncs.
3. **Sync-hash maps never cleaned on logout** (UUID-keyed, grew per unique
   player). Fixed: `ServerPlayConnectionEvents.DISCONNECT` cleanup.

## Residual notes (accepted)

- Diet/temp/job sync latency is up to 4 s (hash-gated), thirst 2 s (always
  sent). Presentation-only impact; values are authoritative either way.
- `ClientSkillData.merge` (used by SkillUpPayload path) is `Math::max` — a
  level-up can never exceed what the server sent; the full `SkillsSyncPayload`
  corrects any drift on join/level-up.

## Verification

- `./gradlew build` green; `bash tools/run_gametests.sh` **101/101 passed**
  (survival + skills + jobs + primitive suites).
