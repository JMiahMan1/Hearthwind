package net.satisfy.brewery.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.brewery.core.registry.EntityTypeRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;

public class BeerMugBlockEntity extends BlockEntity {
    public static final String FLOWER_KEY = "flower";
    private Item flower;

    public BeerMugBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.BEER_MUG_BLOCK_ENTITY.get(), pos, state);
    }

    public Item getFlower() {
        return flower;
    }

    public void setFlower(Item flower) {
        this.flower = flower;
        setChanged();
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput output) {
        super.saveAdditional(output);
        writeFlower(output, flower);
    }

    @Override
    public void loadAdditional(net.minecraft.world.level.storage.ValueInput input) {
        super.loadAdditional(input);
        flower = readFlower(input);
    }

    public void writeFlower(net.minecraft.world.level.storage.ValueOutput output, Item flower) {
        output.store(FLOWER_KEY, ItemStack.OPTIONAL_CODEC, flower == null ? ItemStack.EMPTY : flower.getDefaultInstance());
    }

    public Item readFlower(net.minecraft.world.level.storage.ValueInput input) {
        ItemStack stack = input.read(FLOWER_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        return stack.isEmpty() ? null : stack.getItem();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public void setChanged() {
        if (level instanceof ServerLevel serverLevel) {
            Packet<ClientGamePacketListener> updatePacket = getUpdatePacket();

            for (ServerPlayer player : GeneralUtil.tracking(serverLevel, getBlockPos())) {
                player.connection.send(updatePacket);
            }
        }
        super.setChanged();
    }
}
