package folk.sisby.euphonium.helper;

import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class DimensionHelper {
   private static final Logger LOGGER = LogManager.getLogger();

   public static boolean isOverworld(Level world) {
      return world.dimension() == Level.OVERWORLD;
   }

   public static boolean isNether(Level world) {
      return world.dimension() == Level.NETHER;
   }

   public static boolean isEnd(Level world) {
      return world.dimension() == Level.END;
   }

   public static boolean isDimension(Level world, Identifier dimension) {
      return getDimension(world).equals(dimension);
   }

   public static boolean isDimension(
      net.minecraft.world.level.Level world, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> key
   ) {
      return world.dimension().equals(key);
   }

   public static Identifier getDimension(Level world) {
      net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> key = world.dimension();
      return key.identifier();
   }

   @Nullable
   public static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> getDimension(net.minecraft.resources.Identifier dim) {
      if (Level.OVERWORLD.identifier().equals(dim)) {
         return Level.OVERWORLD;
      } else if (Level.NETHER.identifier().equals(dim)) {
         return Level.NETHER;
      } else {
         return Level.END.identifier().equals(dim) ? Level.END : null;
      }
   }

   public static void encodeDimension(
      net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> worldKey, Consumer<net.minecraft.nbt.Tag> consumer
   ) {
      DataResult<net.minecraft.nbt.Tag> result = Level.RESOURCE_KEY_CODEC.encodeStart(NbtOps.INSTANCE, worldKey);
      result.resultOrPartial(LOGGER::error).ifPresent(consumer);
   }

   public static Optional<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>> decodeDimension(net.minecraft.nbt.Tag nbt) {
      return Level.RESOURCE_KEY_CODEC.parse(NbtOps.INSTANCE, nbt).result();
   }
}
