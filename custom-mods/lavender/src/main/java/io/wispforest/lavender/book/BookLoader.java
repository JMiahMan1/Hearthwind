package io.wispforest.lavender.book;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.JsonOps;
import io.wispforest.lavender.Lavender;
import io.wispforest.lavender.client.BookBakedModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BookLoader {

    private static final Gson GSON = new GsonBuilder().setLenient().disableHtmlEscaping().create();
    private static final TypeToken<Map<String, String>> MACROS_TOKEN = new TypeToken<>() {};
    private static final ResourceFinder BOOK_FINDER = ResourceFinder.json("lavender/books");

    private static final Map<Identifier, Book> LOADED_BOOKS = new HashMap<>();
    private static final Map<Identifier, Book> VISIBLE_BOOKS = new HashMap<>();

    public static void initialize() {
        // dynamic book models registered via ItemModels.ID_MAPPER in LavenderClient
    }

    public static @Nullable Book get(Identifier bookId) {
        return LOADED_BOOKS.get(bookId);
    }

    public static Collection<Book> loadedBooks() {
        return Collections.unmodifiableCollection(VISIBLE_BOOKS.values());
    }

    static Collection<Book> allBooks() {
        return Collections.unmodifiableCollection(LOADED_BOOKS.values());
    }

    public static void reload(ResourceManager manager) {
        LOADED_BOOKS.clear();
        BOOK_FINDER.findResources(manager).forEach((identifier, resource) -> {
            JsonElement jsonElement;
            try (var reader = resource.openAsReader()) {
                jsonElement = GsonHelper.fromJson(GSON, reader, JsonElement.class);
            } catch (IOException e) {
                Lavender.LOGGER.warn("Could not load book '{}'", identifier, e);
                return;
            }

            if (!jsonElement.isJsonObject()) return;
            var bookObject = jsonElement.getAsJsonObject();
            var resourceId = BOOK_FINDER.toResourceId(identifier);

            var textureId = tryGetId(bookObject, "texture");
            var extendId = tryGetId(bookObject, "extend");
            var dynamicBookModelId = tryGetId(bookObject, "dynamic_book_model");

            Component dynamicBookName = null;
            if (bookObject.has("dynamic_book_name")) {
                dynamicBookName = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, bookObject.get("dynamic_book_name")).getOrThrow(JsonParseException::new);
            }

            var openSoundId = tryGetId(bookObject, "open_sound");
            var openSoundEvent = openSoundId != null ? BuiltInRegistries.SOUND_EVENT.getOptional(openSoundId).orElse(null) : null;
            var flippingSoundId = tryGetId(bookObject, "flipping_sound");
            var flippingSoundEvent = flippingSoundId != null ? BuiltInRegistries.SOUND_EVENT.getOptional(flippingSoundId).orElse(null) : null;

            var introEntryId = tryGetId(bookObject, "intro_entry");

            var displayCompletion = GsonHelper.getAsBoolean(bookObject, "display_completion", false);
            var displayUnreadEntryNotifications = GsonHelper.getAsBoolean(bookObject, "display_unread_entry_notifications", true);
            var macros = GSON.fromJson(GsonHelper.getAsJsonObject(bookObject, "macros", new JsonObject()), MACROS_TOKEN);

            Book.ToastSettings newEntriesToast = null;
            if (bookObject.has("new_entries_toast")) {
                var toastObject = bookObject.getAsJsonObject("new_entries_toast");

                Identifier backgroundSprite = null;
                if (toastObject.has("background_sprite")) {
                    backgroundSprite = Identifier.tryParse(GsonHelper.getAsString(toastObject, "background_sprite"));
                }

                newEntriesToast = new Book.ToastSettings(
                    BookContentLoader.itemStackFromString(GsonHelper.getAsString(toastObject, "icon_stack")),
                    ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, toastObject.get("book_name")).getOrThrow(JsonParseException::new),
                    backgroundSprite
                );
            }

            var book = new Book(
                resourceId,
                extendId,
                textureId,
                dynamicBookModelId,
                dynamicBookName,
                openSoundEvent,
                flippingSoundEvent,
                introEntryId,
                displayUnreadEntryNotifications,
                displayCompletion,
                newEntriesToast,
                macros
            );
            LOADED_BOOKS.put(resourceId, book);
            if (extendId == null) VISIBLE_BOOKS.put(resourceId, book);
        });

        LOADED_BOOKS.values().removeIf(book -> {
            if (book.tryResolveExtension()) return false;

            Lavender.LOGGER.warn("Book '" + book.id() + "' (an extension) failed to load because its target was not found");
            return true;
        });
    }

    private static @Nullable Identifier tryGetId(JsonObject json, String key) {
        var jsonString = GsonHelper.getAsString(json, key, null);
        if (jsonString == null) return null;

        return Identifier.tryParse(jsonString);
    }
}
