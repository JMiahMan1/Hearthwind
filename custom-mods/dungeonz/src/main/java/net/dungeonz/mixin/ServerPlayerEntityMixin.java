package net.dungeonz.mixin;

import com.mojang.authlib.GameProfile;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.access.ServerPlayerAccess;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_5321;
import net.minecraft.class_7924;
import net.minecraft.server.MinecraftServer;

@Mixin(class_3222.class)
public abstract class ServerPlayerEntityMixin extends class_1657 implements ServerPlayerAccess {

    @Shadow
    @Mutable
    @Final
    public MinecraftServer server;

    @Unique
    private class_2338 dungeonPortalBlockPos = new class_2338(0, 0, 0);
    @Unique
    private class_2338 dungeonSpawnBlockPos = new class_2338(0, 0, 0);
    @Nullable
    @Unique
    private class_3218 oldWorld = null;

    public ServerPlayerEntityMixin(class_1937 world, class_2338 pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(class_2487 nbt, CallbackInfo info) {
        this.dungeonPortalBlockPos = new class_2338(nbt.method_10550("DungeonPortalBlockPosX"), nbt.method_10550("DungeonPortalBlockPosY"), nbt.method_10550("DungeonPortalBlockPosZ"));
        this.dungeonSpawnBlockPos = new class_2338(nbt.method_10550("DungeonSpawnBlockPosX"), nbt.method_10550("DungeonSpawnBlockPosY"), nbt.method_10550("DungeonSpawnBlockPosZ"));
        if (nbt.method_10545("DungeonRegistryKey")) {
            this.oldWorld = this.server.method_3847(class_5321.method_29179(class_7924.field_41223, class_2960.method_60654(nbt.method_10558("DungeonRegistryKey"))));
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(class_2487 nbt, CallbackInfo info) {
        nbt.method_10569("DungeonPortalBlockPosX", this.dungeonPortalBlockPos.method_10263());
        nbt.method_10569("DungeonPortalBlockPosY", this.dungeonPortalBlockPos.method_10264());
        nbt.method_10569("DungeonPortalBlockPosZ", this.dungeonPortalBlockPos.method_10260());

        nbt.method_10569("DungeonSpawnBlockPosX", this.dungeonSpawnBlockPos.method_10263());
        nbt.method_10569("DungeonSpawnBlockPosY", this.dungeonSpawnBlockPos.method_10264());
        nbt.method_10569("DungeonSpawnBlockPosZ", this.dungeonSpawnBlockPos.method_10260());

        if (this.oldWorld != null) {
            nbt.method_10582("DungeonRegistryKey", this.oldWorld.method_27983().method_29177().toString());
        }
    }

    @Override
    public void setDungeonInfo(class_3218 world, class_2338 portalPos, class_2338 playerPos) {
        this.dungeonPortalBlockPos = new class_2338(portalPos);
        this.dungeonSpawnBlockPos = new class_2338(playerPos);
        this.oldWorld = world;
    }

    @Nullable
    @Override
    public class_3218 getOldServerWorld() {
        return this.oldWorld;
    }

    @Override
    public class_2338 getDungeonPortalBlockPos() {
        return this.dungeonPortalBlockPos;
    }

    @Override
    public class_2338 getDungeonSpawnBlockPos() {
        return this.dungeonSpawnBlockPos;
    }

}
