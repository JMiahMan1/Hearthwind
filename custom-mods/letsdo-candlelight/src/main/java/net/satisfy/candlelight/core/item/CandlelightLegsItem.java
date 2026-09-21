package net.satisfy.candlelight.core.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class CandlelightLegsItem extends Item {
    private final ArmorType type;
    private final Identifier leggingsTexture;

    public CandlelightLegsItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, Properties properties, Identifier leggingsTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.leggingsTexture = leggingsTexture;
    }

    public Identifier getLeggingsTexture() {
        return leggingsTexture;
    }
}