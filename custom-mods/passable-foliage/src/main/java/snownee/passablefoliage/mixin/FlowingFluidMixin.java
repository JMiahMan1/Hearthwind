package snownee.passablefoliage.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import snownee.passablefoliage.PassableFoliage;

/**
 * 26.2 note: {@code canPassThroughWall} is now {@code private static} but
 * still live (called from four spread sites via {@code canPassThrough}); the
 * 6-arg descriptor is unchanged, so this wrap ports verbatim.
 */
@Mixin(value = FlowingFluid.class, priority = 1100)
public class FlowingFluidMixin {

	@WrapMethod(method = "canPassThroughWall")
	private static boolean pfoliage_canPassThroughWallPre(
			Direction direction,
			BlockGetter level,
			BlockPos sourcePos,
			BlockState sourceState,
			BlockPos targetPos,
			BlockState targetState,
			Operation<Boolean> ci) {
		PassableFoliage.setSuppressPassableCheck(true);
		boolean bl = ci.call(direction, level, sourcePos, sourceState, targetPos, targetState);
		PassableFoliage.setSuppressPassableCheck(false);
		return bl;
	}

}
