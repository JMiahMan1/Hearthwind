# How Aged differs from vanilla Minecraft

Player-facing summary of everything Aged changes or adds on top of
vanilla. **Keep this updated with every gameplay commit** — it is the
contract of what the pack does.

## Survival needs (replaces Dehydration + Environmentz)

Vanilla Minecraft only tracks hunger. Aged adds:

| System | What you see | Rules |
|---|---|---|
| **Thirst** | Overlay warnings ("You are getting thirsty") | Drains constantly, 2x while sprinting; below 3/20 bars natural health regen stops; at zero you take starvation-style damage. Drink from water bowls, purify water to avoid the thirst effect. |
| **Body temperature** | "You feel very cold/hot", freeze/heat damage | Drifts toward your biome's climate (deserts scorch, snowy peaks freeze). Wolf & wanderer armor, insulation items and ice packs bias you toward comfort; rain and deep underground shift it too. |
| **Diet** | Deficiency debuffs / bonus hearts | Five food groups (fruit, vegetables, grains, proteins, sugars) tracked separately; each decays over hours. Neglect a group and you suffer its debuff (e.g., no grains = slowness). Keep ALL groups above half for bonus absorption hearts. |
| **Food spoilage** | Food slowly rots in your inventory | Perishable meats/fish/produce randomly rot into rotten flesh over time — faster in hot biomes. Honey, teas and wines never spoil. Cooked food still spoils; plan expeditions accordingly. |

All numbers live in `config/aged_survival.json` — server admins can retune
everything without updates.

## Skills (replaces LevelZ)

Twelve skills level up as you play (max level 30):

- **Mining** — mine stone-type blocks. Levels raise mining speed.
- **Farming** — harvest crops, cull livestock.
- **Stamina** — dig dirt/sand-type blocks.
- **Strength** — melee kills. Levels add attack damage.
- **Archery** — bow/crossbow/trident kills.
- **Health** — passive levels add max HP.
- **Defense** — levels add armor points.
- **Agility** — levels add movement speed.
- **Luck** — levels add luck.
- **Smithing / Alchemy / Trade** — unlock-based crafting tiers (see the
  skill unlock lists in-game; e.g., iron gear needs smithing 14).

XP costs grow each level (~30 XP per current level). Admins tune curve,
XP rates and bonuses in `config/aged_skills.json`.

## Dangerous frontier (replaces RPG Difficulty)

Monsters get stronger the farther they spawn from world spawn:
+2 HP and +0.5 damage every 1000 blocks past a 500-block safe radius,
capped after 20 steps. The wilds are genuinely dangerous; early bases
near spawn are meaningfully safer. Configurable under `mobScaling`.

## Coming next

- Skill unlock gating wired into recipes/blocks from the migrated
  tuning data
- Onboarding instructions/tutorial toasts
- Seasons, river currents & ocean swells (`ideas/rivers-and-waves.md`)
- Client companion mod: HUD bars for thirst/diet/temperature
