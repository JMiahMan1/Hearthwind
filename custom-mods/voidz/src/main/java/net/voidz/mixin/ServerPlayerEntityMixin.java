package net.voidz.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.voidz.access.ServerPlayerAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ServerPlayer.class})
public abstract class ServerPlayerEntityMixin extends Player implements ServerPlayerAccess {
   @Unique
   private BlockPos voidPortingBlockPos = new BlockPos(0, 0, 0);

   public ServerPlayerEntityMixin(MinecraftServer server, ServerLevel level, GameProfile gameProfile, ClientInformation clientInformation) {
      super(level, gameProfile);
   }

   @Inject(method = {"readAdditionalSaveData"}, at = {@At("TAIL")})
   private void readAdditionalSaveDataMixin(ValueInput input, CallbackInfo info) {
      java.util.Optional<Integer> x = input.getInt("VoidPortingBlockPosX");
      if (x.isPresent()) {
         this.voidPortingBlockPos = new BlockPos(x.get(), input.getIntOr("VoidPortingBlockPosY", 0), input.getIntOr("VoidPortingBlockPosZ", 0));
      } else if (this.level() instanceof ServerLevel serverLevel) {
         this.voidPortingBlockPos = serverLevel.getRespawnData().pos();
      }
   }

   @Inject(method = {"addAdditionalSaveData"}, at = {@At("TAIL")})
   private void addAdditionalSaveDataMixin(ValueOutput output, CallbackInfo info) {
      output.putInt("VoidPortingBlockPosX", this.voidPortingBlockPos.getX());
      output.putInt("VoidPortingBlockPosY", this.voidPortingBlockPos.getY());
      output.putInt("VoidPortingBlockPosZ", this.voidPortingBlockPos.getZ());
   }

   @Override
   public void setVoidPortingBlockPos(BlockPos pos) {
      this.voidPortingBlockPos = pos;
   }

   @Override
   public BlockPos getVoidPortingBlockPos() {
      return this.voidPortingBlockPos;
   }
}
