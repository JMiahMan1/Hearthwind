package net.fleshz;

import net.fabricmc.api.ModInitializer;
import net.fleshz.init.BlockInit;
import net.fleshz.init.EventInit;
import net.fleshz.init.ItemInit;
import net.fleshz.init.RecipeInit;
import net.fleshz.network.RottenServerPacket;
import net.minecraft.resources.Identifier;

public class FleshMain implements ModInitializer {

    @Override
    public void onInitialize() {
        BlockInit.init();
        ItemInit.init();
        RecipeInit.init();
        RottenServerPacket.init();
        EventInit.init();
    }

    public static Identifier identifierOf(String name) {
        return Identifier.fromNamespaceAndPath("fleshz", name);
    }
}
