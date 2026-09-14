package net.dungeonz.block.entity;

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
import net.minecraft.class_1301;
import net.minecraft.class_1588;
import net.minecraft.class_1792;
import net.minecraft.class_1937;
import net.minecraft.class_2223;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_2394;
import net.minecraft.class_2487;
import net.minecraft.class_2586;
import net.minecraft.class_2622;
import net.minecraft.class_2680;
import net.minecraft.class_2960;
import net.minecraft.class_3419;
import net.minecraft.class_7225;
import net.minecraft.class_7225.class_7874;
import net.minecraft.class_7923;

public class DungeonGateEntity extends class_2586 {

    private static final List<class_2350> directions = List.of(class_2350.field_11043, class_2350.field_11034, class_2350.field_11035, class_2350.field_11039, class_2350.field_11036, class_2350.field_11033);
    private class_2960 gateBlockId = class_2960.method_60654("minecraft:chiseled_stone_bricks");
    private String unlockItemId = "";
    private String gateParticleId = "minecraft:scrape";
    private List<Integer> dungeonEdgeList = new ArrayList<Integer>();

    public DungeonGateEntity(class_2338 pos, class_2680 state) {
        super(BlockInit.DUNGEON_GATE_ENTITY, pos, state);
    }

    @Override
    public void method_11014(class_2487 nbt, class_7225.class_7874 registryLookup) {
        super.method_11014(nbt, registryLookup);
        this.gateBlockId = class_2960.method_60654(nbt.method_10558("GateBlockId"));
        this.unlockItemId = nbt.method_10558("UnlockItemId");
        this.gateParticleId = nbt.method_10558("GateParticleId");

        if (nbt.method_10550("DungeonEdgeSize") > 0) {
            this.dungeonEdgeList.clear();
            for (int i = 0; i < nbt.method_10550("DungeonEdgeSize") / 3; i++) {
                this.dungeonEdgeList.add(nbt.method_10550("DungeonEdgeX" + i));
                this.dungeonEdgeList.add(nbt.method_10550("DungeonEdgeY" + i));
                this.dungeonEdgeList.add(nbt.method_10550("DungeonEdgeZ" + i));
            }
        }
    }

    @Override
    public void method_11007(class_2487 nbt, class_7225.class_7874 registryLookup) {
        super.method_11007(nbt, registryLookup);
        nbt.method_10582("GateBlockId", this.gateBlockId.toString());
        nbt.method_10582("UnlockItemId", this.unlockItemId.toString());
        nbt.method_10582("GateParticleId", this.gateParticleId.toString());

        nbt.method_10569("DungeonEdgeSize", this.dungeonEdgeList.size());
        if (this.dungeonEdgeList.size() > 0) {
            for (int i = 0; i < this.dungeonEdgeList.size() / 3; i++) {
                nbt.method_10569("DungeonEdgeX" + i, this.dungeonEdgeList.get(i + 3 * i));
                nbt.method_10569("DungeonEdgeY" + i, this.dungeonEdgeList.get(i + 1 + 3 * i));
                nbt.method_10569("DungeonEdgeZ" + i, this.dungeonEdgeList.get(i + 2 + 3 * i));
            }
        }
    }

