package net.satisfy.vinery.core.registry;

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
import net.satisfy.vinery.core.Vinery;

import java.util.EnumMap;
import java.util.Map;

// 26.2: ArmorMaterial/ArmorItem live in world.item.equipment; material is a
// plain value object (no registry), leather stats preserved.
public final class ArmorMaterialRegistry {
    private ArmorMaterialRegistry() {
    }

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
                        Identifier.fromNamespaceAndPath(Vinery.MOD_ID, id)));
        return Holder.direct(mat);
    }

    public static final Holder<ArmorMaterial> WINEMAKER_ARMOR = of("winemaker", def(1, 1, 2, 1));

    public static void init() {
    }
}
