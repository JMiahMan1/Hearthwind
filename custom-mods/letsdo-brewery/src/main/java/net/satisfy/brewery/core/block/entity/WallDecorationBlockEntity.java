package net.satisfy.brewery.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.brewery.core.registry.EntityTypeRegistry;

public class WallDecorationBlockEntity extends BlockEntity {
    private final Component[] text = new Component[]{Component.literal(""), Component.literal(""), Component.literal("")};
    private boolean glowing = false;

    public WallDecorationBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.WALL_DECORATION.get(), pos, state);
    }

    public Component getText(int line) {
        return text[line];
    }

    public void setText(int line, Component component) {
        this.text[line] = component;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public boolean isGlowing() {
        return glowing;
    }

    public void setGlowing(boolean glowing) {
        this.glowing = glowing;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        glowing = input.getBooleanOr("Glowing", false);
        for (int i = 0; i < 3; i++) {
            text[i] = input.read("Text" + i, net.minecraft.network.chat.ComponentSerialization.CODEC).orElse(Component.literal(""));
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Glowing", glowing);
        for (int i = 0; i < 3; i++) {
            output.store("Text" + i, net.minecraft.network.chat.ComponentSerialization.CODEC, text[i]);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
