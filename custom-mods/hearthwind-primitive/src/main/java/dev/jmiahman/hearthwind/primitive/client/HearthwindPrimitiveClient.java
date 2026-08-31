package dev.jmiahman.hearthwind.primitive.client;

import dev.jmiahman.hearthwind.primitive.HearthwindPrimitiveBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.Identifier;

/** Client-side renderers for the primitive module. */
public class HearthwindPrimitiveClient implements ClientModInitializer {

    public static final Identifier EXTRA_SLOT_ICON =
            Identifier.fromNamespaceAndPath("earlystage", "textures/gui/blast_furnace_extra_slot.png");

    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(HearthwindPrimitiveBlocks.SIEVE_ENTITY, SieveBlockRenderer::new);
        BlockEntityRenderers.register(HearthwindPrimitiveBlocks.CRAFTING_ROCK_ENTITY,
                CraftingRockBlockEntityRenderer::new);
        dev.jmiahman.hearthwind.primitive.tiered.TieredTooltips.init();
    }
}
