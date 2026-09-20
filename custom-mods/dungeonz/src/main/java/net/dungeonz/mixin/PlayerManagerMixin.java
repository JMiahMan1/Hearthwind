package net.dungeonz.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;

import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.server.network.CommonListenerCookie;

@Mixin(PlayerList.class)
public class PlayerManagerMixin {

    @Inject(method = "placeNewPlayer", at = @At("TAIL"))
    private void onPlayerConnectMixin(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo info) {
        if (player.level().dimension() == DimensionInit.DUNGEON_WORLD) {
            if (DungeonHelper.getCurrentDungeon(player) != null && DungeonHelper.getDungeonPortalEntity(player).getDungeonPlayerUuids().contains(player.getUUID())
                    && !DungeonHelper.getDungeonPortalEntity(player).isOnCooldown((int) player.level().getGameTime())) {
                Dungeon dungeon = DungeonHelper.getCurrentDungeon(player);
                DungeonServerPacket.writeS2CDungeonInfoPacket(player, dungeon.getBreakableBlockIdList(), dungeon.getplaceableBlockIdList(), dungeon.isElytraAllowed());
            } else {
                DungeonHelper.teleportOutOfDungeon(player);
            }
        }
    }

    @Redirect(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addRespawnedPlayer(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void respawnPlayerMixin(ServerLevel serverWorld, ServerPlayer serverPlayerEntity, ServerPlayer oldPlayer, boolean alive, Entity.RemovalReason removalReason) {
        if (!alive && oldPlayer.level().dimension() == DimensionInit.DUNGEON_WORLD && DungeonHelper.getDungeonPortalEntity(oldPlayer) != null) {
            DungeonPortalEntity dungeonPortalEntity = DungeonHelper.getDungeonPortalEntity(oldPlayer);
            if (!dungeonPortalEntity.getDungeon().isRespawnAllowed()) {
                dungeonPortalEntity.getDungeonPlayerUuids().remove(oldPlayer.getUUID());
                dungeonPortalEntity.addDeadDungeonPlayerUuids(serverPlayerEntity.getUUID());
                if (dungeonPortalEntity.getDungeonPlayerCount() == 0) {
                    dungeonPortalEntity.setCooldownTime(dungeonPortalEntity.getDungeon().getCooldown() + (int) serverWorld.getGameTime());
                }
                dungeonPortalEntity.setChanged();
            }
        }
        serverWorld.addRespawnedPlayer(serverPlayerEntity);
    }

    @ModifyVariable(method = "respawn", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;", ordinal = 0), ordinal = 0)
    private TeleportTransition respawnPlayerMixin(TeleportTransition original, ServerPlayer oldPlayer, boolean alive, Entity.RemovalReason removalReason) {
        if (!alive && oldPlayer.level().dimension() == DimensionInit.DUNGEON_WORLD && DungeonHelper.getDungeonPortalEntity(oldPlayer) != null
                && DungeonHelper.getDungeonPortalEntity(oldPlayer).getDungeon().isRespawnAllowed()) {
            BlockPos pos = DungeonHelper.getDungeonPortalEntity(oldPlayer).getBlockPos();
            return new TeleportTransition(oldPlayer.level(), new Vec3(pos.getX() * 16, 100, pos.getZ() * 16), Vec3.ZERO, oldPlayer.getYRot(), 0.0f, TeleportTransition.DO_NOTHING);
        }
        return original;
    }

}
