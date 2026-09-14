package earth.terrarium.chipped.common.registry;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.resourcefullib.common.lib.Constants;
import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import com.teamresourceful.resourcefullib.common.registry.builtin.ResourcefulBlockRegistry;
import earth.terrarium.chipped.Chipped;
import earth.terrarium.chipped.common.blocks.DirectionBlock;
import earth.terrarium.chipped.common.blocks.SpecialLanternBlock;
import earth.terrarium.chipped.common.blocks.SpecialPointedDripstoneBlock;
import earth.terrarium.chipped.common.blocks.WorkbenchBlock;
import earth.terrarium.chipped.common.palette.IdType;
import earth.terrarium.chipped.common.palette.Palette;
import earth.terrarium.chipped.common.palette.Palettes;
import earth.terrarium.chipped.common.registry.base.ChippedPaletteRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PlaceOnWaterBlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;

import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ModBlocks {

    public static final Function<BlockBehaviour.Properties, Block> DEFAULT_CREATOR = Block::new;

    public static final ResourcefulRegistry<Block> BLOCKS = ResourcefulRegistries.create(BuiltInRegistries.BLOCK, Chipped.MOD_ID);
    public static final ResourcefulBlockRegistry BENCHES = ResourcefulRegistries.createForBlocks(BLOCKS);

    public static final TagKey<Block> SOUL_SAND_TAG = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Chipped.MOD_ID, "soul_sand"));

    public static final RegistryEntry<Block> BOTANIST_WORKBENCH = BENCHES.register("botanist_workbench", WorkbenchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final RegistryEntry<Block> GLASSBLOWER = BENCHES.register("glassblower", WorkbenchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final RegistryEntry<Block> CARPENTERS_TABLE = BENCHES.register("carpenters_table", WorkbenchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final RegistryEntry<Block> LOOM_TABLE = BENCHES.register("loom_table", WorkbenchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final RegistryEntry<Block> MASON_TABLE = BENCHES.register("mason_table", WorkbenchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final RegistryEntry<Block> ALCHEMY_BENCH = BENCHES.register("alchemy_bench", WorkbenchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());
    public static final RegistryEntry<Block> TINKERING_TABLE = BENCHES.register("tinkering_table", WorkbenchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion());

    public static final ChippedPaletteRegistry AMETHYST_BLOCK = createRegistry(Blocks.AMETHYST_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry COAL_BLOCK = createRegistry(Blocks.COAL_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry CRYING_OBSIDIAN = createRegistry(Blocks.CRYING_OBSIDIAN, Palettes.STONE);
    public static final ChippedPaletteRegistry LODESTONE = createRegistry(Blocks.LODESTONE, Palettes.LODESTONE);
    public static final ChippedPaletteRegistry LAPIS_BLOCK = createRegistry(Blocks.LAPIS_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry DIAMOND_BLOCK = createRegistry(Blocks.DIAMOND_BLOCK, Palettes.DIAMOND_BLOCK);
    public static final ChippedPaletteRegistry EMERALD_BLOCK = createRegistry(Blocks.EMERALD_BLOCK, Palettes.EMERALD_BLOCK);
    public static final ChippedPaletteRegistry GOLD_BLOCK = createRegistry(Blocks.GOLD_BLOCK, Palettes.DIAMOND_BLOCK);
    public static final ChippedPaletteRegistry IRON_BLOCK = createRegistry(Blocks.IRON_BLOCK, Palettes.METAL);
    public static final ChippedPaletteRegistry NETHERITE_BLOCK = createRegistry(Blocks.NETHERITE_BLOCK, Palettes.NETHERITE_BLOCK);
    public static final ChippedPaletteRegistry SPONGE = createRegistry(Blocks.SPONGE, Palettes.SPONGE, SpongeBlock::new);
    public static final ChippedPaletteRegistry GLOWSTONE = createRegistry(Blocks.GLOWSTONE, Palettes.GLOWSTONE);

    public static final ChippedPaletteRegistry BLUE_ICE = createRegistry(Blocks.BLUE_ICE, Palettes.STONE);
    public static final ChippedPaletteRegistry DIRT = createRegistry(Blocks.DIRT, Palettes.STONE);
    public static final ChippedPaletteRegistry ICE = createRegistry(Blocks.ICE, Palettes.STONE);
    public static final ChippedPaletteRegistry PACKED_ICE = createRegistry(Blocks.PACKED_ICE, Palettes.STONE);
    public static final ChippedPaletteRegistry SNOW_BLOCK = createRegistry(Blocks.SNOW_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry CLAY = createRegistry(Blocks.CLAY, Palettes.CLAY);
    public static final ChippedPaletteRegistry MUD = createRegistry(Blocks.MUD, Palettes.MUD);
    public static final ChippedPaletteRegistry PACKED_MUD = createRegistry(Blocks.PACKED_MUD, Palettes.MUD);
    public static final ChippedPaletteRegistry ACACIA_LEAVES = createRegistry(Blocks.ACACIA_LEAVES, Palettes.LEAVES, p -> new TintedParticleLeavesBlock(0, p));
    public static final ChippedPaletteRegistry BIRCH_LEAVES = createRegistry(Blocks.BIRCH_LEAVES, Palettes.LEAVES, p -> new TintedParticleLeavesBlock(0, p));
    public static final ChippedPaletteRegistry DARK_OAK_LEAVES = createRegistry(Blocks.DARK_OAK_LEAVES, Palettes.LEAVES, p -> new TintedParticleLeavesBlock(0, p));
    public static final ChippedPaletteRegistry JUNGLE_LEAVES = createRegistry(Blocks.JUNGLE_LEAVES, Palettes.LEAVES, p -> new TintedParticleLeavesBlock(0, p));
    public static final ChippedPaletteRegistry MANGROVE_ROOTS = createRegistry(Blocks.MANGROVE_ROOTS, Palettes.ROOTS, MangroveRootsBlock::new);
    public static final ChippedPaletteRegistry OAK_LEAVES = createRegistry(Blocks.OAK_LEAVES, Palettes.LEAVES, p -> new TintedParticleLeavesBlock(0, p));
    public static final ChippedPaletteRegistry SPRUCE_LEAVES = createRegistry(Blocks.SPRUCE_LEAVES, Palettes.LEAVES, p -> new TintedParticleLeavesBlock(0, p));
    public static final ChippedPaletteRegistry OCHRE_FROGLIGHT = createRegistry(Blocks.OCHRE_FROGLIGHT, Palettes.FROGLIGHT);
    public static final ChippedPaletteRegistry PEARLESCENT_FROGLIGHT = createRegistry(Blocks.PEARLESCENT_FROGLIGHT, Palettes.FROGLIGHT);
    public static final ChippedPaletteRegistry VERDANT_FROGLIGHT = createRegistry(Blocks.VERDANT_FROGLIGHT, Palettes.FROGLIGHT);
    public static final ChippedPaletteRegistry BONE_BLOCK = createRegistry(Blocks.BONE_BLOCK, Palettes.BONE_BLOCK, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry BROWN_MUSHROOM_BLOCK = createRegistry(Blocks.BROWN_MUSHROOM_BLOCK, Palettes.MUSHROOM_BLOCK);
    public static final ChippedPaletteRegistry RED_MUSHROOM_BLOCK = createRegistry(Blocks.RED_MUSHROOM_BLOCK, Palettes.RED_MUSHROOM_BLOCK);
    public static final ChippedPaletteRegistry BROWN_MUSHROOM = createRegistry(Blocks.BROWN_MUSHROOM, Palettes.MUSHROOM, b -> new MushroomBlock(TreeFeatures.HUGE_BROWN_MUSHROOM, b));
    public static final ChippedPaletteRegistry RED_MUSHROOM = createRegistry(Blocks.RED_MUSHROOM, Palettes.MUSHROOM, b -> new MushroomBlock(TreeFeatures.HUGE_RED_MUSHROOM, b));
    public static final ChippedPaletteRegistry COBWEB = createRegistry(Blocks.COBWEB, Palettes.COBWEB, WebBlock::new);
    public static final ChippedPaletteRegistry MUSHROOM_STEM = createRegistry(Blocks.MUSHROOM_STEM, Palettes.MUSHROOM_STEM, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry GRAVEL = createRegistry(Blocks.GRAVEL, Palettes.GRAVEL, p -> new ColoredFallingBlock(new ColorRGBA(0xff807c7b), p));
    public static final ChippedPaletteRegistry HAY_BLOCK = createRegistry(Blocks.HAY_BLOCK, Palettes.HAY_BLOCK, HayBlock::new);
    public static final ChippedPaletteRegistry MOSS_BLOCK = createRegistry(Blocks.MOSS_BLOCK, Palettes.MOSS_BLOCK, props -> new BonemealableFeaturePlacerBlock(CaveFeatures.MOSS_PATCH_BONEMEAL, props));
    public static final ChippedPaletteRegistry MELON = createRegistry(Blocks.MELON, Palettes.MELON);
    public static final ChippedPaletteRegistry SHROOMLIGHT = createRegistry(Blocks.SHROOMLIGHT, Palettes.SHROOMLIGHT);
    public static final ChippedPaletteRegistry SAND = createRegistry(Blocks.SAND, Palettes.SAND, p -> new ColoredFallingBlock(new ColorRGBA(0xdbd3a0), p));
    public static final ChippedPaletteRegistry SOUL_SAND = createRegistry(Blocks.SOUL_SAND, Palettes.SOUL_SAND, SoulSandBlock::new);
    public static final ChippedPaletteRegistry CRIMSON_ROOTS = createRegistry(Blocks.CRIMSON_ROOTS, Palettes.CRIMSON_ROOTS, p -> new Block(p) {});
    public static final ChippedPaletteRegistry WARPED_ROOTS = createRegistry(Blocks.WARPED_ROOTS, Palettes.WARPED_ROOTS, p -> new Block(p) {});
    public static final ChippedPaletteRegistry DRIED_KELP_BLOCK = createRegistry(Blocks.DRIED_KELP_BLOCK, Palettes.DRIED_KELP_BLOCK);
    public static final ChippedPaletteRegistry LILY_PAD = createRegistry(Blocks.LILY_PAD, Palettes.LILY_PAD, p -> new Block(p) {}, PlaceOnWaterBlockItem::new);
    public static final ChippedPaletteRegistry NETHER_SPROUTS = createRegistry(Blocks.NETHER_SPROUTS, Palettes.NETHER_SPROUTS);
    public static final ChippedPaletteRegistry NETHER_WART_BLOCK = createRegistry(Blocks.NETHER_WART_BLOCK, Palettes.WART_BLOCK);
    public static final ChippedPaletteRegistry PUMPKIN = createRegistry(Blocks.PUMPKIN, Palettes.PUMPKIN, PumpkinBlock::new);
    public static final ChippedPaletteRegistry CARVED_PUMPKIN = createRegistry(Blocks.CARVED_PUMPKIN, Palettes.CARVED_PUMPKIN, CarvedPumpkinBlock::new);
    public static final ChippedPaletteRegistry JACK_O_LANTERN = createRegistry(Blocks.JACK_O_LANTERN, Palettes.CARVED_PUMPKIN, CarvedPumpkinBlock::new);
    public static final ChippedPaletteRegistry VINE = createRegistry(Blocks.VINE, Palettes.VINE, VineBlock::new);
    public static final ChippedPaletteRegistry WARPED_WART_BLOCK = createRegistry(Blocks.WARPED_WART_BLOCK, Palettes.WART_BLOCK);
    public static final ChippedPaletteRegistry CRIMSON_FUNGUS = createRegistry(Blocks.CRIMSON_FUNGUS, Palettes.MUSHROOM);
    public static final ChippedPaletteRegistry WARPED_FUNGUS = createRegistry(Blocks.WARPED_FUNGUS, Palettes.WARPED_FUNGUS);

    public static final ChippedPaletteRegistry ACACIA_PLANKS = createRegistry(Blocks.ACACIA_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry BIRCH_PLANKS = createRegistry(Blocks.BIRCH_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry DARK_OAK_PLANKS = createRegistry(Blocks.DARK_OAK_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry JUNGLE_PLANKS = createRegistry(Blocks.JUNGLE_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry MANGROVE_PLANKS = createRegistry(Blocks.MANGROVE_PLANKS, Palettes.MANGROVE_PLANKS);
    public static final ChippedPaletteRegistry OAK_PLANKS = createRegistry(Blocks.OAK_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry SPRUCE_PLANKS = createRegistry(Blocks.SPRUCE_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry CRIMSON_PLANKS = createRegistry(Blocks.CRIMSON_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry WARPED_PLANKS = createRegistry(Blocks.WARPED_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry CHERRY_PLANKS = createRegistry(Blocks.CHERRY_PLANKS, Palettes.PLANKS);
    public static final ChippedPaletteRegistry BAMBOO_PLANKS = createRegistry(Blocks.BAMBOO_PLANKS, Palettes.BAMBOO_PLANKS);
    public static final ChippedPaletteRegistry ACACIA_LOG = createRegistry(Blocks.ACACIA_LOG, Palettes.LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry BIRCH_LOG = createRegistry(Blocks.BIRCH_LOG, Palettes.LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry DARK_OAK_LOG = createRegistry(Blocks.DARK_OAK_LOG, Palettes.LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry JUNGLE_LOG = createRegistry(Blocks.JUNGLE_LOG, Palettes.LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry MANGROVE_LOG = createRegistry(Blocks.MANGROVE_LOG, Palettes.LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry OAK_LOG = createRegistry(Blocks.OAK_LOG, Palettes.LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry SPRUCE_LOG = createRegistry(Blocks.SPRUCE_LOG, Palettes.LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry CRIMSON_STEM = createRegistry(Blocks.CRIMSON_STEM, Palettes.CRIMSON_STEM, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry WARPED_STEM = createRegistry(Blocks.WARPED_STEM, Palettes.WARPED_STEM, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry CHERRY_LOG = createRegistry(Blocks.CHERRY_LOG, Palettes.LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry BARREL = createRegistry(Blocks.BARREL, Palettes.BARREL, BarrelBlock::new);
    public static final ChippedPaletteRegistry BOOKSHELF = createRegistry(Blocks.BOOKSHELF, Palettes.BOOKSHELF);
    public static final ChippedPaletteRegistry LADDER = createRegistry(Blocks.LADDER, Palettes.LADDER, LadderBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_ACACIA_LOG = createRegistry(Blocks.STRIPPED_ACACIA_LOG, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_BIRCH_LOG = createRegistry(Blocks.STRIPPED_BIRCH_LOG, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_DARK_OAK_LOG = createRegistry(Blocks.STRIPPED_DARK_OAK_LOG, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_JUNGLE_LOG = createRegistry(Blocks.STRIPPED_JUNGLE_LOG, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_MANGROVE_LOG = createRegistry(Blocks.STRIPPED_MANGROVE_LOG, Palettes.STRIPPED_MANGROVE_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_OAK_LOG = createRegistry(Blocks.STRIPPED_OAK_LOG, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_SPRUCE_LOG = createRegistry(Blocks.STRIPPED_SPRUCE_LOG, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_WARPED_STEM = createRegistry(Blocks.STRIPPED_WARPED_STEM, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_CRIMSON_STEM = createRegistry(Blocks.STRIPPED_CRIMSON_STEM, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry STRIPPED_CHERRY_LOG = createRegistry(Blocks.STRIPPED_CHERRY_LOG, Palettes.STRIPPED_LOG, RotatedPillarBlock::new);
    public static final ChippedPaletteRegistry ACACIA_DOOR = createRegistry(Blocks.ACACIA_DOOR, Palettes.ACACIA_DOOR, props -> new DoorBlock(BlockSetType.ACACIA, props));
    public static final ChippedPaletteRegistry BIRCH_DOOR = createRegistry(Blocks.BIRCH_DOOR, Palettes.BIRCH_DOOR, props -> new DoorBlock(BlockSetType.BIRCH, props));
    public static final ChippedPaletteRegistry DARK_OAK_DOOR = createRegistry(Blocks.DARK_OAK_DOOR, Palettes.DARK_OAK_DOOR, props -> new DoorBlock(BlockSetType.DARK_OAK, props));
    public static final ChippedPaletteRegistry JUNGLE_DOOR = createRegistry(Blocks.JUNGLE_DOOR, Palettes.JUNGLE_DOOR, props -> new DoorBlock(BlockSetType.JUNGLE, props));
    public static final ChippedPaletteRegistry MANGROVE_DOOR = createRegistry(Blocks.MANGROVE_DOOR, Palettes.MANGROVE_DOOR, props -> new DoorBlock(BlockSetType.MANGROVE, props));
    public static final ChippedPaletteRegistry OAK_DOOR = createRegistry(Blocks.OAK_DOOR, Palettes.DOOR, props -> new DoorBlock(BlockSetType.OAK, props));
    public static final ChippedPaletteRegistry SPRUCE_DOOR = createRegistry(Blocks.SPRUCE_DOOR, Palettes.SPRUCE_DOOR, props -> new DoorBlock(BlockSetType.SPRUCE, props));
    public static final ChippedPaletteRegistry CRIMSON_DOOR = createRegistry(Blocks.CRIMSON_DOOR, Palettes.CRIMSON_DOOR, props -> new DoorBlock(BlockSetType.CRIMSON, props));
    public static final ChippedPaletteRegistry WARPED_DOOR = createRegistry(Blocks.WARPED_DOOR, Palettes.WARPED_DOOR, props -> new DoorBlock(BlockSetType.WARPED, props));
    public static final ChippedPaletteRegistry CHERRY_DOOR = createRegistry(Blocks.CHERRY_DOOR, Palettes.CHERRY_DOOR, props -> new DoorBlock(BlockSetType.CHERRY, props));
    public static final ChippedPaletteRegistry BAMBOO_DOOR = createRegistry(Blocks.BAMBOO_DOOR, Palettes.BAMBOO_DOOR, props -> new DoorBlock(BlockSetType.BAMBOO, props));
    public static final ChippedPaletteRegistry ACACIA_TRAPDOOR = createRegistry(Blocks.ACACIA_TRAPDOOR, Palettes.ACACIA_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.ACACIA, props));
    public static final ChippedPaletteRegistry BIRCH_TRAPDOOR = createRegistry(Blocks.BIRCH_TRAPDOOR, Palettes.TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.BIRCH, props));
    public static final ChippedPaletteRegistry DARK_OAK_TRAPDOOR = createRegistry(Blocks.DARK_OAK_TRAPDOOR, Palettes.TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.DARK_OAK, props));
    public static final ChippedPaletteRegistry JUNGLE_TRAPDOOR = createRegistry(Blocks.JUNGLE_TRAPDOOR, Palettes.JUNGLE_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.JUNGLE, props));
    public static final ChippedPaletteRegistry MANGROVE_TRAPDOOR = createRegistry(Blocks.MANGROVE_TRAPDOOR, Palettes.MANGROVE_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.MANGROVE, props));
    public static final ChippedPaletteRegistry OAK_TRAPDOOR = createRegistry(Blocks.OAK_TRAPDOOR, Palettes.OAK_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.OAK, props));
    public static final ChippedPaletteRegistry SPRUCE_TRAPDOOR = createRegistry(Blocks.SPRUCE_TRAPDOOR, Palettes.SPRUCE_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.SPRUCE, props));
    public static final ChippedPaletteRegistry CRIMSON_TRAPDOOR = createRegistry(Blocks.CRIMSON_TRAPDOOR, Palettes.CRIMSON_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.CRIMSON, props));
    public static final ChippedPaletteRegistry WARPED_TRAPDOOR = createRegistry(Blocks.WARPED_TRAPDOOR, Palettes.WARPED_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.WARPED, props));
    public static final ChippedPaletteRegistry CHERRY_TRAPDOOR = createRegistry(Blocks.CHERRY_TRAPDOOR, Palettes.CHERRY_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.CHERRY, props));
    public static final ChippedPaletteRegistry BAMBOO_TRAPDOOR = createRegistry(Blocks.BAMBOO_TRAPDOOR, Palettes.BAMBOO_TRAPDOOR, props -> new TrapDoorBlock(BlockSetType.BAMBOO, props));
    public static final Pair<ChippedPaletteRegistry, ChippedPaletteRegistry> TORCH = createTorchRegistry(Blocks.TORCH, Blocks.WALL_TORCH, Palettes.TORCH, TorchBlock::new, WallTorchBlock::new);

    public static final ChippedPaletteRegistry GLASS = createRegistry(Blocks.GLASS, Palettes.GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry GLASS_PANE = createRegistry(Blocks.GLASS_PANE, Palettes.GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry BLACK_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.BLACK), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry BLACK_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.BLACK), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry BLUE_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.BLUE), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry BLUE_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.BLUE), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry BROWN_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.BROWN), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry BROWN_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.BROWN), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry CYAN_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.CYAN), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry CYAN_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.CYAN), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry GRAY_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.GRAY), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry GRAY_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.GRAY), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry GREEN_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.GREEN), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry GREEN_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.GREEN), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry LIGHT_BLUE_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.LIGHT_BLUE), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry LIGHT_BLUE_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.LIGHT_BLUE), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry LIGHT_GRAY_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.LIGHT_GRAY), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry LIGHT_GRAY_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.LIGHT_GRAY), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry LIME_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.LIME), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry LIME_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.LIME), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry MAGENTA_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.MAGENTA), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry MAGENTA_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.MAGENTA), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry ORANGE_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.ORANGE), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry ORANGE_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.ORANGE), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry PINK_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.PINK), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry PINK_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.PINK), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry PURPLE_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.PURPLE), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry PURPLE_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.PURPLE), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry RED_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.RED), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry RED_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.RED), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry WHITE_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.WHITE), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry WHITE_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.WHITE), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);
    public static final ChippedPaletteRegistry YELLOW_STAINED_GLASS = createRegistry(Blocks.STAINED_GLASS.pick(DyeColor.YELLOW), Palettes.STAINED_GLASS, p -> new TransparentBlock(p) {});
    public static final ChippedPaletteRegistry YELLOW_STAINED_GLASS_PANE = createRegistry(Blocks.STAINED_GLASS_PANE.pick(DyeColor.YELLOW), Palettes.STAINED_GLASS_PANE, IronBarsBlock::new);

    public static final ChippedPaletteRegistry BLACK_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.BLACK), Palettes.WOOL);
    public static final ChippedPaletteRegistry BLUE_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.BLUE), Palettes.WOOL);
    public static final ChippedPaletteRegistry BROWN_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.BROWN), Palettes.WOOL);
    public static final ChippedPaletteRegistry CYAN_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.CYAN), Palettes.WOOL);
    public static final ChippedPaletteRegistry GRAY_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.GRAY), Palettes.WOOL);
    public static final ChippedPaletteRegistry GREEN_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.GREEN), Palettes.WOOL);
    public static final ChippedPaletteRegistry LIGHT_BLUE_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.LIGHT_BLUE), Palettes.WOOL);
    public static final ChippedPaletteRegistry LIGHT_GRAY_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.LIGHT_GRAY), Palettes.WOOL);
    public static final ChippedPaletteRegistry LIME_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.LIME), Palettes.WOOL);
    public static final ChippedPaletteRegistry MAGENTA_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.MAGENTA), Palettes.WOOL);
    public static final ChippedPaletteRegistry ORANGE_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.ORANGE), Palettes.WOOL);
    public static final ChippedPaletteRegistry PINK_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.PINK), Palettes.WOOL);
    public static final ChippedPaletteRegistry PURPLE_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.PURPLE), Palettes.WOOL);
    public static final ChippedPaletteRegistry RED_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.RED), Palettes.WOOL);
    public static final ChippedPaletteRegistry WHITE_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.WHITE), Palettes.WOOL);
    public static final ChippedPaletteRegistry YELLOW_WOOL = createRegistry(Blocks.WOOL.pick(DyeColor.YELLOW), Palettes.WOOL);
    public static final ChippedPaletteRegistry BLACK_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.BLACK), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry BLUE_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.BLUE), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry BROWN_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.BROWN), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry CYAN_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.CYAN), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry GRAY_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.GRAY), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry GREEN_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.GREEN), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry LIGHT_BLUE_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.LIGHT_BLUE), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry LIGHT_GRAY_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.LIGHT_GRAY), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry LIME_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.LIME), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry MAGENTA_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.MAGENTA), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry ORANGE_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.ORANGE), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry PINK_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.PINK), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry PURPLE_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.PURPLE), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry RED_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.RED), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry WHITE_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.WHITE), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry YELLOW_CARPET = createRegistry(Blocks.CARPET.pick(DyeColor.YELLOW), Palettes.CARPET, CarpetBlock::new);
    public static final ChippedPaletteRegistry ANCIENT_DEBRIS = createRegistry(Blocks.ANCIENT_DEBRIS, Palettes.STONE);
    public static final ChippedPaletteRegistry ANDESITE = createRegistry(Blocks.ANDESITE, Palettes.STONE);
    public static final ChippedPaletteRegistry BASALT = createRegistry(Blocks.BASALT, Palettes.BASALT);
    public static final ChippedPaletteRegistry BLACKSTONE = createRegistry(Blocks.BLACKSTONE, Palettes.BLACKSTONE);
    public static final ChippedPaletteRegistry CALCITE = createRegistry(Blocks.CALCITE, Palettes.STONE);
    public static final ChippedPaletteRegistry COBBLESTONE = createRegistry(Blocks.COBBLESTONE, Palettes.STONE);
    public static final ChippedPaletteRegistry DARK_PRISMARINE = createRegistry(Blocks.DARK_PRISMARINE, Palettes.DARK_PRISMARINE);
    public static final ChippedPaletteRegistry DEEPSLATE = createRegistry(Blocks.DEEPSLATE, Palettes.DEEPSLATE);
    public static final ChippedPaletteRegistry DIORITE = createRegistry(Blocks.DIORITE, Palettes.STONE);
    public static final ChippedPaletteRegistry DRIPSTONE_BLOCK = createRegistry(Blocks.DRIPSTONE_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry END_STONE = createRegistry(Blocks.END_STONE, Palettes.STONE);
    public static final ChippedPaletteRegistry GILDED_BLACKSTONE = createRegistry(Blocks.GILDED_BLACKSTONE, Palettes.GILDED_BLACKSTONE);
    public static final ChippedPaletteRegistry GRANITE = createRegistry(Blocks.GRANITE, Palettes.STONE);
    public static final ChippedPaletteRegistry MAGMA_BLOCK = createRegistry(Blocks.MAGMA_BLOCK, Palettes.STONE); // Don't use the magma block type because we don't want these blocks to burn you
    public static final ChippedPaletteRegistry MOSSY_COBBLESTONE = createRegistry(Blocks.MOSSY_COBBLESTONE, Palettes.STONE);
    public static final ChippedPaletteRegistry MOSSY_STONE_BRICKS = createRegistry(Blocks.MOSSY_STONE_BRICKS, Palettes.MOSSY_STONE_BRICKS);
    public static final ChippedPaletteRegistry NETHER_BRICKS = createRegistry(Blocks.NETHER_BRICKS, Palettes.STONE);
    public static final ChippedPaletteRegistry NETHERRACK = createRegistry(Blocks.NETHERRACK, Palettes.STONE);
    public static final ChippedPaletteRegistry OBSIDIAN = createRegistry(Blocks.OBSIDIAN, Palettes.STONE);
    public static final ChippedPaletteRegistry PRISMARINE = createRegistry(Blocks.PRISMARINE, Palettes.PRISMARINE);
    public static final ChippedPaletteRegistry PURPUR_BLOCK = createRegistry(Blocks.PURPUR_BLOCK, Palettes.PURPUR_BLOCK);
    public static final ChippedPaletteRegistry QUARTZ_BLOCK = createRegistry(Blocks.QUARTZ_BLOCK, Palettes.QUARTZ_BLOCK);
    public static final ChippedPaletteRegistry RAW_COPPER_BLOCK = createRegistry(Blocks.RAW_COPPER_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry RAW_GOLD_BLOCK = createRegistry(Blocks.RAW_GOLD_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry RAW_IRON_BLOCK = createRegistry(Blocks.RAW_IRON_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry RED_NETHER_BRICKS = createRegistry(Blocks.RED_NETHER_BRICKS, Palettes.STONE);
    public static final ChippedPaletteRegistry RED_SANDSTONE = createRegistry(Blocks.RED_SANDSTONE, Palettes.STONE);
    public static final ChippedPaletteRegistry SANDSTONE = createRegistry(Blocks.SANDSTONE, Palettes.STONE);
    public static final ChippedPaletteRegistry STONE = createRegistry(Blocks.STONE, Palettes.BASE_STONE);
    public static final ChippedPaletteRegistry SMOOTH_STONE = createRegistry(Blocks.SMOOTH_STONE, Palettes.STONE);
    public static final ChippedPaletteRegistry TUFF = createRegistry(Blocks.TUFF, Palettes.STONE);
    public static final ChippedPaletteRegistry TERRACOTTA = createRegistry(Blocks.TERRACOTTA, Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry BLACK_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.BLACK), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry BLUE_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.BLUE), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry BROWN_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.BROWN), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry CYAN_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.CYAN), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry GRAY_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.GRAY), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry GREEN_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.GREEN), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry LIGHT_BLUE_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry LIGHT_GRAY_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry LIME_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.LIME), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry MAGENTA_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.MAGENTA), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry ORANGE_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.ORANGE), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry PINK_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.PINK), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry PURPLE_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.PURPLE), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry RED_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.RED), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry WHITE_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.WHITE), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry YELLOW_TERRACOTTA = createRegistry(Blocks.DYED_TERRACOTTA.pick(DyeColor.YELLOW), Palettes.TERRACOTTA);
    public static final ChippedPaletteRegistry BLACK_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.BLACK), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry BLUE_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.BLUE), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry BROWN_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.BROWN), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry CYAN_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.CYAN), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry GRAY_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.GRAY), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry GREEN_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.GREEN), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry LIGHT_BLUE_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.LIGHT_BLUE), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry LIGHT_GRAY_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.LIGHT_GRAY), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry LIME_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.LIME), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry MAGENTA_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.MAGENTA), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry ORANGE_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.ORANGE), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry PINK_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.PINK), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry PURPLE_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.PURPLE), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry RED_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.RED), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry WHITE_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.WHITE), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry YELLOW_GLAZED_TERRACOTTA = createRegistry(Blocks.GLAZED_TERRACOTTA.pick(DyeColor.YELLOW), Palettes.GLAZED_TERRACOTTA, GlazedTerracottaBlock::new);
    public static final ChippedPaletteRegistry BLACK_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.BLACK), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry BLUE_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.BLUE), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry BROWN_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.BROWN), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry CYAN_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.CYAN), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry GRAY_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.GRAY), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry GREEN_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.GREEN), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry LIGHT_BLUE_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry LIGHT_GRAY_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.LIGHT_GRAY), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry LIME_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.LIME), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry MAGENTA_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.MAGENTA), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry ORANGE_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.ORANGE), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry PINK_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.PINK), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry PURPLE_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.PURPLE), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry RED_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.RED), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry WHITE_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.WHITE), Palettes.CONCRETE);
    public static final ChippedPaletteRegistry YELLOW_CONCRETE = createRegistry(Blocks.CONCRETE.pick(DyeColor.YELLOW), Palettes.CONCRETE);

    public static final ChippedPaletteRegistry BRICKS = createRegistry(Blocks.BRICKS, Palettes.BRICKS);
    public static final ChippedPaletteRegistry BORDERLESS_BRICKS = registerBorderlessBricks("borderless_bricks", Palettes.BRICKS);
    public static final ChippedPaletteRegistry MUD_BRICKS = createRegistry(Blocks.MUD_BRICKS, Palettes.MUD);
    public static final ChippedPaletteRegistry POINTED_DRIPSTONE = createRegistry(Blocks.POINTED_DRIPSTONE, Palettes.POINTED_DRIPSTONE, SpecialPointedDripstoneBlock::new);

    public static final ChippedPaletteRegistry IRON_BARS = createRegistry(Blocks.IRON_BARS, Palettes.IRON_BARS, IronBarsBlock::new);
    public static final ChippedPaletteRegistry SEA_LANTERN = createRegistry(Blocks.SEA_LANTERN, Palettes.SEA_LANTERN);
    public static final ChippedPaletteRegistry REDSTONE_LAMP = createRegistry(Blocks.REDSTONE_LAMP, Palettes.REDSTONE_LAMP, RedstoneLampBlock::new);
    public static final ChippedPaletteRegistry REDSTONE_BLOCK = createRegistry(Blocks.REDSTONE_BLOCK, Palettes.STONE);
    public static final ChippedPaletteRegistry LANTERN = createRegistry(Blocks.LANTERN, Palettes.LANTERN, LanternBlock::new);
    public static final ChippedPaletteRegistry SOUL_LANTERN = createRegistry(Blocks.SOUL_LANTERN, Palettes.SOUL_LANTERN, LanternBlock::new);
    public static final ChippedPaletteRegistry SPECIAL_LANTERN = registerSpecialLanterns("special_lantern", Palettes.SPECIAL_LANTERN);
    public static final ChippedPaletteRegistry SPECIAL_SOUL_LANTERN = registerSpecialLanterns("special_soul_lantern", Palettes.SPECIAL_SOUL_LANTERN);
    public static final Pair<ChippedPaletteRegistry, ChippedPaletteRegistry> REDSTONE_TORCH = createTorchRegistry(Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WALL_TORCH, Palettes.REDSTONE_TORCH, (o, p) -> new RedstoneTorchBlock(p), (o, p) -> new RedstoneWallTorchBlock(p));

    public static ChippedPaletteRegistry createRegistry(Block ref, Palette palette) {
        return createRegistry(ref, palette, DEFAULT_CREATOR);
    }

    public static ChippedPaletteRegistry createRegistry(Block ref, Palette palette, Function<BlockBehaviour.Properties, Block> blockType) {
        return createRegistry(ref, palette, blockType, BlockItem::new);
    }

    public static ChippedPaletteRegistry createRegistry(Block ref, Palette palette, Function<BlockBehaviour.Properties, Block> blockType, BiFunction<Block, Item.Properties, BlockItem> itemType) {
        var registry = new ChippedPaletteRegistry(BLOCKS, ref, palette);
        for (var entry : palette.ids()) {
            String id = entry.getSecond().replace("%", BuiltInRegistries.BLOCK.getKey(ref).getPath().toLowerCase(Locale.ROOT));
            IdType type = Objects.requireNonNull(entry.getFirst());
            if (type == IdType.PILLAR) {
                if (blockType != DEFAULT_CREATOR) {
                    Constants.LOGGER.error("ID: {}, Reference: {}, Palette: {}", id, BuiltInRegistries.BLOCK.getKey(ref).getPath(), palette);
                    throw new IllegalArgumentException("Cannot use custom block type with non-default id type");
                }
                registry.register(id, RotatedPillarBlock::new, () -> createProperties(ref));
            } else if (type == IdType.DIRECTIONAL) {
                registry.register(id, DirectionBlock::new, () -> createProperties(ref));
            } else {
                registry.register(id, blockType, () -> createProperties(ref));
            }
        }
        ModItems.createItemRegistry(registry, itemType);
        return registry;
    }

    public static Pair<ChippedPaletteRegistry, ChippedPaletteRegistry> createTorchRegistry(Block ref1, Block ref2, Palette palette, BiFunction<SimpleParticleType, BlockBehaviour.Properties, Block> blockType1, BiFunction<SimpleParticleType, BlockBehaviour.Properties, Block> blockType2) {
        var registry1 = new ChippedPaletteRegistry(BLOCKS, ref1, palette);
        var registry2 = new ChippedPaletteRegistry(BLOCKS, ref2, palette);
        var itemRegistry = ResourcefulRegistries.createForItems(ModItems.ITEMS);

        for (String s : palette) {
            String id1 = s.replace("%", BuiltInRegistries.BLOCK.getKey(ref1).getPath().toLowerCase(Locale.ROOT));
            RegistryEntry<Block> torch1 = registry1.register(id1, (properties) -> blockType1.apply(ParticleTypes.FLAME, properties), () -> createProperties(ref1));

            String id2 = s.replace("%", BuiltInRegistries.BLOCK.getKey(ref2).getPath().toLowerCase(Locale.ROOT));
            RegistryEntry<Block> torch2 = registry2.register(id2, (properties) -> blockType2.apply(ParticleTypes.FLAME, properties), () -> createProperties(ref2));

            itemRegistry.register(torch1.getId().getPath(), (properties) -> new StandingAndWallBlockItem(torch1.get(), torch2.get(), Direction.DOWN, properties), Item.Properties::new);
        }

        return Pair.of(registry1, registry2);
    }

    public static ChippedPaletteRegistry registerSpecialLanterns(String block, Palette palette) {
        var registry = new ChippedPaletteRegistry(BLOCKS, Blocks.LANTERN, palette);
        registry.register(palette.get(0).replace("%", block), (properties) -> new SpecialLanternBlock(properties, SpecialLanternBlock.CHONK_LANTERN_SHAPE), () -> createProperties(Blocks.LANTERN));
        registry.register(palette.get(1).replace("%", block), (properties) -> new SpecialLanternBlock(properties, SpecialLanternBlock.DONUT_LANTERN_SHAPE_EAST, SpecialLanternBlock.DONUT_LANTERN_SHAPE_NORTH), () -> createProperties(Blocks.LANTERN));
        registry.register(palette.get(2).replace("%", block), (properties) -> new SpecialLanternBlock(properties, SpecialLanternBlock.TALL_LANTERN_SHAPE), () -> createProperties(Blocks.LANTERN));
        registry.register(palette.get(3).replace("%", block), (properties) -> new SpecialLanternBlock(properties, SpecialLanternBlock.THICC_LANTERN_SHAPE), () -> createProperties(Blocks.LANTERN));
        ModItems.createItemRegistry(registry, BlockItem::new);
        return registry;
    }

    public static ChippedPaletteRegistry registerBorderlessBricks(String block, Palette palette) {
        var registry = new ChippedPaletteRegistry(BLOCKS, Blocks.BRICKS, block, palette);
        for (var entry : palette.ids()) {
            String id = entry.getSecond().replace("%", block);
            if (Objects.requireNonNull(entry.getFirst()) == IdType.PILLAR) {
                registry.register(id, RotatedPillarBlock::new, () -> createProperties(Blocks.BRICKS));
            } else {
                registry.register(id, Block::new, () -> createProperties(Blocks.BRICKS));
            }
        }
        ModItems.createItemRegistry(registry, BlockItem::new);
        return registry;
    }

    private static BlockBehaviour.Properties createProperties(Block ref) {
        return BlockBehaviour.Properties.ofFullCopy(ref).noLootTable();
    }

    private static Boolean never(BlockState state, BlockGetter blockGetter, BlockPos pos, EntityType<?> entity) {
        return false;
    }
}
