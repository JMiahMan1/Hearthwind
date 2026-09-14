package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.At;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.class_1297;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2535;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_3324;
import net.minecraft.class_5454;
import net.minecraft.class_8792;

@Mixin(class_3324.class)
public class PlayerManagerMixin {

    @Inject(method = "onPlayerConnect", at = @At("TAIL"))
    private void onPlayerConnectMixin(class_2535 connection, class_3222 player, class_8792 clientData, CallbackInfo info) {
        if (player.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD) {
            if (DungeonHelper.getCurrentDungeon(player) != null && DungeonHelper.getDungeonPortalEntity(player).getDungeonPlayerUuids().contains(player.method_5667())
                    && !DungeonHelper.getDungeonPortalEntity(player).isOnCooldown((int) player.method_37908().method_8510())) {
                Dungeon dungeon = DungeonHelper.getCurrentDungeon(player);
                DungeonServerPacket.writeS2CDungeonInfoPacket(player, dungeon.getBreakableBlockIdList(), dungeon.getplaceableBlockIdList(), dungeon.isElytraAllowed());
            } else {
                DungeonHelper.teleportOutOfDungeon(player);
            }
        }
    }

    @Inject(method = "respawnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;onPlayerRespawned(Lnet/minecraft/server/network/ServerPlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void respawnPlayerMixin(class_3222 oldPlayer, boolean alive, class_1297.class_5529 removalReason, CallbackInfoReturnable<class_3222> info, class_5454 teleportTarget,
            class_3218 serverWorld, class_3222 serverPlayerEntity) {
        if (!alive && oldPlayer.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD && DungeonHelper.getDungeonPortalEntity(oldPlayer) != null) {
            DungeonPortalEntity dungeonPortalEntity = DungeonHelper.getDungeonPortalEntity(oldPlayer);
            if (!dungeonPortalEntity.getDungeon().isRespawnAllowed()) {
                dungeonPortalEntity.getDungeonPlayerUuids().remove(oldPlayer.method_5667());
                dungeonPortalEntity.addDeadDungeonPlayerUuids(serverPlayerEntity.method_5667());
                if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                    dungeonPortalEntity.setCooldownTime(dungeonPortalEntity.getDungeon().getCooldown() + (int) serverWorld.method_8510());
                }
                dungeonPortalEntity.method_5431();
            }
        }
    }

    @ModifyVariable(method = "respawnPlayer", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getRespawnTarget(ZLnet/minecraft/world/TeleportTarget$PostDimensionTransition;)Lnet/minecraft/world/TeleportTarget;", ordinal = 0), ordinal = 0)
    private class_5454 respawnPlayerMixin(class_5454 original, class_3222 oldPlayer, boolean alive, class_1297.class_5529 removalReason) {
        if (!alive && oldPlayer.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD && DungeonHelper.getDungeonPortalEntity(oldPlayer) != null
                && DungeonHelper.getDungeonPortalEntity(oldPlayer).getDungeon().isRespawnAllowed()) {
            class_2338 pos = DungeonHelper.getDungeonPortalEntity(oldPlayer).method_11016();
            return new class_5454(oldPlayer.method_51469(), new class_243(pos.method_10263() * 16, 100, pos.method_10260() * 16), class_243.field_1353, oldPlayer.method_36454(), 0.0f, alive, class_5454.field_52245);
        }
        return original;
    }

}
