package folk.sisby.surveyor.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({BlockBehaviour.class})
public interface AccessAbstractBlock {
   @Accessor("hasCollision")
   boolean isCollidable();

   @Invoker("getCloneItemStack")
   ItemStack invokeGetPickStack(LevelReader var1, BlockPos var2, BlockState var3, boolean var4);
}
