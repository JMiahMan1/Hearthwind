package net.dungeonz.mixin;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.DimensionInit;
import net.minecraft.class_1937;
import net.minecraft.class_3218;
import net.minecraft.class_5321;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Shadow
    @Mutable
    @Final
    private Map<class_5321<class_1937>, class_3218> worlds;

    @Inject(method = "updateMobSpawnOptions", at = @At("TAIL"))
    private void updateMobSpawnOptionsMixin(CallbackInfo info) {
        if (worlds.get(DimensionInit.DUNGEON_WORLD) != null) {
            worlds.get(DimensionInit.DUNGEON_WORLD).method_8424(false, false);
        }
    }
}
