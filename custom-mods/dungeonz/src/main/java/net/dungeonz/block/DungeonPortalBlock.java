package net.dungeonz.block;

import java.util.Iterator;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.dungeonz.DungeonzMain;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.util.DungeonHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.entity.BlockEntityTicker;

public class DungeonPortalBlock extends BaseEntityBlock implements LiquidBlockContainer {

    public static final MapCodec<DungeonPortalBlock> CODEC = DungeonPortalBlock.simpleCodec(DungeonPortalBlock::new);

    public DungeonPortalBlock(Properties settings) {
        super(settings);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DungeonPortalEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        // 26.x: ENTITYBLOCK_ANIMATED is gone; vanilla EndPortalBlock is INVISIBLE with a BER.
        return RenderShape.INVISIBLE;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.level().getBlockEntity(pos) != null && player.level().getBlockEntity(pos) instanceof DungeonPortalEntity dungeonPortalEntity) {
            if (isOtherDungeonPortalBlockNearby(world, pos)) {
                dungeonPortalEntity = getMainDungeonPortalEntity(world, pos);
                pos = getMainDungeonPortalBlockPos(world, pos);
            }
            if (player.canUseGameMasterBlocks() && (dungeonPortalEntity.getDungeon() == null || player.isShiftKeyDown())) {
                if (!world.isClientSide()) {
                    DungeonServerPacket.writeS2COpenOpScreenPacket((ServerPlayer) player, dungeonPortalEntity, null);
                }
                return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            } else if (dungeonPortalEntity.getDungeon() != null) {
                if (!world.isClientSide()) {
                    player.openMenu(state.getMenuProvider(world, pos));
                }
                return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
            }
        }
        return super.useWithoutItem(state, world, pos, player, hit);
    }

    @Override
    public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
        if (!world.isClientSide() && !entity.isPassenger() && !entity.isVehicle() && entity.canUsePortal(false) && entity instanceof ServerPlayer) {
            if (!entity.isOnPortalCooldown()) {
                if (isOtherDungeonPortalBlockNearby(world, pos)) {
                    pos = getMainDungeonPortalBlockPos(world, pos);
                }
                DungeonHelper.teleportDungeon((ServerPlayer) entity, pos, entity.getUUID());
                entity.setPortalCooldown();
            }
        }
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return DungeonGateBlock.createTickerHelper(type, BlockInit.DUNGEON_PORTAL_ENTITY, world.isClientSide() ? DungeonPortalEntity::clientTick : DungeonPortalEntity::serverTick);
    }

    public static boolean isOtherDungeonPortalBlockNearby(Level world, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.withinManhattan(pos, 1, 1, 1)) {
            if (checkPos.equals(pos)) {
                continue;
            }
            if (world.getBlockState(checkPos).is(BlockInit.DUNGEON_PORTAL)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static BlockPos getMainDungeonPortalBlockPos(Level world, BlockPos pos) {
        BlockPos checkPos = new BlockPos(pos);
        for (int i = 1; i < 30; i++) {
            if (world.getBlockState(checkPos.east(1)).is(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.east(1);
            } else {
                break;
            }
        }
        for (int i = 1; i < 30; i++) {
            if (world.getBlockState(checkPos.south(1)).is(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.south(1);
            } else {
                break;
            }
        }
        for (int i = 1; i < 30; i++) {
            if (world.getBlockState(checkPos.below(1)).is(BlockInit.DUNGEON_PORTAL)) {
                checkPos = checkPos.below(1);
            } else {
                break;
            }
        }
        return world.getBlockEntity(checkPos) instanceof DungeonPortalEntity dungeonPortalEntity ? dungeonPortalEntity.getBlockPos() : null;
    }

    @Nullable
    public static DungeonPortalEntity getMainDungeonPortalEntity(Level world, BlockPos pos) {
        if (getMainDungeonPortalBlockPos(world, pos) != null) {
            return (DungeonPortalEntity) world.getBlockEntity(getMainDungeonPortalBlockPos(world, pos));
        }
        return null;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // Used for not getting removed by water
    @Override
    public boolean canPlaceLiquid(@Nullable LivingEntity player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
        return false;
    }
}
