package net.satisfy.brewery.core.event.brew_event;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.satisfy.brewery.core.block.entity.BrewstationBlockEntity;

import java.util.Set;

public abstract class BrewEvent {

    private int timeLeft;

    protected BrewEvent() {
        this(0);
    }

    protected BrewEvent(int time) {
        timeLeft = time;
    }

    public void tick(BrewstationBlockEntity entity) {
        onTick(entity);
        timeLeft--;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeForEvent(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public abstract CompoundTag save(CompoundTag compoundTag);

    public abstract void load(CompoundTag compoundTag);

    public void onTick(BrewstationBlockEntity entity) {

    }

    public abstract void start(Set<BlockPos> components, Level level);

    public abstract boolean isFinish(Set<BlockPos> components, Level level);

    public void finish(Set<BlockPos> components, Level level) {

    }
}
