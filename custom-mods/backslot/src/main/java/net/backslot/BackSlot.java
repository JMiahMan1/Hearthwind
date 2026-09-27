package net.backslot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clean-room 26.2 port of BackSlot 1.2.15 (GPL-3.0, by Globox1997). The
 * behaviour was reconstructed from the shipped jar; no upstream source was
 * used. Storage lives in two player attachments instead of hidden inventory
 * indexes so entity sync and persistence come from the attachment API.
 */
public class BackSlot implements ModInitializer {

    public static final String MOD_ID = "backslot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Slot ids used on the wire; they match the upstream inventory indexes. */
    public static final int BACK_SLOT = 41;
    public static final int BELT_SLOT = 42;

    public static BackSlotConfig CONFIG = new BackSlotConfig();

    public static final AttachmentType<ItemStack> BACK_ITEM = AttachmentRegistry.<ItemStack>builder()
            .persistent(ItemStack.OPTIONAL_CODEC)
            .copyOnDeath()
            .initializer(() -> ItemStack.EMPTY)
            .syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.all())
            .buildAndRegister(id("back_item"));

    public static final AttachmentType<ItemStack> BELT_ITEM = AttachmentRegistry.<ItemStack>builder()
            .persistent(ItemStack.OPTIONAL_CODEC)
            .copyOnDeath()
            .initializer(() -> ItemStack.EMPTY)
            .syncWith(ItemStack.OPTIONAL_STREAM_CODEC, AttachmentSyncPredicate.all())
            .buildAndRegister(id("belt_item"));

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        CONFIG = BackSlotConfig.load(FabricLoader.getInstance().getConfigDir());
        BackSlotNetworking.register();
        LOGGER.info("BackSlot initialized: back at ({}, {}), belt at ({}, {})",
                CONFIG.backSlotX, CONFIG.backSlotY, CONFIG.beltSlotX, CONFIG.beltSlotY);
    }
}
