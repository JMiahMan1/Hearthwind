package com.talhanation.smallships;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;

/**
 * 26.2 removed vanilla EntityDataSerializers.COMPOUND_TAG, but ships sync a
 * CompoundTag of tweakable attributes (Ship.ATTRIBUTES / Ship.SHIELD_DATA).
 * Re-register an equivalent serializer under a smallships-owned id.
 */
public final class ModEntityDataSerializers {
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG =
            EntityDataSerializer.forValueType(ByteBufCodecs.TRUSTED_COMPOUND_TAG);

    static {
        EntityDataSerializers.registerSerializer(COMPOUND_TAG);
    }

    private ModEntityDataSerializers() {}
}
