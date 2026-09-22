package ca.fxco.memoryleakfix.mixin.biomeTemperatureLeak;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Supplier;

@Mixin(Biome.class)
public abstract class Biome_threadLocalMixin {

	private static ThreadLocal<Long2FloatLinkedOpenHashMap> memoryLeakFix$betterTempCache;

	@WrapOperation(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Ljava/lang/ThreadLocal;withInitial(Ljava/util/function/Supplier;)Ljava/lang/ThreadLocal;"
			)
	)
	private ThreadLocal<Long2FloatLinkedOpenHashMap> memoryLeakFix$useStaticThreadLocal(Supplier<?> supplier, Operation<ThreadLocal<Long2FloatLinkedOpenHashMap>> original) {
		if (memoryLeakFix$betterTempCache == null) {
			memoryLeakFix$betterTempCache = original.call(supplier);
		}
		return memoryLeakFix$betterTempCache;
	}
}
