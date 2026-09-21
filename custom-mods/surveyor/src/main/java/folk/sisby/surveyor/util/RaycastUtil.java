package folk.sisby.surveyor.util;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;

public class RaycastUtil {
	public static HitResult playerViewRaycast(ServerPlayer player, int renderDistance) {
		// Calculate View Distance to Rendering Cylinder
		Vec3 cameraPos = player.getEyePosition();
		float pitch = player.getXRot();
		double phi = -pitch * (float) (Math.PI / 180.0);
		int blockRadius = renderDistance << 4;
		double y = blockRadius * Math.tan(phi);
		double bottom = player.level().getMinY() - cameraPos.y;
		double top = player.level().getMaxY() - cameraPos.y;
		double distance;
		if (y < bottom || y > top) { // Distance To Circular Planes
			distance = Math.abs(Mth.clamp(y, bottom, top) / Math.sin(phi));
		} else { // Distance To Curved Surface
			distance = Math.sqrt(y * y + blockRadius * blockRadius);
		}
		return raycast(player, cameraPos, distance);
	}

	private static HitResult raycast(ServerPlayer player, Vec3 cameraPos, double distance) {
		// Scale Cartesian Rotation Vector By Distance
		Vec3 cameraRotation = player.getLookAngle();
		Vec3 endPos = cameraPos.add(cameraRotation.x * distance, cameraRotation.y * distance, cameraRotation.z * distance);
		return BlockGetter.traverseBlocks(
			cameraPos,
			endPos,
			new ClipContext(
				cameraPos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player
			),
			(innerContext, pos) -> {
				LevelChunk chunk = player.level().getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()), ChunkStatus.FULL, false);
				if (chunk == null) {
					Vec3 vec3d = innerContext.getFrom().subtract(innerContext.getTo());
					return BlockHitResult.miss(Vec3.atCenterOf(pos), Direction.getApproximateNearest(vec3d.x, vec3d.y, vec3d.z), pos);
				}
				BlockState blockState = chunk.getBlockState(pos);
				FluidState fluidState = blockState.getFluidState();
				Vec3 vec3d = innerContext.getFrom();
				Vec3 vec3d2 = innerContext.getTo();
				VoxelShape voxelShape = innerContext.getBlockShape(blockState, player.level(), pos);
				BlockHitResult blockHitResult = player.level().clipWithInteractionOverride(vec3d, vec3d2, pos, voxelShape, blockState);
				VoxelShape voxelShape2 = innerContext.getFluidShape(fluidState, player.level(), pos);
				BlockHitResult blockHitResult2 = voxelShape2.clip(vec3d, vec3d2, pos);
				double d = blockHitResult == null ? Double.MAX_VALUE : innerContext.getFrom().distanceToSqr(blockHitResult.getLocation());
				double e = blockHitResult2 == null ? Double.MAX_VALUE : innerContext.getFrom().distanceToSqr(blockHitResult2.getLocation());
				return d <= e ? blockHitResult : blockHitResult2;
			}, innerContext -> {
				Vec3 vec3d = innerContext.getFrom().subtract(innerContext.getTo());
				return BlockHitResult.miss(innerContext.getTo(), Direction.getApproximateNearest(vec3d.x, vec3d.y, vec3d.z), BlockPos.containing(innerContext.getTo()));
			}
		);
	}
}
