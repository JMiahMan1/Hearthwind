package com.teamremastered.tlc.structures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.teamremastered.tlc.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

/** Jigsaw structure for the Lost Castle; the 26.2 APIs it uses are unchanged. */
public class LostCastle extends Structure {
    public static final MapCodec<LostCastle> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
            Identifier.CODEC.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
            Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size),
            HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
            Heightmap.Types.CODEC.optionalFieldOf("project_start_to_heightmap")
                    .forGetter(structure -> structure.projectStartToHeightmap),
            JigsawStructure.MaxDistance.CODEC.fieldOf("max_distance_from_center")
                    .forGetter(structure -> structure.maxDistanceFromCenter),
            DimensionPadding.CODEC.optionalFieldOf("dimension_padding", JigsawStructure.DEFAULT_DIMENSION_PADDING)
                    .forGetter(structure -> structure.dimensionPadding),
            LiquidSettings.CODEC.optionalFieldOf("liquid_settings", JigsawStructure.DEFAULT_LIQUID_SETTINGS)
                    .forGetter(structure -> structure.liquidSettings)
    ).apply(instance, LostCastle::new));

    private final Holder<StructureTemplatePool> startPool;
    private final Optional<Identifier> startJigsawName;
    private final int size;
    private final HeightProvider startHeight;
    private final Optional<Heightmap.Types> projectStartToHeightmap;
    private final JigsawStructure.MaxDistance maxDistanceFromCenter;
    private final DimensionPadding dimensionPadding;
    private final LiquidSettings liquidSettings;

    public LostCastle(Structure.StructureSettings config,
                      Holder<StructureTemplatePool> startPool,
                      Optional<Identifier> startJigsawName,
                      int size,
                      HeightProvider startHeight,
                      Optional<Heightmap.Types> projectStartToHeightmap,
                      JigsawStructure.MaxDistance maxDistanceFromCenter,
                      DimensionPadding dimensionPadding,
                      LiquidSettings liquidSettings) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.dimensionPadding = dimensionPadding;
        this.liquidSettings = liquidSettings;
    }

    private static int distanceFromSpawn(ChunkPos structurePos) {
        ChunkPos spawnPointPos = new ChunkPos(0, 0);
        int structurePosX = structurePos.x() << 4;
        int structurePosZ = structurePos.z() << 4;
        return (int) Math.sqrt(Math.pow(structurePosX - spawnPointPos.x(), 2.0)
                + Math.pow(structurePosZ - spawnPointPos.z(), 2.0));
    }

    private static boolean extraSpawningChecks(Structure.GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();
        int startHeight = context.chunkGenerator().getFirstOccupiedHeight(x, z,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
        int height1 = context.chunkGenerator().getFirstOccupiedHeight(x + 78, z,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
        int height2 = context.chunkGenerator().getFirstOccupiedHeight(x - 78, z,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
        int height3 = context.chunkGenerator().getFirstOccupiedHeight(x, z + 78,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
        int height4 = context.chunkGenerator().getFirstOccupiedHeight(x, z - 78,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
        return Math.abs(startHeight - height1) < 10
                && Math.abs(startHeight - height2) < 10
                && Math.abs(startHeight - height3) < 10
                && Math.abs(startHeight - height4) < 10
                && distanceFromSpawn(chunkPos) > 5000;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        if (!extraSpawningChecks(context)) {
            return Optional.empty();
        }
        int startY = this.startHeight.sample(context.random(),
                new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        ChunkPos chunkPos = context.chunkPos();
        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();
        BlockPos blockPos = new BlockPos(x, startY, z);
        return JigsawPlacement.addPieces(context, this.startPool, this.startJigsawName, this.size, blockPos,
                false, this.projectStartToHeightmap, this.maxDistanceFromCenter, PoolAliasLookup.EMPTY,
                this.dimensionPadding, this.liquidSettings);
    }

    @Override
    public StructureType<?> type() {
        return Services.PLATFORM.getStructureType();
    }
}
