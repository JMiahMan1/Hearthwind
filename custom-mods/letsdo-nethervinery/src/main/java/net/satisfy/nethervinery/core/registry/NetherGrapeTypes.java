package net.satisfy.nethervinery.core.registry;


import net.satisfy.vinery.core.registry.GrapeTypeRegistry;
import net.satisfy.vinery.core.util.GrapeType;

public class NetherGrapeTypes {

    public static final GrapeType CRIMSON = GrapeTypeRegistry.registerGrapeType("crimson", false, true);
    public static final GrapeType WARPED = GrapeTypeRegistry.registerGrapeType("warped", false, false);

    public static void register() {
    }

    public static void addGrapeAttributes() {
        CRIMSON.setItems(NetherObjectRegistry.CRIMSON_GRAPE, NetherObjectRegistry.CRIMSON_GRAPE_SEEDS, NetherObjectRegistry.CRIMSON_GRAPEJUICE);
        WARPED.setItems(NetherObjectRegistry.WARPED_GRAPE, NetherObjectRegistry.WARPED_GRAPE_SEEDS, NetherObjectRegistry.WARPED_GRAPEJUICE);
    }
}
