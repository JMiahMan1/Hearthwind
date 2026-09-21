package net.satisfy.candlelight.core.networking.handler;

import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.candlelight.core.block.TypewriterBlock;
import net.satisfy.candlelight.core.block.entity.TypewriterEntity;
import net.satisfy.candlelight.core.networking.packet.SyncTypewriterDataC2SPacket;
import net.satisfy.candlelight.core.registry.ObjectRegistry;

public class TypewriterDataC2SPacketHandler implements NetworkManager.NetworkReceiver<SyncTypewriterDataC2SPacket>{

    @Override
    public void receive(SyncTypewriterDataC2SPacket packet, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        CompoundTag nbt = packet.nbt();
        BlockPos pos = packet.pos();
        boolean sign = packet.sign();
        ItemStack note = sign ? ObjectRegistry.NOTE_PAPER_WRITTEN.get().getDefaultInstance() : ObjectRegistry.NOTE_PAPER_WRITEABLE.get().getDefaultInstance();
        context.queue(() -> {
            BlockEntity blockEntity = player.level().getBlockEntity(pos);
            if (blockEntity instanceof TypewriterEntity typeWriterEntity) {
                note.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
                typeWriterEntity.addPaper(note);
            }
            BlockState blockState = player.level().getBlockState(pos);
            if (sign) {
                player.level().setBlock(pos, blockState.setValue(TypewriterBlock.FULL, 2), 2);
                player.level().sendBlockUpdated(pos, blockState, blockState.setValue(TypewriterBlock.FULL, 2), Block.UPDATE_CLIENTS);
            }
        });
    }
}