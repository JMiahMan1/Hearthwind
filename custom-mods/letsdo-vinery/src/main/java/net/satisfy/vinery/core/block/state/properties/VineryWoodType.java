package net.satisfy.vinery.core.block.state.properties;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.satisfy.vinery.core.Vinery;

public class VineryWoodType {
    public static final WoodType DARK_CHERRY = new WoodType(Identifier.fromNamespaceAndPath(Vinery.MOD_ID, "dark_cherry").toString(), BlockSetType.OAK);
}
