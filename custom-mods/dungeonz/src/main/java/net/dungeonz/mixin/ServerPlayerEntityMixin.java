package net.dungeonz.mixin;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import com.mojang.authlib.GameProfile;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.access.ServerPlayerAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements ServerPlayerAccess {

    @Unique
    private BlockPos dungeonPortalBlockPos = new BlockPos(0, 0, 0);
    @Unique
    private BlockPos dungeonSpawnBlockPos = new BlockPos(0, 0, 0);
    @Nullable
    @Unique
    private ServerLevel oldWorld = null;

    public ServerPlayerEntityMixin(Level world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(ValueInput input, CallbackInfo info) {
        this.dungeonPortalBlockPos = new BlockPos(input.getIntOr("DungeonPortalBlockPosX", 0), input.getIntOr("DungeonPortalBlockPosY", 0), input.getIntOr("DungeonPortalBlockPosZ", 0));
        this.dungeonSpawnBlockPos = new BlockPos(input.getIntOr("DungeonSpawnBlockPosX", 0), input.getIntOr("DungeonSpawnBlockPosY", 0), input.getIntOr("DungeonSpawnBlockPosZ", 0));
        java.util.Optional<String> dungeonKey = input.getString("DungeonRegistryKey");
        if (dungeonKey.isPresent()) {
            this.oldWorld = this.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(dungeonKey.get())));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(ValueOutput output, CallbackInfo info) {
        output.putInt("DungeonPortalBlockPosX", this.dungeonPortalBlockPos.getX());
        output.putInt("DungeonPortalBlockPosY", this.dungeonPortalBlockPos.getY());
        output.putInt("DungeonPortalBlockPosZ", this.dungeonPortalBlockPos.getZ());

        output.putInt("DungeonSpawnBlockPosX", this.dungeonSpawnBlockPos.getX());
        output.putInt("DungeonSpawnBlockPosY", this.dungeonSpawnBlockPos.getY());
        output.putInt("DungeonSpawnBlockPosZ", this.dungeonSpawnBlockPos.getZ());

        if (this.oldWorld != null) {
            output.putString("DungeonRegistryKey", this.oldWorld.dimension().identifier().toString());
        }
    }

    @Override
    public void setDungeonInfo(ServerLevel world, BlockPos portalPos, BlockPos playerPos) {
        this.dungeonPortalBlockPos = new BlockPos(portalPos);
        this.dungeonSpawnBlockPos = new BlockPos(playerPos);
        this.oldWorld = world;
    }

    @Nullable
    @Override
    public ServerLevel getOldServerWorld() {
        return this.oldWorld;
    }

    @Override
    public BlockPos getDungeonPortalBlockPos() {
        return this.dungeonPortalBlockPos;
    }

    @Override
    public BlockPos getDungeonSpawnBlockPos() {
        return this.dungeonSpawnBlockPos;
    }

}
