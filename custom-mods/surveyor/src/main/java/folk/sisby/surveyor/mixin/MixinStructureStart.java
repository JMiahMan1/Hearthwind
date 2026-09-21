package folk.sisby.surveyor.mixin;

import folk.sisby.surveyor.structure.WorldStructures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureStart.class)
public abstract class MixinStructureStart {
	@Inject(method = "place", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/structure/Structure;postPlace(Lnet/minecraft/world/WorldGenLevel;Lnet/minecraft/world/gen/StructureManager;Lnet/minecraft/world/gen/chunk/ChunkGenerator;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BoundingBox;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/structure/StructurePiecesList;)V"))
	private void structureGenerated(WorldGenLevel serverWorldAccess, StructureManager structureAccessor, ChunkGenerator chunkGenerator, Random random, BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
		StructureStart self = (StructureStart) (Object) this;
		ServerLevel world = serverWorldAccess instanceof ServerLevel sw ? sw : ((WorldGenRegion) serverWorldAccess).world;
		WorldStructures.onStructurePlace(world, self);
	}
}
