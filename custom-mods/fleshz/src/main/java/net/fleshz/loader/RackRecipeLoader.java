package net.fleshz.loader;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fleshz.init.RecipeInit;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class RackRecipeLoader implements SimpleSynchronousResourceReloadListener {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Identifier getFabricId() {
        return Identifier.fromNamespaceAndPath("fleshz", "rack_items");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        RecipeInit.RACK_ITEM_LIST.clear();
        RecipeInit.RACK_RESULT_ITEM_LIST.clear();
        RecipeInit.RACK_RESULT_TIME_LIST.clear();

        manager.listResources("rack_items", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try (InputStream stream = resourceRef.open()) {
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                Identifier itemId = Identifier.tryParse(data.get("item").getAsString());
                Identifier resultId = Identifier.tryParse(data.get("result").getAsString());
                if (itemId == null || resultId == null || itemId.getPath().equals("air") || resultId.getPath().equals("air")) {
                    LOGGER.info("{} is not a valid item identifier at resource {}", data.get("item").getAsString(), id);
                    return;
                }
                if (data.get("time").getAsInt() < 0) {
                    LOGGER.info("{} is not a valid time at resource {}", data.get("time").getAsInt(), id);
                    return;
                }

                Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(null);
                Item result = BuiltInRegistries.ITEM.getOptional(resultId).orElse(null);
                if (item == null || result == null) {
                    return;
                }
                RecipeInit.RACK_ITEM_LIST.add(item);
                RecipeInit.RACK_RESULT_ITEM_LIST.add(result);
                RecipeInit.RACK_RESULT_TIME_LIST.add(data.get("time").getAsInt());

            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id, e.toString());
            }
        });
        LOGGER.info("Loaded {} fleshz rack recipes", RecipeInit.RACK_ITEM_LIST.size());
    }
}
