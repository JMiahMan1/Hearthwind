package net.smitherz.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record GemComponent(List<net.minecraft.world.item.ItemStack> gems, int size) {
   public static final GemComponent DEFAULT = new GemComponent(new ArrayList<>(), 0);
   public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, GemComponent> PACKET_CODEC = StreamCodec.ofMember(
      (value, buf) -> {
         ItemStack.OPTIONAL_LIST_STREAM_CODEC.encode(buf, value.gems());
         buf.writeInt(value.size());
      }, buf -> new GemComponent((List<net.minecraft.world.item.ItemStack>)ItemStack.OPTIONAL_LIST_STREAM_CODEC.decode(buf), buf.readInt())
   );
   public static final Codec<GemComponent> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("gems").forGetter(GemComponent::gems), Codec.INT.fieldOf("size").forGetter(GemComponent::size)
         )
         .apply(instance, GemComponent::new)
   );
}
