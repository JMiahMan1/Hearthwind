package dev.jmiahman.aged.skills;

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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * XP awarding hooks: mining/farming/digging from block breaks, combat
 * skills from kills. All amounts come from SkillsConfig.
 */
public final class SkillEvents {
    private SkillEvents() {}

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(SkillEvents::onBlockBroken);
        ServerLivingEntityEvents.AFTER_DEATH.register(SkillEvents::onDeath);
    }

    private static void onBlockBroken(Level world, net.minecraft.world.entity.player.Player player, BlockPos pos,
            BlockState state, BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer sp)
                || sp.getAbilities().instabuild
                || state.isAir()) {
            return;
        }
        SkillsConfig.Xp cfg = SkillsConfig.get().xp;
        if (state.is(BlockTags.CROPS)) {
            SkillXp.addXp(sp, Skill.FARMING, cfg.farmingPerCrop);
        } else if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            SkillXp.addXp(sp, Skill.MINING, cfg.miningPerBlock);
        } else if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            SkillXp.addXp(sp, Skill.STAMINA, cfg.staminaPerDig);
        }
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
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
