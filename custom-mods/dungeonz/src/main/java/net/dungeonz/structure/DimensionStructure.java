package net.dungeonz.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.WorldInit;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DimensionStructure extends Structure {

    // A custom codec that changes the size limit for our code_structure_sky_fan.json's config to not be capped at 7.
    // With this, we can have a structure with a size limit up to 30 if we want to have extremely long branches of pieces in the structure.
    public static final MapCodec<DimensionStructure> CODEC = RecordCodecBuilder.<DimensionStructure>mapCodec(
            instance -> instance.group(DimensionStructure.settingsCodec(instance), StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size), HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
                    Codec.STRING.fieldOf("dungeon_type").forGetter(structure -> structure.dungeonType)).apply(instance, DimensionStructure::new));

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final String dungeonType;

    public DimensionStructure(Structure.StructureSettings config, Holder<StructureTemplatePool> startPool, Optional<Identifier> startJigsawName, int size, HeightProvider startHeight,
                              Optional<Heightmap.Types> projectStartToHeightmap, int maxDistanceFromCenter, String dungeonType) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.dungeonType = dungeonType;
    }

    private static boolean extraSpawningChecks(Structure.GenerationContext context) {
        // Grabs the chunk position we are at
        ChunkPos chunkpos = context.chunkPos();

        // Checks to make sure our structure does not spawn above land that's higher than y = 150
        // to demonstrate how this method is good for checking extra conditions for spawning
        return context.chunkGenerator().getFirstOccupiedHeight(chunkpos.getMinBlockX(), chunkpos.getMinBlockZ(), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState()) < 150;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {

        // Check if the spot is valid for our structure. This is just as another method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it will not generate the structure.
        if (!DimensionStructure.extraSpawningChecks(context)) {
            return Optional.empty();
        }
        // Set's our spawning blockpos's y offset to be 60 blocks up.
        // Since we are going to have heightmap/terrain height spawning set to true further down, this will make it so we spawn 60 blocks above terrain.
        // If we wanted to spawn on ocean floor, we would set heightmap/terrain height spawning to false and the grab the y value of the terrain with OCEAN_FLOOR_WG heightmap.
        int startY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));

        // Turns the chunk coordinates into actual coordinates we can use. (Gets corner of that chunk)
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMinBlockX(), startY, chunkPos.getMinBlockZ());

        // public static Optional<Structure.StructurePosition> generate(Structure.Context context, RegistryEntry<StructurePool> structurePool, Optional<Identifier> id, int size, BlockPos pos, boolean
        // useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter, StructurePoolAliasLookup aliasLookup, DimensionPadding dimensionPadding,
        // StructureLiquidSettings liquidSettings) {

        // Optional<Structure.StructurePosition> optional = StructurePoolBasedGenerator.generate(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), 512,
        // StructurePoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);

        Optional<GenerationStub> structurePiecesGenerator = JigsawPlacement.addPieces(context, // Used for StructurePoolBasedGenerator to get all the proper behaviors done.
                this.startPool, // The starting pool to use to create the structure layout from
                this.startJigsawName, // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
                this.size, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                blockPos, // Where to spawn the structure.
                false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                this.projectStartToHeightmap, // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                // Here, blockpos's y value is 60 which means the structure spawn 60 blocks above terrain height.
                // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
                // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                new JigsawStructure.MaxDistance(this.maxDistanceFromCenter), // Maximum limit for how far pieces can spawn from center. You cannot set this bigger than 128 or else pieces gets cutoff.
                PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);

        /*
         * Note, you are always free to make your own StructurePoolBasedGenerator class and implementation of how the structure should generate. It is tricky but extremely powerful if you are doing
         * something that vanilla's jigsaw system cannot do. Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation
         * of pieces.
         */
        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.

        return structurePiecesGenerator;
    }

    @Override
    public void afterPlace(WorldGenLevel world, StructureManager structureAccessor, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox box, ChunkPos chunkPos, PiecesContainer pieces) {
        List<BlockPos> list = new ArrayList<BlockPos>();

        pieces.pieces().forEach((piece) -> {
            for (int i = piece.getBoundingBox().minX(); i <= piece.getBoundingBox().maxX(); i++) {
                for (int u = piece.getBoundingBox().minY(); u <= piece.getBoundingBox().maxY(); u++) {
                    for (int o = piece.getBoundingBox().minZ(); o <= piece.getBoundingBox().maxZ(); o++) {
                        BlockPos pos = new BlockPos(i, u, o);
                        if (!world.getBlockState(pos).isAir() && !list.contains(pos) && world.getBlockState(pos).is(BlockInit.DUNGEON_PORTAL)) {
                            list.add(pos);
                        }
                    }
                }
            }
        });

        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) world.getBlockEntity(list.get(i));
                dungeonPortalEntity.setDungeonType(dungeonType);
                dungeonPortalEntity.setDifficulty(dungeonPortalEntity.getDungeon().getDifficultyList().get(0));
                dungeonPortalEntity.setMaxGroupSize(dungeonPortalEntity.getDungeon().getMaxGroupSize());
                dungeonPortalEntity.setMinGroupSize(dungeonPortalEntity.getDungeon().getMinGroupSize());
                dungeonPortalEntity.setChanged();
            }
        }
    }

    @Override
    public StructureType<?> type() {
        return WorldInit.DIMENSION_STRUCTURES;
    }
}
