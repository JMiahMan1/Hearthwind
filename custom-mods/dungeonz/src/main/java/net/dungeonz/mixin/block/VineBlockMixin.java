package net.dungeonz.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.init.ConfigInit;
import net.dungeonz.init.DimensionInit;
import net.minecraft.class_1269;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2541;
import net.minecraft.class_2680;
import net.minecraft.class_3218;
import net.minecraft.class_3965;
import net.minecraft.class_5819;

@Mixin(class_2541.class)
public abstract class VineBlockMixin extends class_2248 {

    public VineBlockMixin(class_2251 settings) {
        super(settings);
    }

    @Override
    protected class_1269 method_55766(class_2680 state, class_1937 world, class_2338 pos, class_1657 player, class_3965 hit) {
        if (ConfigInit.CONFIG.devMode && world.method_8320(pos).method_27852(class_2246.field_10597)) {
            if (!world.method_8608()) {
                if (world.method_22347(pos.method_10074())) {
                    world.method_8652(pos.method_10074(), (class_2680) this.method_9564().method_11657(class_2541.method_10828(hit.method_17780().method_10153()), true), class_2248.field_31028);
                }
            }
            return class_1269.method_29236(world.field_9236);
        }
        return super.method_55766(state, world, pos, player, hit);
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    public void randomTick(class_2680 state, class_3218 world, class_2338 pos, class_5819 random, CallbackInfo info) {
        if (world.method_27983() == DimensionInit.DUNGEON_WORLD || ConfigInit.CONFIG.devMode) {
            info.cancel();
        }
    }

}
