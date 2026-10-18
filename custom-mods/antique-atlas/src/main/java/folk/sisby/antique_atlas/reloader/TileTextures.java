/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.metadata.MetadataSectionSerializer
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceMetadata
 *  net.minecraft.server.packs.resources.SimplePreparableReloadListener
 *  net.minecraft.util.ExtraCodecs
 *  net.minecraft.util.ExtraCodecs$TagOrElementLocation
 *  net.minecraft.util.profiling.ProfilerFiller
 */
package folk.sisby.antique_atlas.reloader;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.util.CodecUtil;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;

public class TileTextures
extends SimplePreparableReloadListener<Map<Identifier, TileTextureMeta>>
implements IdentifiableResourceReloadListener {
    public static final TileTextures INSTANCE = new TileTextures();
    public static final Identifier ID = AntiqueAtlas.id("tile_textures");
    protected final Map<Identifier, TileTexture> textures = new HashMap<Identifier, TileTexture>();

    public static TileTextures getInstance() {
        return INSTANCE;
    }

    public Map<Identifier, TileTexture> getTextures() {
        return this.textures;
    }

    protected Map<Identifier, TileTextureMeta> prepare(ResourceManager manager, ProfilerFiller profiler) {
        HashMap<Identifier, TileTextureMeta> textureMeta = new HashMap<Identifier, TileTextureMeta>();
        for (Map.Entry e : manager.listResources("textures/atlas/tile", id -> id.getPath().endsWith(".png")).entrySet()) {
            Identifier id2 = Identifier.fromNamespaceAndPath((String)((Identifier)e.getKey()).getNamespace(), (String)((Identifier)e.getKey()).getPath().substring("textures/atlas/tile/".length(), ((Identifier)e.getKey()).getPath().length() - ".png".length()));
            try {
                ResourceMetadata metadata = ((Resource)e.getValue()).metadata();
                metadata.getSection(TileTextureMeta.METADATA).ifPresentOrElse(meta -> textureMeta.put(id2, (TileTextureMeta)meta), () -> {
                    AntiqueAtlas.LOGGER.info("[Antique Atlas] Metadata not present for {} - using defaults.", e.getKey());
                    textureMeta.put(id2, TileTextureMeta.DEFAULT);
                });
            }
            catch (IOException ex) {
                AntiqueAtlas.LOGGER.error("[Antique Atlas] Failed to access tile texture metadata for {}", e.getKey(), (Object)ex);
                textureMeta.put(id2, TileTextureMeta.DEFAULT);
            }
        }
        return textureMeta;
    }

    protected void apply(Map<Identifier, TileTextureMeta> prepared, ResourceManager manager, ProfilerFiller profiler) {
        AntiqueAtlas.LOGGER.info("[Antique Atlas] Reloading Tile Textures...");
        prepared.forEach((id, meta) -> meta.warnMissing((Identifier)id, prepared.keySet()));
        HashMap invalidParents = new HashMap();
        prepared.forEach((id, meta) -> {
            if (meta.parent != null && !prepared.containsKey(meta.parent)) {
                invalidParents.put(id, meta.parent);
                AntiqueAtlas.LOGGER.error("[Antique Atlas] Failed to reload a tile texture! {} had invalid parent {}", id, (Object)meta.parent);
            }
        });
        invalidParents.keySet().forEach(prepared::remove);
        prepared.forEach((id, meta) -> {
            Optional<TileTextureMeta> parent = meta.parent().map(prepared::get);
            while (parent.isPresent()) {
                meta.inheritFromAncestor(parent.orElseThrow());
                parent = parent.orElseThrow().parent().map(prepared::get);
            }
        });
        HashMap<Identifier, Set<Identifier>> textureTags = new HashMap<>();
        prepared.forEach((id, meta) -> meta.tags.forEach(tag -> textureTags.computeIfAbsent(tag, t -> new HashSet()).add(id)));
        prepared.forEach((id, meta) -> meta.substituteTags((Identifier)id, textureTags));
        prepared.forEach((id, meta) -> meta.applyTilesToThis((Identifier)id, prepared));
        HashMap<Identifier, TileTexture.Builder> textureBuilders = new HashMap<Identifier, TileTexture.Builder>();
        prepared.forEach((id, meta) -> textureBuilders.put((Identifier)id, meta.toBuilder((Identifier)id)));
        this.textures.clear();
        textureBuilders.forEach((id, builder) -> this.textures.put((Identifier)id, TileTexture.empty(id, builder.innerBorder())));
        textureBuilders.forEach((id, builder) -> builder.build(this.textures));
    }

    public Identifier getFabricId() {
        return ID;
    }


}
