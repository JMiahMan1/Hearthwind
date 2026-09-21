package net.satisfy.vinery.fabric;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resources.Identifier;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.registry.CompostableRegistry;
import net.satisfy.vinery.fabric.config.VineryFabricConfig;
import net.satisfy.vinery.fabric.core.registry.VineryFabricVillagers;
import net.satisfy.vinery.fabric.core.world.VineryBiomeModification;

import java.util.Optional;

public class VineryFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Another mod (e.g. nethervinery, which sorts earlier) may already
        // have registered this - AutoConfig.register throws on duplicates.
        try {
            AutoConfig.getConfigHolder(VineryFabricConfig.class);
        } catch (Exception ignored) {
            AutoConfig.register(VineryFabricConfig.class, GsonConfigSerializer::new);
        }
        VineryFabricVillagers.registerPOIAndProfession();

        Vinery.init();
        CompostableRegistry.registerCompostable();
        VineryBiomeModification.init();
        Vinery.commonSetup();

        Optional<ModContainer> modContainer = FabricLoader.getInstance().getModContainer(Vinery.MOD_ID);
        modContainer.ifPresent(container -> {
            ResourceLoader.registerBuiltinPack(
                    Identifier.fromNamespaceAndPath(Vinery.MOD_ID, "bushy_leaves"),
                    container,
                    PackActivationType.NORMAL
            );
            // Sobriety: the alcohol datapack (wine trades, wine recipes)
            // only exists when alcohol is enabled.
            if (!dev.jmiahman.hearthwind.survival.Sobriety.alcoholRemoved()) {
                ResourceLoader.registerBuiltinPack(
                        Identifier.fromNamespaceAndPath(Vinery.MOD_ID, "vinery_alcohol"),
                        container,
                        PackActivationType.ALWAYS_ENABLED
                );
            }
        });
    }
}
