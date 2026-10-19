package io.github.hexagonnico.spidercaves.items;

import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;

/**
 * 26.2 turns ArmorMaterial into a data record (defense values per ArmorType)
 * and removes ArmorItem entirely; the set is now described here and applied to
 * each piece through Item.Properties#humanoidArmor.
 */
public final class ModArmorMaterial {
	public static final ArmorMaterial SPIDER_ARMOR = new ArmorMaterial(
			195,
			Map.of(
					ArmorType.HELMET, 2,
					ArmorType.CHESTPLATE, 5,
					ArmorType.LEGGINGS, 4,
					ArmorType.BOOTS, 1
			),
			17,
			SoundEvents.ARMOR_EQUIP_LEATHER,
			0.0F,
			0.0F,
			TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("spider_caves", "spider_fang")),
			ResourceKey.create(net.minecraft.world.item.equipment.EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("spider_caves", "spider"))
	);

	private ModArmorMaterial() {
	}
}
