package snownee.passablefoliage.mixin;

import java.util.Arrays;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.state.BlockState;
import snownee.passablefoliage.PassableFoliage;

/**
 * 26.2 note: {@code Cache} itself went {@code private} (and
 * {@code largeCollisionShape}/{@code isCollisionShapeFullBlock} from
 * {@code protected} to {@code public final}, {@code faceSturdy} to
 * {@code private final}), so the target is named by string and the shadows
 * match 26.2 visibility exactly. Runtime access still comes from the
 * access widener, as upstream.
 */
@Mixin(targets = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase$Cache")
public class BlockStateCacheMixin {

	@Final
	@Mutable
	@Shadow
	public boolean largeCollisionShape;
	@Final
	@Mutable
	@Shadow
	public boolean isCollisionShapeFullBlock;
	@Final
	@Mutable
	@Shadow
	private boolean[] faceSturdy;

	@Inject(at = @At(value = "RETURN"), method = "<init>")
	private void pfoliage_modifyCollisionShape(BlockState state, CallbackInfo info) {
		if (PassableFoliage.isPassable(state)) {
			largeCollisionShape = false;
			Arrays.fill(faceSturdy, false);
			isCollisionShapeFullBlock = false;
		}
	}

}
