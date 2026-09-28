package dev.jmiahman.hearthwind.jobs;

import java.util.Optional;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Job XP hooks: block breaks and kills award job XP if the broken/killed id
 * matches the player's current job level track. Reuses the same event buses
 * as SkillEvents but checks JobDefs instead of skill config.
 */
public final class JobEvents {
    private JobEvents() {}

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register(JobEvents::onBlockBroken);
        ServerLivingEntityEvents.AFTER_DEATH.register(JobEvents::onDeath);
    }

    private static void onBlockBroken(Level world, net.minecraft.world.entity.player.Player player, BlockPos pos,
            BlockState state, BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer sp) || sp.getAbilities().instabuild || state.isAir()) return;
        String id = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        // The builder is paid for PLACING blocks, not for breaking them: the
        // reference ladder shares its block list with the miner and lumberjack
        // so a naive match would pay the builder twice for one wall.
        JobState.awardIfMatch(sp, id, jobDef -> !jobDef.id.equals("builder"));
        sendJobSync(sp);
    }

    /**
     * Block placement award for the builder job. Only ids in the reference
     * placement tag pay, because the placement tag and the job ladder are
     * deliberately different lists: Aged pays the builder for a plank
     * <em>placed</em>, while the ladder entry exists to keep those blocks
     * out of the other jobs' break rewards.
     *
     * @return true when the block was builder work
     */
    public static boolean awardBlockPlaced(ServerPlayer sp, BlockState placed) {
        if (sp == null || placed == null || placed.isAir() || sp.getAbilities().instabuild) {
            return false;
        }
        if (!JobCorpus.isBuilderPlacement(placed)) {
            return false;
        }
        String id = BuiltInRegistries.BLOCK.getKey(placed.getBlock()).toString();
        // The placement tag is what makes this builder work, so the ladder
        // must not also have to list the block: the ladder only sets the size
        // of the award.
        JobState.awardTier(sp, "builder", id);
        sendJobSync(sp);
        return true;
    }

    /**
     * Brewing award.
     *
     * <p>The brewer corpus ladder is keyed by <em>potion</em> ids
     * ({@code minecraft:awkward}, {@code minecraft:strong_swiftness}, ...),
     * not by block or item ids, so the id that has to match is the potion
     * the brewing stand produced - which is also why the brewer needs its
     * own award path: with only block/item ids in the match table, no
     * brewed potion could ever pay and the job would be unearnable.
     *
     * <p>The reward is the corpus tier of the potion, so brewing a
     * strength potion is worth three times brewing a mundane one. A potion
     * the ladder does not list pays nothing, which is what keeps a click on
     * a brewing stand from turning into free XP. Effects are checked too,
     * after the potion id, so a modded potion that is not in the corpus by
     * name still pays if one of its effects is.
     */
    public static void awardBrew(ServerPlayer sp, ItemStack potion) {
        if (sp == null || potion == null || potion.isEmpty()) {
            return;
        }
        if (!JobState.employed(sp, "brewer")) {
            return;
        }
        PotionContents contents = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        Optional<Holder<Potion>> brewed = contents.potion();
        if (brewed.isEmpty()) {
            return;
        }
        int tier = brewed.get().unwrapKey()
                .map(key -> JobCorpus.levelFor("brewer", key.identifier().toString()))
                .orElse(0);
        if (tier <= 0) {
            for (MobEffectInstance instance : brewed.get().value().getEffects()) {
                Optional<ResourceKey<MobEffect>> key = instance.getEffect().unwrapKey();
                if (key.isPresent()) {
                    tier = Math.max(tier, JobCorpus.levelFor("brewer", key.get().identifier().toString()));
                }
            }
        }
        if (tier > 0) {
            JobState.awardTier(sp, "brewer", tier);
        }
        if (sp.connection != null) {
            sendJobSync(sp);
        }
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        if (!(source.getEntity() instanceof ServerPlayer sp)) return;
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        JobState.awardIfMatch(sp, id);
        sendJobSync(sp);
    }

    private static void sendJobSync(ServerPlayer sp) {
        JobsSync.send(sp);
    }

    /**
     * Item-gain award shared by the crafting, furnace and fishing hooks:
     * the paid XP comes from the item registry id against the player's
     * current job ladders (wheat -> farmer, iron_ingot -> smither, ...).
     */
    public static void awardItem(ServerPlayer sp, ItemStack stack) {
        if (sp == null || stack == null || stack.isEmpty()) {
            return;
        }
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        JobState.awardIfMatch(sp, id);
        if (sp.connection != null) {
            sendJobSync(sp);
        }
    }
}
