package folk.sisby.surveyor.util;

import folk.sisby.surveyor.Surveyor;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.world.level.chunk.ChunkAccess;

public class ChunkUtil {
   public static Integer airCount(ChunkAccess chunk) {
      return Arrays.stream(chunk.getSections()).mapToInt(s -> 4096 - s.nonEmptyBlockCount).sum();
   }

   public static Map<RegionPos, File> getRegionFiles(File folder, String prefix) {
      Map<RegionPos, File> files = new HashMap<>();

      for (File file : Objects.requireNonNullElse(folder.listFiles(), new File[0])) {
         String[] split = file.getName().split("\\.");
         if (split.length == 4 && split[0].equals(prefix) && split[3].equals("dat")) {
            try {
               files.put(new RegionPos(Integer.parseInt(split[1]), Integer.parseInt(split[2])), file);
            } catch (NumberFormatException var9) {
            }
         }
      }

      return files;
   }

   public static Map<RegionPos, CompoundTag> getRegionNbt(File folder, String prefix) {
      Map<RegionPos, File> regionFiles = getRegionFiles(folder, prefix);
      Map<RegionPos, CompoundTag> regions = new HashMap<>();

      for (RegionPos regionPos : regionFiles.keySet()) {
         CompoundTag regionCompound = null;

         try {
            regionCompound = NbtIo.readCompressed(regionFiles.get(regionPos).toPath(), NbtAccounter.unlimitedHeap());
         } catch (ReportedNbtException | IOException var8) {
            Surveyor.LOGGER.error("[Surveyor] Error loading region nbt file {}.", regionFiles.get(regionPos).getName(), var8);
         }

         if (regionCompound != null) {
            regions.put(regionPos, regionCompound);
         }
      }

      return regions;
   }
}
