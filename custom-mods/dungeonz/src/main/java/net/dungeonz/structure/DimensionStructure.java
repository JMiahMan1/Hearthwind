package net.dungeonz.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.WorldInit;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2794;
import net.minecraft.class_2902;
import net.minecraft.class_2960;
import net.minecraft.class_3195;
import net.minecraft.class_3341;
import net.minecraft.class_3778;
import net.minecraft.class_3785;
import net.minecraft.class_5138;
import net.minecraft.class_5281;
import net.minecraft.class_5434;
import net.minecraft.class_5819;
import net.minecraft.class_5868;
import net.minecraft.class_6122;
import net.minecraft.class_6624;
import net.minecraft.class_6880;
import net.minecraft.class_7151;
import net.minecraft.class_8891;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DimensionStructure extends class_3195 {

    // A custom codec that changes the size limit for our code_structure_sky_fan.json's config to not be capped at 7.
    // With this, we can have a structure with a size limit up to 30 if we want to have extremely long branches of pieces in the structure.
    public static final MapCodec<DimensionStructure> CODEC = RecordCodecBuilder.<DimensionStructure>mapCodec(
            instance -> instance.group(DimensionStructure.method_42697(instance), class_3785.field_24954.fieldOf("start_pool").forGetter(structure -> structure.startPool),
                    class_2960.field_25139.optionalFieldOf("start_jigsaw_name").forGetter(structure -> structure.startJigsawName),
                    Codec.intRange(0, 30).fieldOf("size").forGetter(structure -> structure.size), class_6122.field_31540.fieldOf("start_height").forGetter(structure -> structure.startHeight),
                    class_2902.class_2903.field_24772.optionalFieldOf("project_start_to_heightmap").forGetter(structure -> structure.projectStartToHeightmap),
                    Codec.intRange(1, 128).fieldOf("max_distance_from_center").forGetter(structure -> structure.maxDistanceFromCenter),
                    Codec.STRING.fieldOf("dungeon_type").forGetter(structure -> structure.dungeonType)).apply(instance, DimensionStructure::new));

    private final class_6880<class_3785> startPool;
    private final Optional<class_2960> startJigsawName;
    private final int size;
    private final class_6122 startHeight;
    private final Optional<class_2902.class_2903> projectStartToHeightmap;
    private final int maxDistanceFromCenter;
    private final String dungeonType;

    public DimensionStructure(class_3195.class_7302 config, class_6880<class_3785> startPool, Optional<class_2960> startJigsawName, int size, class_6122 startHeight,
                              Optional<class_2902.class_2903> projectStartToHeightmap, int maxDistanceFromCenter, String dungeonType) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
        this.dungeonType = dungeonType;
    }

    private static boolean extraSpawningChecks(class_3195.class_7149 context) {
        // Grabs the chunk position we are at
        class_1923 chunkpos = context.comp_568();

        // Checks to make sure our structure does not spawn above land that's higher than y = 150
        // to demonstrate how this method is good for checking extra conditions for spawning
        return context.comp_562().method_18028(chunkpos.method_8326(), chunkpos.method_8328(), class_2902.class_2903.field_13203, context.comp_569(), context.comp_564()) < 150;
    }

    @Override
    public Optional<class_3195.class_7150> method_38676(class_3195.class_7149 context) {

        // Check if the spot is valid for our structure. This is just as another method for cleanness.
        // Returning an empty optional tells the game to skip this spot as it will not generate the structure.
        if (!DimensionStructure.extraSpawningChecks(context)) {
            return Optional.empty();
        }
        // Set's our spawning blockpos's y offset to be 60 blocks up.
        // Since we are going to have heightmap/terrain height spawning set to true further down, this will make it so we spawn 60 blocks above terrain.
        // If we wanted to spawn on ocean floor, we would set heightmap/terrain height spawning to false and the grab the y value of the terrain with OCEAN_FLOOR_WG heightmap.
        int startY = this.startHeight.method_35391(context.comp_566(), new class_5868(context.comp_562(), context.comp_569()));

        // Turns the chunk coordinates into actual coordinates we can use. (Gets corner of that chunk)
        class_1923 chunkPos = context.comp_568();
        class_2338 blockPos = new class_2338(chunkPos.method_8326(), startY, chunkPos.method_8328());

        // public static Optional<Structure.StructurePosition> generate(Structure.Context context, RegistryEntry<StructurePool> structurePool, Optional<Identifier> id, int size, BlockPos pos, boolean
        // useExpansionHack, Optional<Heightmap.Type> projectStartToHeightmap, int maxDistanceFromCenter, StructurePoolAliasLookup aliasLookup, DimensionPadding dimensionPadding,
        // StructureLiquidSettings liquidSettings) {

        // Optional<Structure.StructurePosition> optional = StructurePoolBasedGenerator.generate(context, structurePool, Optional.of(id), size, pos, false, Optional.empty(), 512,
        // StructurePoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);

        Optional<class_7150> structurePiecesGenerator = class_3778.method_30419(context, // Used for StructurePoolBasedGenerator to get all the proper behaviors done.
                this.startPool, // The starting pool to use to create the structure layout from
                this.startJigsawName, // Can be used to only spawn from one Jigsaw block. But we don't need to worry about this.
                this.size, // How deep a branch of pieces can go away from center piece. (5 means branches cannot be longer than 5 pieces from center piece)
                blockPos, // Where to spawn the structure.
                false, // "useExpansionHack" This is for legacy villages to generate properly. You should keep this false always.
                this.projectStartToHeightmap, // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                // Here, blockpos's y value is 60 which means the structure spawn 60 blocks above terrain height.
                // Set this to false for structure to be place only at the passed in blockpos's Y value instead.
                // Definitely keep this false when placing structures in the nether as otherwise, heightmap placing will put the structure on the Bedrock roof.
                this.maxDistanceFromCenter, // Maximum limit for how far pieces can spawn from center. You cannot set this bigger than 128 or else pieces gets cutoff.
                class_8891.field_46826, class_5434.field_51911, class_5434.field_52235);

        /*
         * Note, you are always free to make your own StructurePoolBasedGenerator class and implementation of how the structure should generate. It is tricky but extremely powerful if you are doing
         * something that vanilla's jigsaw system cannot do. Such as for example, forcing 3 pieces to always spawn every time, limiting how often a piece spawns, or remove the intersection limitation
         * of pieces.
         */
        // Return the pieces generator that is now set up so that the game runs it when it needs to create the layout of structure pieces.

        return structurePiecesGenerator;
    }

    @Override
    public void method_38694(class_5281 world, class_5138 structureAccessor, class_2794 chunkGenerator, class_5819 random, class_3341 box, class_1923 chunkPos, class_6624 pieces) {
        List<class_2338> list = new ArrayList<class_2338>();

        pieces.comp_132().forEach((piece) -> {
            for (int i = piece.method_14935().method_35415(); i <= piece.method_14935().method_35418(); i++) {
                for (int u = piece.method_14935().method_35416(); u <= piece.method_14935().method_35419(); u++) {
                    for (int o = piece.method_14935().method_35417(); o <= piece.method_14935().method_35420(); o++) {
                        class_2338 pos = new class_2338(i, u, o);
                        if (!world.method_8320(pos).method_26215() && !list.contains(pos) && world.method_8320(pos).method_27852(BlockInit.DUNGEON_PORTAL)) {
                            list.add(pos);
                        }
                    }
                }
            }
        });

        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                DungeonPortalEntity dungeonPortalEntity = (DungeonPortalEntity) world.method_8321(list.get(i));
                dungeonPortalEntity.setDungeonType(dungeonType);
                dungeonPortalEntity.setDifficulty(dungeonPortalEntity.getDungeon().getDifficultyList().get(0));
                dungeonPortalEntity.setMaxGroupSize(dungeonPortalEntity.getDungeon().getMaxGroupSize());
                dungeonPortalEntity.setMinGroupSize(dungeonPortalEntity.getDungeon().getMinGroupSize());
                dungeonPortalEntity.method_5431();
            }
        }
    }

    @Override
    public class_7151<?> method_41618() {
        return WorldInit.DIMENSION_STRUCTURES;
    }
}
