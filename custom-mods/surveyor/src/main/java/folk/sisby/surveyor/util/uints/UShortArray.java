package folk.sisby.surveyor.util.uints;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record UShortArray(short[] value) implements ArrayUInts {
   public static final int TYPE = 8;

   public static UInts ofInts(int[] ints) {
      short[] value = new short[ints.length];

      for (int i = 0; i < ints.length; i++) {
         value[i] = (short)ints[i];
      }

      return new UShortArray(value);
   }

   public static UInts ofPacked(int[] ints, int cardinality) {
      short[] value = new short[cardinality];

      for (int i = 0; i < value.length; i += 2) {
         value[i] = (short)(ints[i / 2] >>> 16);
      }

      for (int i = 1; i < value.length; i += 2) {
         value[i] = (short)(ints[i / 2] & 65535);
      }

      return new UShortArray(value);
   }

   public static UInts fromBuf(FriendlyByteBuf buf, int cardinality) {
      return ofPacked(buf.readVarIntArray(), cardinality);
   }

   public int[] packToInts() {
      int[] packed = new int[this.value.length / 2 + (this.value.length & 1)];

      for (int i = 0; i < this.value.length; i += 2) {
         packed[i / 2] = packed[i / 2] | this.value[i] << 16;
      }

      for (int i = 1; i < this.value.length; i += 2) {
         packed[i / 2] = packed[i / 2] | this.value[i];
      }

      return packed;
   }

   @Override
   public int get(int i) {
      return this.value[i] & 65535;
   }

   @Override
   public void writeNbt(CompoundTag nbt, String key) {
      nbt.putIntArray(key, this.packToInts());
   }

   @Override
   public void writeBuf(FriendlyByteBuf buf) {
      buf.writeVarIntArray(this.packToInts());
   }

   @Override
   public int getType() {
      return 8;
   }
}
