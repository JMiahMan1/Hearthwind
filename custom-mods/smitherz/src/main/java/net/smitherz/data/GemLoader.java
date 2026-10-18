package net.smitherz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item.Properties;
import net.smitherz.init.ItemInit;
import net.smitherz.item.Gem;

public class GemLoader {
   public static void readGemFile(File file) {
      try (FileReader fileReader = new FileReader(file)) {
         JsonObject data = JsonParser.parseReader(fileReader).getAsJsonObject();

         for (Entry<String, JsonElement> entry : data.entrySet()) {
            JsonObject jsonObject = (JsonObject)entry.getValue();
            Map<net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute>, net.minecraft.world.entity.ai.attributes.AttributeModifier> map = new HashMap<>();
            JsonArray jsonArray = jsonObject.get("attributes").getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
               JsonObject attributejsonObject = jsonArray.get(i).getAsJsonObject();
               map.put(
                  (Holder)BuiltInRegistries.ATTRIBUTE.get(Identifier.parse(attributejsonObject.get("type").getAsString())).orElse(null),
                  new AttributeModifier(
                     Identifier.parse(attributejsonObject.get("type").getAsString()),
                     attributejsonObject.get("modifier").getAsJsonObject().get("amount").getAsFloat(),
                     Operation.valueOf(attributejsonObject.get("modifier").getAsJsonObject().get("operation").getAsString().toUpperCase())
                  )
               );
            }

            ItemInit.register(
               entry.getKey(),
               new Gem(
                  new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", entry.getKey()))),
                  map,
                  jsonObject.get("link_chance").getAsFloat(),
                  jsonObject.get("break_chance").getAsFloat(),
                  jsonObject.get("unlink_chance").getAsFloat(),
                  jsonObject.has("tag") && jsonObject.get("tag") != null
                     ? TagKey.create(Registries.ITEM, Identifier.parse(jsonObject.get("tag").getAsString()))
                     : null
               )
            );
         }
      } catch (IOException var12) {
         var12.printStackTrace();
      }
   }
}
