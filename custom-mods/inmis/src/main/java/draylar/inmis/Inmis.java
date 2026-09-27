package draylar.inmis;

import draylar.inmis.config.BackpackInfo;
import draylar.inmis.config.InmisConfig;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.EnderBackpackItem;
import draylar.inmis.item.component.BackpackComponent;
import draylar.inmis.menu.BackpackMenu;
import draylar.inmis.network.InmisNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Inmis backpack port for 26.2 (MIT, upstream by Draylar). Behaviour mirrors
 * the 1.21 sources; the trinket integration is omitted because Trinkets is
 * not partnered into this pack, and the back rendering lives in a follow-up.
 */
public class Inmis implements ModInitializer {

    public static final String MOD_ID = "inmis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final ResourceKey<CreativeModeTab> GROUP = ResourceKey.create(
            Registries.CREATIVE_MODE_TAB, id("backpack"));

    public static final List<BackpackItem> BACKPACKS = new ArrayList<>();

    public static final Item ENDER_POUCH = Registry.register(BuiltInRegistries.ITEM, id("ender_pouch"),
            new EnderBackpackItem(new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, id("ender_pouch")))
                    .stacksTo(1)));

    public static final DataComponentType<BackpackComponent> BACKPACK_COMPONENT = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE, id("backpack"),
            DataComponentType.<BackpackComponent>builder()
                    .persistent(BackpackComponent.CODEC)
                    .networkSynchronized(BackpackComponent.STREAM_CODEC)
                    .build());

    public static final ExtendedMenuType<BackpackMenu, ItemStack> BACKPACK_MENU =
            new ExtendedMenuType<>(BackpackMenu::new, ItemStack.STREAM_CODEC);

    public static InmisConfig CONFIG = InmisConfig.defaults();

    @Override
    public void onInitialize() {
        CONFIG = InmisConfig.load(FabricLoader.getInstance().getConfigDir());
        Registry.register(BuiltInRegistries.MENU, id("backpack"), BACKPACK_MENU);
        registerBackpacks();
        InmisNetworking.init();
    }

    private void registerBackpacks() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, GROUP,
                FabricCreativeModeTab.builder()
                        .icon(() -> new ItemStack(BACKPACKS.isEmpty() ? ENDER_POUCH : BACKPACKS.get(1)))
                        .title(Component.translatable("itemGroup.inmis.backpack"))
                        .build());

        for (BackpackInfo backpack : CONFIG.backpacks) {
            String itemName = backpack.getName().toLowerCase() + "_backpack";
            Item.Properties settings = new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, id(itemName)))
                    .stacksTo(1);

            // Aged's config allows the chest armor slot to hold backpacks.
            if (CONFIG.allowBackpacksInChestplate) {
                settings = settings.component(DataComponents.EQUIPPABLE,
                        Equippable.builder(net.minecraft.world.entity.EquipmentSlot.CHEST)
                                .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                                .build());
            }
            if (backpack.isFireImmune()) {
                settings = settings.fireResistant();
            }

            BackpackItem registered = Registry.register(BuiltInRegistries.ITEM,
                    id(itemName),
                    new BackpackItem(backpack, settings));
            BACKPACKS.add(registered);
        }

        CreativeModeTabEvents.modifyOutputEvent(GROUP).register(output -> {
            BACKPACKS.forEach(output::accept);
            output.accept(ENDER_POUCH);
        });
    }

    public static boolean isBackpackEmpty(ItemStack stack) {
        BackpackComponent component = stack.get(BACKPACK_COMPONENT);
        return component == null || component.getContainer().isEmpty();
    }

    public static List<ItemStack> getBackpackContents(ItemStack stack) {
        BackpackComponent component = stack.get(BACKPACK_COMPONENT);
        return component == null ? null : component.getContainer().getItems();
    }

    public static void wipeBackpack(ItemStack stack) {
        BackpackComponent component = stack.get(BACKPACK_COMPONENT);
        if (component != null) {
            component.getContainer().clearContent();
        }
    }

    public static void openEnderChest(net.minecraft.server.level.ServerPlayer player) {
        player.openMenu(new net.minecraft.world.SimpleMenuProvider(
                (containerId, inventory, p) -> net.minecraft.world.inventory.ChestMenu.threeRows(
                        containerId, inventory, player.getEnderChestInventory()),
                Component.translatable("container.enderchest")));
        player.awardStat(net.minecraft.stats.Stats.OPEN_ENDERCHEST);
    }

    public static Identifier id(String name) {
        return Identifier.fromNamespaceAndPath(MOD_ID, name);
    }
}
