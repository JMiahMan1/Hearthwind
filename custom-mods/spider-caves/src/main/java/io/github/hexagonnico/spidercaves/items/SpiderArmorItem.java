package io.github.hexagonnico.spidercaves.items;

import io.github.hexagonnico.spidercaves.SpiderCaves;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;

public class SpiderArmorItem extends Item {
	public SpiderArmorItem(ArmorType type, Properties properties) {
		super(properties.humanoidArmor(ModArmorMaterial.SPIDER_ARMOR, type));
	}

	@Override
	public void appendHoverText(
			@NotNull ItemStack itemStack,
			@NotNull Item.TooltipContext context,
			@NotNull TooltipDisplay display,
			@NotNull Consumer<Component> tooltip,
			@NotNull TooltipFlag flag
	) {
		super.appendHoverText(itemStack, context, display, tooltip, flag);
		tooltip.accept(Component.empty());
		tooltip.accept(Component.translatable("item.modifiers.full_set").withStyle(ChatFormatting.GRAY));
		tooltip.accept(Component.translatable("item.modifiers.spider_armor_full_set").withStyle(ChatFormatting.BLUE));
	}

	public static boolean isWearingFullSet(LivingEntity entity) {
		return entity.getItemBySlot(EquipmentSlot.HEAD).is((Item) SpiderCaves.SPIDER_HELMET.get())
				&& entity.getItemBySlot(EquipmentSlot.CHEST).is((Item) SpiderCaves.SPIDER_CHESTPLATE.get())
				&& entity.getItemBySlot(EquipmentSlot.LEGS).is((Item) SpiderCaves.SPIDER_LEGGINGS.get())
				&& entity.getItemBySlot(EquipmentSlot.FEET).is((Item) SpiderCaves.SPIDER_BOOTS.get());
	}
}
