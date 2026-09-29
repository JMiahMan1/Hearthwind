package dev.jmiahman.hearthwind.skills.mixin;

import dev.jmiahman.hearthwind.skills.MobScaling;
import dev.jmiahman.hearthwind.skills.SkillsConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

/**
 * A dangerous mob can drop more than its loot table says, the way
 * RPGDifficulty's {@code dropMoreLoot} does: one roll against
 * {@code healthFactor * moreLootChance} (Aged: 0.02 per point, capped at
 * 2.0), and on a hit the same table is generated again and every stack that
 * survives the per-stack skip is added to the pile.
 *
 * <p>26.x splits the loot call into {@code dropFromLootTable}, which is where
 * the reference's {@code dropLoot} TAIL injection lands. The extra stacks go
 * out through the same consumer the vanilla path used, so they spawn with the
 * usual pickup delay and drop animation.
 *
 * <p>The mixin targets {@link LivingEntity} rather than {@link Mob} on
 * purpose: the five-argument overload is declared only on {@code LivingEntity}
 * and {@code Mob} merely re-declares the three-argument one, so injecting into
 * {@code Mob} with this descriptor fails to apply. {@code LivingEntity} is
 * where the whole chain ends up anyway - {@code Mob.dropFromLootTable} calls
 * {@code super}, which resolves the table and delegates to this method.
 */
@Mixin(LivingEntity.class)
public abstract class MobMoreLootMixin {

    private static final Random LOOT_RANDOM = new Random();

    /**
     * The five-argument overload has to be named with its full descriptor.
     * {@code dropFromLootTable} is declared three times on {@code LivingEntity}
     * (3, 4 and 5 arguments) and a bare name makes the injector bind the
     * 3-argument one, which then fails to apply with "Invalid descriptor".
     */
    @Inject(method = "dropFromLootTable(Lnet/minecraft/server/level/ServerLevel;"
            + "Lnet/minecraft/world/damagesource/DamageSource;ZLnet/minecraft/resources/ResourceKey;"
            + "Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void hearthwind$dropExtraLoot(ServerLevel level, DamageSource source, boolean playerKilled,
            ResourceKey<LootTable> lootTable, java.util.function.Consumer<ItemStack> consumer,
            CallbackInfo info) {
        SkillsConfig.MobScaling cfg = SkillsConfig.get().mobScaling;
        if (!cfg.dropMoreLoot || !((Object) this instanceof Mob mob)) {
            return;
        }
        double factor = mob.getAttached(MobScaling.HEALTH_FACTOR);
        if (factor <= 1.0) {
            return;
        }
        LootTable table = level.getServer().reloadableRegistries().getLootTable(lootTable);
        LootParams.Builder builder = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, mob)
                .withParameter(LootContextParams.ORIGIN, mob.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, source.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, source.getDirectEntity());
        if (playerKilled && mob.getLastHurtByPlayer() != null) {
            builder = builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, mob.getLastHurtByPlayer())
                    .withLuck(mob.getLastHurtByPlayer().getLuck());
        }
        LootParams params = builder.create(LootContextParamSets.ENTITY);
        List<ItemStack> rolls = new java.util.ArrayList<>();
        table.getRandomItems(params, mob.getLootTableSeed(), rolls::add);
        for (ItemStack stack : MobScaling.extraLootStacks(rolls, factor, LOOT_RANDOM.nextDouble(),
                LOOT_RANDOM, cfg)) {
            consumer.accept(stack);
        }
    }
}
