package net.dungeonz.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.access.InGameHudAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_329;

@Environment(EnvType.CLIENT)
@Mixin(class_329.class)
public abstract class InGameHudMixin implements InGameHudAccess {

    private int dungeonCountdownTicks = 0;
    private int dungeonCountdownRemainingTicks = 0;

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickMixin(CallbackInfo info) {
        if (this.dungeonCountdownRemainingTicks > 0) {
            this.dungeonCountdownRemainingTicks--;
        }
    }

    @Override
    public void setDungeonCountdownTicks(int dungeonCountdownTicks) {
        this.dungeonCountdownTicks = dungeonCountdownTicks;
        this.dungeonCountdownRemainingTicks = 18;
    }

    @Override
    public int getDungeonCountdownTicks() {
        return this.dungeonCountdownTicks;
    }

    @Override
    public int getDungeonCountdownRemainingTicks() {
        return this.dungeonCountdownRemainingTicks;
    }

}
