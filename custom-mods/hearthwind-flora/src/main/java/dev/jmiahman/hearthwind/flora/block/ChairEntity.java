package dev.jmiahman.hearthwind.flora.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ChairEntity extends ArmorStand {
    public ChairEntity(Level level) {
        super(EntityTypes.ARMOR_STAND, level);
        this.setInvisible(true);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public static boolean sitPlayer(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) return true;
        ChairEntity chair = new ChairEntity(level);
        chair.setPos(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5);
        level.addFreshEntity(chair);
        player.startRiding(chair);
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && (this.getPassengers().isEmpty() || this.level().isEmptyBlock(this.blockPosition()))) {
            this.discard();
        }
    }
}
