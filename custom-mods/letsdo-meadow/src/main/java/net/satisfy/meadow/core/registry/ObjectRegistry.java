package net.satisfy.meadow.core.registry;

import dev.architectury.registry.fuel.FuelRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.satisfy.meadow.Meadow;
import net.satisfy.meadow.core.block.*;
import net.satisfy.meadow.core.entity.PineBoatEntity;
import net.satisfy.meadow.core.item.*;
import net.satisfy.meadow.core.util.GeneralUtil;
import net.satisfy.meadow.core.util.MeadowWoodType;
import net.satisfy.meadow.core.util.WoodenCauldronBehavior;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ObjectRegistry {
    public static final TreeGrower PINE = new TreeGrower("pine_tree", Optional.empty(), Optional.of(configuredFeatureKey("pine_tree")), Optional.empty());
    public static final TreeGrower YELLOW_PINE = new TreeGrower("yellow_pine_tree", Optional.empty(), Optional.of(configuredFeatureKey("yellow_pine_tree")), Optional.empty());
    public static final TreeGrower ALPINE_BIRCH = new TreeGrower("alpine_birch_tree", Optional.empty(), Optional.of(configuredFeatureKey("alpine_birch_tree")), Optional.empty());

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Meadow.MOD_ID, Registries.ITEM);
    public static final Registrar<Item> ITEM_REGISTRAR = ITEMS.getRegistrar();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Meadow.MOD_ID, Registries.BLOCK);
    public static final Registrar<Block> BLOCK_REGISTRAR = BLOCKS.getRegistrar();

    public static final RegistrySupplier<Block> ALPINE_SALT_ORE = registerWithItem("alpine_salt_ore", () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("alpine_salt_ore"))).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final RegistrySupplier<Block> ALPINE_COAL_ORE = registerWithItem("alpine_coal_ore", () -> new DropExperienceBlock(UniformInt.of(0, 2), BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("alpine_coal_ore"))).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final RegistrySupplier<Block> ALPINE_LAPIS_ORE = registerWithItem("alpine_lapis_ore", () -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("alpine_lapis_ore"))).strength(4f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ALPINE_GOLD_ORE = registerWithItem("alpine_gold_ore", () -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("alpine_gold_ore"))).mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(3.0F, 3.0F)));
    public static final RegistrySupplier<Block> ALPINE_EMERALD_ORE = registerWithItem("alpine_emerald_ore", () -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("alpine_emerald_ore"))).strength(4f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ALPINE_IRON_ORE = registerWithItem("alpine_iron_ore", () -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("alpine_iron_ore"))).strength(4f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ALPINE_COPPER_ORE = registerWithItem("alpine_copper_ore", () -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("alpine_copper_ore"))).strength(4f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ALPINE_DIAMOND_ORE = registerWithItem("alpine_diamond_ore", () -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("alpine_diamond_ore"))).strength(4f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> ALPINE_REDSTONE_ORE = registerWithItem("alpine_redstone_ore", () -> new DropExperienceBlock(UniformInt.of(0, 1), BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("alpine_redstone_ore"))).strength(4f).requiresCorrectToolForDrops()));
    public static final RegistrySupplier<Block> LIMESTONE = registerWithItem("limestone", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("limestone")))));
    public static final RegistrySupplier<Block> LIMESTONE_STAIRS = registerWithItem("limestone_stairs", () -> new StairBlock(LIMESTONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(LIMESTONE.get()).setId(blockKey(Meadow.identifier("limestone_stairs")))));
    public static final RegistrySupplier<Block> LIMESTONE_SLAB = registerWithItem("limestone_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(LIMESTONE.get()).setId(blockKey(Meadow.identifier("limestone_slab")))));
    public static final RegistrySupplier<Block> COBBLED_LIMESTONE = registerWithItem("cobbled_limestone", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.COBBLESTONE).setId(blockKey(Meadow.identifier("cobbled_limestone")))));
    public static final RegistrySupplier<Block> COBBLED_LIMESTONE_STAIRS = registerWithItem("cobbled_limestone_stairs", () -> new StairBlock(COBBLED_LIMESTONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(COBBLED_LIMESTONE.get()).setId(blockKey(Meadow.identifier("cobbled_limestone_stairs")))));
    public static final RegistrySupplier<Block> COBBLED_LIMESTONE_SLAB = registerWithItem("cobbled_limestone_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_LIMESTONE.get()).setId(blockKey(Meadow.identifier("cobbled_limestone_slab")))));
    public static final RegistrySupplier<Block> LIMESTONE_BRICKS = registerWithItem("limestone_bricks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS).setId(blockKey(Meadow.identifier("limestone_bricks")))));
    public static final RegistrySupplier<Block> LIMESTONE_BRICK_STAIRS = registerWithItem("limestone_brick_stairs", () -> new StairBlock(LIMESTONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(LIMESTONE_BRICKS.get()).setId(blockKey(Meadow.identifier("limestone_brick_stairs")))));
    public static final RegistrySupplier<Block> LIMESTONE_BRICK_SLAB = registerWithItem("limestone_brick_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(LIMESTONE_BRICKS.get()).setId(blockKey(Meadow.identifier("limestone_brick_slab")))));
    public static final RegistrySupplier<Block> MOSSY_COBBLED_LIMESTONE = registerWithItem("mossy_cobbled_limestone", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_COBBLESTONE).setId(blockKey(Meadow.identifier("mossy_cobbled_limestone")))));
    public static final RegistrySupplier<Block> MOSSY_COBBLED_LIMESTONE_STAIRS = registerWithItem("mossy_cobbled_limestone_stairs", () -> new StairBlock(MOSSY_COBBLED_LIMESTONE.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(MOSSY_COBBLED_LIMESTONE.get()).setId(blockKey(Meadow.identifier("mossy_cobbled_limestone_stairs")))));
    public static final RegistrySupplier<Block> MOSSY_COBBLED_LIMESTONE_SLAB = registerWithItem("mossy_cobbled_limestone_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(MOSSY_COBBLED_LIMESTONE.get()).setId(blockKey(Meadow.identifier("mossy_cobbled_limestone_slab")))));
    public static final RegistrySupplier<Block> MOSSY_LIMESTONE_BRICKS = registerWithItem("mossy_limestone_bricks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.MOSSY_STONE_BRICKS).setId(blockKey(Meadow.identifier("mossy_limestone_bricks")))));
    public static final RegistrySupplier<Block> MOSSY_LIMESTONE_BRICK_STAIRS = registerWithItem("mossy_limestone_brick_stairs", () -> new StairBlock(LIMESTONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(LIMESTONE_BRICKS.get()).setId(blockKey(Meadow.identifier("mossy_limestone_brick_stairs")))));
    public static final RegistrySupplier<Block> MOSSY_LIMESTONE_BRICK_SLAB = registerWithItem("mossy_limestone_brick_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(MOSSY_LIMESTONE_BRICKS.get()).setId(blockKey(Meadow.identifier("mossy_limestone_brick_slab")))));
    public static final RegistrySupplier<Block> CRACKED_LIMESTONE_BRICKS = registerWithItem("cracked_limestone_bricks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CRACKED_STONE_BRICKS).setId(blockKey(Meadow.identifier("cracked_limestone_bricks")))));
    public static final RegistrySupplier<Block> CHISELED_LIMESTONE = registerWithItem("chiseled_limestone", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.CHISELED_STONE_BRICKS).setId(blockKey(Meadow.identifier("chiseled_limestone")))));
    public static final RegistrySupplier<Block> POLISHED_LIMESTONE = registerWithItem("polished_limestone", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_ANDESITE).setId(blockKey(Meadow.identifier("polished_limestone")))));
    public static final RegistrySupplier<Block> LIMESTONE_WALL = registerWithItem("limestone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(LIMESTONE.get()).setId(blockKey(Meadow.identifier("limestone_wall")))));
    public static final RegistrySupplier<Block> COBBLED_LIMESTONE_WALL = registerWithItem("cobbled_limestone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_LIMESTONE.get()).setId(blockKey(Meadow.identifier("cobbled_limestone_wall")))));
    public static final RegistrySupplier<Block> LIMESTONE_BRICK_WALL = registerWithItem("limestone_brick_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(LIMESTONE_BRICKS.get()).setId(blockKey(Meadow.identifier("limestone_brick_wall")))));
    public static final RegistrySupplier<Block> MOSSY_COBBLED_LIMESTONE_WALL = registerWithItem("mossy_cobbled_limestone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(MOSSY_COBBLED_LIMESTONE.get()).setId(blockKey(Meadow.identifier("mossy_cobbled_limestone_wall")))));
    public static final RegistrySupplier<Block> MOSSY_LIMESTONE_BRICK_WALL = registerWithItem("mossy_limestone_brick_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(MOSSY_LIMESTONE_BRICKS.get()).setId(blockKey(Meadow.identifier("mossy_limestone_brick_wall")))));
    public static final RegistrySupplier<Block> PINE_SAPLING = registerWithItem("pine_sapling", () -> new SaplingBlock(PINE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING).setId(blockKey(Meadow.identifier("pine_sapling")))));
    public static final RegistrySupplier<Block> PINE_LEAVES = registerWithItem("pine_leaves", () -> new TintedParticleLeavesBlock(0.01F, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LEAVES).setId(blockKey(Meadow.identifier("pine_leaves")))));
    public static final RegistrySupplier<Block> YELLOW_PINE_SAPLING = registerWithItem("yellow_pine_sapling", () -> new SaplingBlock(YELLOW_PINE, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_SAPLING).setId(blockKey(Meadow.identifier("yellow_pine_sapling")))));
    public static final RegistrySupplier<Block> YELLOW_PINE_LEAVES = registerWithItem("yellow_pine_leaves", () -> new TintedParticleLeavesBlock(0.01F, BlockBehaviour.Properties.ofFullCopy(Blocks.SPRUCE_LEAVES).setId(blockKey(Meadow.identifier("yellow_pine_leaves")))));
    public static final RegistrySupplier<Block> ALPINE_BIRCH_SAPLING = registerWithItem("alpine_birch_sapling", () -> new SaplingBlock(ALPINE_BIRCH, BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_SAPLING).setId(blockKey(Meadow.identifier("alpine_birch_sapling")))));
    public static final RegistrySupplier<Block> ALPINE_BIRCH_LEAVES = registerWithItem("alpine_birch_leaves", () -> new HangingLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BIRCH_LEAVES).setId(blockKey(Meadow.identifier("alpine_birch_leaves")))));
    public static final RegistrySupplier<Block> ALPINE_BIRCH_LOG = registerWithItem("alpine_birch_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("alpine_birch_log"))).sound(SoundType.WOOD).strength(2.0f)));
    public static final RegistrySupplier<Block> PINE_LOG = registerLog("pine_log");
    public static final RegistrySupplier<Block> PINE_WOOD = registerLog("pine_wood");
    public static final RegistrySupplier<Block> STRIPPED_PINE_WOOD = registerLog("stripped_pine_wood");
    public static final RegistrySupplier<Block> STRIPPED_PINE_LOG = registerLog("stripped_pine_log");
    public static final RegistrySupplier<Block> PINE_BEAM = registerLog("pine_beam");
    public static final RegistrySupplier<Block> PINE_PLANKS = registerWithItem("pine_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_planks"))).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PINE_STAIRS = registerWithItem("pine_stairs", () -> new StairBlock(PINE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(PINE_PLANKS.get()).setId(blockKey(Meadow.identifier("pine_stairs")))));
    public static final RegistrySupplier<Block> PINE_SLAB = registerWithItem("pine_slab", () -> new SlabBlock(getSlabSettings(Meadow.identifier("pine_slab"))));
    public static final RegistrySupplier<Block> RECLAIMED_PINE_PLANKS = registerWithItem("reclaimed_pine_planks", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("reclaimed_pine_planks"))).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> RECLAIMED_PINE_STAIRS = registerWithItem("reclaimed_pine_stairs", () -> new StairBlock(RECLAIMED_PINE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(PINE_PLANKS.get()).setId(blockKey(Meadow.identifier("reclaimed_pine_stairs")))));
    public static final RegistrySupplier<Block> RECLAIMED_PINE_SLAB = registerWithItem("reclaimed_pine_slab", () -> new SlabBlock(getSlabSettings(Meadow.identifier("reclaimed_pine_slab"))));
    public static final RegistrySupplier<Block> PINE_PRESSURE_PLATE = registerWithItem("pine_pressure_plate", () -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_pressure_plate"))).noCollision().strength(0.5f).sound(SoundType.WOOD).mapColor(PINE_PLANKS.get().defaultMapColor())));
    public static final RegistrySupplier<Block> PINE_BUTTON = registerWithItem("pine_button", () -> woodenButton(Meadow.identifier("pine_button"), FeatureFlags.VANILLA));
    public static final RegistrySupplier<Block> PINE_TRAPDOOR = registerWithItem("pine_trapdoor", () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_TRAPDOOR).setId(blockKey(Meadow.identifier("pine_trapdoor")))));
    public static final RegistrySupplier<Block> PINE_DOOR = registerWithItem("pine_door", () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_door"))).strength(3.0f).sound(SoundType.WOOD).noOcclusion().mapColor(PINE_PLANKS.get().defaultMapColor())));
    public static final RegistrySupplier<Block> RECLAIMED_PINE_TRAPDOOR = registerWithItem("reclaimed_pine_trapdoor", () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_TRAPDOOR).setId(blockKey(Meadow.identifier("reclaimed_pine_trapdoor")))));
    public static final RegistrySupplier<Block> RECLAIMED_PINE_DOOR = registerWithItem("reclaimed_pine_door", () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("reclaimed_pine_door"))).strength(3.0f).sound(SoundType.WOOD).noOcclusion().mapColor(PINE_PLANKS.get().defaultMapColor())));
    public static final RegistrySupplier<Block> PINE_FENCE = registerWithItem("pine_fence", () -> new FenceBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_fence"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PINE_FENCE_GATE = registerWithItem("pine_fence_gate", () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_fence_gate"))).strength(2.0f, 3.0f).sound(SoundType.WOOD).mapColor(PINE_PLANKS.get().defaultMapColor())));
    public static final RegistrySupplier<Block> PINE_RAILING = registerWithItem("pine_railing", () -> new FenceBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_railing"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PINE_CABINET = registerWithItem("pine_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_cabinet"))), () -> SoundEvents.WOODEN_TRAPDOOR_OPEN, () -> SoundEvents.WOODEN_TRAPDOOR_CLOSE, () -> false));
    public static final RegistrySupplier<Block> PINE_WALL_CABINET = registerWithItem("pine_wall_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_wall_cabinet"))), () -> SoundEvents.WOODEN_TRAPDOOR_OPEN, () -> SoundEvents.WOODEN_TRAPDOOR_CLOSE, () -> true));
    public static final RegistrySupplier<Block> PINE_CHEESE_RACK = registerWithItem("pine_cheese_rack", () -> new CheeseRackBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_cheese_rack"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> PINE_CHAIR = registerWithItem("pine_chair", () -> new ChairBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PINE_TABLE = registerWithItem("pine_table", () -> new TableBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_table"))).requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> PINE_BENCH = registerWithItem("pine_bench", () -> new BenchBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_bench"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> PINE_SOFA_RUSTIC = registerWithItem("pine_sofa_rustic", () -> new SofaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_sofa_rustic"))).sound(SoundType.WOOD).mapColor(MapColor.COLOR_BROWN)));
    public static final RegistrySupplier<Block> PINE_SOFA_LINEN = registerWithItem("pine_sofa_linen", () -> new SofaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_sofa_linen"))).sound(SoundType.WOOD).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistrySupplier<Block> PINE_SOFA_JACQUARD = registerWithItem("pine_sofa_jacquard", () -> new SofaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_sofa_jacquard"))).sound(SoundType.WOOD).mapColor(MapColor.COLOR_BLUE)));
    public static final RegistrySupplier<Block> PINE_SOFA_PLAID = registerWithItem("pine_sofa_plaid", () -> new SofaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_sofa_plaid"))).sound(SoundType.WOOD).mapColor(MapColor.COLOR_GRAY)));
    public static final RegistrySupplier<Block> PINE_SOFA_CHAMBRAY = registerWithItem("pine_sofa_chambray", () -> new SofaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_sofa_chambray"))).sound(SoundType.WOOD).mapColor(MapColor.COLOR_BLUE)));
    public static final RegistrySupplier<Block> PINE_SOFA_TWEED = registerWithItem("pine_sofa_tweed", () -> new SofaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_sofa_tweed"))).sound(SoundType.WOOD).mapColor(MapColor.COLOR_BLACK)));
    public static final RegistrySupplier<Block> PINE_SOFA_WARPED = registerWithItem("pine_sofa_warped", () -> new SofaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_sofa_warped"))).sound(SoundType.WOOD).mapColor(MapColor.COLOR_CYAN)));
    public static final RegistrySupplier<Block> PINE_DRESSER = registerWithItem("pine_dresser", () -> new DresserBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_dresser"))), () -> SoundEvents.WOODEN_TRAPDOOR_OPEN, () -> SoundEvents.WOODEN_TRAPDOOR_CLOSE));
    public static final RegistrySupplier<Block> PINE_WARDROBE = registerWithItem("pine_wardrobe", () -> new WardrobeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("pine_wardrobe"))).sound(SoundType.WOOD).mapColor(MapColor.TERRACOTTA_WHITE)));
    public static final RegistrySupplier<Block> PINE_WINDOW = registerWithItem("pine_window", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Meadow.identifier("pine_window")))));
    public static final RegistrySupplier<Block> PINE_WINDOW_PANE = registerWithItem("pine_window_pane", () -> new WindowBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).setId(blockKey(Meadow.identifier("pine_window_pane")))));
    public static final RegistrySupplier<Block> PINE_SHUTTER = registerWithItem("pine_shutter", () -> new ShutterBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("pine_shutter"))).strength(1.0F).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> STONE_TABLE = registerWithItem("stone_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("stone_table"))).strength(4f, 0.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistrySupplier<Block> STONE_BENCH = registerWithItem("stone_bench", () -> new BenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Meadow.identifier("stone_bench"))).strength(4f, 0.0f).sound(SoundType.STONE)));
    public static final RegistrySupplier<Block> TILED_STOVE = registerWithItem("stove_tiles", () -> new TiledStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Meadow.identifier("stove_tiles")))));
    public static final RegistrySupplier<Block> TILED_STOVE_FIREPLACE = registerWithItem("stove_tiles_wood", () -> new TiledStoveBlockFireplace(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Meadow.identifier("stove_tiles_wood"))).lightLevel(s -> s.getValue(TiledStoveBlock.LIT) ? 13 : 0).randomTicks()));
    public static final RegistrySupplier<Block> TILED_STOVE_SMOKER = registerWithItem("stove_tiles_lid", () -> new TiledStoveBlockSmoker(BlockBehaviour.Properties.ofFullCopy(Blocks.SMOKER).setId(blockKey(Meadow.identifier("stove_tiles_lid"))), Direction.UP));
    public static final RegistrySupplier<Block> TILED_STOVE_BENCH = registerWithItem("stove_tiles_bench", () -> new TiledStoveBlockBench(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Meadow.identifier("stove_tiles_bench")))));
    public static final RegistrySupplier<Block> ARTISAN_GLASS_WINDOW = registerWithItem("artisan_glass_window", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Meadow.identifier("artisan_glass_window")))));
    public static final RegistrySupplier<Block> ARTISAN_GLASS_WINDOW_PANE = registerWithItem("artisan_glass_window_pane", () -> new WindowBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).setId(blockKey(Meadow.identifier("artisan_glass_window_pane")))));
    public static final RegistrySupplier<Block> ORNATE_GLASS_WINDOW = registerWithItem("ornate_glass_window", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Meadow.identifier("ornate_glass_window")))));
    public static final RegistrySupplier<Block> ORNATE_GLASS_WINDOW_PANE = registerWithItem("ornate_glass_window_pane", () -> new WindowBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS_PANE).setId(blockKey(Meadow.identifier("ornate_glass_window_pane")))));
    public static final RegistrySupplier<Block> RUSTIC_WOOL = registerWithItem("rustic_wool", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("rustic_wool")))));
    public static final RegistrySupplier<Block> RUSTIC_CARPET = registerWithItem("rustic_carpet", () -> new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("rustic_carpet")))));
    public static final RegistrySupplier<Block> RUSTIC_BED = registerWithItem("rustic_bed", () -> new MeadowBedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(blockKey(Meadow.identifier("rustic_bed"))).sound(SoundType.WOOD).strength(0.2F).noOcclusion()));
    public static final RegistrySupplier<Block> LINEN = registerWithItem("linen", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("linen")))));
    public static final RegistrySupplier<Block> LINEN_CARPET = registerWithItem("linen_carpet", () -> new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("linen_carpet")))));
    public static final RegistrySupplier<Block> LINEN_BED = registerWithItem("linen_bed", () -> new MeadowBedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(blockKey(Meadow.identifier("linen_bed"))).sound(SoundType.WOOD).strength(0.2F).noOcclusion()));
    public static final RegistrySupplier<Block> JACQUARD_WOOL = registerWithItem("jacquard_wool", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("jacquard_wool")))));
    public static final RegistrySupplier<Block> JACQUARD_CARPET = registerWithItem("jacquard_carpet", () -> new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("jacquard_carpet")))));
    public static final RegistrySupplier<Block> JACQUARD_BED = registerWithItem("jacquard_bed", () -> new MeadowBedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(blockKey(Meadow.identifier("jacquard_bed"))).sound(SoundType.WOOD).strength(0.2F).noOcclusion()));
    public static final RegistrySupplier<Block> PLAID_WOOL = registerWithItem("plaid_wool", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("plaid_wool")))));
    public static final RegistrySupplier<Block> PLAID_CARPET = registerWithItem("plaid_carpet", () -> new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("plaid_carpet")))));
    public static final RegistrySupplier<Block> PLAID_BED = registerWithItem("plaid_bed", () -> new MeadowBedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(blockKey(Meadow.identifier("plaid_bed"))).sound(SoundType.WOOD).strength(0.2F).noOcclusion()));
    public static final RegistrySupplier<Block> CHAMBRAY_WOOL = registerWithItem("chambray_wool", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("chambray_wool")))));
    public static final RegistrySupplier<Block> CHAMBRAY_CARPET = registerWithItem("chambray_carpet", () -> new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("chambray_carpet")))));
    public static final RegistrySupplier<Block> CHAMBRAY_BED = registerWithItem("chambray_bed", () -> new MeadowBedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(blockKey(Meadow.identifier("chambray_bed"))).sound(SoundType.WOOD).strength(0.2F).noOcclusion()));
    public static final RegistrySupplier<Block> TWEED_WOOL = registerWithItem("tweed_wool", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("tweed_wool")))));
    public static final RegistrySupplier<Block> TWEED_CARPET = registerWithItem("tweed_carpet", () -> new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("tweed_carpet")))));
    public static final RegistrySupplier<Block> TWEED_BED = registerWithItem("tweed_bed", () -> new MeadowBedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(blockKey(Meadow.identifier("tweed_bed"))).sound(SoundType.WOOD).strength(0.2F).noOcclusion()));
    public static final RegistrySupplier<Block> WARPED_WOOL = registerWithItem("warped_wool", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("warped_wool")))));
    public static final RegistrySupplier<Block> WARPED_CARPET = registerWithItem("warped_carpet", () -> new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("warped_carpet")))));
    public static final RegistrySupplier<Block> WARPED_BED = registerWithItem("warped_bed", () -> new MeadowBedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(blockKey(Meadow.identifier("warped_bed"))).sound(SoundType.WOOD).strength(0.2F).noOcclusion()));
    public static final RegistrySupplier<Block> STRAW_BED = registerWithItem("straw_bed", () -> new MeadowBedBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.WHITE)).setId(blockKey(Meadow.identifier("straw_bed"))).sound(SoundType.WOOD).strength(0.2F).noOcclusion()));
    public static final RegistrySupplier<Block> CHEESE_FORM = registerWithItem("cheese_form", () -> new CheeseFormBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("cheese_form"))).noOcclusion().strength(0.1f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> FONDUE_POT = registerWithItem("fondue_pot", () -> new FonduePotBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("fondue_pot"))).requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.METAL)));
    public static final RegistrySupplier<Block> COOKING_CAULDRON = registerWithItem("cookpot", () -> new CookingCauldronBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("cookpot"))).requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.METAL)));
    public static final RegistrySupplier<Block> COOKING_FRAME = registerWithItem("cooking_frame", () -> new CookingFrameBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("cooking_frame"))).requiresCorrectToolForDrops().lightLevel((blockState) -> 13).strength(3.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> WOODCUTTER = registerWithItem("woodcutter", () -> new WoodcutterBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("woodcutter"))).requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> WOODEN_CAULDRON = registerWithItem("wooden_cauldron", () -> new WoodenCauldronBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("wooden_cauldron"))).requiresCorrectToolForDrops().strength(2.0f).noOcclusion().sound(SoundType.WOOD).mapColor(MapColor.PODZOL)));
    public static final RegistrySupplier<Block> WHEELBARROW = registerWithItem("wheelbarrow", () -> new WheelBarrowBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("wheelbarrow"))).requiresCorrectToolForDrops().strength(3.5F).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> WOODEN_WATER_CAULDRON = registerWithoutItem("wooden_water_cauldron", () -> new WoodenWaterCauldronBlock(Biome.Precipitation.RAIN, BlockBehaviour.Properties.ofFullCopy(ObjectRegistry.WOODEN_CAULDRON.get()).setId(blockKey(Meadow.identifier("wooden_water_cauldron")))));
    public static final RegistrySupplier<Block> WOODEN_POWDER_SNOW_CAULDRON = registerWithoutItem("wooden_powder_snow_cauldron", () -> new WoodenPowderSnowCauldronBlock(Biome.Precipitation.SNOW, CauldronInteractions.POWDER_SNOW, BlockBehaviour.Properties.ofFullCopy(ObjectRegistry.WOODEN_CAULDRON.get()).setId(blockKey(Meadow.identifier("wooden_powder_snow_cauldron")))));
    public static final RegistrySupplier<Block> FIREWOOD = registerWithItem("firewood", () -> new FirewoodBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("firewood"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MILK_CAN = registerWithItem("milk_can", () -> new MilkCanBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("milk_can"))).requiresCorrectToolForDrops().strength(0.8f).noOcclusion().sound(SoundType.METAL)));
    public static final RegistrySupplier<Block> WATERING_CAN = registerWithoutItem("watering_can", () -> new WateringCanBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Meadow.identifier("watering_can")))));
    public static final RegistrySupplier<Item> WATERING_CAN_ITEM = registerItem("watering_can", () -> new WateringCanItem(ObjectRegistry.WATERING_CAN.get(), getSettings().durability(5)));
    public static final RegistrySupplier<Block> CLIMBING_ROPE = registerWithItem("climbing_rope", () -> new ClimbingRopeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("climbing_rope"))).noCollision()));
    public static final RegistrySupplier<Block> OIL_LANTERN = registerWithItem("oil_lantern", () -> new OilLantern(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("oil_lantern"))).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.LANTERN).lightLevel((blockState) -> 14).noOcclusion()));
    public static final RegistrySupplier<Block> CAMERA = registerWithItem("camera", () -> new CameraBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("camera")))));
    public static final RegistrySupplier<Block> DOORMAT = registerWithItem("doormat", () -> new DoormatBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Meadow.identifier("doormat")))));
    public static final RegistrySupplier<Block> WOODEN_FLOWER_POT_BIG = registerWithItem("wooden_flower_pot_big", () -> new FlowerPotBigBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("wooden_flower_pot_big"))).instabreak()));
    public static final RegistrySupplier<Block> WOODEN_FLOWER_POT_SMALL = registerWithItem("wooden_flower_pot_small", () -> new FlowerPotSmallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("wooden_flower_pot_small"))).instabreak()));
    public static final RegistrySupplier<Block> WOODEN_FLOWER_BOX = registerWithItem("wooden_flower_box", () -> new FlowerBoxBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Meadow.identifier("wooden_flower_box"))).instabreak()));
    public static final RegistrySupplier<Item> FUR_HELMET = registerItem("fur_helmet", () -> new FurHelmetItem(ArmorMaterialRegistry.FUR_ARMOR, ArmorType.HELMET, getSettings().rarity(Rarity.EPIC).durability(ArmorType.HELMET.getDurability(15)).stacksTo(1), Meadow.identifier("textures/models/armor/fur.png")));
    public static final RegistrySupplier<Item> FUR_CHESTPLATE = registerItem("fur_chestplate", () -> new FurChestItem(ArmorMaterialRegistry.FUR_ARMOR, ArmorType.CHESTPLATE, getSettings().rarity(Rarity.RARE).durability(ArmorType.CHESTPLATE.getDurability(15)).stacksTo(1), Meadow.identifier("textures/models/armor/fur.png")));
    public static final RegistrySupplier<Item> FUR_LEGGINGS = registerItem("fur_leggings", () -> new FurLegsItem(ArmorMaterialRegistry.FUR_ARMOR, ArmorType.LEGGINGS, getSettings().rarity(Rarity.RARE).durability(ArmorType.LEGGINGS.getDurability(15)).stacksTo(1), Meadow.identifier("textures/models/armor/fur.png")));
    public static final RegistrySupplier<Item> FUR_BOOTS = registerItem("fur_boots", () -> new FurBootsItem(ArmorMaterialRegistry.FUR_ARMOR, ArmorType.BOOTS, getSettings().rarity(Rarity.RARE).durability(ArmorType.BOOTS.getDurability(15)).stacksTo(1), Meadow.identifier("textures/models/armor/fur.png")));
    public static final RegistrySupplier<Block> SMALL_FIR = registerWithItem("small_fir", () -> new DoublePlantBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ROSE_BUSH).setId(blockKey(Meadow.identifier("small_fir")))));
    public static final RegistrySupplier<Block> ALPINE_POPPY = registerWithItem("alpine_poppy", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(Meadow.identifier("alpine_poppy")))));
    public static final RegistrySupplier<Block> DELPHINIUM = registerWithItem("delphinium", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(Meadow.identifier("delphinium")))));
    public static final RegistrySupplier<Block> SAXIFRAGE = registerWithItem("saxifrage", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(Meadow.identifier("saxifrage")))));
    public static final RegistrySupplier<Block> ENZIAN = registerWithItem("enzian", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(Meadow.identifier("enzian")))));
    public static final RegistrySupplier<Block> FIRE_LILY = registerWithItem("fire_lily", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(Meadow.identifier("fire_lily")))));
    public static final RegistrySupplier<Block> ERIOPHORUM = registerWithItem("eriophorum", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(Meadow.identifier("eriophorum")))));
    public static final RegistrySupplier<Block> TALL_ERIOPHORUM = registerWithItem("tall_eriophorum", () -> new TallFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ROSE_BUSH).setId(blockKey(Meadow.identifier("tall_eriophorum")))));
    public static final RegistrySupplier<Item> CHEESECAKE_SLICE = registerItem("cheesecake_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> CHEESE_TART_SLICE = registerItem("cheese_tart_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> CHEESE_SLICE = registerItem("cheese_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> SHEEP_CHEESE_SLICE = registerItem("sheep_cheese_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> GRAIN_CHEESE_SLICE = registerItem("grain_cheese_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> AMETHYST_CHEESE_SLICE = registerItem("amethyst_cheese_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> BUFFALO_CHEESE_SLICE = registerItem("buffalo_cheese_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> GOAT_CHEESE_SLICE = registerItem("goat_cheese_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> WARPED_CHEESE_SLICE = registerItem("warped_cheese_slice", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build(), net.minecraft.world.item.component.Consumable.builder().onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 300, 1), 1.0F)).build())));
    public static final RegistrySupplier<Block> CHEESECAKE = registerWithItem("cheesecake", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("cheesecake"))), ObjectRegistry.CHEESECAKE_SLICE, CheeseWheelBlock.CheeseType.CHEESECAKE));
    public static final RegistrySupplier<Block> CHEESE_TART = registerWithItem("cheese_tart", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("cheese_tart"))), ObjectRegistry.CHEESE_TART_SLICE, CheeseWheelBlock.CheeseType.CHEESE_TART));
    public static final RegistrySupplier<Block> CHEESE_WHEEL = registerWithItem("cheese_wheel", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("cheese_wheel"))), ObjectRegistry.CHEESE_SLICE, CheeseWheelBlock.CheeseType.REGULAR));
    public static final RegistrySupplier<Block> AMETHYST_CHEESE_WHEEL = registerWithItem("amethyst_cheese_wheel", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("amethyst_cheese_wheel"))), ObjectRegistry.AMETHYST_CHEESE_SLICE, CheeseWheelBlock.CheeseType.REGULAR));
    public static final RegistrySupplier<Block> BUFFALO_CHEESE_WHEEL = registerWithItem("buffalo_cheese_wheel", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("buffalo_cheese_wheel"))), ObjectRegistry.BUFFALO_CHEESE_SLICE, CheeseWheelBlock.CheeseType.BUFFALO));
    public static final RegistrySupplier<Block> WARPED_CHEESE_WHEEL = registerWithItem("warped_cheese_wheel", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("warped_cheese_wheel"))), ObjectRegistry.WARPED_CHEESE_SLICE, CheeseWheelBlock.CheeseType.WARPED));
    public static final RegistrySupplier<Block> GRAIN_CHEESE_WHEEL = registerWithItem("grain_cheese_wheel", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("grain_cheese_wheel"))), ObjectRegistry.GRAIN_CHEESE_SLICE, CheeseWheelBlock.CheeseType.GRAIN));
    public static final RegistrySupplier<Block> SHEEP_CHEESE_WHEEL = registerWithItem("sheep_cheese_wheel", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("sheep_cheese_wheel"))), ObjectRegistry.SHEEP_CHEESE_SLICE, CheeseWheelBlock.CheeseType.SHEEP));
    public static final RegistrySupplier<Block> GOAT_CHEESE_WHEEL = registerWithItem("goat_cheese_wheel", () -> new CheeseWheelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Meadow.identifier("goat_cheese_wheel"))), ObjectRegistry.GOAT_CHEESE_SLICE, CheeseWheelBlock.CheeseType.GOAT));
    public static final RegistrySupplier<Item> ALPINE_SALT = registerItem("alpine_salt", () -> new Item(getSettings()));
    public static final RegistrySupplier<Item> RENNET = registerItem("rennet", () -> new Item(getSettings().craftRemainder(Items.GLASS_BOTTLE)));
    public static final RegistrySupplier<Item> CHEESE_SANDWICH = registerItem("cheese_sandwich", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.7f).build())));
    public static final RegistrySupplier<Item> CHEESE_ROLL = registerItem("cheese_roll", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(7).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Item> CHEESE_STICK = registerItem("cheese_stick", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.6f).build())));
    public static final RegistrySupplier<Item> RAW_BUFFALO_MEAT = registerItem("raw_buffalo_meat", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build())));
    public static final RegistrySupplier<Item> COOKED_BUFFALO_MEAT = registerItem("cooked_buffalo_meat", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.8f).build())));
    public static final RegistrySupplier<Block> ROASTED_BUFFALO_HAM = registerWithItem("roasted_buffalo_ham", () -> new FoodBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("roasted_buffalo_ham"))), new MobEffectInstance(MobEffects.REGENERATION, 600, 1), 8, 0.9f));
    public static final RegistrySupplier<Item> SAUSAGE_WITH_CHEESE = registerItem("sausage_with_cheese", () -> new Item(getSettings().food(new FoodProperties.Builder().nutrition(8).saturationModifier(0.9f).build())));
    public static final RegistrySupplier<Item> WOODEN_BUCKET = registerItem("wooden_bucket", () -> new WoodenBucket(Fluids.EMPTY, getSettings().stacksTo(16)));
    public static final RegistrySupplier<Item> WOODEN_WATER_BUCKET = registerItem("wooden_water_bucket", () -> new WoodenBucket(Fluids.WATER, getSettings().stacksTo(1).craftRemainder(ObjectRegistry.WOODEN_BUCKET.get())));
    public static final RegistrySupplier<Item> WOODEN_MILK_BUCKET = registerItem("wooden_milk_bucket", () -> new WoodenMilkBucket(getSettings().stacksTo(1).craftRemainder(ObjectRegistry.WOODEN_BUCKET.get()).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)));
    public static final RegistrySupplier<Item> WOODEN_SHEEP_MILK_BUCKET = registerItem("wooden_sheep_milk_bucket", () -> new WoodenMilkBucket(getSettings().stacksTo(1).craftRemainder(ObjectRegistry.WOODEN_BUCKET.get()).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)));
    public static final RegistrySupplier<Item> WOODEN_BUFFALO_MILK_BUCKET = registerItem("wooden_buffalo_milk_bucket", () -> new WoodenMilkBucket(getSettings().stacksTo(1).craftRemainder(ObjectRegistry.WOODEN_BUCKET.get()).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)));
    public static final RegistrySupplier<Item> WOODEN_GOAT_MILK_BUCKET = registerItem("wooden_goat_milk_bucket", () -> new WoodenMilkBucket(getSettings().stacksTo(1).craftRemainder(ObjectRegistry.WOODEN_BUCKET.get()).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)));
    public static final RegistrySupplier<Item> WOODEN_WARPED_MILK_BUCKET = registerItem("wooden_warped_milk_bucket", () -> new WoodenMilkBucket(getSettings().stacksTo(1).craftRemainder(ObjectRegistry.WOODEN_BUCKET.get()).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)));
    public static final RegistrySupplier<Item> WOODEN_GRAIN_MILK_BUCKET = registerItem("wooden_grain_milk_bucket", () -> new WoodenMilkBucket(getSettings().stacksTo(1).craftRemainder(ObjectRegistry.WOODEN_BUCKET.get()).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)));
    public static final RegistrySupplier<Item> WOODEN_AMETHYST_MILK_BUCKET = registerItem("wooden_amethyst_milk_bucket", () -> new WoodenMilkBucket(getSettings().stacksTo(1).craftRemainder(ObjectRegistry.WOODEN_BUCKET.get()).component(DataComponents.CONSUMABLE, Consumables.MILK_BUCKET)));
    public static final RegistrySupplier<Item> WATER_BUFFALO_SPAWN_EGG_ITEM = registerItem("water_buffalo_spawn_egg", () -> new SpawnEggItem(getSettings().component(DataComponents.ENTITY_DATA, TypedEntityData.of(EntityTypeRegistry.WATER_BUFFALO.get(), new CompoundTag()))));
    public static final RegistrySupplier<Item> HIGHLAND_WOOLY_COW_SPAWN_EGG = registerItem("highland_wooly_cow_spawn_egg", () -> new CattleSpawnEggItem(EntityTypeRegistry.WOOLY_COW, getSettings(), 0));
    public static final RegistrySupplier<Item> UMBRA_WOOLY_COW_SPAWN_EGG = registerItem("umbra_wooly_cow_spawn_egg", () -> new CattleSpawnEggItem(EntityTypeRegistry.WOOLY_COW, getSettings(), 1));
    public static final RegistrySupplier<Item> WARPED_WOOLY_COW_SPAWN_EGG = registerItem("warped_wooly_cow_spawn_egg", () -> new CattleSpawnEggItem(EntityTypeRegistry.WOOLY_COW, getSettings(), 2));
    public static final RegistrySupplier<Block> POTTED_ENZIAN = registerWithoutItem("potted_enzian", () -> new FlowerPotBlock(ObjectRegistry.ENZIAN.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_enzian")))));
    public static final RegistrySupplier<Block> POTTED_FIRE_LILY = registerWithoutItem("potted_fire_lily", () -> new FlowerPotBlock(ObjectRegistry.FIRE_LILY.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_fire_lily")))));
    public static final RegistrySupplier<Block> POTTED_ALPINE_POPPY = registerWithoutItem("potted_alpine_poppy", () -> new FlowerPotBlock(ObjectRegistry.ALPINE_POPPY.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_alpine_poppy")))));
    public static final RegistrySupplier<Block> POTTED_SAXIFRAGE = registerWithoutItem("potted_saxifrage", () -> new FlowerPotBlock(ObjectRegistry.SAXIFRAGE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_saxifrage")))));
    public static final RegistrySupplier<Block> POTTED_DELPHINIUM = registerWithoutItem("potted_delphinium", () -> new FlowerPotBlock(ObjectRegistry.DELPHINIUM.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_delphinium")))));
    public static final RegistrySupplier<Block> POTTED_ERIOPHORUM = registerWithoutItem("potted_eriophorum", () -> new FlowerPotBlock(ObjectRegistry.ERIOPHORUM.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_eriophorum")))));
    public static final RegistrySupplier<Block> POTTED_PINE_SAPLING = registerWithoutItem("potted_pine_sapling", () -> new FlowerPotBlock(ObjectRegistry.PINE_SAPLING.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_pine_sapling")))));
    public static final RegistrySupplier<Block> POTTED_YELLOW_PINE_SAPLING = registerWithoutItem("potted_yellow_pine_sapling", () -> new FlowerPotBlock(ObjectRegistry.YELLOW_PINE_SAPLING.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_yellow_pine_sapling")))));
    public static final RegistrySupplier<Block> POTTED_ALPINE_BIRCH_SAPLING = registerWithoutItem("potted_alpine_birch_sapling", () -> new FlowerPotBlock(ObjectRegistry.ALPINE_BIRCH_SAPLING.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Meadow.identifier("potted_alpine_birch_sapling")))));
    public static final RegistrySupplier<Item> PINE_BOAT = registerItem("pine_boat", () -> new PineBoatItem(false, PineBoatEntity.Type.PINE, getSettings()));
    public static final RegistrySupplier<Item> PINE_CHEST_BOAT = registerItem("pine_chest_boat", () -> new PineBoatItem(true, PineBoatEntity.Type.PINE, getSettings()));
    public static final RegistrySupplier<Block> MEADOW_BANNER = registerWithoutItem("meadow_banner", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("meadow_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Item> MEADOW_BANNER_ITEM = registerItem("meadow_banner", () -> new CompletionistBannerItem(MEADOW_BANNER.get(), getSettings()));
    public static final RegistrySupplier<Block> MEADOW_WALL_BANNER = registerWithoutItem("meadow_wall_banner", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(Meadow.identifier("meadow_wall_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Item> FELTING_NEEDLE = registerItem("felting_needle", () -> new FeltingNeedleItem(getSettings().durability(128)));
    public static final RegistrySupplier<Block> PINE_SIGN = registerWithoutItem("pine_sign", () -> new PineStandingSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN).setId(blockKey(Meadow.identifier("pine_sign"))), MeadowWoodType.PINE));
    public static final RegistrySupplier<Block> PINE_WALL_SIGN = registerWithoutItem("pine_wall_sign", () -> new PineWallSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN).setId(blockKey(Meadow.identifier("pine_wall_sign"))), MeadowWoodType.PINE));
    public static final RegistrySupplier<Block> PINE_HANGING_SIGN = registerWithoutItem("pine_hanging_sign", () -> new PineCeilingHangingSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN).setId(blockKey(Meadow.identifier("pine_hanging_sign"))), MeadowWoodType.PINE));
    public static final RegistrySupplier<Block> PINE_WALL_HANGING_SIGN = registerWithoutItem("pine_wall_hanging_sign", () -> new PineWallHangingSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN).setId(blockKey(Meadow.identifier("pine_wall_hanging_sign"))), MeadowWoodType.PINE));
    public static final RegistrySupplier<Item> PINE_SIGN_ITEM = registerItem("pine_sign", () -> new SignItem(ObjectRegistry.PINE_SIGN.get(), ObjectRegistry.PINE_WALL_SIGN.get(), getSettings().stacksTo(16)));
    public static final RegistrySupplier<Item> PINE_HANGING_SIGN_ITEM = registerItem("pine_hanging_sign", () -> new HangingSignItem(ObjectRegistry.PINE_HANGING_SIGN.get(), ObjectRegistry.PINE_WALL_HANGING_SIGN.get(), getSettings().stacksTo(16)));

    private static RegistrySupplier<Block> registerLog(String path) {
        return registerWithItem(path, () -> new RotatedPillarBlock(getLogBlockSettings(Meadow.identifier(path))));
    }

    private static BlockBehaviour.Properties getLogBlockSettings(Identifier id) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(id)).strength(2.0F).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties getSlabSettings(Identifier id) {
        return getLogBlockSettings(id).explosionResistance(3.0F);
    }

    private static ResourceKey<Item> itemKey(Identifier id) {
        return ResourceKey.create(Registries.ITEM, id);
    }

    private static ResourceKey<Block> blockKey(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    private static Identifier currentId;

    private static Item.Properties getSettings(Consumer<Item.Properties> consumer) {
        Item.Properties settings = new Item.Properties();
        if (currentId != null) {
            settings.setId(itemKey(currentId));
        }
        consumer.accept(settings);
        return settings;
    }

    public static Item.Properties getSettings() {
        return getSettings(settings -> {
        });
    }

    public static void init() {
        ITEMS.register();
        BLOCKS.register();
    }

    public static void commonInit() {
        FuelRegistry.register(300, PINE_FENCE.get(), PINE_FENCE_GATE.get(), PINE_PLANKS.get(), PINE_LOG.get(), PINE_WOOD.get(), STRIPPED_PINE_LOG.get(), STRIPPED_PINE_WOOD.get());
        FuelRegistry.register(2800, FIREWOOD.get());
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> configuredFeatureKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath(Meadow.MOD_ID, name));
    }

    private static ButtonBlock woodenButton(Identifier id, FeatureFlag... featureFlags) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of().setId(blockKey(id)).noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY);
        if (featureFlags.length > 0) {
            properties = properties.requiredFeatures(featureFlags);
        }

        return new ButtonBlock(BlockSetType.OAK, 30, properties);
    }

    public static <T extends Block> RegistrySupplier<T> registerWithItem(String name, Supplier<T> block) {
        return GeneralUtil.registerWithItem(BLOCKS, BLOCK_REGISTRAR, ITEMS, ITEM_REGISTRAR, Meadow.identifier(name), block);
    }

    public static <T extends Block> RegistrySupplier<T> registerWithoutItem(String path, Supplier<T> block) {
        return GeneralUtil.registerWithoutItem(BLOCKS, BLOCK_REGISTRAR, Meadow.identifier(path), block);
    }

    public static <T extends Item> RegistrySupplier<T> registerItem(String path, Supplier<T> itemSupplier) {
        Identifier id = Meadow.identifier(path);
        currentId = id;
        try {
            return GeneralUtil.registerItem(ITEMS, ITEM_REGISTRAR, id, itemSupplier);
        } finally {
            currentId = null;
        }
    }
}
