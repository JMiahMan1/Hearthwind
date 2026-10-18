package folk.sisby.antique_atlas.reloader;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import folk.sisby.antique_atlas.TileTexture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.util.CodecUtil;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public class TileTextureMeta {
    public static final TileTextureMeta DEFAULT = new TileTextureMeta(null, null, Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of(), Set.of());
    public static final Codec<TileTextureMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(Identifier.CODEC.optionalFieldOf("parent").forGetter(TileTextureMeta::parent), CodecUtil.ofEnum(BorderType.class).optionalFieldOf("borderType").forGetter(TileTextureMeta::borderType), CodecUtil.set(Identifier.CODEC).fieldOf("tags").orElseGet(HashSet::new).forGetter(TileTextureMeta::tags), CodecUtil.set(ExtraCodecs.TAG_OR_ELEMENT_ID).fieldOf("tilesTo").orElseGet(HashSet::new).forGetter(TileTextureMeta::tilesTo), CodecUtil.set(ExtraCodecs.TAG_OR_ELEMENT_ID).fieldOf("tilesToHorizontal").orElseGet(HashSet::new).forGetter(TileTextureMeta::tilesToHorizontal), CodecUtil.set(ExtraCodecs.TAG_OR_ELEMENT_ID).fieldOf("tilesToVertical").orElseGet(HashSet::new).forGetter(TileTextureMeta::tilesToVertical), CodecUtil.set(ExtraCodecs.TAG_OR_ELEMENT_ID).fieldOf("tilesToThis").orElseGet(HashSet::new).forGetter(TileTextureMeta::tilesToThis), CodecUtil.set(ExtraCodecs.TAG_OR_ELEMENT_ID).fieldOf("tilesToThisHorizontal").orElseGet(HashSet::new).forGetter(TileTextureMeta::tilesToThisHorizontal), CodecUtil.set(ExtraCodecs.TAG_OR_ELEMENT_ID).fieldOf("tilesToThisVertical").orElseGet(HashSet::new).forGetter(TileTextureMeta::tilesToThisVertical)).apply(instance, (p, b, t, tt, tth, ttv, ttt, ttth, tttv) -> new TileTextureMeta(p.orElse(null), b.orElse(null), (Set<Identifier>)t, (Set<ExtraCodecs.TagOrElementLocation>)tt, (Set<ExtraCodecs.TagOrElementLocation>)tth, (Set<ExtraCodecs.TagOrElementLocation>)ttv, (Set<ExtraCodecs.TagOrElementLocation>)ttt, (Set<ExtraCodecs.TagOrElementLocation>)ttth, (Set<ExtraCodecs.TagOrElementLocation>)tttv)));
    public static final MetadataSectionType<TileTextureMeta> METADATA = new MetadataSectionType<>(AntiqueAtlas.id("tiling").toString(), CODEC);
    protected final Identifier parent;
    protected BorderType borderType;
    protected final Set<Identifier> tags;
    protected final Set<ExtraCodecs.TagOrElementLocation> tilesTo;
    protected final Set<ExtraCodecs.TagOrElementLocation> tilesToHorizontal;
    protected final Set<ExtraCodecs.TagOrElementLocation> tilesToVertical;
    protected final Set<ExtraCodecs.TagOrElementLocation> tilesToThis;
    protected final Set<ExtraCodecs.TagOrElementLocation> tilesToThisHorizontal;
    protected final Set<ExtraCodecs.TagOrElementLocation> tilesToThisVertical;

    public TileTextureMeta(Identifier parent, BorderType borderType, Set<Identifier> tags, Set<ExtraCodecs.TagOrElementLocation> tilesTo, Set<ExtraCodecs.TagOrElementLocation> tilesToHorizontal, Set<ExtraCodecs.TagOrElementLocation> tilesToVertical, Set<ExtraCodecs.TagOrElementLocation> tilesToThis, Set<ExtraCodecs.TagOrElementLocation> tilesToThisHorizontal, Set<ExtraCodecs.TagOrElementLocation> tilesToThisVertical) {
        this.parent = parent;
        this.borderType = borderType;
        this.tags = tags;
        this.tilesTo = tilesTo;
        this.tilesToHorizontal = tilesToHorizontal;
        this.tilesToVertical = tilesToVertical;
        this.tilesToThis = tilesToThis;
        this.tilesToThisHorizontal = tilesToThisHorizontal;
        this.tilesToThisVertical = tilesToThisVertical;
    }

    public void warnMissing(Identifier thisId, Set<Identifier> identifiers) {
        for (Set<ExtraCodecs.TagOrElementLocation> entrySet : List.of(this.tilesTo, this.tilesToHorizontal, this.tilesToVertical, this.tilesToThis, this.tilesToThisHorizontal, this.tilesToThisVertical)) {
            for (ExtraCodecs.TagOrElementLocation entry : entrySet) {
                if (entry.tag() || identifiers.contains(entry.id())) continue;
                AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references texture {}, which is missing!", (Object)thisId, (Object)entry.id());
            }
        }
    }

    void inheritFromAncestor(TileTextureMeta other) {
        if (other.borderType().isPresent()) {
            this.borderType = other.borderType;
        }
        this.tags.addAll(other.tags);
        this.tilesTo.addAll(other.tilesTo);
        this.tilesToHorizontal.addAll(other.tilesToHorizontal);
        this.tilesToVertical.addAll(other.tilesToVertical);
        this.tilesToThis.addAll(other.tilesToThis);
        this.tilesToThisHorizontal.addAll(other.tilesToThisHorizontal);
        this.tilesToThisVertical.addAll(other.tilesToThisVertical);
    }

    void substituteTags(Identifier thisId, Map<Identifier, Set<Identifier>> tags) {
        for (Set<ExtraCodecs.TagOrElementLocation> entrySet : List.of(this.tilesTo, this.tilesToHorizontal, this.tilesToVertical, this.tilesToThis, this.tilesToThisHorizontal, this.tilesToThisVertical)) {
            HashSet<ExtraCodecs.TagOrElementLocation> entryTags = new HashSet<ExtraCodecs.TagOrElementLocation>();
            for (ExtraCodecs.TagOrElementLocation entry : entrySet) {
                if (!entry.tag()) continue;
                entryTags.add(entry);
            }
            if (!entryTags.isEmpty()) {
                entrySet.removeAll(entryTags);
            }
            for (ExtraCodecs.TagOrElementLocation entry : entryTags) {
                Set<Identifier> resolvedIds = tags.getOrDefault(entry.id(), Set.of());
                if (resolvedIds.isEmpty()) {
                    AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references tag {}, which is empty", (Object)thisId, (Object)entry.id());
                    continue;
                }
                entrySet.addAll(resolvedIds.stream().map(id -> new ExtraCodecs.TagOrElementLocation(id, false)).toList());
            }
        }
    }

    void applyTilesToThis(Identifier thisId, Map<Identifier, TileTextureMeta> map) {
        for (ExtraCodecs.TagOrElementLocation entryId : this.tilesToThis) {
            if (entryId.tag()) {
                throw new IllegalStateException("tags must be resolved to apply tilesToThis!");
            }
            if (map.containsKey(entryId.id())) {
                map.get((Object)entryId.id()).tilesTo.add(new ExtraCodecs.TagOrElementLocation(thisId, false));
                continue;
            }
            AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references texture {}, which is missing", (Object)thisId, (Object)entryId.id());
        }
        for (ExtraCodecs.TagOrElementLocation entryId : this.tilesToThisHorizontal) {
            if (entryId.tag()) {
                throw new IllegalStateException("tags must be resolved to apply tilesToThis!");
            }
            if (map.containsKey(entryId.id())) {
                map.get((Object)entryId.id()).tilesToHorizontal.add(new ExtraCodecs.TagOrElementLocation(thisId, false));
                continue;
            }
            AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references texture {}, which is missing", (Object)thisId, (Object)entryId.id());
        }
        for (ExtraCodecs.TagOrElementLocation entryId : this.tilesToThisVertical) {
            if (entryId.tag()) {
                throw new IllegalStateException("tags must be resolved to apply tilesToThis!");
            }
            if (map.containsKey(entryId.id())) {
                map.get((Object)entryId.id()).tilesToVertical.add(new ExtraCodecs.TagOrElementLocation(thisId, false));
                continue;
            }
            AntiqueAtlas.LOGGER.warn("[Antique Atlas] Tile texture {} references texture {}, which is missing", (Object)thisId, (Object)entryId.id());
        }
    }

    public TileTexture.Builder toBuilder(Identifier thisId) {
        return new TileTexture.Builder(thisId, this.borderType().orElse(BorderType.OUTER) == BorderType.INNER, this.tilesTo.stream().map(ExtraCodecs.TagOrElementLocation::id).collect(Collectors.toSet()), this.tilesToHorizontal.stream().map(ExtraCodecs.TagOrElementLocation::id).collect(Collectors.toSet()), this.tilesToVertical.stream().map(ExtraCodecs.TagOrElementLocation::id).collect(Collectors.toSet()));
    }

    public Optional<Identifier> parent() {
        return Optional.ofNullable(this.parent);
    }

    public Optional<BorderType> borderType() {
        return Optional.ofNullable(this.borderType);
    }

    public Set<Identifier> tags() {
        return this.tags;
    }

    public Set<ExtraCodecs.TagOrElementLocation> tilesTo() {
        return this.tilesTo;
    }

    public Set<ExtraCodecs.TagOrElementLocation> tilesToHorizontal() {
        return this.tilesToHorizontal;
    }

    public Set<ExtraCodecs.TagOrElementLocation> tilesToVertical() {
        return this.tilesToVertical;
    }

    public Set<ExtraCodecs.TagOrElementLocation> tilesToThis() {
        return this.tilesToThis;
    }

    public Set<ExtraCodecs.TagOrElementLocation> tilesToThisHorizontal() {
        return this.tilesToThisHorizontal;
    }

    public Set<ExtraCodecs.TagOrElementLocation> tilesToThisVertical() {
        return this.tilesToThisVertical;
    }

    public static enum BorderType {
        OUTER,
        INNER;

    }
}
