package dev.jmiahman.hearthwind.jobs;

import com.mojang.serialization.Codec;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.Identifier;

/**
 * Per-player current Age (0 = Stranded, 1 = Camp, 2 = Copper, ...).
 * Wired to advancements later; currently set via {@code /job age} command.
 */
public final class AgeState {
    private static final AttachmentType<Integer> AGE =
            AttachmentRegistry.<Integer>builder()
                    .persistent(Codec.INT)
                    .copyOnDeath()
                    .buildAndRegister(
                            Identifier.fromNamespaceAndPath("hearthwind_jobs", "age"));

    private AgeState() {}

    public static void set(net.minecraft.world.entity.Entity entity, int age) {
        entity.setAttached(AGE, age);
    }

    public static int get(net.minecraft.world.entity.Entity entity) {
        return entity.getAttachedOrElse(AGE, 0);
    }

    public static AttachmentType<Integer> type() {
        return AGE;
    }
}
