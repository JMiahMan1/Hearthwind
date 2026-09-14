package net.dungeonz.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.dungeonz.DungeonzMain;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.init.ConfigInit;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.class_1299;
import net.minecraft.class_2487;
import net.minecraft.class_2522;
import net.minecraft.class_2960;
import net.minecraft.class_3300;
import net.minecraft.class_7923;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

public class DungeonLoader implements SimpleSynchronousResourceReloadListener {

    @Override
    public class_2960 getFabricId() {
        return class_2960.method_60655("dungeonz", "dungeon_loader");
    }

    @Override
    public void method_14491(class_3300 manager) {
        manager.method_14488("dungeon", id -> id.method_12832().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.method_14482();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                String dungeonTypeId = data.get("dungeon_type").getAsString();
                if (!ConfigInit.CONFIG.defaultDungeons && (dungeonTypeId.equals("dark_dungeon") || dungeonTypeId.equals("temple_dungeon"))) {
                    return;
                }
                int maxGroupSize = data.get("max_group_size").getAsInt();
                int minGroupSize = data.has("min_group_size") ? data.get("min_group_size").getAsInt() : 0;
                int requiredLevel = data.has("required_level") ? data.get("required_level").getAsInt() : 0;
                int cooldown = data.get("cooldown").getAsInt();
                boolean allowElytra = data.has("elytra") ? data.get("elytra").getAsBoolean() : false;
                boolean allowRespawn = data.has("respawn") ? data.get("respawn").getAsBoolean() : true;
                boolean keepInventory = data.has("keep_inventory") ? data.get("keep_inventory").getAsBoolean() : false;
                boolean allowEnderPearl = data.has("ender_pearl") ? data.get("ender_pearl").getAsBoolean() : false;
                boolean allowPositiveEffects = data.has("positive_effects") ? data.get("positive_effects").getAsBoolean() : false;
                class_2960 dungeonBackgroundId = data.has("background_texture") && !data.get("background_texture").getAsString().isEmpty() ? class_2960.method_60654(data.get("background_texture").getAsString()) : null;
                class_2960 dungeonStructurePoolId = class_2960.method_60654(data.get("dungeon_structure_pool_id").getAsString());

                List<String> difficulties = new ArrayList<String>();
                // Use LinkedHashMap to preserve insertion order from JSON
                Gson gson = new Gson();
                Type type = new TypeToken<LinkedHashMap<String, JsonObject>>(){}.getType();
                LinkedHashMap<String, JsonObject> difficultyMap = gson.fromJson(data.get("difficulty"), type);

                HashMap<String, Float> difficultyMobHealthModificator = new HashMap<String, Float>();
                HashMap<String, Float> difficultyMobDamageModificator = new HashMap<String, Float>();
                HashMap<String, Float> difficultyMobProtectionModificator = new HashMap<String, Float>();
                HashMap<String, Float> difficultyMobSpeedModificator = new HashMap<String, Float>();
                HashMap<String, List<String>> difficultyLootTableIds = new HashMap<String, List<String>>();
                HashMap<String, Float> difficultyBossHealthModificator = new HashMap<String, Float>();
                HashMap<String, Float> difficultyBossDamageModificator = new HashMap<String, Float>();
                HashMap<String, Float> difficultyBossProtectionModificator = new HashMap<String, Float>();
                HashMap<String, Float> difficultyBossSpeedModificator = new HashMap<String, Float>();
                HashMap<String, String> difficultyBossLootTable = new HashMap<String, String>();

                for (String difficulty : difficultyMap.keySet()) {
                    difficulties.add(difficulty);
                    JsonObject specificDifficultyObject = difficultyMap.get(difficulty);

                    difficultyMobHealthModificator.put(difficulty, specificDifficultyObject.get("mob_health_modificator").getAsFloat());
                    difficultyMobDamageModificator.put(difficulty, specificDifficultyObject.get("mob_damage_modificator").getAsFloat());
                    difficultyMobProtectionModificator.put(difficulty, specificDifficultyObject.get("mob_protection_modificator").getAsFloat());
                    difficultyMobSpeedModificator.put(difficulty, specificDifficultyObject.get("mob_speed_modificator").getAsFloat());
                    List<String> lootTableIds = new ArrayList<String>();
                    for (int i = 0; i < specificDifficultyObject.get("loot_table_ids").getAsJsonArray().size(); i++) {
                        lootTableIds.add(specificDifficultyObject.get("loot_table_ids").getAsJsonArray().get(i).getAsString());
                    }
                    difficultyLootTableIds.put(difficulty, lootTableIds);
                    difficultyBossHealthModificator.put(difficulty, specificDifficultyObject.get("boss_health_modificator").getAsFloat());
                    difficultyBossDamageModificator.put(difficulty, specificDifficultyObject.get("boss_damage_modificator").getAsFloat());
                    difficultyBossProtectionModificator.put(difficulty, specificDifficultyObject.get("boss_protection_modificator").getAsFloat());
                    difficultyBossSpeedModificator.put(difficulty, specificDifficultyObject.get("boss_speed_modificator").getAsFloat());
                    difficultyBossLootTable.put(difficulty, specificDifficultyObject.get("boss_loot_table_id").getAsString());
                }

                JsonObject blockObject = data.get("blocks").getAsJsonObject();
                Iterator<String> blockIterator = blockObject.keySet().iterator();

                HashMap<Integer, List<class_1299<?>>> blockIdEntityMap = new HashMap<Integer, List<class_1299<?>>>();
                HashMap<Integer, HashMap<String, Float>> blockIdEntitySpawnChance = new HashMap<Integer, HashMap<String, Float>>();
                HashMap<Integer, Integer> blockIdBlockReplacement = new HashMap<Integer, Integer>();
                int bossBlockId = -1;
                int bossLootBlockId = -1;
                int exitBlockId = -1;
                class_1299<?> bossEntityType = null;
                class_2487 bossNbtCompound = null;

                while (blockIterator.hasNext()) {
                    String block = blockIterator.next();
                    if (class_7923.field_41175.method_10223(class_2960.method_60654(block)).toString().equals("Block{minecraft:air}")) {
                        DungeonzMain.LOGGER.warn("{} is not a valid block identifier", block);
                        continue;
                    }
                    int rawBlockId = class_7923.field_41175.method_10206(class_7923.field_41175.method_10223(class_2960.method_60654(block)));

                    JsonObject specificBlockObject = blockObject.get(block).getAsJsonObject();

                    if (specificBlockObject.has("spawns")) {
                        List<class_1299<?>> entityTypes = new ArrayList<class_1299<?>>();
                        for (int i = 0; i < specificBlockObject.get("spawns").getAsJsonArray().size(); i++) {
                            if (!class_7923.field_41177.method_10250(class_2960.method_60654(specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString()))) {
                                DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString());
                                continue;
                            }
                            entityTypes.add(class_7923.field_41177.method_10223(class_2960.method_60654(specificBlockObject.get("spawns").getAsJsonArray().get(i).getAsString())));
                        }
                        blockIdEntityMap.put(rawBlockId, entityTypes);

                        HashMap<String, Float> difficultyChance = new HashMap<String, Float>();
                        for (String difficulty : difficulties) {
                            difficultyChance.put(difficulty, specificBlockObject.get("chance").getAsJsonObject().get(difficulty).getAsFloat());
                        }
                        blockIdEntitySpawnChance.put(rawBlockId, difficultyChance);

                    } else if (specificBlockObject.has("boss_entity")) {
                        if (!class_7923.field_41177.method_10250(class_2960.method_60654(specificBlockObject.get("boss_entity").getAsString()))) {
                            DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", specificBlockObject.get("boss_entity").getAsString());
                        }
                        bossEntityType = class_7923.field_41177.method_10223(class_2960.method_60654(specificBlockObject.get("boss_entity").getAsString()));
                        bossNbtCompound = tryReadNbtData(specificBlockObject);
                        bossBlockId = rawBlockId;
                    } else if (specificBlockObject.has("exit_block") && specificBlockObject.get("exit_block").getAsBoolean()) {
                        exitBlockId = rawBlockId;
                    } else if (specificBlockObject.has("boss_loot_block") && specificBlockObject.get("boss_loot_block").getAsBoolean()) {
                        bossLootBlockId = rawBlockId;
                    } else {
                        DungeonzMain.LOGGER.warn("{} has no set spawns nor exit block nor boss loot block nor boss entity", blockIterator);
                    }

                    if (!specificBlockObject.get("replace").isJsonNull()) {
                        class_2960 blockIdentifier = class_2960.method_60654(specificBlockObject.get("replace").getAsString());
                        if (!blockIdentifier.toString().equals("minecraft:air") && class_7923.field_41175.method_10223(blockIdentifier).toString().equals("Block{minecraft:air}")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid block identifier", specificBlockObject.get("replace").getAsString());
                            continue;
                        }
                        blockIdBlockReplacement.put(rawBlockId, class_7923.field_41175.method_10206(class_7923.field_41175.method_10223(blockIdentifier)));
                    } else {
                        blockIdBlockReplacement.put(rawBlockId, -1);
                    }
                }

                JsonObject spawnerObject = data.get("spawner").getAsJsonObject();
                Iterator<String> spawnerIterator = spawnerObject.keySet().iterator();

                HashMap<Integer, Integer> spawnerEntityIdCountMap = new HashMap<Integer, Integer>();

                while (spawnerIterator.hasNext()) {
                    String entityString = spawnerIterator.next();
                    class_2960 entityIdentifier = class_2960.method_60654(entityString);

                    if (class_7923.field_41177.method_10223(entityIdentifier).toString().equals("entity.minecraft.pig")) {
                        DungeonzMain.LOGGER.warn("{} is not a valid entity identifier", entityString);
                        continue;
                    }
                    spawnerEntityIdCountMap.put(class_7923.field_41177.method_10206(class_7923.field_41177.method_10223(entityIdentifier)), spawnerObject.get(entityString).getAsInt());
                }
                List<Integer> breakableBlockIds = new ArrayList<Integer>();
                if (data.has("breakable")) {
                    for (int i = 0; i < data.get("breakable").getAsJsonArray().size(); i++) {
                        class_2960 blockIdentifier = class_2960.method_60654(data.get("breakable").getAsJsonArray().get(i).getAsString());
                        if (class_7923.field_41175.method_10223(blockIdentifier).toString().equals("Block{minecraft:air}")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid block identifier", data.get("breakable").getAsJsonArray().get(i).getAsString());
                            continue;
                        }
                        breakableBlockIds.add(class_7923.field_41175.method_10206(class_7923.field_41175.method_10223(blockIdentifier)));
                    }
                }
                List<Integer> placeableBlockIds = new ArrayList<Integer>();
                if (data.has("placeable")) {
                    for (int i = 0; i < data.get("placeable").getAsJsonArray().size(); i++) {
                        class_2960 blockIdentifier = class_2960.method_60654(data.get("placeable").getAsJsonArray().get(i).getAsString());
                        if (class_7923.field_41175.method_10223(blockIdentifier).toString().equals("Block{minecraft:air}")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid block identifier", data.get("placeable").getAsJsonArray().get(i).getAsString());
                            continue;
                        }
                        placeableBlockIds.add(class_7923.field_41175.method_10206(class_7923.field_41175.method_10223(blockIdentifier)));
                    }
                }

                JsonObject requiredObject = data.get("required").getAsJsonObject();
                Iterator<String> requiredIterator = requiredObject.keySet().iterator();

                HashMap<String, HashMap<Integer, Integer>> difficultyRequiredItemCountMap = new HashMap<>();

                while (requiredIterator.hasNext()) {
                    String difficulty = requiredIterator.next();
                    if (!difficulties.contains(difficulty)) {
                        DungeonzMain.LOGGER.warn("{} is not a valid difficulty at the required list", difficulty);
                        continue;
                    }

                    HashMap<Integer, Integer> requiredItemCountMap = new HashMap<>();

                    for (String itemString : requiredObject.get(difficulty).getAsJsonObject().keySet()) {
                        class_2960 itemIdentifier = class_2960.method_60654(itemString);
                        if (class_7923.field_41178.method_10223(itemIdentifier).toString().equals("air")) {
                            DungeonzMain.LOGGER.warn("{} is not a valid item identifier", itemString);
                            continue;
                        }
                        requiredItemCountMap.put(class_7923.field_41178.method_10206(class_7923.field_41178.method_10223(itemIdentifier)), requiredObject.get(difficulty).getAsJsonObject().get(itemString).getAsInt());
                    }
                    difficultyRequiredItemCountMap.put(difficulty, requiredItemCountMap);
                }
                for (String difficulty : difficulties) {
                    if (!difficultyRequiredItemCountMap.containsKey(difficulty)) {
                        difficultyRequiredItemCountMap.put(difficulty, new HashMap<>());
                    }
                }

                if (bossEntityType == null) {
                    DungeonzMain.LOGGER.error("{} has no set boss", data);
                    return;
                }
                Dungeon.addDungeon(new Dungeon(dungeonTypeId, blockIdEntityMap, blockIdEntitySpawnChance, blockIdBlockReplacement, spawnerEntityIdCountMap, difficultyRequiredItemCountMap, breakableBlockIds,
                        placeableBlockIds, difficulties, difficultyMobHealthModificator, difficultyMobDamageModificator, difficultyMobProtectionModificator, difficultyMobSpeedModificator, difficultyLootTableIds, difficultyBossHealthModificator,
                        difficultyBossDamageModificator, difficultyBossProtectionModificator, difficultyBossSpeedModificator, difficultyBossLootTable, bossEntityType, bossNbtCompound, bossBlockId,
                        bossLootBlockId, exitBlockId, allowRespawn, allowElytra, keepInventory, allowEnderPearl, allowPositiveEffects, maxGroupSize, minGroupSize, requiredLevel, cooldown, dungeonBackgroundId, dungeonStructurePoolId));
            } catch (Exception e) {
                DungeonzMain.LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

    @Nullable
    private static class_2487 tryReadNbtData(JsonObject json) {
        if (json.has("data") && json.get("data") != null && !json.get("data").getAsString().equals("")) {
            try {
                return new class_2522(new StringReader(json.get("data").getAsString())).method_10727();
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                throw new JsonParseException("Failed to load nbt data of json object " + json);
            }
        }
        return null;
    }

}
