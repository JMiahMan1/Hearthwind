package net.satisfy.farm_and_charm.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.satisfy.farm_and_charm.core.registry.EntityTypeRegistry;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ChickenCoopBlockItem extends BlockItem {

    public ChickenCoopBlockItem(Block block, Properties properties) {
        super(block, properties);

    }

    @Override
    public boolean isFoil(ItemStack stack) {
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        return data != null && !data.copyTagWithoutId().isEmpty();
    }

    @Override
    public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        var entityData = stack.get(DataComponents.ENTITY_DATA);
        if (entityData != null) {
            CompoundTag tag = entityData.copyTagWithoutId();
            boolean isCoopData = tag.contains("Chickens") || tag.contains("EggCount");

            if (isCoopData) {
                if (stack.get(DataComponents.BLOCK_ENTITY_DATA) == null) {
                    CompoundTag blockEntityTag = tag.copy();
                    blockEntityTag.putString("id", Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(EntityTypeRegistry.CHICKEN_COOP_BLOCK_ENTITY.get())).toString());
                    stack.set(DataComponents.BLOCK_ENTITY_DATA, net.minecraft.world.item.component.TypedEntityData.of(EntityTypeRegistry.CHICKEN_COOP_BLOCK_ENTITY.get(), blockEntityTag));
                }
            }

            stack.remove(DataComponents.ENTITY_DATA);
        }

        super.inventoryTick(stack, level, entity, slot);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull Item.TooltipContext ctx, @NotNull net.minecraft.world.item.component.TooltipDisplay display, @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
        var data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) return;
        var tag = data.copyTagWithoutId();
        int eggCount = tag.getIntOr("EggCount", 0);
        ListTag chickens = tag.getListOrEmpty("Chickens");
        boolean added = false;
        if (!chickens.isEmpty()) {
            tooltip.accept(Component.empty());
            added = true;
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.chickencoop_chickens", chickens.size(), 3).withStyle(ChatFormatting.GRAY));
        }
        if (eggCount > 0) {
            if (!added) tooltip.accept(Component.empty());
            tooltip.accept(Component.translatable("tooltip.farm_and_charm.chickencoop_eggs", eggCount, 9).withStyle(ChatFormatting.GRAY));
        }
    }
}
