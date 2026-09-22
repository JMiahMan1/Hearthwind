package io.wispforest.lavender.book;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.HashMap;
import java.util.Map;

public final class ResourceFinder {
    private final String prefix;
    private final String extension;

    public ResourceFinder(String prefix, String extension) {
        this.prefix = prefix;
        this.extension = extension;
    }

    public static ResourceFinder json(String prefix) {
        return new ResourceFinder(prefix, ".json");
    }

    public Map<Identifier, Resource> findResources(ResourceManager manager) {
        Map<Identifier, Resource> found = new HashMap<>();
        for (var entry : manager.listResources(this.prefix, id -> id.getPath().endsWith(this.extension)).entrySet()) {
            found.put(entry.getKey(), entry.getValue());
        }
        return found;
    }

    public Identifier toResourceId(Identifier resourceId) {
        String path = resourceId.getPath();
        int start = this.prefix.isEmpty() ? 0 : this.prefix.length() + 1;
        String relative = path.substring(start);
        if (relative.endsWith(this.extension)) {
            relative = relative.substring(0, relative.length() - this.extension.length());
        }
        return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), relative);
    }
}
