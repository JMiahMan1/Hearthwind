package net.adventurez.item;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class AdventureArmorMaterials {

    public static final ArmorMaterial GILDED_NETHERITE = register("gilded_netherite", makeDefence(), 15,
            SoundEvents.ARMOR_EQUIP_NETHERITE, 3.5F, 0.1F, ItemTags.REPAIRS_NETHERITE_ARMOR);

    private static EnumMap<ArmorType, Integer> makeDefence() {
        EnumMap<ArmorType, Integer> map = new EnumMap<>(ArmorType.class);
        map.put(ArmorType.BOOTS, 3);
        map.put(ArmorType.LEGGINGS, 6);
        map.put(ArmorType.CHESTPLATE, 8);
        map.put(ArmorType.HELMET, 3);
        map.put(ArmorType.BODY, 11);
        return map;
    }

    private static ArmorMaterial register(String id, EnumMap<ArmorType, Integer> defense, int enchantability,
            Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<net.minecraft.world.item.Item> repairIngredient) {
        EnumMap<ArmorType, Integer> fullDefense = new EnumMap<>(ArmorType.class);
        for (ArmorType type : ArmorType.values()) {
            fullDefense.put(type, defense.getOrDefault(type, 0));
        }
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath("adventurez", id));
        return new ArmorMaterial(37, Map.copyOf(fullDefense), enchantability, equipSound, toughness, knockbackResistance, repairIngredient, asset);
    }
}
