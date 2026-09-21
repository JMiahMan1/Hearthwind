# Quests and Ages

## There are no quest mods

Aged 3.1.2 ships zero quest mods (no FTB Quests, HQM, Triumph, or
equivalent in its 213-jar mod list). Progression is delivered by two
systems, and Hearthwind keeps that design:

1. **Vanilla advancements** (mostly implicit: skill gates, job gates,
   recipe unlocks).
2. **The Lavender guide book** (`aged_guide_book`, 61 entries) loaded
   via Paxi.

There is deliberately no quest book with checklists and rewards.
Direction comes from the guide book; enforcement comes from gates.

## The six Ages

`docs/PROJECT_DIRECTION.md` defines the slow-tech Ages:

| Age | Name | Theme |
|---|---|---|
| 0 | Stranded | Rocks, flint, campfire survival |
| 1 | Camp | Sieve, farming, tanning |
| 2 | Copper | First metal tools, copper cauldron |
| 3 | Iron/Steel | Iron gear, steel production |
| 4 | Mechanical preview | Create wheels after skill gates |
| 5 | Mechanical | Full Create, storage, pumps |

## How Age is tracked

Per-player current Age lives in the `hearthwind_jobs:age` attachment
(`custom-mods/hearthwind-jobs/.../AgeState.java`, default 0). Today it
is set via the `/job age` debug command; the plan is to wire it to
advancement grants so Ages advance automatically.

## What Age gates today

- `JobState.java:210`: smither and brewer jobs require Age 2+
  (Copper Age); joining earlier is denied with a chat message.
- `JobRewards.java:26`: smither/brewer bonus rewards also require
  Age 2+.

Skill gates (`SkillGates.java`, 649 merged entries) and job ladders
(`JobDefs`, 8 jobs) enforce the within-Age grind; Age gates enforce
the between-Age tech walls.

## Guide book status

- The Aged Lavender book content (61 Markdown files) is staged for
  re-hosting under the `hearthwind:` namespace, rewritten per-Age.
- Until the Lavender fork-port lands, the `/guide` written book
  (`StarterKit.java`) is the in-game fallback and works on vanilla
  clients.
- Lavender upstream has no 26.x build (max 1.21.4); the port plan is
  a jsr305-first slim port to dodge the missing owo-lib, then a full
  upstream merge.
