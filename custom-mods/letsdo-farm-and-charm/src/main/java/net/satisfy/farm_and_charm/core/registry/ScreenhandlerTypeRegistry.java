package net.satisfy.farm_and_charm.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.satisfy.farm_and_charm.FarmAndCharm;
import net.satisfy.farm_and_charm.client.gui.handler.CookingPotGuiHandler;
import net.satisfy.farm_and_charm.client.gui.handler.RoasterGuiHandler;
import net.satisfy.farm_and_charm.client.gui.handler.StoveGuiHandler;

import java.util.function.Supplier;

// 26.2: MenuType ctor is private. Same 3 handlers preserved via Fabric
// ExtendedMenuType with unit payload (handlers resolve their BlockEntity via
// createMenu on the server side, as before); registered on Registries.MENU.
public class ScreenhandlerTypeRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(FarmAndCharm.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<StoveGuiHandler>> STOVE_SCREEN_HANDLER =
            create("stove_gui_handler", () -> new ExtendedMenuType<>((syncId, inv, unused) -> new StoveGuiHandler(syncId, inv), StreamCodec.unit(net.minecraft.core.BlockPos.ZERO)));
    public static final RegistrySupplier<MenuType<CookingPotGuiHandler>> COOKING_POT_SCREEN_HANDLER =
            create("cooking_pot_gui_handler", () -> new ExtendedMenuType<>((syncId, inv, unused) -> new CookingPotGuiHandler(syncId, inv), StreamCodec.unit(net.minecraft.core.BlockPos.ZERO)));
    public static final RegistrySupplier<MenuType<RoasterGuiHandler>> ROASTER_SCREEN_HANDLER =
            create("roaster_gui_handler", () -> new ExtendedMenuType<>((syncId, inv, unused) -> new RoasterGuiHandler(syncId, inv), StreamCodec.unit(net.minecraft.core.BlockPos.ZERO)));

    public static void init() {
        MENU_TYPES.register();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends MenuType<?>> RegistrySupplier<T> create(String name, Supplier<T> type) {
        return (RegistrySupplier<T>) MENU_TYPES.register(name, (Supplier) type);
    }
}
