package folk.sisby.surveyor.util;

import java.util.Collection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class NbtUtil {
   public static void removeRecursive(CompoundTag nbt, Collection<String> keys) {
      keys.forEach(nbt::remove);

      for (String key : nbt.keySet()) {
         if (nbt.getCompound(key).isPresent()) {
            removeRecursive((CompoundTag)nbt.getCompound(key).get(), keys);
         } else if (nbt.getList(key).isPresent()) {
            for (Tag listNbt : (ListTag)nbt.getList(key).get()) {
               removeRecursive((CompoundTag)listNbt, keys);
            }
         }
      }
   }
}
