package net.satisfy.candlelight;

import net.minecraft.resources.Identifier;
import net.satisfy.candlelight.core.event.CommonEvents;
import net.satisfy.candlelight.core.networking.CandlelightMessages;
import net.satisfy.candlelight.core.registry.*;

public class Candlelight {
    public static final String MOD_ID = "candlelight";

    public static Identifier identifier(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void init() {
        ObjectRegistry.init();
        ScreenHandlerTypeRegistry.init();
        MobEffectRegistry.init();
        SoundEventRegistry.init();
        EntityTypeRegistry.init();
        CommonEvents.init();
        TabRegistry.init();
        CandlelightMessages.init();
    }

    public static void commonInit() {
        FlammableBlockRegistry.init();
    }
}