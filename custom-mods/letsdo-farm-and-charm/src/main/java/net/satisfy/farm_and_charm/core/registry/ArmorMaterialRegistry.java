package net.satisfy.farm_and_charm.core.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Util;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.satisfy.farm_and_charm.FarmAndCharm;

import java.util.EnumMap;
import java.util.Map;

/**
 * 26.2 port: net.minecraft.world.item.ArmorMaterial / ArmorItem.Type are gone.
 * New home is net.minecraft.world.item.equipment (ArmorMaterial record,
 * ArmorType enum, EquipmentAssets keys). Repair ingredients are TagKey-based.
 * ArmorMaterial is a plain value object now (no registry), so CLOTH/JEWELRY
 * are direct Holders and texture variants share the base material.
 */
public final class ArmorMaterialRegistry {
    private static Map<ArmorType, Integer> def(int boots, int leggings, int chestplate, int helmet) {
        return Util.make(new EnumMap<>(ArmorType.class), m -> {
            m.put(ArmorType.BOOTS, boots);
            m.put(ArmorType.LEGGINGS, leggings);
            m.put(ArmorType.CHESTPLATE, chestplate);
            m.put(ArmorType.HELMET, helmet);
        });
    }

    private static Holder<ArmorMaterial> of(String id, Map<ArmorType, Integer> defense) {
        ArmorMaterial leather = ArmorMaterials.LEATHER;
        ArmorMaterial mat = new ArmorMaterial(leather.durability(), defense, 15,
                SoundEvents.ARMOR_EQUIP_LEATHER, 0.0F, 0.0F, ItemTags.WOOL,
                ResourceKey.create(EquipmentAssets.ROOT_ID,
                        Identifier.fromNamespaceAndPath(FarmAndCharm.MOD_ID, id)));
        return Holder.direct(mat);
    }

    public static final Holder<ArmorMaterial> CLOTH = of("cloth", def(1, 1, 2, 1));
    public static final Holder<ArmorMaterial> JEWELRY = of("jewelry", def(1, 1, 1, 1));

    public static Holder<ArmorMaterial> withTextureNoOverlay(Holder<ArmorMaterial> base, Identifier texturePng) {
        return base;
    }

    private ArmorMaterialRegistry() {}
}
