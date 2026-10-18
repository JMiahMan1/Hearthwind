package folk.sisby.antique_atlas.reloader;
import net.minecraft.server.packs.metadata.MetadataSectionType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.MarkerTexture;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record MarkerTextureMeta(Optional<Identifier> item, Optional<Integer> textureWidth, Optional<Integer> textureHeight, Optional<Integer> mipLevels, Optional<Integer> offsetX, Optional<Integer> offsetY, Optional<Integer> nearClip, Optional<Integer> farClip) {
    public static final MarkerTextureMeta DEFAULT = new MarkerTextureMeta(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    public static final Codec<MarkerTextureMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(Identifier.CODEC.optionalFieldOf("item").forGetter(MarkerTextureMeta::item), Codec.INT.optionalFieldOf("textureWidth").forGetter(MarkerTextureMeta::textureWidth), Codec.INT.optionalFieldOf("textureHeight").forGetter(MarkerTextureMeta::textureHeight), Codec.INT.optionalFieldOf("mipLevels").forGetter(MarkerTextureMeta::mipLevels), Codec.INT.optionalFieldOf("offsetX").forGetter(MarkerTextureMeta::offsetX), Codec.INT.optionalFieldOf("offsetY").forGetter(MarkerTextureMeta::offsetY), Codec.INT.optionalFieldOf("nearClip").forGetter(MarkerTextureMeta::nearClip), Codec.INT.optionalFieldOf("farClip").forGetter(MarkerTextureMeta::farClip)).apply(instance, MarkerTextureMeta::new));
    public static final MetadataSectionType<MarkerTextureMeta> METADATA = new MetadataSectionType<>(AntiqueAtlas.id("marker").toString(), CODEC);

    public MarkerTexture build(Identifier id, boolean accent) {
        int textureWidth = this.textureWidth.orElse(32);
        int textureHeight = this.textureHeight.orElse(32);
        int mipLevels = this.mipLevels.orElse(0);
        int offsetX = this.offsetX.orElse(-textureWidth / 2);
        int offsetY = this.offsetY.orElse(-textureHeight / 2);
        int nearClip = this.nearClip.orElse(1);
        int farClip = this.farClip.orElse(Integer.MAX_VALUE);
        Identifier item = this.item.orElse(null);
        return MarkerTexture.ofId(id, item, offsetX, offsetY, textureWidth, textureHeight, mipLevels, nearClip, farClip, accent);
    }
}
