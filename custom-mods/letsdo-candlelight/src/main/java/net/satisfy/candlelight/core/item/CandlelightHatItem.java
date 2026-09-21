package net.satisfy.candlelight.core.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class CandlelightHatItem extends Item {
    private final ArmorType type;
    private final Identifier hatTexture;

    public CandlelightHatItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier hatTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.hatTexture = hatTexture;
    }

    public Identifier getHatTexture() {
        return hatTexture;
    }
}