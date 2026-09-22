package io.wispforest.lavender.structure;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.Vec3i;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Optional;
import java.util.function.BiConsumer;

public class StructureTemplate {

    private final BlockStatePredicate[][][] predicates;
    private final EnumMap<BlockStatePredicate.MatchCategory, MutableInt> predicateCountByType;

    public final int xSize, ySize, zSize;
    public final Vec3i anchor;
    public final Identifier id;

    public StructureTemplate(Identifier id, BlockStatePredicate[][][] predicates, int xSize, int ySize, int zSize, @Nullable Vec3i anchor) {
        this.id = id;
        this.predicates = predicates;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;

        this.anchor = anchor != null
            ? anchor
            : new Vec3i(this.xSize / 2, 0, this.ySize / 2);

        this.predicateCountByType = new EnumMap<>(BlockStatePredicate.MatchCategory.class);
        for (var type : BlockStatePredicate.MatchCategory.values()) {
            this.forEachPredicate((blockPos, predicate) -> {
                if (!predicate.isOf(type)) return;
                this.predicateCountByType.computeIfAbsent(type, $ -> new MutableInt()).increment();
            });
        }
    }

    /**
     * @return How many predicates of this structure template fall
     * into the given match category
     */
    public int predicatesOfType(BlockStatePredicate.MatchCategory type) {
        return this.predicateCountByType.get(type).intValue();
    }

    /**
     * @return The anchor position of this template,
     * to be used when placing in the world
     */
    public Vec3i anchor() {
        return this.anchor;
    }

    // --- iteration ---

    public void forEachPredicate(BiConsumer<BlockPos, BlockStatePredicate> action) {
        this.forEachPredicate(action, Rotation.NONE);
    }

    /**
     * Execute {@code action} for every predicate in this structure template,
     * rotated on the y-axis by {@code rotation}
     */
    public void forEachPredicate(BiConsumer<BlockPos, BlockStatePredicate> action, Rotation rotation) {
        var mutable = new BlockPos.MutableBlockPos();

        for (int x = 0; x < this.predicates.length; x++) {
            for (int y = 0; y < this.predicates[x].length; y++) {
                for (int z = 0; z < this.predicates[x][y].length; z++) {

                    switch (rotation) {
                        case CLOCKWISE_90 -> mutable.set(this.zSize - z - 1, y, x);
                        case COUNTERCLOCKWISE_90 -> mutable.set(z, y, this.xSize - x - 1);
                        case CLOCKWISE_180 -> mutable.set(this.xSize - x - 1, y, this.zSize - z - 1);
                        default -> mutable.set(x, y, z);
                    }

                    action.accept(mutable, this.predicates[x][y][z]);
                }
            }
        }
    }

    // --- validation ---

    /**
     * Shorthand of {@link #validate(Level, BlockPos, Direction)} which uses
     * {@link Direction#NONE}
     */
    public boolean validate(Level world, BlockPos anchor) {
        return this.validate(world, anchor, Rotation.NONE);
    }

    /**
     * @return {@code true} if this template matches the block states present
     * in the given world at the given position
     */
    public boolean validate(Level world, BlockPos anchor, Rotation rotation) {
        return this.countValidStates(world, anchor, rotation) == this.predicatesOfType(BlockStatePredicate.MatchCategory.NON_NULL);
    }

    /**
     * Shorthand of {@link #countValidStates(Level, BlockPos, Direction)} which uses
     * {@link Direction#NONE}
     */
    public int countValidStates(Level world, BlockPos anchor) {
        return countValidStates(world, anchor, Rotation.NONE, BlockStatePredicate.MatchCategory.NON_NULL);
    }

    /**
     * Shorthand of {@link #countValidStates(Level, BlockPos, Direction, BlockStatePredicate.MatchCategory)}
     * which uses {@link io.wispforest.lavender.structure.BlockStatePredicate.MatchCategory#NON_NULL}
     */
    public int countValidStates(Level world, BlockPos anchor, Rotation rotation) {
        return countValidStates(world, anchor, rotation, BlockStatePredicate.MatchCategory.NON_NULL);
    }

