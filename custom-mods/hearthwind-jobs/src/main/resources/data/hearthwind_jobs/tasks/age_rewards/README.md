# Age rewards

Per-Age task/reward wiring for the jobs module.

## Mechanics

- `AgeState` (`hearthwind_jobs:age` attachment, default 0) is the
  single source of per-player Age. Debug override: `/job age`.
- `JobGates` restricts job joining by Age: smither and brewer unlock
  at Age 2 (Copper); see `JobState.java:210`.
- `JobRewards` gates bonus rewards behind the same Age check; see
  `JobRewards.java:26`.

## Adding a new Age reward

1. Decide the minimum Age and the job ladder rung.
2. Add the gate next to the existing Age 2 checks (keep the denial
   message style: "The X unlocks at <Name> Age (Age N).").
3. Add a server gametest in `HearthwindJobsGameTests` asserting
   deny-below / admit-at.
4. Document the player-visible effect in `docs/PLAYER_CHANGES.md`.

Advancement-driven Age transitions (replacing `/job age`) are
tracked in `docs/QUESTS_AND_AGES.md`.
