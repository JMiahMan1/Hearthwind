package folk.sisby.surveyor.util.uints;

import folk.sisby.surveyor.util.ArrayUtil;
import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public interface UInts {
   byte NULL_TYPE = 0;
   int MAX_SHORT = 65535;
   int MAX_BYTE = 255;
   int MAX_NIBBLE = 15;
   int NIBBLE_SIZE = 4;
   int SHORT_MASK = 65535;
   int BYTE_MASK = 255;
   int NIBBLE_MASK = 15;

   static UInts remap(UInts input, Function<Integer, Integer> remapping, int defaultValue, int cardinality) {
      return ((UInts)(input == null ? new UInt(defaultValue) : input)).remap(remapping, defaultValue, cardinality);
   }

   static void writeBuf(UInts array, FriendlyByteBuf buf) {
      if (array == null) {
         buf.writeVarInt(0);
      } else {
         buf.writeVarInt(array.getType());
         array.writeBuf(buf);
      }
   }

   static UInts readNbt(Tag nbt, int cardinality) {
      if (nbt == null) {
         return null;
      } else {
         return switch (nbt.getId()) {
            case 1 -> UByte.fromNbt(nbt);
            case 2 -> UShort.fromNbt(nbt);
            case 3 -> UInt.fromNbt(nbt);
            default -> throw new IllegalStateException("UIntArray encountered unexpected NBT type: " + nbt.getId());
            case 7 -> UByteArray.fromNbt(nbt, cardinality);
            case 11 -> UIntArray.fromNbt(nbt, cardinality);
         };
      }
   }

   static UInts readBuf(FriendlyByteBuf buf, int cardinality) {
      int type = buf.readVarInt();

      return switch (type) {
         case 0 -> null;
         case 1 -> UByte.fromBuf(buf);
         case 2 -> UShort.fromBuf(buf);
         case 3 -> UInt.fromBuf(buf);
         default -> throw new IllegalStateException("UIntArray encountered unexpected buf type: " + type);
         case 6 -> UNibbleArray.fromBuf(buf);
         case 7 -> UByteArray.fromBuf(buf);
         case 8 -> UShortArray.fromBuf(buf, cardinality);
         case 11 -> UIntArray.fromBuf(buf);
      };
   }

   static UInts fromUInts(int[] uints, int defaultValue) {
      return ArrayUtil.isSingle(uints) ? ofSingle(uints[0], defaultValue) : ofMany(uints);
   }

   static UInts ofMany(int[] uints) {
      int max = Arrays.stream(uints).max().orElseThrow();
      if (max <= 15) {
         return UNibbleArray.ofInts(uints);
      } else if (max <= 255) {
         return UByteArray.ofInts(uints);
      } else {
         return max <= 65535 ? UShortArray.ofInts(uints) : UIntArray.ofInts(uints);
      }
   }

   static UInts ofSingle(int uint, int defaultValue) {
      if (uint == defaultValue) {
         return null;
      } else if (uint <= 255) {
         return UByte.ofInt(uint);
      } else {
         return uint <= 65535 ? UShort.ofInt(uint) : UInt.ofInt(uint);
      }
   }

   int getType();

   int[] getUnmasked(BitSet var1);

   void writeNbt(CompoundTag var1, String var2);

   void writeBuf(FriendlyByteBuf var1);

   int get(int var1);

   UInts remap(Function<Integer, Integer> var1, int var2, int var3);
}
