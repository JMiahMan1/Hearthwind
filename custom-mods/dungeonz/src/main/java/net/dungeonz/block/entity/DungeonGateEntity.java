package net.dungeonz.block.entity;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import org.jetbrains.annotations.Nullable;

import net.dungeonz.block.DungeonGateBlock;
import net.dungeonz.init.BlockInit;
import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.init.SoundInit;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.registries.BuiltInRegistries;

public class DungeonGateEntity extends BlockEntity {

    private static final List<Direction> directions = List.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN);
    private Identifier gateBlockId = Identifier.parse("minecraft:chiseled_stone_bricks");
    private String unlockItemId = "";
    private String gateParticleId = "minecraft:scrape";
    private List<Integer> dungeonEdgeList = new ArrayList<Integer>();

    public DungeonGateEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DUNGEON_GATE_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.gateBlockId = Identifier.parse(input.getStringOr("GateBlockId", ""));
        this.unlockItemId = input.getStringOr("UnlockItemId", "");
        this.gateParticleId = input.getStringOr("GateParticleId", "");

        if (input.getIntOr("DungeonEdgeSize", 0) > 0) {
            this.dungeonEdgeList.clear();
            for (int i = 0; i < input.getIntOr("DungeonEdgeSize", 0) / 3; i++) {
                this.dungeonEdgeList.add(input.getIntOr("DungeonEdgeX" + i, 0));
                this.dungeonEdgeList.add(input.getIntOr("DungeonEdgeY" + i, 0));
                this.dungeonEdgeList.add(input.getIntOr("DungeonEdgeZ" + i, 0));
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("GateBlockId", this.gateBlockId.toString());
        output.putString("UnlockItemId", this.unlockItemId.toString());
        output.putString("GateParticleId", this.gateParticleId.toString());

        output.putInt("DungeonEdgeSize", this.dungeonEdgeList.size());
        if (this.dungeonEdgeList.size() > 0) {
            for (int i = 0; i < this.dungeonEdgeList.size() / 3; i++) {
                output.putInt("DungeonEdgeX" + i, this.dungeonEdgeList.get(3 * i));
                output.putInt("DungeonEdgeY" + i, this.dungeonEdgeList.get(1 + 3 * i));
                output.putInt("DungeonEdgeZ" + i, this.dungeonEdgeList.get(2 + 3 * i));
            }
        }
    }

    public static void serverTick(Level world, BlockPos pos, BlockState state, DungeonGateEntity blockEntity) {
        if (world.getGameTime() % 20 == 0 && blockEntity.unlockItemId == null && blockEntity.getDungeonEdgeList().size() >= 6 && world.dimension() == DimensionInit.DUNGEON_WORLD
                && !ConfigInit.CONFIG.devMode) {
            if (!world.getBlockState(pos.below()).is(BlockInit.DUNGEON_GATE)) {
                if (world.getBlockState(pos.north()).is(BlockInit.DUNGEON_GATE) && !world.getBlockState(pos.south()).is(BlockInit.DUNGEON_GATE)) {
                    if (!blockEntity.areHostileEntitiesAlive()) {
                        blockEntity.unlockGate(pos);
                    }
                } else if (world.getBlockState(pos.east()).is(BlockInit.DUNGEON_GATE) && !world.getBlockState(pos.west()).is(BlockInit.DUNGEON_GATE)) {
                    if (blockEntity.areHostileEntitiesAlive()) {
                        blockEntity.unlockGate(pos);
                    }
                }
            }
        }
    }

    private boolean areHostileEntitiesAlive() {
        if (this.getDungeonEdgeList().size() < 6) {
            return false;
        }
        List<Monster> hostileEntities = level.getEntitiesOfClass(Monster.class, new AABB(this.getDungeonEdgeList().get(0), this.getDungeonEdgeList().get(1),
                this.getDungeonEdgeList().get(2), this.getDungeonEdgeList().get(3), this.getDungeonEdgeList().get(4), this.getDungeonEdgeList().get(5)), EntitySelector.NO_SPECTATORS);
        if (hostileEntities.isEmpty()) {
            return false;
        }
        return true;
    }

    public void unlockGate(BlockPos pos) {
        level.playSound(null, pos, SoundInit.DUNGEON_GATE_UNLOCK_EVENT, SoundSource.BLOCKS, 1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);

        List<BlockPos> dungeonGatesPosList = DungeonGateEntity.getConnectedDungeonGatePosList(level, pos);
        for (int i = 0; i < dungeonGatesPosList.size(); i++) {
            if (level.getBlockEntity(dungeonGatesPosList.get(i)) != null && level.getBlockEntity(dungeonGatesPosList.get(i)) instanceof DungeonGateEntity) {
                DungeonGateEntity otherDungeonGateEntity = (DungeonGateEntity) level.getBlockEntity(dungeonGatesPosList.get(i));

                level.setBlockAndUpdate(dungeonGatesPosList.get(i), otherDungeonGateEntity.getBlockState().cycle(DungeonGateBlock.ENABLED));
                otherDungeonGateEntity.setChanged();
            }
        }
    }

    public static List<BlockPos> getConnectedDungeonGatePosList(Level world, BlockPos pos) {
        List<BlockPos> dungeonGates = new ArrayList<BlockPos>();
        List<Integer> directionLengths = new ArrayList<Integer>();

        for (int i = 0; i < DungeonGateEntity.directions.size(); i++) {
            for (int u = 1; u < 100; u++) {
                if (!world.getBlockState(pos.relative(DungeonGateEntity.directions.get(i), u)).is(BlockInit.DUNGEON_GATE)) {
                    directionLengths.add(u - 1);
                    break;
                }
            }
        }
        for (int i = -directionLengths.get(5); i <= directionLengths.get(4); i++) {
            for (int u = -directionLengths.get(0); u <= directionLengths.get(2); u++) {
                for (int o = -directionLengths.get(1); o <= directionLengths.get(3); o++) {
                    BlockPos checkPos = pos.above(i).south(u).west(o);
                    if (world.getBlockState(checkPos).is(BlockInit.DUNGEON_GATE) && !dungeonGates.contains(checkPos)) {
                        dungeonGates.add(checkPos);
                    }
                }
            }
        }

        return dungeonGates;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(Provider registryLookup) {
        return this.saveWithoutMetadata(registryLookup);
    }

    public void setUnlockItemId(String unlockItemId) {
        this.unlockItemId = unlockItemId;
    }

    @Nullable
    public Item getUnlockItem() {
        if (this.unlockItemId.equals("")) {
            return null;
        }
        return BuiltInRegistries.ITEM.get(Identifier.parse(this.unlockItemId)).map(ref -> ref.value()).orElse(null);
    }

    public void setBlockId(Identifier gateBlockId) {
        this.gateBlockId = gateBlockId;
    }

    public BlockState getDisguiseBlockState() {
        return BuiltInRegistries.BLOCK.get(this.gateBlockId).map(ref -> ref.value().defaultBlockState()).orElse(Blocks.AIR.defaultBlockState());
    }

    public void setParticleEffectId(String gateParticleId) {
        this.gateParticleId = gateParticleId;
    }

    @Nullable
    public ParticleOptions getParticleEffect() {
        if (this.gateParticleId.equals("")) {
            return null;
        }
        try {
            // return (T)((ParticleEffect)type.getCodec().codec().parse(registryLookup.getOps(NbtOps.INSTANCE), nbtCompound).getOrThrow(INVALID_OPTIONS_EXCEPTION::create));
            // Registries.PARTICLE_TYPE.get(Identifier.of(this.gateParticleId.toString())).getCodec().codec();

            // return ParticleEffectArgumentType.readParameters(new StringReader(this.gateParticleId.toString()), Registries.PARTICLE_TYPE.getReadOnlyWrapper());
            // return ParticleEffectArgumentType.readParameters(new StringReader(this.gateParticleId.toString()),
            // RegistryWrapper.WrapperLookup.of(Registries.PARTICLE_TYPE.getReadOnlyWrapper().streamEntries()));

            return ParticleArgument.readParticle(new StringReader(this.gateParticleId.toString()),
                    HolderLookup.Provider.create(Stream.of(BuiltInRegistries.PARTICLE_TYPE)));
        } catch (CommandSyntaxException commandSyntaxException) {
        }
        return null;
    }

    public void addDungeonEdge(int edgeX, int edgeY, int edgeZ) {
        this.dungeonEdgeList.add(edgeX);
        this.dungeonEdgeList.add(edgeY);
        this.dungeonEdgeList.add(edgeZ);
    }

    public List<Integer> getDungeonEdgeList() {
        return this.dungeonEdgeList;
    }

}
