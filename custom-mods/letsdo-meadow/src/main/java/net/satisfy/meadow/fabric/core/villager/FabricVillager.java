package net.satisfy.meadow.fabric.core.villager;

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
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.core.registry.ObjectRegistry;

import java.util.HashSet;
import java.util.Set;

/**
 * 26.2: POI + profession stay code-registered (vanilla registries); trades
 * are datapack trade sets (data/meadow/trade_set + villager_trade), converted
 * from the old Fabric TradeOfferHelper listings. The meadow villager type is
 * registered for its outfit textures; biome mapping has no 26.2 API.
 */
@SuppressWarnings("unused")
public class FabricVillager {

    private static final Identifier CHEESEMAKER_POI_IDENTIFIER = Meadow.identifier("cheesemaker_poi");
    private static final Identifier CHEESEMAKER_PROFESSION_IDENTIFIER = Meadow.identifier("cheesemaker");
    public static PoiType CHEESEMAKER_POI;
    public static VillagerProfession CHEESEMAKER;

    private static final Identifier HERMIT_POI_IDENTIFIER = Meadow.identifier("hermit_poi");
    private static final Identifier HERMIT_PROFESSION_IDENTIFIER = Meadow.identifier("hermit");
    public static PoiType HERMIT_POI;
    public static VillagerProfession HERMIT;

    public static final VillagerType MEADOW = Registry.register(BuiltInRegistries.VILLAGER_TYPE, Meadow.identifier("meadow"), new VillagerType());

    public static void init() {
        Set<BlockState> cheesemakerStates = new HashSet<>();
        cheesemakerStates.add(ObjectRegistry.CHEESE_FORM.get().defaultBlockState());
        CHEESEMAKER_POI = Registry.register(
                BuiltInRegistries.POINT_OF_INTEREST_TYPE, CHEESEMAKER_POI_IDENTIFIER,
                new PoiType(cheesemakerStates, 1, 12)
        );
        CHEESEMAKER = Registry.register(
                BuiltInRegistries.VILLAGER_PROFESSION, CHEESEMAKER_PROFESSION_IDENTIFIER,
                new VillagerProfession(
                        Component.translatable("entity.minecraft.villager.cheesemaker"),
                        holder -> holder.value() == CHEESEMAKER_POI,
                        holder -> holder.value() == CHEESEMAKER_POI,
                        ImmutableSet.of(),
                        ImmutableSet.of(),
                        SoundEvents.VILLAGER_WORK_FARMER,
                        cheesemakerTrades()
                )
        );

        Set<BlockState> hermitStates = new HashSet<>();
        hermitStates.add(ObjectRegistry.WOODCUTTER.get().defaultBlockState());
        HERMIT_POI = Registry.register(
                BuiltInRegistries.POINT_OF_INTEREST_TYPE, HERMIT_POI_IDENTIFIER,
                new PoiType(hermitStates, 1, 12)
        );
        HERMIT = Registry.register(
                BuiltInRegistries.VILLAGER_PROFESSION, HERMIT_PROFESSION_IDENTIFIER,
                new VillagerProfession(
                        Component.translatable("entity.minecraft.villager.hermit"),
                        holder -> holder.value() == HERMIT_POI,
                        holder -> holder.value() == HERMIT_POI,
                        ImmutableSet.of(),
                        ImmutableSet.of(),
                        SoundEvents.VILLAGER_WORK_FARMER,
                        hermitTrades()
                )
        );
    }

    private static Int2ObjectOpenHashMap<ResourceKey<TradeSet>> cheesemakerTrades() {
        Int2ObjectOpenHashMap<ResourceKey<TradeSet>> trades = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            trades.put(level, ResourceKey.create(Registries.TRADE_SET,
                    Identifier.fromNamespaceAndPath(Meadow.MOD_ID, "cheesemaker/level_" + level)));
        }
        return trades;
    }

    private static Int2ObjectOpenHashMap<ResourceKey<TradeSet>> hermitTrades() {
        Int2ObjectOpenHashMap<ResourceKey<TradeSet>> trades = new Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            trades.put(level, ResourceKey.create(Registries.TRADE_SET,
                    Identifier.fromNamespaceAndPath(Meadow.MOD_ID, "hermit/level_" + level)));
        }
        return trades;
    }
}