    /**
     * @return The amount of predicates in this template which match the block
     * states present in the given world at the given position
     */
    public int countValidStates(Level world, BlockPos anchor, Rotation rotation, BlockStatePredicate.MatchCategory predicateFilter) {
        var validStates = new MutableInt();
        var mutable = new BlockPos.MutableBlockPos();

        this.forEachPredicate((pos, predicate) -> {
            if (!predicate.isOf(predicateFilter)) return;

            if (predicate.matches(world.getBlockState(mutable.set(pos).move(anchor)).rotate(inverse(rotation)))) {
                validStates.increment();
            }
        }, rotation);

        return validStates.intValue();
    }

    // --- utility ---

    public BlockAndTintGetter asBlockRenderView() {
        var world = Minecraft.getInstance().level;
        return new BlockAndTintGetter() {
            @Override
            public net.minecraft.world.level.CardinalLighting cardinalLighting() {
                return net.minecraft.world.level.CardinalLighting.DEFAULT;
            }

            @Override
            public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
                if (world == null) return 0;
                return colorResolver.getColor(world.getBiome(pos).value(), pos.getX(), pos.getZ());
            }

            @Override
            public net.minecraft.world.level.lighting.LevelLightEngine getLightEngine() {
                return world != null ? world.getLightEngine() : null;
            }

            @Nullable
            @Override
            public BlockEntity getBlockEntity(BlockPos pos) {
                return null;
            }

            @Override
            public BlockState getBlockState(BlockPos pos) {
                if (pos.getX() < 0 || pos.getX() >= StructureTemplate.this.xSize || pos.getY() < 0 || pos.getY() >= StructureTemplate.this.ySize || pos.getZ() < 0 || pos.getZ() >= StructureTemplate.this.zSize)
                    return Blocks.AIR.defaultBlockState();
                return StructureTemplate.this.predicates[pos.getX()][pos.getY()][pos.getZ()].preview();
            }

            @Override
            public FluidState getFluidState(BlockPos pos) {
                return Fluids.EMPTY.defaultFluidState();
            }

            @Override
            public int getMinY() {
                return world != null ? world.getMinY() : 0;
            }

            @Override
            public int getHeight() {
                return ySize;
            }
        };
    }

    public static Rotation inverse(Rotation rotation) {
        return switch (rotation) {
            case NONE -> Rotation.NONE;
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            case CLOCKWISE_180 -> Rotation.CLOCKWISE_180;
        };
    }

    // --- parsing ---

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static StructureTemplate parse(Identifier resourceId, JsonObject json) {
        var keyObject = GsonHelper.getAsJsonObject(json, "keys");
        var keys = new Char2ObjectOpenHashMap<BlockStatePredicate>();
        Vec3i anchor = null;

        for (var entry : keyObject.entrySet()) {
            char key;
            if (entry.getKey().length() == 1) {
                key = entry.getKey().charAt(0);
                if (key == '#') {
                    throw new JsonParseException("Key '#' is reserved for 'anchor' declarations");
                }

            } else if (entry.getKey().equals("anchor")) {
                key = '#';
            } else {
                continue;
            }

            try {
                var result = BlockStateParser.parseForTesting((net.minecraft.core.HolderLookup<Block>) (Object) BuiltInRegistries.BLOCK, entry.getValue().getAsString(), false);
                if (result.left().isPresent()) {
                    var predicate = result.left().get();

                    keys.put(key, new BlockStatePredicate() {
                        @Override
                        public BlockState preview() {
                            return predicate.blockState();
                        }

                        @Override
                        public Result test(BlockState state) {
                            if (state.getBlock() != predicate.blockState().getBlock()) return Result.NO_MATCH;

                            for (var propAndValue : predicate.properties().entrySet()) {
                                if (!java.util.Objects.equals(state.getValue((Property<?>) propAndValue.getKey()), propAndValue.getValue())) {
                                    return Result.BLOCK_MATCH;
                                }
                            }

                            return Result.STATE_MATCH;
                        }
                    });
                } else {
                    var predicate = result.right().get();

                    var previewStates = new ArrayList<BlockState>();
                    predicate.tag().forEach(registryEntry -> {
                        var block = registryEntry.value();
                        var state = block.defaultBlockState();

                        for (var propAndValue : predicate.vagueProperties().entrySet()) {
                            Property prop = block.getStateDefinition().getProperty(propAndValue.getKey());
                            if (prop == null) return;

                            Optional<? extends Comparable<?>> value = ((Property<?>) prop).getValue(propAndValue.getValue());
                            if (value.isEmpty()) return;

                            state = (BlockState) ((net.minecraft.world.level.block.state.StateHolder<?, ?>) state).setValue((Property) prop, (Comparable) value.get());
                        }

                        previewStates.add(state);
                    });

                    keys.put(key, new BlockStatePredicate() {
                        @Override
                        public BlockState preview() {
                            if (previewStates.isEmpty()) return Blocks.AIR.defaultBlockState();
                            return previewStates.get((int) (System.currentTimeMillis() / 1000 % previewStates.size()));
                        }

                        @Override
                        public Result test(BlockState state) {
                            if (!predicate.tag().contains(state.typeHolder())) return Result.NO_MATCH;

                            for (var propAndValue : predicate.vagueProperties().entrySet()) {
                                var prop = state.getBlock().getStateDefinition().getProperty(propAndValue.getKey());
                                if (prop == null) return Result.BLOCK_MATCH;

                                var expected = ((Property<? extends Comparable<?>>) prop).getValue(propAndValue.getValue());
                                if (expected.isEmpty()) return Result.BLOCK_MATCH;

                                if (!java.util.Objects.equals(state.getValue((Property<?>) prop), expected.get())) return Result.BLOCK_MATCH;
                            }

                            return Result.STATE_MATCH;
                        }
                    });
                }
            } catch (CommandSyntaxException e) {
                throw new JsonParseException("Failed to parse block state predicate", e);
            }
        }

        var layersArray = GsonHelper.getAsJsonArray(json, "layers");
        int xSize = 0, ySize = layersArray.size(), zSize = 0;

        for (var element : layersArray) {
            if (!(element instanceof JsonArray layer)) {
                throw new JsonParseException("Every element in the 'layers' array must itself be an array");
            }

            if (zSize == 0) {
                zSize = layer.size();
            } else if (zSize != layer.size()) {
                throw new JsonParseException("Every layer must have the same amount of rows");
            }

            for (var rowElement : layer) {
                if (!rowElement.isJsonPrimitive()) {
                    throw new JsonParseException("Every element in a row must be a primitive");
                }
                if (xSize == 0) {
                    xSize = rowElement.getAsString().length();
                } else if (xSize != rowElement.getAsString().length()) {
                    throw new JsonParseException("Every row must have the same length");
                }
            }
        }

        var result = new BlockStatePredicate[xSize][][];
        for (int x = 0; x < xSize; x++) {
            result[x] = new BlockStatePredicate[ySize][];
            for (int y = 0; y < ySize; y++) {
                result[x][y] = new BlockStatePredicate[zSize];
            }
        }

        for (int y = 0; y < layersArray.size(); y++) {
            var layer = (JsonArray) layersArray.get(y);
            for (int z = 0; z < layer.size(); z++) {
                var row = layer.get(z).getAsString();
                for (int x = 0; x < row.length(); x++) {
                    char key = row.charAt(x);

                    BlockStatePredicate predicate;
                    if (keys.containsKey(key)) {
                        predicate = keys.get(key);

                        if (key == '#') {
                            if (anchor != null) {
                                throw new JsonParseException("Anchor key '#' cannot be used twice within the same structure");
                            }

                            anchor = new Vec3i(x, y, z);
                        }
                    } else if (key == ' ') {
                        predicate = BlockStatePredicate.NULL_PREDICATE;
                    } else if (key == '_') {
                        predicate = BlockStatePredicate.AIR_PREDICATE;
                    } else {
                        throw new JsonParseException("Unknown key '" + key + "'");
                    }

                    result[x][y][z] = predicate;
                }
            }
        }

        return new StructureTemplate(resourceId, result, xSize, ySize, zSize, anchor);
    }
}
