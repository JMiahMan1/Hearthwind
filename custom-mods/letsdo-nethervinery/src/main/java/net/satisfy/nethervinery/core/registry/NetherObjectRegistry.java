package net.satisfy.nethervinery.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.satisfy.nethervinery.core.block.NetherNineBottleStorageBlock;
import net.satisfy.nethervinery.core.NetherVinery;
import net.satisfy.nethervinery.core.block.*;
import net.satisfy.nethervinery.core.util.NetherVineryIdentifier;
import net.satisfy.vinery.core.block.*;
import net.satisfy.vinery.core.item.GrapeBushSeedItem;
import net.satisfy.vinery.core.item.GrapeItem;
import net.satisfy.vinery.core.registry.MobEffectRegistry;
import net.satisfy.vinery.core.util.GeneralUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class NetherObjectRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(NetherVinery.MODID, Registries.ITEM);
    public static final Registrar<Item> ITEM_REGISTRAR = ITEMS.getRegistrar();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(NetherVinery.MODID, Registries.BLOCK);
    public static final Registrar<Block> BLOCK_REGISTRAR = BLOCKS.getRegistrar();

    public static final RegistrySupplier<Block> OBSIDIAN_STEM = registerWithItem("obsidian_stem", () -> new PaleStemBlock(getGrapevineSettings(NetherVineryIdentifier.of("obsidian_stem"))));
    public static final RegistrySupplier<Item> CRIMSON_NETHER_BAG = registerItem("crimson_nether_bag", () -> new BundleItem(getSettings(NetherVineryIdentifier.of("crimson_nether_bag")).stacksTo(1)));
    public static final RegistrySupplier<Item> WARPED_NETHER_BAG = registerItem("warped_nether_bag", () -> new BundleItem(getSettings(NetherVineryIdentifier.of("warped_nether_bag")).stacksTo(1)));
    public static final RegistrySupplier<Block> CRIMSON_GRAPE_BUSH = registerBlock("crimson_grape_bush", () -> new CrimsonGrapeBush(getBushSettings(NetherVineryIdentifier.of("crimson_grape_bush")), NetherGrapeTypes.CRIMSON));
    public static final RegistrySupplier<Block> WARPED_GRAPE_BUSH = registerBlock("warped_grape_bush", () -> new WarpedGrapeBush(getBushSettings(NetherVineryIdentifier.of("warped_grape_bush")), NetherGrapeTypes.WARPED));
    public static final RegistrySupplier<Item> CRIMSON_GRAPE_SEEDS = registerItem("crimson_grape_seeds", () -> new GrapeBushSeedItem(CRIMSON_GRAPE_BUSH.get(), getSettings(NetherVineryIdentifier.of("crimson_grape_seeds")), NetherGrapeTypes.CRIMSON));
    public static final RegistrySupplier<Item> CRIMSON_GRAPE = registerItem("crimson_grape", () -> new GrapeItem(getSettings(NetherVineryIdentifier.of("crimson_grape")).food(Foods.SWEET_BERRIES), NetherGrapeTypes.CRIMSON, CRIMSON_GRAPE_SEEDS.get()));
    public static final RegistrySupplier<Item> WARPED_GRAPE_SEEDS = registerItem("warped_grape_seeds", () -> new GrapeBushSeedItem(WARPED_GRAPE_BUSH.get(), getSettings(NetherVineryIdentifier.of("warped_grape_seeds")), NetherGrapeTypes.WARPED));
    public static final RegistrySupplier<Item> WARPED_GRAPE = registerItem("warped_grape", () -> new GrapeItem(getSettings(NetherVineryIdentifier.of("warped_grape")).food(Foods.SWEET_BERRIES), NetherGrapeTypes.WARPED, WARPED_GRAPE_SEEDS.get()));
    // 26.2: Blocks.RED_WOOL is gone (WOOL is a ColorCollection); crates use the
    // vinery grape-bag pattern: fresh Properties with wool sound.
    public static final RegistrySupplier<Block> WARPED_GRAPE_CRATE = registerWithItem("warped_grape_crate", () -> new FacingBlock(getCrateSettings(NetherVineryIdentifier.of("warped_grape_crate"))));
    public static final RegistrySupplier<Block> CRIMSON_GRAPE_GRATE = registerWithItem("crimson_grape_crate", () -> new FacingBlock(getCrateSettings(NetherVineryIdentifier.of("crimson_grape_crate"))));
    public static final RegistrySupplier<Item> WARPED_GRAPEJUICE = registerItem("warped_grapejuice", () -> new Item(getSettings(NetherVineryIdentifier.of("warped_grapejuice"))));
    public static final RegistrySupplier<Item> CRIMSON_GRAPEJUICE = registerItem("crimson_grapejuice", () -> new Item(getSettings(NetherVineryIdentifier.of("crimson_grapejuice"))));
    public static final RegistrySupplier<Block> GHASTLY_GRENACHE = registerBlock("ghastly_grenache", () -> new  NetherWineBottleBlock(getWineSettings(NetherVineryIdentifier.of("ghastly_grenache")), 2));
    public static final RegistrySupplier<Block> NETHERITE_NECTAR = registerBlock("netherite_nectar", () -> new NetherWineBottleBlock(getWineSettings(NetherVineryIdentifier.of("netherite_nectar")), 3));
    public static final RegistrySupplier<Block> BLAZEWINE_PINOT = registerBlock("blazewine_pinot", () -> new NetherWineBottleBlock(getWineSettings(NetherVineryIdentifier.of("blazewine_pinot")), 1));
    public static final RegistrySupplier<Block> NETHER_FIZZ = registerBlock("nether_fizz", () -> new NetherWineBottleBlock(getWineSettings(NetherVineryIdentifier.of("nether_fizz")), 2));
    public static final RegistrySupplier<Block> LAVA_FIZZ = registerBlock("lava_fizz", () -> new NetherWineBottleBlock(getWineSettings(NetherVineryIdentifier.of("lava_fizz")), 3));
    public static final RegistrySupplier<Block> IMPROVED_NETHER_FIZZ = registerBlock("improved_nether_fizz", () -> new NetherWineBottleBlock(getWineSettings(NetherVineryIdentifier.of("improved_nether_fizz")), 3));
    public static final RegistrySupplier<Block> IMPROVED_LAVA_FIZZ = registerBlock("improved_lava_fizz", () -> new NetherWineBottleBlock(getWineSettings(NetherVineryIdentifier.of("improved_lava_fizz")), 3));
    // 26.2: MobEffectRegistry fields are Identifiers (no .get()); resolve Holders via getHolder.
    public static final RegistrySupplier<Item> GHASTLY_GRENACHE_ITEM = registerWineItem("ghastly_grenache", GHASTLY_GRENACHE, () -> MobEffectRegistry.getHolder(MobEffectRegistry.IMPROVED_JUMP_BOOST), 1600, 0, true);
    public static final RegistrySupplier<Item> BLAZEWINE_PINOT_ITEM = registerWineItem("blazewine_pinot", BLAZEWINE_PINOT, () -> MobEffectRegistry.getHolder(MobEffectRegistry.LAVA_WALKER), 1600, 0, true);
    // 26.2: RegistrySupplier exposes Holders via asHolder().
    public static final RegistrySupplier<Item> NETHERITE_NECTAR_ITEM = registerFixedDurationWineItem("netherite_nectar", NETHERITE_NECTAR, 240, () -> NetherEffects.NETHERITE.asHolder(), 0);
    public static final RegistrySupplier<Item> NETHER_FIZZ_ITEM = registerFixedDurationWineItem("nether_fizz", NETHER_FIZZ, 30, () -> NetherEffects.HEARTHSTONE.asHolder(), 0);
    public static final RegistrySupplier<Item> LAVA_FIZZ_ITEM = registerFixedDurationWineItem("lava_fizz", LAVA_FIZZ, 30, () -> NetherEffects.GRAVEDIGGER.asHolder(), 0);
    public static final RegistrySupplier<Item> IMPROVED_NETHER_FIZZ_ITEM = registerFixedDurationWineItem("improved_nether_fizz", IMPROVED_NETHER_FIZZ, 30, () -> NetherEffects.IMPROVED_HEARTHSTONE.asHolder(), 0);
    public static final RegistrySupplier<Item> IMPROVED_LAVA_FIZZ_ITEM = registerFixedDurationWineItem("improved_lava_fizz", IMPROVED_LAVA_FIZZ, 30, () -> NetherEffects.IMPROVED_GRAVEDIGGER.asHolder(), 0);
    public static final RegistrySupplier<Block> CRIMSON_FERMENTATION_BARREL = registerWithItem("crimson_fermentation_barrel", () -> new NetherFermentationBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(NetherVineryIdentifier.of("crimson_fermentation_barrel"))).noOcclusion()));
    public static final RegistrySupplier<Block> CRIMSON_GRAPEVINE_POT = registerWithItem("crimson_grapevine_pot", () -> new NetherGrapevinePotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).setId(blockKey(NetherVineryIdentifier.of("crimson_grapevine_pot"))).noOcclusion()));
    public static final RegistrySupplier<Block> CRIMSON_APPLE_PRESS = registerWithItem("crimson_apple_press", () -> new NetherApplePressBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).setId(blockKey(NetherVineryIdentifier.of("crimson_apple_press"))).noOcclusion()));
    public static final RegistrySupplier<Block> CRIMSON_WINE_RACK_BIG = registerWithItem("crimson_wine_rack_big", () -> new NetherNineBottleStorageBlock(BlockBehaviour.Properties.of().setId(blockKey(NetherVineryIdentifier.of("crimson_wine_rack_big"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> CRIMSON_WINE_RACK_SMALL = registerWithItem("crimson_wine_rack_small", () -> new NetherFourBottleStorageBlock(BlockBehaviour.Properties.of().setId(blockKey(NetherVineryIdentifier.of("crimson_wine_rack_small"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> CRIMSON_WINE_RACK_MID = registerWithItem("crimson_wine_rack_mid", () -> new NetherBigBottleStorageBlock(BlockBehaviour.Properties.of().setId(blockKey(NetherVineryIdentifier.of("crimson_wine_rack_mid"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> REINFORCED_CRIMSON_PLANKS = registerWithItem("reinforced_crimson_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).setId(blockKey(NetherVineryIdentifier.of("reinforced_crimson_planks")))));
    public static final RegistrySupplier<Block> CRESTED_CRIMSON_PLANKS = registerWithItem("crested_crimson_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).setId(blockKey(NetherVineryIdentifier.of("crested_crimson_planks")))));
    public static final RegistrySupplier<Block> CRIMSON_BARREL = registerWithItem("crimson_barrel", () -> new BarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(NetherVineryIdentifier.of("crimson_barrel")))));
    public static final RegistrySupplier<Block> WARPED_FERMENTATION_BARREL = registerWithItem("warped_fermentation_barrel", () -> new NetherFermentationBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(NetherVineryIdentifier.of("warped_fermentation_barrel"))).noOcclusion()));
    public static final RegistrySupplier<Block> WARPED_GRAPEVINE_POT = registerWithItem("warped_grapevine_pot", () -> new NetherGrapevinePotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).setId(blockKey(NetherVineryIdentifier.of("warped_grapevine_pot"))).noOcclusion()));
    public static final RegistrySupplier<Block> WARPED_APPLE_PRESS = registerWithItem("warped_apple_press", () -> new NetherApplePressBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).setId(blockKey(NetherVineryIdentifier.of("warped_apple_press"))).noOcclusion()));
    public static final RegistrySupplier<Block> WARPED_WINE_RACK_BIG = registerWithItem("warped_wine_rack_big", () -> new NetherNineBottleStorageBlock(BlockBehaviour.Properties.of().setId(blockKey(NetherVineryIdentifier.of("warped_wine_rack_big"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> WARPED_WINE_RACK_SMALL = registerWithItem("warped_wine_rack_small", () -> new NetherFourBottleStorageBlock(BlockBehaviour.Properties.of().setId(blockKey(NetherVineryIdentifier.of("warped_wine_rack_small"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> WARPED_WINE_RACK_MID = registerWithItem("warped_wine_rack_mid", () -> new NetherBigBottleStorageBlock(BlockBehaviour.Properties.of().setId(blockKey(NetherVineryIdentifier.of("warped_wine_rack_mid"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> REINFORCED_WARPED_PLANKS = registerWithItem("reinforced_warped_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).setId(blockKey(NetherVineryIdentifier.of("reinforced_warped_planks")))));
    public static final RegistrySupplier<Block> CRESTED_WARPED_PLANKS = registerWithItem("crested_warped_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS).setId(blockKey(NetherVineryIdentifier.of("crested_warped_planks")))));
    public static final RegistrySupplier<Block> WARPED_BARREL = registerWithItem("warped_barrel", () -> new BarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).setId(blockKey(NetherVineryIdentifier.of("warped_barrel")))));
    // 26.2: BlockBehaviour.getSoundType(BlockState) is protected; read it off the
    // default BlockState instead (mirrors vinery ObjectRegistry lattices).
    public static final RegistrySupplier<Block> WARPED_LATTICE = registerWithItem("warped_lattice", () -> new LatticeBlock(BlockBehaviour.Properties.of().setId(blockKey(NetherVineryIdentifier.of("warped_lattice"))).strength(2.0F, 3.0F).sound(Blocks.JUNGLE_PLANKS.defaultBlockState().getSoundType()).noOcclusion()));
    public static final RegistrySupplier<Block> CRIMSON_LATTICE = registerWithItem("crimson_lattice", () -> new LatticeBlock(BlockBehaviour.Properties.of().setId(blockKey(NetherVineryIdentifier.of("crimson_lattice"))).strength(2.0F, 3.0F).sound(Blocks.MANGROVE_PLANKS.defaultBlockState().getSoundType()).noOcclusion()));

    private static <T extends Item> RegistrySupplier<T> registerItem(String path, Supplier<T> item) {
        final Identifier id = NetherVineryIdentifier.of(path);
        return ITEM_REGISTRAR.register(id, item);
    }

    private static <T extends Block> RegistrySupplier<T> registerBlock(String path, Supplier<T> block) {
        final Identifier id = NetherVineryIdentifier.of(path);
        return BLOCK_REGISTRAR.register(id, block);
    }

    public static void init() {
        ITEMS.register();
        BLOCKS.register();
    }

    private static ResourceKey<Item> itemKey(Identifier id) {
        return ResourceKey.create(Registries.ITEM, id);
    }

    private static ResourceKey<Block> blockKey(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    private static Item.Properties getSettings(Identifier id, Consumer<Item.Properties> consumer) {
        Item.Properties settings = new Item.Properties().setId(itemKey(id));
        consumer.accept(settings);
        return settings;
    }

    private static Item.Properties getSettings(Identifier id) {
        return getSettings(id, settings -> {
        });
    }

    // 26.2: drink effects apply via NetherDrinkBlockItem.finishUsingItem (mirrors
    // vinery DrinkBlockItem.setEffectSupplier); the base food stays plain because
    // FoodProperties.Builder.effect is gone.
    private static RegistrySupplier<Item> registerWineItem(String name, Supplier<Block> wineBlock, Supplier<Holder<MobEffect>> effect, int duration, int amplifier, boolean scaleDurationWithAge) {
        return registerItemtem(name, () -> {
            NetherDrinkBlockItem item = new NetherDrinkBlockItem(
                    wineBlock.get(),
                    new Item.Properties().setId(itemKey(NetherVineryIdentifier.of(name))).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).alwaysEdible().build()),
                    duration,
                    scaleDurationWithAge
            );
            item.setEffectSupplier(effect, duration, amplifier);
            return item;
        });
    }

    private static RegistrySupplier<Item> registerFixedDurationWineItem(
            String name,
            Supplier<Block> wineBlock,
            int fixedDurationTicks,
            Supplier<Holder<MobEffect>> effectSupplier,
            int amplifier
    ) {
        return registerItemtem(name, () -> {
            NetherDrinkBlockItem item = new NetherDrinkBlockItem(
                    wineBlock.get(),
                    new Item.Properties().setId(itemKey(NetherVineryIdentifier.of(name))).food(new FoodProperties.Builder().nutrition(1).saturationModifier(0.3f).alwaysEdible().build()),
                    fixedDurationTicks,
                    false
            );
            item.setEffectSupplier(effectSupplier, fixedDurationTicks, amplifier);
            return item;
        });
    }

    private static BlockBehaviour.Properties getGrapevineSettings(Identifier id) {
        return BlockBehaviour.Properties.of().setId(blockKey(id)).strength(3.0F).randomTicks().sound(SoundType.STONE).noOcclusion();
    }

    private static BlockBehaviour.Properties getCrateSettings(Identifier id) {
        return BlockBehaviour.Properties.of().setId(blockKey(id)).strength(2.0F, 3.0F).sound(SoundType.WOOL);
    }

    private static BlockBehaviour.Properties getBushSettings(Identifier id) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).setId(blockKey(id));
    }

    private static BlockBehaviour.Properties getWineSettings(Identifier id) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(id)).noOcclusion().instabreak();
    }

    public static <T extends Block> RegistrySupplier<T> registerWithItem(String path, Supplier<T> block) {
        return GeneralUtil.registerWithItem(BLOCKS, BLOCK_REGISTRAR, ITEMS, ITEM_REGISTRAR, NetherVineryIdentifier.of(path), block);
    }

    public static <T extends Item> RegistrySupplier<T> registerItemtem(String path, Supplier<T> itemSupplier) {
        return GeneralUtil.registerItem(ITEMS, ITEM_REGISTRAR, NetherVineryIdentifier.of(path), itemSupplier);
    }
}
