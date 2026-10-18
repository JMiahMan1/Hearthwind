package net.smitherz.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.smitherz.SmitherzMain;
import net.smitherz.init.ConfigInit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class UpgradeLoader implements SimpleSynchronousResourceReloadListener {
   private static final Logger LOGGER = LogManager.getLogger("SmitherZ");

   public Identifier getFabricId() {
      return Identifier.fromNamespaceAndPath("smitherz", "upgrade_loader");
   }

   public void onResourceManagerReload(ResourceManager resourceManager) {
      SmitherzMain.upgradeSlotMap.clear();
      resourceManager.listResources("gem_upgrades", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
         try {
            InputStream stream = resourceRef.open();
            JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

            for (int i = 0; i <= ConfigInit.CONFIG.maxGemSlots; i++) {
               if (data.has(String.valueOf(i))) {
                  JsonArray data2 = data.getAsJsonArray(String.valueOf(i));

                  for (int u = 0; u < data2.size(); u++) {
                     SmitherzMain.upgradeSlotMap.put(data2.get(u).getAsString(), i);
                  }
               }
            }
         } catch (Exception var7) {
            LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), var7.toString());
         }
      });
      SmitherzMain.gemDropMap.clear();
      SmitherzMain.gemRpgDropMap.clear();
      if (ConfigInit.CONFIG.mobsCanDropGems) {
         resourceManager.listResources("gem_drops", id -> id.getPath().endsWith(".json"))
            .forEach(
               (id, resourceRef) -> {
                  try {
                     InputStream stream = resourceRef.open();
                     JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                     for (String s : data.keySet()) {
                        JsonObject jsonObject = data.get(s).getAsJsonObject();
                        List<net.minecraft.world.entity.EntityType<?>> entityTypes = new ArrayList<>();

                        for (JsonElement element : jsonObject.getAsJsonArray("mob_types")) {
                           String mobType = element.getAsString();
                           if (((EntityType)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(mobType)))
                              .toString()
                              .equals("entity.minecraft.pig")) {
                              LOGGER.info("Resource {} was not loaded cause {} is not a valid entity identifier", id.toString(), mobType);
                              return;
                           }

                           entityTypes.add((EntityType)BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(mobType)));
                        }

                        for (net.minecraft.world.entity.EntityType<?> entityType : entityTypes) {
                           int rarityGroup = jsonObject.get("rarity_group").getAsInt();
                           float difficultyMultiplier = jsonObject.has("difficulty_multiplier") ? jsonObject.get("difficulty_multiplier").getAsFloat() : 0.0F;
                           Map<net.minecraft.world.item.Item, Float> itemChanceMap = new HashMap<>();
                           float chance = jsonObject.get("drop_chance").getAsFloat();

                           for (JsonElement jsonElement : jsonObject.getAsJsonArray("items")) {
                              String item = jsonElement.getAsString();
                              if (((Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(item))).toString().equals("air")) {
                                 LOGGER.info("{} is not a valid item identifier", item);
                              } else {
                                 itemChanceMap.put((Item)BuiltInRegistries.ITEM.getValue(Identifier.parse(item)), chance);
                              }
                           }

                           if (SmitherzMain.gemDropMap.containsKey(entityType)) {
                              if (SmitherzMain.gemDropMap.get(entityType).containsKey(rarityGroup)) {
                                 SmitherzMain.gemDropMap.get(entityType).get(rarityGroup).putAll(itemChanceMap);
                                 SmitherzMain.gemRpgDropMap.get(entityType).get(difficultyMultiplier).putAll(itemChanceMap);
                              } else {
                                 SmitherzMain.gemDropMap.get(entityType).put(rarityGroup, itemChanceMap);
                                 SmitherzMain.gemRpgDropMap.get(entityType).put(difficultyMultiplier, itemChanceMap);
                              }
                           } else {
                              LinkedHashMap<Integer, Map<net.minecraft.world.item.Item, Float>> gemDrops = new LinkedHashMap<>();
                              gemDrops.put(rarityGroup, itemChanceMap);
                              SmitherzMain.gemDropMap.put(entityType, gemDrops);
                              LinkedHashMap<Float, Map<net.minecraft.world.item.Item, Float>> gemRpgDrops = new LinkedHashMap<>();
                              gemRpgDrops.put(difficultyMultiplier, itemChanceMap);
                              SmitherzMain.gemRpgDropMap.put(entityType, gemRpgDrops);
                           }
                        }
                     }
                  } catch (Exception var17) {
                     LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), var17.toString());
                  }
               }
            );
      }
   }
}