    public static void serverTick(class_1937 world, class_2338 pos, class_2680 state, DungeonGateEntity blockEntity) {
        if (world.method_8510() % 20 == 0 && blockEntity.unlockItemId == null && blockEntity.getDungeonEdgeList().size() >= 6 && world.method_27983() == DimensionInit.DUNGEON_WORLD
                && !ConfigInit.CONFIG.devMode) {
            if (!world.method_8320(pos.method_10074()).method_27852(BlockInit.DUNGEON_GATE)) {
                if (world.method_8320(pos.method_10095()).method_27852(BlockInit.DUNGEON_GATE) && !world.method_8320(pos.method_10072()).method_27852(BlockInit.DUNGEON_GATE)) {
                    if (!blockEntity.areHostileEntitiesAlive()) {
                        blockEntity.unlockGate(pos);
                    }
                } else if (world.method_8320(pos.method_10078()).method_27852(BlockInit.DUNGEON_GATE) && !world.method_8320(pos.method_10067()).method_27852(BlockInit.DUNGEON_GATE)) {
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
        List<class_1588> hostileEntities = field_11863.method_8390(class_1588.class, new class_238(this.getDungeonEdgeList().get(0), this.getDungeonEdgeList().get(1),
                this.getDungeonEdgeList().get(2), this.getDungeonEdgeList().get(3), this.getDungeonEdgeList().get(4), this.getDungeonEdgeList().get(5)), class_1301.field_6155);
        if (hostileEntities.isEmpty()) {
            return false;
        }
        return true;
    }

    public void unlockGate(class_2338 pos) {
        field_11863.method_8396(null, pos, SoundInit.DUNGEON_GATE_UNLOCK_EVENT, class_3419.field_15245, 1.0f, 0.9f + field_11863.method_8409().method_43057() * 0.2f);

        List<class_2338> dungeonGatesPosList = DungeonGateEntity.getConnectedDungeonGatePosList(field_11863, pos);
        for (int i = 0; i < dungeonGatesPosList.size(); i++) {
            if (field_11863.method_8321(dungeonGatesPosList.get(i)) != null && field_11863.method_8321(dungeonGatesPosList.get(i)) instanceof DungeonGateEntity) {
                DungeonGateEntity otherDungeonGateEntity = (DungeonGateEntity) field_11863.method_8321(dungeonGatesPosList.get(i));

                field_11863.method_8501(dungeonGatesPosList.get(i), otherDungeonGateEntity.method_11010().method_28493(DungeonGateBlock.ENABLED));
                otherDungeonGateEntity.method_5431();
            }
        }
    }

    public static List<class_2338> getConnectedDungeonGatePosList(class_1937 world, class_2338 pos) {
        List<class_2338> dungeonGates = new ArrayList<class_2338>();
        List<Integer> directionLengths = new ArrayList<Integer>();

        for (int i = 0; i < DungeonGateEntity.directions.size(); i++) {
            for (int u = 1; u < 100; u++) {
                if (!world.method_8320(pos.method_10079(DungeonGateEntity.directions.get(i), u)).method_27852(BlockInit.DUNGEON_GATE)) {
                    directionLengths.add(u - 1);
                    break;
                }
            }
        }
        for (int i = -directionLengths.get(5); i <= directionLengths.get(4); i++) {
            for (int u = -directionLengths.get(0); u <= directionLengths.get(2); u++) {
                for (int o = -directionLengths.get(1); o <= directionLengths.get(3); o++) {
                    class_2338 checkPos = pos.method_10086(i).method_10077(u).method_10088(o);
                    if (world.method_8320(checkPos).method_27852(BlockInit.DUNGEON_GATE) && !dungeonGates.contains(checkPos)) {
                        dungeonGates.add(checkPos);
                    }
                }
            }
        }

        return dungeonGates;
    }

    @Override
    public class_2622 method_38235() {
        return class_2622.method_38585(this);
    }

    @Override
    public class_2487 method_16887(class_7874 registryLookup) {
        return this.method_38244(registryLookup);
    }

    public void setUnlockItemId(String unlockItemId) {
        this.unlockItemId = unlockItemId;
    }

    @Nullable
    public class_1792 getUnlockItem() {
        if (this.unlockItemId.equals("")) {
            return null;
        }
        return class_7923.field_41178.method_10223(class_2960.method_60654(this.unlockItemId));
    }

    public void setBlockId(class_2960 gateBlockId) {
        this.gateBlockId = gateBlockId;
    }

    public class_2680 getBlockState() {
        return class_7923.field_41175.method_10223(this.gateBlockId).method_9564();
    }

    public void setParticleEffectId(String gateParticleId) {
        this.gateParticleId = gateParticleId;
    }

    @Nullable
    public class_2394 getParticleEffect() {
        if (this.gateParticleId.equals("")) {
            return null;
        }
        try {
            // return (T)((ParticleEffect)type.getCodec().codec().parse(registryLookup.getOps(NbtOps.INSTANCE), nbtCompound).getOrThrow(INVALID_OPTIONS_EXCEPTION::create));
            // Registries.PARTICLE_TYPE.get(Identifier.of(this.gateParticleId.toString())).getCodec().codec();

            // return ParticleEffectArgumentType.readParameters(new StringReader(this.gateParticleId.toString()), Registries.PARTICLE_TYPE.getReadOnlyWrapper());
            // return ParticleEffectArgumentType.readParameters(new StringReader(this.gateParticleId.toString()),
            // RegistryWrapper.WrapperLookup.of(Registries.PARTICLE_TYPE.getReadOnlyWrapper().streamEntries()));

            return class_2223.method_9418(new StringReader(this.gateParticleId.toString()),
                    class_7225.class_7874.method_46761(Stream.of(class_7923.field_41180.method_46771())));
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
