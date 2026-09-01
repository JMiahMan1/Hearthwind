package com.talhanation.smallships.world.item.fabric;

import com.talhanation.smallships.SmallShipsMod;
import com.talhanation.smallships.world.entity.ship.*;
import com.talhanation.smallships.world.item.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings({"CodeBlock2Expr", "UnstableApiUsage"})
public class ModItemsImpl {
    private static final Map<String, Item> entries = new HashMap<>();

    public static Item getItem(String id) {
        return entries.get(id);
    }

    static {
        register("sail", SailItem::new, new Item.Properties().stacksTo(16));
        register("cannon", CannonItem::new, new Item.Properties().stacksTo(1));
        register("cannon_ball", CannonBallItem::new, new Item.Properties().stacksTo(16));

        for (Ship.Type type : Ship.Type.values()) {
            String name = type.getName().replaceAll("[^a-z0-9_.-]", "_");
            register(name + "_" + CogEntity.ID, (prop) -> new CogItem(Ship.Type.byName(name), prop), new Item.Properties().stacksTo(1));
            register(name + "_" + BriggEntity.ID, (prop) -> new BriggItem(Ship.Type.byName(name), prop), new Item.Properties().stacksTo(1));
            register(name + "_" + GalleyEntity.ID, (prop) -> new GalleyItem(Ship.Type.byName(name), prop), new Item.Properties().stacksTo(1));
            register(name + "_" + DrakkarEntity.ID, (prop) -> new DrakkarItem(Ship.Type.byName(name), prop), new Item.Properties().stacksTo(1));
        }

        ResourceKey<CreativeModeTab> creativeModeTab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, "creative_mode_tab"));
        CreativeModeTab customCreativeModeTab = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                .title(Component.translatable("itemGroup.smallships"))
                .icon(() -> new ItemStack(ModItems.CANNON))
                .displayItems((itemDisplayParameters, output) -> {
                    for (Item item : entries.values()) {
                        output.accept(item);
                    }
                })
                .build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, creativeModeTab, customCreativeModeTab);
    }

    private static void register(String id, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> rk = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID, id));
        Item item = factory.apply(properties.setId(rk));
        Registry.register(BuiltInRegistries.ITEM, rk, item);
        entries.put(id, item);
    }
}
