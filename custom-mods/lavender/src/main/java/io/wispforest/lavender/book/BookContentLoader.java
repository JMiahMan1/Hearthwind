package io.wispforest.lavender.book;

import com.google.common.collect.ImmutableSet;
import com.google.gson.*;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.JsonOps;
import io.wispforest.lavender.Lavender;
import io.wispforest.lavender.mixin.access.RegistryOpsAccessor;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.server.packs.resources.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class BookContentLoader implements ResourceManagerReloadListener, IdentifiableResourceReloadListener {

    private static final ResourceFinder ENTRY_FINDER = new ResourceFinder("lavender/entries", ".md");
    private static final ResourceFinder CATEGORY_FINDER = new ResourceFinder("lavender/categories", ".md");
    private static final Gson GSON = new GsonBuilder().setLenient().disableHtmlEscaping().create();

    public static void initialize() {
        ResourceManagerHelper.get(net.minecraft.server.packs.PackType.CLIENT_RESOURCES).registerReloadListener(new BookContentLoader());
    }

    @Override
    public Identifier getFabricId() {
        return Lavender.id("book_content_loader");
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        if (Minecraft.getInstance().level == null) return;
        reloadContents(manager);
    }

    public static void reloadContents(ResourceManager manager) {
        var entries = findResources(manager, ENTRY_FINDER);
        var categories = findResources(manager, CATEGORY_FINDER);

        for (var book : BookLoader.allBooks()) {
            forResourceOfBook(categories, book, "category", (identifier, resource) -> {
                var markdown = parseMarkdown(book, identifier, resource);
                if (markdown == null) return;

                var parentCategory = GsonHelper.getAsString(markdown.meta, "parent", null);
                var parentCategoryId = parentCategory != null
                    ? parentCategory.indexOf(':') > 0 ? Identifier.tryParse(parentCategory) : Identifier.fromNamespaceAndPath(identifier.getNamespace(), parentCategory)
                    : null;

                book.addCategory(new Category(
                    identifier,
                    parentCategoryId,
                    GsonHelper.getAsString(markdown.meta, "title"),
                    getIcon(markdown.meta),
                    GsonHelper.getAsBoolean(markdown.meta, "secret", false),
                    GsonHelper.getAsInt(markdown.meta, "ordinal", Integer.MAX_VALUE),
                    markdown.content
                ));
            });
        }

        for (var book : BookLoader.allBooks()) {
            forResourceOfBook(entries, book, "entry", (identifier, resource) -> {
                var markdown = parseMarkdown(book, identifier, resource);
                if (markdown == null) return;

                var entryCategories = new ArrayList<Identifier>();
                for (var categoryElement : GsonHelper.getAsJsonArray(markdown.meta, "categories", new JsonArray())) {
                    var categoryString = categoryElement.getAsString();
                    entryCategories.add(categoryString.indexOf(':') > 0 ? Identifier.tryParse(categoryString) : Identifier.fromNamespaceAndPath(identifier.getNamespace(), categoryString));
                }

                var legacyCategory = GsonHelper.getAsString(markdown.meta, "category", null);
                if (legacyCategory != null) {
                    entryCategories.add(legacyCategory.indexOf(':') > 0 ? Identifier.tryParse(legacyCategory) : Identifier.fromNamespaceAndPath(identifier.getNamespace(), legacyCategory));
                }

                var title = GsonHelper.getAsString(markdown.meta, "title");
                var icon = getIcon(markdown.meta);
                var secret = GsonHelper.getAsBoolean(markdown.meta, "secret", false);
                var ordinal = GsonHelper.getAsInt(markdown.meta, "ordinal", Integer.MAX_VALUE);

                var associatedItems = new ImmutableSet.Builder<ItemStack>();
                for (var itemElement : GsonHelper.getAsJsonArray(markdown.meta, "associated_items", new JsonArray())) {
                    associatedItems.addAll(itemsFromString(itemElement.getAsString()));
                }

                var requiredAdvancements = new ImmutableSet.Builder<Identifier>();
                for (var advancementElement : GsonHelper.getAsJsonArray(markdown.meta, "required_advancements", new JsonArray())) {
                    if (!advancementElement.isJsonPrimitive()) continue;

                    var advancementId = Identifier.tryParse(advancementElement.getAsString());
                    if (advancementId == null) {
                        Lavender.LOGGER.warn("Did not add advancement '{}' as requirement to entry '{}' as it is not a valid advancement identifier", advancementElement.getAsString(), identifier);
                        continue;
                    }

                    requiredAdvancements.add(advancementId);
                }

                var additionalSearchTerms = new ImmutableSet.Builder<String>();
                for (var termElement : GsonHelper.getAsJsonArray(markdown.meta, "additional_search_terms", new JsonArray())) {
                    if (!termElement.isJsonPrimitive()) continue;

                    var term = termElement.getAsString();
                    // Lowercase the term in advance to save a little time when searching.
                    additionalSearchTerms.add(term.toLowerCase(Locale.ROOT));
                }

                var entry = new Entry(
                        identifier,
                        entryCategories,
                        title,
                        icon,
                        secret,
                        ordinal,
                        requiredAdvancements.build(),
                        associatedItems.build(),
                        additionalSearchTerms.build(),
                        markdown.content
                );
                if (entry.id().getPath().equals("landing_page")) {
                    book.setLandingPage(entry);
                } else {
                    book.addEntry(entry);
                }
            });
        }
    }

    private static Map<String, Map<String, Resource>> findResources(ResourceManager manager, ResourceFinder finder) {
        var resources = new HashMap<String, Map<String, Resource>>();
        finder.findResources(manager).forEach((identifier, resource) -> {
            var resourceId = finder.toResourceId(identifier);
            resources.computeIfAbsent(resourceId.getNamespace(), s -> new HashMap<>()).put(resourceId.getPath(), resource);
        });

        return resources;
    }

    private static void forResourceOfBook(Map<String, Map<String, Resource>> resources, Book book, String resourceType, BiConsumer<Identifier, Resource> action) {
        if (!resources.containsKey(book.id().getNamespace())) return;

        var targetBook = book.id().getPath();
        var activeLanguage = Minecraft.getInstance().getLanguageManager().getSelected();

        var discoveredResources = new HashMap<Identifier, Resource>();

        resources.get(book.id().getNamespace()).forEach((path, resource) -> {
            var bookResourcePath = getBookResourcePath(path, targetBook, null);
            if (bookResourcePath == null) return;

            discoveredResources.put(Identifier.fromNamespaceAndPath(book.id().getNamespace(), bookResourcePath), resource);
        });

        resources.get(book.id().getNamespace()).forEach((path, resource) -> {
            var bookResourcePath = getBookResourcePath(path, targetBook, activeLanguage);
            if (bookResourcePath == null) return;

            discoveredResources.put(Identifier.fromNamespaceAndPath(book.id().getNamespace(), bookResourcePath), resource);
        });

        discoveredResources.forEach((resourceId, resource) -> {
            try {
                action.accept(resourceId, resource);
            } catch (RuntimeException e) {
                Lavender.LOGGER.warn("Could not load {} '{}'", resourceType, resourceId, e);
            }
        });
    }

    private static @Nullable String getBookResourcePath(String resourcePath, String bookName, @Nullable String activeLanguage) {
        String book = null;
        String language = null;

        if (resourcePath.indexOf('/') != -1) {
            book = resourcePath.substring(0, resourcePath.indexOf('/'));
            resourcePath = resourcePath.substring(resourcePath.indexOf('/') + 1);
        }

        if (resourcePath.indexOf('/') != -1) {
            language = resourcePath.substring(0, resourcePath.indexOf('/'));
            if (Minecraft.getInstance().getLanguageManager().getLanguages().keySet().contains(language)) {
                resourcePath = resourcePath.substring(resourcePath.indexOf('/') + 1);
            } else {
                language = null;
            }
        }

        if (!bookName.equals(book) || !Objects.equals(activeLanguage, language)) return null;
        return resourcePath;
    }

    private static @Nullable MarkdownResource parseMarkdown(Book book, Identifier resourceId, Resource resource) {
        try {
            var content = IOUtils.toString(resource.open(), StandardCharsets.UTF_8).strip();
            JsonObject meta;

            if (content.startsWith("```json")) {
                content = content.substring("```json".length());
                int frontmatterEnd = content.indexOf("```");
                if (frontmatterEnd == -1) {
                    throw new RuntimeException("Unterminated markdown meta");
                }

                meta = GSON.fromJson(content.substring(0, frontmatterEnd), JsonObject.class);
                content = content.substring(frontmatterEnd + 3).stripLeading();

                if (meta.has(ResourceConditions.CONDITIONS_KEY)) {
                    var conditions = ResourceCondition.CONDITION_CODEC.parse(JsonOps.INSTANCE, meta.get(ResourceConditions.CONDITIONS_KEY));
                    var level = Minecraft.getInstance().level;
                    var infoGetter = level != null
                        ? ((RegistryOpsAccessor) RegistryOps.create(JsonOps.INSTANCE, level.registryAccess())).lavender$getInfoGetter()
                        : null;

                    if (infoGetter != null && conditions.isSuccess() && !conditions.getOrThrow().test(infoGetter)) {
                        return null;
                    }
                }

                return new MarkdownResource(meta, book.expandMacros(resourceId, content.replaceAll("\\r\\n?", "\n")));
            } else {
                throw new RuntimeException("Missing markdown meta");
            }
        } catch (Exception e) {
            Lavender.LOGGER.warn("Could not load markdown file {}", resourceId, e);
            return null;
        }
    }

    private record MarkdownResource(JsonObject meta, String content) {}

    private static Function<Sizing, UIComponent> getIcon(JsonObject meta) {
        if (meta.has("icon")) {
            var stack = itemStackFromString(GsonHelper.getAsString(meta, "icon"));
            return sizing -> UIComponents.item(stack).sizing(sizing);
        } else if (meta.has("icon_sprite")) {
            var id = Identifier.tryParse(GsonHelper.getAsString(meta, "icon_sprite"));
            if (id == null) return null;

            return sizing -> UIComponents.sprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(Identifier.withDefaultNamespace("gui")).getSprite(id)).sizing(sizing);
        } else {
            return sizing -> UIContainers.stack(sizing, sizing);
        }
    }

    private static Collection<ItemStack> itemsFromString(String itemsString) {
        if (!itemsString.startsWith("#")) return List.of(itemStackFromString(itemsString));

        var tagId = Identifier.tryParse(itemsString.substring(1));
        if (tagId == null) {
            Lavender.LOGGER.warn("Could not parse tag ID '{}'", itemsString);
            return List.of();
        }

        var entries = BuiltInRegistries.ITEM.getTagOrEmpty(TagKey.create(net.minecraft.core.registries.Registries.ITEM, tagId));
        var list = new java.util.ArrayList<ItemStack>();
        for (var holder : entries) {
            list.add(holder.value().getDefaultInstance());
        }
        if (list.isEmpty()) {
            Lavender.LOGGER.warn("Unknown item tag: '{}'", itemsString);
            return List.of();
        }
        return list;
    }

    public static ItemStack itemStackFromString(String stackString) {
        try {
            var level = Minecraft.getInstance().level;
            var lookup = level != null ? level.registryAccess() : Minecraft.getInstance().player != null
                ? Minecraft.getInstance().player.registryAccess()
                : null;
            if (lookup == null) return ItemStack.EMPTY;

            var parsed = new ItemParser(lookup).parse(new StringReader(stackString));
            var stack = new ItemStack(parsed.item(), 1);
            if (parsed.components() != null) stack.applyComponents(parsed.components());

            return stack;
        } catch (CommandSyntaxException e) {
            throw new JsonSyntaxException("Invalid item stack: '" + stackString + "'", e);
        }
    }
}
