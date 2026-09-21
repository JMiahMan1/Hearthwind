package net.satisfy.farm_and_charm.core.block.entity;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.farm_and_charm.core.item.food.EffectFoodHelper;
import net.satisfy.farm_and_charm.core.registry.EntityTypeRegistry;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EffectFoodBlockEntity extends BlockEntity {
    public static final String STORED_EFFECTS_KEY = "StoredEffects";
    private List<Pair<MobEffectInstance, Float>> effects;

    public EffectFoodBlockEntity(BlockPos pos, BlockState state) {
        this(EntityTypeRegistry.EFFECT_FOOD_BLOCK_ENTITY.get(), pos, state);
    }

    public EffectFoodBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void addEffects(List<Pair<MobEffectInstance, Float>> effects) {
        this.effects = effects.stream()
                .filter(p -> p.getFirst().getEffect() != MobEffects.HUNGER)
                .collect(Collectors.toList());
    }

    public List<Pair<MobEffectInstance, Float>> getEffects() {
        return effects != null ? effects : Collections.emptyList();
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        ListTag list = new ListTag();
        for (CompoundTag one : input.read(STORED_EFFECTS_KEY, CompoundTag.CODEC.listOf()).orElse(java.util.List.of())) list.add(one);
        this.effects = EffectFoodHelper.fromNbt(list);
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        if (effects == null) return;
        output.store(STORED_EFFECTS_KEY, CompoundTag.CODEC.listOf(), effects.stream().map(effect -> {
            CompoundTag one = new CompoundTag();
            one.putShort("id", (short) BuiltInRegistries.MOB_EFFECT.asHolderIdMap().getId(effect.getFirst().getEffect()));
            one.putInt("duration", effect.getFirst().getDuration());
            one.putInt("amplifier", effect.getFirst().getAmplifier());
            one.putFloat("chance", effect.getSecond());
            return one;
        }).toList());
    }
}
