package dev.jmiahman.hearthwind.client.render;

import dev.jmiahman.hearthwind.world.fauna.NaturalistFauna;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Universal Fauna Entity Renderer for Hearthwind & Naturalist Wildlife (Minecraft 26.2).
 */
public class FaunaEntityRenderer<T extends Mob> extends MobRenderer<T, LivingEntityRenderState, QuadrupedModel<LivingEntityRenderState>> {
    private final Identifier texture;

    public FaunaEntityRenderer(EntityRendererProvider.Context context, String mobName, ModelLayerLocation layer, float shadow) {
        super(context, new CowModel(context.bakeLayer(layer)), shadow);
        this.texture = Identifier.fromNamespaceAndPath(NaturalistFauna.MOD_ID, "textures/entity/" + mobName + "/" + mobName + ".png");
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return this.texture;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @SuppressWarnings("unchecked")
    public static void registerAll() {
        for (var entry : NaturalistFauna.ENTITIES.entrySet()) {
            String name = entry.getKey();
            EntityType<? extends Mob> type = (EntityType<? extends Mob>) entry.getValue();
            if (type != null) {
                EntityRendererRegistry.register(type, context ->
                        new FaunaEntityRenderer<>(context, name, ModelLayers.COW, 0.5f));
            }
        }
    }
}
