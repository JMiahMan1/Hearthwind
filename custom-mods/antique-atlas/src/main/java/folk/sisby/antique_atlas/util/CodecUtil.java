package folk.sisby.antique_atlas.util;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class CodecUtil {
   public static <T> Codec<Set<T>> set(Codec<T> codec) {
      return codec.listOf().xmap(HashSet::new, ArrayList::new);
   }

   public static <T extends Enum<T>> Codec<T> ofEnum(Class<T> enumClass) {
      return Codec.STRING.flatXmap(id -> {
         try {
            return DataResult.success(Enum.valueOf(enumClass, id.toUpperCase(Locale.ROOT)));
         } catch (Exception var3) {
            return DataResult.error(() -> "Unknown type: " + id);
         }
      }, value -> DataResult.success(value.name()));
   }

   public record CodecResourceMetadataSerializer<T>(Codec<T> codec, Identifier id)
      {
      @NotNull
      public String getMetadataSectionName() {
         return this.id.toString();
      }

      @NotNull
      public T fromJson(JsonObject json) {
         DataResult<T> result = this.codec.parse(JsonOps.INSTANCE, json);
         if (result.error().isPresent()) {
            throw new IllegalStateException("Failed to parse " + this.id + " metadata section: " + result.error().get());
         } else if (result.result().isEmpty()) {
            throw new IllegalStateException("Failed to parse " + this.id + " metadata section: Empty result");
         } else {
            return (T)result.result().get();
         }
      }
   }
}
