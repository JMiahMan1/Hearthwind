package net.fleshz.init;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fleshz.FleshMain;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ItemInit {

    public static final Item ROTTEN_LEATHER = register("rotten_leather");
    public static final Item HIDE = register("hide");
    public static final Item PREPARED_HIDE = register("prepared_hide");

    private static Item register(String path) {
        Identifier id = FleshMain.identifierOf(path);
        Item item = new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(output -> output.accept(item));
        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static void init() {
    }
}
