package net.dungeonz.init;

import java.util.List;
import java.util.function.UnaryOperator;

import net.dungeonz.item.*;
import net.dungeonz.item.component.DungeonCompassComponent;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponentType;

public class ItemInit {

    // Item Group
    public static final ResourceKey<CreativeModeTab> DUNGEONZ_ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("dungeonz", "item_group"));

    // Component
    public static final DataComponentType<DungeonCompassComponent> DUNGEON_COMPASS_DATA = registerComponent("fill_level",
            builder -> builder.persistent(DungeonCompassComponent.CODEC).networkSynchronized(DungeonCompassComponent.PACKET_CODEC));

    public static List<ItemStack> getRequiredDungeonCompassCalibrationItems() {
        return List.of(new ItemStack(Items.AMETHYST_SHARD, 3));
    }

    public static final Item DUNGEON_COMPASS = new DungeonCompassItem(new Item.Properties().stacksTo(1)
            .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("dungeonz", "dungeon_compass"))));

    private static <T> DataComponentType<T> registerComponent(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, DUNGEONZ_ITEM_GROUP,
                FabricCreativeModeTab.builder().icon(() -> new ItemStack(DUNGEON_COMPASS)).title(Component.translatable("item.dungeonz.item_group")).build());
        Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath("dungeonz", "dungeon_compass"), DUNGEON_COMPASS);
        CreativeModeTabEvents.modifyOutputEvent(DUNGEONZ_ITEM_GROUP).register(output -> output.accept(DUNGEON_COMPASS));
    }

}
