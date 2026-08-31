package dev.jmiahman.hearthwind.skills;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * XP awarding hooks: mining/farming/digging from block breaks, combat
 * skills from kills. All amounts come from SkillsConfig.
 */
public final class SkillEvents {
    /**
     * Blocks whose mining counts toward the MINING skill even though they are
     * broken by hand. The levelz ladder gates every pickaxe-mineable block
     * (sandstone 2, stone and cobblestone 5, andesite 8...), so without the
     * surface rocks - the Age-0 activity - a fresh player could never earn the
     * first mining level and progression would deadlock.
     */
    private static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> ROCK_BLOCKS =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,
                    net.minecraft.resources.Identifier.fromNamespaceAndPath(
                            "earlystage", "rock_blocks"));

    private SkillEvents() {}

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(SkillEvents::onBlockBroken);
        ServerLivingEntityEvents.AFTER_DEATH.register(SkillEvents::onDeath);
        // Defense: XP for taking melee damage
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> {
                    if (entity instanceof ServerPlayer sp
                            && !sp.getAbilities().invulnerable
                            && amount > 0
                            && source.getEntity() != null) {
                        SkillXp.addXp(sp, Skill.DEFENSE, SkillsConfig.get().xp.defensePerHit);
                    }
                    return true;
                });
        // Archery: XP on any projectile hit (not just kill)
        net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, source, amount) -> {
                    if (source.getEntity() instanceof ServerPlayer sp
                            && amount > 0) {
                        net.minecraft.world.entity.projectile.Projectile proj =
                                source.getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile p ? p : null;
                        if (proj != null) {
                            SkillXp.addXp(sp, Skill.ARCHERY, SkillsConfig.get().xp.archeryPerHit);
                        }
                    }
                    return true;
                });
        // Smithing: XP when taking a result from the smithing table
        net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player instanceof ServerPlayer sp) {
                var state = world.getBlockState(hitResult.getBlockPos());
                if (state.is(net.minecraft.world.level.block.Blocks.SMITHING_TABLE)) {
                    // XP awarded via SmithingResultMixin (see hearthwind-jobs smithing mixin)
                    // Fallback: award small XP on interaction with the table
                    SkillXp.addXp(sp, Skill.SMITHING, SkillsConfig.get().xp.smithingPerInteract);
                }
            }
            return net.minecraft.world.InteractionResult.PASS;
        });
        // Trade: XP on villager trade completion
        net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (player instanceof ServerPlayer sp
                    && entity instanceof net.minecraft.world.entity.npc.villager.AbstractVillager) {
                SkillXp.addXp(sp, Skill.TRADE, SkillsConfig.get().xp.tradePerTransaction);
            }
            return net.minecraft.world.InteractionResult.PASS;
        });
        net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> SkillsSync.send(handler.getPlayer()));
    }

    public static void onBlockBroken(Level world, net.minecraft.world.entity.player.Player player, BlockPos pos,
            BlockState state, BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer sp)
                || sp.getAbilities().instabuild
                || state.isAir()) {
            return;
        }
        SkillsConfig.Xp cfg = SkillsConfig.get().xp;
        if (isFarmingBlock(state)) {
            SkillXp.addXp(sp, Skill.FARMING, cfg.farmingPerCrop);
        } else if (state.is(ROCK_BLOCKS)) {
            SkillXp.addXp(sp, Skill.MINING, cfg.miningPerBlock);
        } else if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            SkillXp.addXp(sp, Skill.MINING, cfg.miningPerBlock);
        } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            SkillXp.addXp(sp, Skill.STAMINA, cfg.staminaPerDig);
        }
    }

    private static boolean isFarmingBlock(BlockState state) {
        if (state.is(BlockTags.CROPS)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.BEE_GROWABLES)
                || state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.PUMPKIN)
                || state.is(Blocks.MELON)
                || state.is(Blocks.COCOA)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.NETHER_WART)) {
            return true;
        }
        String ns = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getNamespace();
        return "vinery".equals(ns) || "candlelight".equals(ns) || "meadow".equals(ns) || "herbalbrews".equals(ns) || "bakery".equals(ns);
    }

    public static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer sp)) {
            return;
        }
        SkillsConfig.Xp cfg = SkillsConfig.get().xp;
        if (entity instanceof Animal) {
            SkillXp.addXp(sp, Skill.FARMING, cfg.farmingPerAnimalKill);
            return;
        }
        ItemStack weapon = source.getWeaponItem();
        boolean ranged = isRanged(weapon);
        if (ranged) {
            SkillXp.addXp(sp, Skill.ARCHERY, cfg.archeryPerRangedKill);
        } else {
            SkillXp.addXp(sp, Skill.STRENGTH, cfg.strengthPerMeleeKill);
        }
    }

    private static boolean isRanged(ItemStack weapon) {
        return !weapon.isEmpty()
                && (weapon.getItem() instanceof BowItem
                        || weapon.getItem() instanceof CrossbowItem
                        || weapon.getItem() instanceof TridentItem);
    }
}
