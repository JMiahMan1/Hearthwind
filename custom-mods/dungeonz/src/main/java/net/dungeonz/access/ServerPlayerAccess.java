package net.dungeonz.access;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface ServerPlayerAccess {

    public void setDungeonInfo(ServerLevel world, BlockPos portalPos, BlockPos playerPos);

    @Nullable
    public ServerLevel getOldServerWorld();

    public BlockPos getDungeonPortalBlockPos();

    public BlockPos getDungeonSpawnBlockPos();
}
