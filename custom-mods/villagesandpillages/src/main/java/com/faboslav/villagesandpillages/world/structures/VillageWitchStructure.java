package com.faboslav.villagesandpillages.world.structures;

import com.faboslav.villagesandpillages.init.VillagesAndPillagesStructureTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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

import java.util.Optional;

public class VillageWitchStructure extends Structure {
    public static final MapCodec<VillageWitchStructure> CODEC = RecordCodecBuilder.mapCodec(builder -> builder
            .group(
                    settingsCodec(builder),
                    StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    Codec.intRange(0, 128).fieldOf("size").forGetter(structure -> structure.maxDepth),
                    HeightProvider.CODEC.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    Codec.BOOL.optionalFieldOf("use_expansion_hack", false).forGetter(structure -> structure.useExpansionHack),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter)
            )
            .apply(builder, VillageWitchStructure::new));

    public final Holder<StructureTemplatePool> startPool;
    public final int maxDepth;
    public final HeightProvider startHeight;
    public final boolean useExpansionHack;
    public final int maxDistanceFromCenter;

    public VillageWitchStructure(
            Structure.StructureSettings settings,
            Holder<StructureTemplatePool> startPool,
            int maxDepth,
            HeightProvider startHeight,
            boolean useExpansionHack,
            int maxDistanceFromCenter
    ) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
        this.startHeight = startHeight;
        this.useExpansionHack = useExpansionHack;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        int startY = this.startHeight.sample(context.random(), new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
        BlockPos startPos = new BlockPos(context.chunkPos().getMinBlockX(), startY, context.chunkPos().getMinBlockZ());

        return JigsawPlacement.addPieces(
                context,
                this.startPool,
                Optional.empty(),
                this.maxDepth,
                startPos,
                this.useExpansionHack,
                Optional.of(Heightmap.Types.WORLD_SURFACE_WG),
                new JigsawStructure.MaxDistance(this.maxDistanceFromCenter),
                PoolAliasLookup.EMPTY,
                DimensionPadding.ZERO,
                JigsawStructure.DEFAULT_LIQUID_SETTINGS
        );
    }

    @Override
    public StructureType<?> type() {
        return VillagesAndPillagesStructureTypes.VILLAGE_WITCH_STRUCTURE;
    }
}
