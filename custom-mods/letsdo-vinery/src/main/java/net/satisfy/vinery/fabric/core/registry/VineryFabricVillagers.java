package net.satisfy.vinery.fabric.core.registry;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.vinery.core.Vinery;
import net.satisfy.vinery.core.registry.ObjectRegistry;

import java.util.HashSet;
import java.util.Set;

/**
 * 26.2: POI + profession stay code-registered (vanilla registries); the
 * config-driven trade registration is replaced by datapack trade sets
 * (data/vinery/trade_set + tags + villager_trade), generated from the old
 * config defaults. Wine trades ship in the vinery_alcohol built-in pack.
 */
public class VineryFabricVillagers {

    private static final Identifier WINEMAKER_POI_IDENTIFIER = Vinery.identifier("winemaker_poi");
    private static final Identifier WINEMAKER_PROFESSION_IDENTIFIER = Vinery.identifier("winemaker");
    public static PoiType WINEMAKER_POI;
    public static VillagerProfession WINEMAKER;

    public static void registerPOIAndProfession() {
        Set<BlockState> states = new HashSet<>();
        states.add(ObjectRegistry.FERMENTATION_BARREL.get().defaultBlockState());
        WINEMAKER_POI = Registry.register(
                BuiltInRegistries.POINT_OF_INTEREST_TYPE, WINEMAKER_POI_IDENTIFIER,
                new PoiType(states, 1, 12)
        );

        Int2ObjectOpenHashMap<ResourceKey<TradeSet>> trades = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            trades.put(level, ResourceKey.create(Registries.TRADE_SET,
                    Identifier.fromNamespaceAndPath(Vinery.MOD_ID, "winemaker/level_" + level)));
        }
        WINEMAKER = Registry.register(
                BuiltInRegistries.VILLAGER_PROFESSION, WINEMAKER_PROFESSION_IDENTIFIER,
                new VillagerProfession(
                        Component.translatable("entity.minecraft.villager.winemaker"),
                        holder -> holder.value() == WINEMAKER_POI,
                        holder -> holder.value() == WINEMAKER_POI,
                        ImmutableSet.of(),
                        ImmutableSet.of(),
                        SoundEvents.VILLAGER_WORK_FARMER,
                        trades
                )
        );
    }
}
