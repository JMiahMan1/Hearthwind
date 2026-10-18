package straywave.minecraft.immersivesnow.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import straywave.minecraft.immersivesnow.Queue;

@Mixin({ServerLevel.class})
public class ServerLevelMixin {
   @Inject(
      method = {"tickChunk"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
         shift = Shift.AFTER,
         ordinal = 0
      )}
   )
   private void snow$addToQueue(LevelChunk chunk, int randomTickSpeed, CallbackInfo ci) {
      if (chunk.getLevel().dimension().identifier().toString().equals("minecraft:overworld")) {
         Queue.tryAdd(chunk.getPos(), true);
      }
   }
}
