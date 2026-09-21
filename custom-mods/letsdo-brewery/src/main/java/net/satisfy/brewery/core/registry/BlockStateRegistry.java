package net.satisfy.brewery.core.registry;

import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.satisfy.brewery.core.block.property.BrewMaterial;
import net.satisfy.brewery.core.block.property.Heat;
import net.satisfy.brewery.core.block.property.Liquid;

public class BlockStateRegistry {
    public static final EnumProperty<BrewMaterial> MATERIAL = EnumProperty.create("material", BrewMaterial.class);
    public static final EnumProperty<Liquid> LIQUID = EnumProperty.create("liquid", Liquid.class);
    public static final EnumProperty<Heat> HEAT = EnumProperty.create("heat", Heat.class);
    public static final BooleanProperty WHISTLE = BooleanProperty.create("whistle");
    public static final BooleanProperty TIME = BooleanProperty.create("time");
}
