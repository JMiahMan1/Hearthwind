package snownee.passablefoliage.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 26.2 note: {@code BlockBehaviour.isCollisionShapeFullBlock} went
 * {@code protected} (it was publicly reachable on 26.1, which is how
 * upstream called it directly). The access widener opens it at runtime;
 * this invoker makes it reachable at compile time.
 */
@Mixin(BlockBehaviour.class)
public interface BlockBehaviourAccessor {
	@Invoker("isCollisionShapeFullBlock")
	boolean pfoliage$invokeIsCollisionShapeFullBlock(BlockState state, BlockGetter getter, BlockPos pos);
}
