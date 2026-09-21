package net.satisfy.bakery;

import net.minecraft.resources.Identifier;
import net.satisfy.bakery.core.event.CommonEvents;
import net.satisfy.bakery.core.network.PacketHandler;
import net.satisfy.bakery.core.registry.*;

public class Bakery {
    public static final String MOD_ID = "bakery";

    public static Identifier identifier(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void init() {
        MobEffectRegistry.init();
        ObjectRegistry.init();
        EntityTypeRegistry.init();
        RecipeTypeRegistry.init();
        PacketHandler.init();
        CommonEvents.init();
        TabRegistry.init();
        SoundEventRegistry.init();
    }
}

