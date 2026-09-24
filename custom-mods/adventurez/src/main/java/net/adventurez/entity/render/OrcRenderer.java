package net.adventurez.entity.render;

import net.adventurez.entity.OrcEntity;
import net.adventurez.entity.model.OrcModel;
import net.adventurez.entity.render.feature.OrcInventoryFeatureRenderer;
import net.adventurez.init.RenderInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class OrcRenderer extends MobRenderer<OrcEntity, AdventureRenderState, OrcModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("adventurez", "textures/entity/orc.png");

    public OrcRenderer(EntityRendererProvider.Context context) {
        super(context, new OrcModel(context.bakeLayer(RenderInit.ORC_LAYER)), 0.7F);
        this.addLayer(new OrcInventoryFeatureRenderer(this));
    }

    @Override
    public AdventureRenderState createRenderState() {
        return new AdventureRenderState();
    }

    @Override
    public void extractRenderState(OrcEntity entity, AdventureRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.capture(entity, partialTicks);
        state.size = entity.getSize();
        int itemId = entity.getEntityData().get(OrcEntity.INVENTORY_ITEM_ID);
        if (!entity.isBigOrc() && itemId != -1) {
            this.itemModelResolver.updateForLiving(state.inventoryItem, new net.minecraft.world.item.ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.byId(itemId)), net.minecraft.world.item.ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, entity);
        } else {
            state.inventoryItem.clear();
        }
    }

    @Override
    protected void scale(AdventureRenderState state, PoseStack poseStack) {
        float scale = 0.55F * state.size;
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public Identifier getTextureLocation(AdventureRenderState state) {
        return TEXTURE;
    }
}
