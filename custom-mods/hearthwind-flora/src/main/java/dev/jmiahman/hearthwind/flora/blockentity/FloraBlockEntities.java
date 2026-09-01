package dev.jmiahman.hearthwind.flora.blockentity;

import java.util.Set;

import dev.jmiahman.hearthwind.flora.VineryContent;
import dev.jmiahman.hearthwind.flora.FarmAndCharmContent;
import dev.jmiahman.hearthwind.flora.BreweryContent;
import dev.jmiahman.hearthwind.flora.HerbalBrewsContent;
import dev.jmiahman.hearthwind.flora.MeadowContent;
import dev.jmiahman.hearthwind.flora.CandlelightContent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class FloraBlockEntities {
    public static BlockEntityType<FermentationBarrelBlockEntity> FERMENTATION_BARREL;
    public static BlockEntityType<ApplePressBlockEntity> APPLE_PRESS;
    public static BlockEntityType<CookingPotBlockEntity> COOKING_POT;
    public static BlockEntityType<StoveBlockEntity> STOVE;
    public static BlockEntityType<BrewstationBlockEntity> BREWSTATION;
    public static BlockEntityType<TeaKettleBlockEntity> TEA_KETTLE;
    public static BlockEntityType<CheeseRackBlockEntity> CHEESE_RACK;
    public static BlockEntityType<StorageBlockEntity> STORAGE;

    public static BlockEntityType<FarmMachineryBlockEntities.WaterSprinklerBlockEntity> SPRINKLER;
    public static BlockEntityType<FarmMachineryBlockEntities.FeedingTroughBlockEntity> FEEDING_TROUGH;
    public static BlockEntityType<FarmMachineryBlockEntities.ChickenCoopBlockEntity> CHICKEN_COOP;
    public static BlockEntityType<FarmMachineryBlockEntities.DinnerBellBlockEntity> DINNER_BELL;

    public static void registerAll() {
        FERMENTATION_BARREL = register("vinery", "fermentation_barrel",
                FermentationBarrelBlockEntity::new,
                VineryContent.BLOCKS.get("fermentation_barrel"));

        APPLE_PRESS = register("vinery", "apple_press",
                ApplePressBlockEntity::new,
                VineryContent.BLOCKS.get("apple_press"));

        COOKING_POT = register("farm_and_charm", "cooking_pot",
                CookingPotBlockEntity::new,
                FarmAndCharmContent.BLOCKS.get("cooking_pot"));

        STOVE = register("farm_and_charm", "stove",
                StoveBlockEntity::new,
                FarmAndCharmContent.BLOCKS.get("stove"));

        BREWSTATION = register("brewery", "brewstation",
                BrewstationBlockEntity::new,
                BreweryContent.BLOCKS.get("beer_barrel"));

        TEA_KETTLE = register("herbalbrews", "tea_kettle",
                TeaKettleBlockEntity::new,
                HerbalBrewsContent.BLOCKS.get("tea_kettle"));

        CHEESE_RACK = register("meadow", "cheese_rack",
                CheeseRackBlockEntity::new,
                MeadowContent.BLOCKS.get("cheese_form"));

        STORAGE = register("vinery", "storage",
                StorageBlockEntity::new,
                VineryContent.BLOCKS.get("wine_box"));

        SPRINKLER = register("farm_and_charm", "sprinkler",
                FarmMachineryBlockEntities.WaterSprinklerBlockEntity::new,
                FarmAndCharmContent.BLOCKS.get("water_sprinkler"));

        FEEDING_TROUGH = register("farm_and_charm", "feeding_trough",
                FarmMachineryBlockEntities.FeedingTroughBlockEntity::new,
                FarmAndCharmContent.BLOCKS.get("feeding_trough"));

        CHICKEN_COOP = register("farm_and_charm", "chicken_coop",
                FarmMachineryBlockEntities.ChickenCoopBlockEntity::new,
                FarmAndCharmContent.BLOCKS.get("chicken_coop"));

        DINNER_BELL = register("candlelight", "dinner_bell",
                FarmMachineryBlockEntities.DinnerBellBlockEntity::new,
                CandlelightContent.BLOCKS.get("dinner_bell"));
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> BlockEntityType<T> register(
            String modId, String name, BlockEntityType.BlockEntitySupplier<T> factory, Block block) {
        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(
                BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                Identifier.fromNamespaceAndPath(modId, name));

        Set<Block> blocks = block != null ? Set.of(block) : Set.of();
        BlockEntityType<T> type = new BlockEntityType<>(factory, blocks);
        Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
        return type;
    }
}
