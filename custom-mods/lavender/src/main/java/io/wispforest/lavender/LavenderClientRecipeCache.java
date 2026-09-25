package io.wispforest.lavender;

import io.wispforest.endec.StructEndec;
import io.wispforest.endec.impl.StructEndecBuilder;
import io.wispforest.lavender.client.LavenderBookScreen;
import io.wispforest.owo.serialization.CodecUtils;
import io.wispforest.owo.serialization.endec.MinecraftEndecs;
import it.unimi.dsi.fastutil.objects.Reference2LongMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LavenderClientRecipeCache {

    private static final Map<Identifier, RecipeHolder<?>> RECIPE_CACHE = new HashMap<>();
    private static final Reference2LongMap<Identifier> LAST_FETCHED_TIMESTAMP = new Reference2LongOpenHashMap<>();

    public static Optional<RecipeHolder<?>> getOrFetchRecipe(Identifier recipeId) {
        if (RECIPE_CACHE.containsKey(recipeId)) return Optional.of(RECIPE_CACHE.get(recipeId));

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.getConnection() == null) {
            return Optional.empty();
        }

        if (System.currentTimeMillis() - LAST_FETCHED_TIMESTAMP.getOrDefault(recipeId, 0) < 5_000) {
            return Optional.empty();
        }

        LAST_FETCHED_TIMESTAMP.put(recipeId, System.currentTimeMillis());
        Lavender.CHANNEL.clientHandle().send(new RequestRecipePacket(recipeId));

        return Optional.empty();
    }

    // ---

    public static void initialize() {
        Lavender.CHANNEL.registerServerbound(RequestRecipePacket.class, (packet, serverAccess) -> {
            var recipeEntry = serverAccess.runtime().getRecipeManager().byKey(ResourceKey.create(Registries.RECIPE, packet.recipeId));
            if (recipeEntry.isEmpty()) return;

            var recipe = recipeEntry.get().value();
            Lavender.CHANNEL.serverHandle(serverAccess.player()).send(new RecipePayloadPacket(packet.recipeId, recipe));
        });

        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, b) -> {
            Lavender.CHANNEL.serverHandle(player).send(new ClearRecipeCachePacket());
        });

        Lavender.CHANNEL.registerClientboundDeferred(RecipePayloadPacket.class, RecipePayloadPacket.ENDEC);
        Lavender.CHANNEL.registerClientboundDeferred(ClearRecipeCachePacket.class);
    }

    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        Lavender.CHANNEL.registerClientbound(RecipePayloadPacket.class, RecipePayloadPacket.ENDEC, (payload, clientAccess) -> handleRecipePayload(payload));
        Lavender.CHANNEL.registerClientbound(ClearRecipeCachePacket.class, (packet, clientAccess) -> handleClearCache());
    }

    @Environment(EnvType.CLIENT)
    private static void handleRecipePayload(RecipePayloadPacket payload) {
        RECIPE_CACHE.put(payload.recipeId, new RecipeHolder<>(ResourceKey.create(Registries.RECIPE, payload.recipeId), payload.recipe));

        if (Minecraft.getInstance().gui.screen() instanceof LavenderBookScreen bookScreen) {
            bookScreen.rebuildContent(null);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void handleClearCache() {
        RECIPE_CACHE.clear();
        LAST_FETCHED_TIMESTAMP.clear();
    }

    public record ClearRecipeCachePacket() {}

    public record RequestRecipePacket(Identifier recipeId) {}

    public record RecipePayloadPacket(Identifier recipeId, Recipe<?> recipe) {
        public static final StructEndec<RecipePayloadPacket> ENDEC = StructEndecBuilder.of(
            MinecraftEndecs.IDENTIFIER.fieldOf("recipe_id", RecipePayloadPacket::recipeId),
            CodecUtils.toEndec(Recipe.CODEC).fieldOf("recipe", RecipePayloadPacket::recipe),
            RecipePayloadPacket::new
        );
    }
}
