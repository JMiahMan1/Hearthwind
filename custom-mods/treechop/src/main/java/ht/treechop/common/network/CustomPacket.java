package ht.treechop.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public interface CustomPacket extends CustomPacketPayload {
   Identifier getId();

   void encode(FriendlyByteBuf var1);
}
