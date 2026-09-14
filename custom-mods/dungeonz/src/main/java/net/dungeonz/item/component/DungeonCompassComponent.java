package net.dungeonz.item.component;

import java.util.Optional;
import net.minecraft.class_2338;
import net.minecraft.class_9135;
import net.minecraft.class_9139;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

public record DungeonCompassComponent(String dungeonType, boolean hasDungeon, Optional<class_2338> dungeonPos) {

    public static final DungeonCompassComponent DEFAULT = new DungeonCompassComponent("", false, Optional.of(class_2338.method_49637(0D, 0D, 0D)));

    public static final Codec<DungeonCompassComponent> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(Codec.STRING.fieldOf("dungeon_type").forGetter(DungeonCompassComponent::dungeonType),
                    Codec.BOOL.fieldOf("has_dungeon").forGetter(DungeonCompassComponent::hasDungeon), class_2338.field_25064.optionalFieldOf("dungeon_pos").forGetter(DungeonCompassComponent::dungeonPos))
                    .apply(instance, DungeonCompassComponent::new));

    public static final class_9139<ByteBuf, DungeonCompassComponent> PACKET_CODEC = class_9139.method_56436(class_9135.field_48554, DungeonCompassComponent::dungeonType, class_9135.field_48547,
            DungeonCompassComponent::hasDungeon, class_2338.field_48404.method_56433(class_9135::method_56382), DungeonCompassComponent::dungeonPos, DungeonCompassComponent::new);

}
