/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  folk.sisby.surveyor.landmark.Landmark
 *  net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.packs.metadata.MetadataSectionSerializer
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.ResourceMetadata
 *  net.minecraft.server.packs.resources.SimplePreparableReloadListener
 *  net.minecraft.util.profiling.ProfilerFiller
 */
package folk.sisby.antique_atlas.reloader;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.util.CodecUtil;
import folk.sisby.surveyor.landmark.Landmark;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

public class MarkerTextures
extends SimplePreparableReloadListener<Map<Identifier, MarkerTextureMeta>>
implements IdentifiableResourceReloadListener {
    public static final MarkerTextures INSTANCE = new MarkerTextures();
    public static final Identifier ID = AntiqueAtlas.id("marker_textures");
    protected final Map<Identifier, MarkerTexture> textures = new HashMap<Identifier, MarkerTexture>();

    public static MarkerTextures getInstance() {
        return INSTANCE;
    }

    public MarkerTexture get(Identifier id) {
        return this.textures.get(id);
    }

    public MarkerTexture getOrDefault(Identifier id) {
        return this.getOrDefault(id, MarkerTexture.DEFAULT);
    }

    public MarkerTexture getOrDefault(Identifier id, MarkerTexture defaultTexture) {
        return this.textures.getOrDefault(id, defaultTexture);
    }

    public Identifier minimumId(Identifier id) {
        while (this.get(id) == null && id.getPath().contains("/")) {
            id = id.withPath(id.getPath().substring(0, id.getPath().lastIndexOf(47)));
        }
        return this.get(id) == null ? id.withPath("default") : id;
    }

    public MarkerTexture fromLandmark(Landmark landmark) {
        return this.getOrDefault(this.minimumId(landmark.id()));
    }

    public MarkerTexture fromLandmark(Landmark landmark, String variant) {
        Identifier id = this.minimumId(landmark.id());
        return this.getOrDefault(id.withPath(p -> p + "/" + variant), this.getOrDefault(id));
    }

    public Map<Identifier, MarkerTexture> asMap() {
        return new HashMap<Identifier, MarkerTexture>(this.textures);
    }

    protected Map<Identifier, MarkerTextureMeta> prepare(ResourceManager manager, ProfilerFiller profiler) {
        HashMap<Identifier, MarkerTextureMeta> textureMeta = new HashMap<Identifier, MarkerTextureMeta>();
        for (Map.Entry e : manager.listResources("textures/atlas/marker", id -> id.getPath().endsWith(".png")).entrySet()) {
            Identifier id2 = Identifier.fromNamespaceAndPath((String)((Identifier)e.getKey()).getNamespace(), (String)((Identifier)e.getKey()).getPath().substring("textures/atlas/marker/".length(), ((Identifier)e.getKey()).getPath().length() - ".png".length()));
            try {
                ResourceMetadata metadata = ((Resource)e.getValue()).metadata();
                textureMeta.put(id2, metadata.getSection(MarkerTextureMeta.METADATA).orElse(MarkerTextureMeta.DEFAULT));
            }
            catch (IOException ex) {
                AntiqueAtlas.LOGGER.error("[Antique Atlas] Failed to access marker texture metadata for {}", e.getKey(), (Object)ex);
                textureMeta.put(id2, MarkerTextureMeta.DEFAULT);
            }
        }
        return textureMeta;
    }

    protected void apply(Map<Identifier, MarkerTextureMeta> prepared, ResourceManager manager, ProfilerFiller profiler) {
        AntiqueAtlas.LOGGER.info("[Antique Atlas] Reloading Marker Textures...");
        this.textures.clear();
        prepared.forEach((id, meta) -> {
            if (id.getPath().endsWith("_accent")) {
                Identifier mainId = id.withPath(s -> s.substring(0, s.length() - "_accent".length()));
                MarkerTextureMeta main = (MarkerTextureMeta)prepared.get(mainId);
                if (main != null) {
                    this.textures.put(mainId, main.build(mainId, true));
                } else {
                    AntiqueAtlas.LOGGER.error("[Antique Atlas] Marker accent {} has no main texture! Discarding.", id);
                }
            }
        });
        prepared.forEach((id, meta) -> {
            if (!this.textures.containsKey(id) && !id.getPath().endsWith("_accent")) {
                this.textures.put((Identifier)id, meta.build((Identifier)id, false));
            }
        });
    }

    public Identifier getFabricId() {
        return ID;
    }


}
