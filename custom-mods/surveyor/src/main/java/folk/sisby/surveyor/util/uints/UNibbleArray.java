package folk.sisby.surveyor.util.uints;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record UNibbleArray(byte[] value) implements ArrayUInts {
   public static final byte TYPE = 6;

   public static UInts ofInts(int[] value) {
      byte[] packed = new byte[value.length / 2 + (value.length & 1)];

      for (int i = 0; i < value.length; i += 2) {
         packed[i / 2] = (byte)(packed[i / 2] | (byte)(value[i] << 4));
      }

      for (int i = 1; i < value.length; i += 2) {
         packed[i / 2] = (byte)(packed[i / 2] | (byte)value[i]);
      }

      return new UNibbleArray(packed);
   }

   public static UInts fromBuf(FriendlyByteBuf buf) {
      return new UNibbleArray(buf.readByteArray());
   }

   @Override
   public int get(int i) {
      return ((i & 1) == 0 ? this.value[i / 2] >>> 4 : this.value[i / 2]) & 15;
   }

   @Override
   public void writeNbt(CompoundTag nbt, String key) {
      nbt.putByteArray(key, this.value);
   }

   @Override
   public void writeBuf(FriendlyByteBuf buf) {
      buf.writeByteArray(this.value);
   }

   @Override
   public int getType() {
      return 6;
   }
}
