package net.dungeonz.item.component;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

public record DungeonCompassComponent(String dungeonType, boolean hasDungeon, Optional<BlockPos> dungeonPos) {

    public static final DungeonCompassComponent DEFAULT = new DungeonCompassComponent("", false, Optional.of(BlockPos.containing(0D, 0D, 0D)));

    public static final Codec<DungeonCompassComponent> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(Codec.STRING.fieldOf("dungeon_type").forGetter(DungeonCompassComponent::dungeonType),
                    Codec.BOOL.fieldOf("has_dungeon").forGetter(DungeonCompassComponent::hasDungeon), BlockPos.CODEC.optionalFieldOf("dungeon_pos").forGetter(DungeonCompassComponent::dungeonPos))
                    .apply(instance, DungeonCompassComponent::new));

    public static final StreamCodec<ByteBuf, DungeonCompassComponent> PACKET_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8, DungeonCompassComponent::dungeonType, ByteBufCodecs.BOOL,
            DungeonCompassComponent::hasDungeon, BlockPos.STREAM_CODEC.apply(ByteBufCodecs::optional), DungeonCompassComponent::dungeonPos, DungeonCompassComponent::new);

}
