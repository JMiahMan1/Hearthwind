package net.satisfy.candlelight.core.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class CandlelightBootsItem extends Item {
    private final ArmorType type;
    private final Identifier bootsTexture;

    public CandlelightBootsItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier bootsTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.bootsTexture = bootsTexture;
    }

    public Identifier getBootsTexture() {
        return bootsTexture;
    }
}