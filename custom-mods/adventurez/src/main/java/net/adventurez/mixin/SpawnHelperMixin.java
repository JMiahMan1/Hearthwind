package net.adventurez.mixin;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.adventurez.init.ConfigInit;
import net.adventurez.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;

@Mixin(NaturalSpawner.class)
public class SpawnHelperMixin {

    private static final WeightedList<MobSpawnSettings.SpawnerData> ADDED_SPAWNS;

    @Inject(method = "mobsAt", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/levelgen/structure/structures/NetherFortressStructure;FORTRESS_ENEMIES:Lnet/minecraft/util/random/WeightedList;"), cancellable = true)
    private static void getSpawnEntriesMixin(ServerLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, MobCategory spawnGroup, BlockPos pos,
            @Nullable Holder<Biome> biomeEntry, CallbackInfoReturnable<WeightedList<MobSpawnSettings.SpawnerData>> info) {
        List<Weighted<MobSpawnSettings.SpawnerData>> spawnersList = new ArrayList<>(NetherFortressStructure.FORTRESS_ENEMIES.unwrap());
        spawnersList.addAll(ADDED_SPAWNS.unwrap());
        info.setReturnValue(WeightedList.of(spawnersList));
    }

    static {
        ADDED_SPAWNS = WeightedList.of(
                new Weighted<>(new MobSpawnSettings.SpawnerData(EntityInit.NECROMANCER, 1, 1), ConfigInit.CONFIG.necromancer_spawn_weight),
                new Weighted<>(new MobSpawnSettings.SpawnerData(EntityInit.BLAZE_GUARDIAN, 1, 1), ConfigInit.CONFIG.blaze_guardian_spawn_weight));
    }
}
