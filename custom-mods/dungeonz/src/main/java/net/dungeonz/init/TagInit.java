package net.dungeonz.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;

public class TagInit {

    public static final TagKey<EntityType<?>> IMMUNE_TO_ZOMBIFICATION = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("dungeonz", "immune_to_zombification"));

    public static void init() {
    }

}
