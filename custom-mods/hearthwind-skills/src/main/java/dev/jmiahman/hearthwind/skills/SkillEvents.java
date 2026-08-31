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
