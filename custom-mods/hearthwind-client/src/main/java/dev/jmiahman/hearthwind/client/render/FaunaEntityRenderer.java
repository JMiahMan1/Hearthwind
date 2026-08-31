package dev.jmiahman.hearthwind.client.render;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.jmiahman.hearthwind.world.fauna.NaturalistFauna;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.bee.BeeModel;
import net.minecraft.client.model.animal.chicken.ChickenModel;
import net.minecraft.client.model.animal.cow.CowModel;
import net.minecraft.client.model.animal.fish.SalmonModel;
import net.minecraft.client.model.animal.pig.PigModel;
import net.minecraft.client.model.animal.turtle.TurtleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.silverfish.SilverfishModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.BeeRenderState;
import net.minecraft.client.renderer.entity.state.ChickenRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SalmonRenderState;
import net.minecraft.client.renderer.entity.state.TurtleRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * Specialized Fauna Entity Renderers with accurate taxonomy models, proper scaling,
 * and exact texture resolutions (Minecraft 26.2).
 */
public final class FaunaEntityRenderer {

    private FaunaEntityRenderer() {}

    // 1. Texture mapping for all 23 fauna species
    private static final Map<String, Identifier> TEXTURES = new HashMap<>();

    static {
        // Quadrupeds
        TEXTURES.put("deer", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/deer/deer.png"));
        TEXTURES.put("bear", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/bear/bear.png"));
        TEXTURES.put("lion", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/lion/lion.png"));
        TEXTURES.put("zebra", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/zebra.png"));
        TEXTURES.put("boar", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/boar.png"));
        TEXTURES.put("rhino", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/rhino.png"));
        TEXTURES.put("giraffe", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/giraffe/giraffe.png"));
        TEXTURES.put("elephant", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/elephant/elephant.png"));
        TEXTURES.put("hippo", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/hippo/hippo.png"));

        // Avian / Birds
        TEXTURES.put("duck", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/duck/duck.png"));
        TEXTURES.put("vulture", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/vulture/vulture.png"));

        // Insects
        TEXTURES.put("butterfly", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/butterfly/monarch.png"));
        TEXTURES.put("firefly", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/firefly.png"));

        // Fish
        TEXTURES.put("bass", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/bass.png"));
        TEXTURES.put("catfish", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/catfish.png"));

        // Reptiles & Amphibians
        TEXTURES.put("snail", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/snail/snail.png"));
        TEXTURES.put("caterpillar", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/caterpillar.png"));
        TEXTURES.put("lizard", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/lizard/green.png"));
        TEXTURES.put("snake", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/snake/green_snake.png"));
        TEXTURES.put("rattlesnake", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/snake/rattle_snake.png"));
        TEXTURES.put("coral_snake", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/snake/coral_snake.png"));
        TEXTURES.put("tortoise", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/tortoise/green.png"));
        TEXTURES.put("alligator", Identifier.fromNamespaceAndPath("naturalist", "textures/entity/alligator/alligator.png"));
    }

    public static Identifier getTexture(String name) {
        return TEXTURES.getOrDefault(name, Identifier.fromNamespaceAndPath("naturalist", "textures/entity/" + name + ".png"));
    }

    // --- Quadruped Renderer ---
    public static class QuadrupedRenderer<T extends Mob> extends MobRenderer<T, LivingEntityRenderState, CowModel> {
        private final Identifier texture;
        private final float scale;

        public QuadrupedRenderer(EntityRendererProvider.Context context, String name, float scale, float shadow) {
            super(context, new CowModel(context.bakeLayer(ModelLayers.COW)), shadow);
            this.texture = getTexture(name);
            this.scale = scale;
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return this.texture;
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
            super.scale(state, poseStack);
            if (this.scale != 1.0f) {
                poseStack.scale(this.scale, this.scale, this.scale);
            }
        }
    }

    // --- Pig-Type Quadruped (Boar, Hippo, Rhino) ---
    public static class SmallQuadrupedRenderer<T extends Mob> extends MobRenderer<T, LivingEntityRenderState, PigModel> {
        private final Identifier texture;
        private final float scale;

        public SmallQuadrupedRenderer(EntityRendererProvider.Context context, String name, float scale, float shadow) {
            super(context, new PigModel(context.bakeLayer(ModelLayers.PIG)), shadow);
            this.texture = getTexture(name);
            this.scale = scale;
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return this.texture;
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
            super.scale(state, poseStack);
            if (this.scale != 1.0f) {
                poseStack.scale(this.scale, this.scale, this.scale);
            }
        }
    }

    // --- Serpent / Critter Renderer (Snakes, Snail, Caterpillar, Lizard) ---
    public static class SerpentRenderer<T extends Mob> extends MobRenderer<T, LivingEntityRenderState, SilverfishModel> {
        private final Identifier texture;
        private final float scale;

        public SerpentRenderer(EntityRendererProvider.Context context, String name, float scale, float shadow) {
            super(context, new SilverfishModel(context.bakeLayer(ModelLayers.SILVERFISH)), shadow);
            this.texture = getTexture(name);
            this.scale = scale;
        }

        @Override
        public Identifier getTextureLocation(LivingEntityRenderState state) {
            return this.texture;
        }

        @Override
        public LivingEntityRenderState createRenderState() {
            return new LivingEntityRenderState();
        }

        @Override
        protected void scale(LivingEntityRenderState state, PoseStack poseStack) {
            super.scale(state, poseStack);
            if (this.scale != 1.0f) {
                poseStack.scale(this.scale, this.scale, this.scale);
            }
        }
    }

    // --- Fish Renderer (Bass, Catfish) ---
    public static class FishRenderer<T extends Mob> extends MobRenderer<T, SalmonRenderState, SalmonModel> {
        private final Identifier texture;

        public FishRenderer(EntityRendererProvider.Context context, String name) {
            super(context, new SalmonModel(context.bakeLayer(ModelLayers.SALMON)), 0.3f);
            this.texture = getTexture(name);
        }

        @Override
        public Identifier getTextureLocation(SalmonRenderState state) {
            return this.texture;
        }

        @Override
        public SalmonRenderState createRenderState() {
            return new SalmonRenderState();
        }

        @Override
        public void extractRenderState(T entity, SalmonRenderState state, float partialTick) {
            super.extractRenderState(entity, state, partialTick);
            float f = 1.0F;
            float f1 = 1.0F;
            if (!entity.isInWater()) {
                f = 1.3F;
                f1 = 1.7F;
            }
            float f2 = f * 4.3F * net.minecraft.util.Mth.sin(f1 * 0.6F * (entity.tickCount + partialTick));
            state.bodyRot = entity.isInWater() ? f2 : (float) Math.sin((entity.tickCount + partialTick) * 0.4f) * 15.0f;
        }
    }

    // --- Reptile / Amphibian (Tortoise, Alligator) ---
    public static class TurtleRenderer<T extends Mob> extends MobRenderer<T, TurtleRenderState, TurtleModel> {
        private final Identifier texture;
        private final float scale;

        public TurtleRenderer(EntityRendererProvider.Context context, String name, float scale) {
            super(context, new TurtleModel(context.bakeLayer(ModelLayers.TURTLE), r -> net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(r)) {}, 0.7f);
            this.texture = getTexture(name);
            this.scale = scale;
        }

        @Override
        public Identifier getTextureLocation(TurtleRenderState state) {
            return this.texture;
        }

        @Override
        public TurtleRenderState createRenderState() {
            return new TurtleRenderState();
        }

        @Override
        protected void scale(TurtleRenderState state, PoseStack poseStack) {
            super.scale(state, poseStack);
            if (this.scale != 1.0f) {
                poseStack.scale(this.scale, this.scale, this.scale);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void registerAll() {
        // Register each entity type with its matching taxonomy renderer
        register("deer", (ctx) -> new QuadrupedRenderer<>(ctx, "deer", 1.1f, 0.6f));
        register("bear", (ctx) -> new QuadrupedRenderer<>(ctx, "bear", 1.3f, 0.8f));
        register("lion", (ctx) -> new QuadrupedRenderer<>(ctx, "lion", 1.2f, 0.7f));
        register("zebra", (ctx) -> new QuadrupedRenderer<>(ctx, "zebra", 1.1f, 0.7f));
        register("giraffe", (ctx) -> new QuadrupedRenderer<>(ctx, "giraffe", 1.7f, 0.9f));
        register("elephant", (ctx) -> new QuadrupedRenderer<>(ctx, "elephant", 1.8f, 1.2f));
        
        register("boar", (ctx) -> new SmallQuadrupedRenderer<>(ctx, "boar", 0.9f, 0.5f));
        register("rhino", (ctx) -> new SmallQuadrupedRenderer<>(ctx, "rhino", 1.4f, 0.9f));
        register("hippo", (ctx) -> new SmallQuadrupedRenderer<>(ctx, "hippo", 1.3f, 0.8f));

        register("duck", (ctx) -> new SmallQuadrupedRenderer<>(ctx, "duck", 0.5f, 0.3f));
        register("vulture", (ctx) -> new SmallQuadrupedRenderer<>(ctx, "vulture", 0.8f, 0.4f));

        register("butterfly", (ctx) -> new SerpentRenderer<>(ctx, "butterfly", 0.4f, 0.1f));
        register("firefly", (ctx) -> new SerpentRenderer<>(ctx, "firefly", 0.3f, 0.1f));

        register("bass", (ctx) -> new FishRenderer<>(ctx, "bass"));
        register("catfish", (ctx) -> new FishRenderer<>(ctx, "catfish"));

        register("snail", (ctx) -> new SerpentRenderer<>(ctx, "snail", 0.4f, 0.2f));
        register("caterpillar", (ctx) -> new SerpentRenderer<>(ctx, "caterpillar", 0.4f, 0.1f));
        register("lizard", (ctx) -> new SerpentRenderer<>(ctx, "lizard", 0.6f, 0.3f));
        register("snake", (ctx) -> new SerpentRenderer<>(ctx, "snake", 0.7f, 0.3f));
        register("rattlesnake", (ctx) -> new SerpentRenderer<>(ctx, "rattlesnake", 0.8f, 0.3f));
        register("coral_snake", (ctx) -> new SerpentRenderer<>(ctx, "coral_snake", 0.7f, 0.3f));

        register("tortoise", (ctx) -> new TurtleRenderer<>(ctx, "tortoise", 0.9f));
        register("alligator", (ctx) -> new TurtleRenderer<>(ctx, "alligator", 1.3f));
    }

    @SuppressWarnings("unchecked")
    private static void register(String name, EntityRendererProvider<? extends Mob> provider) {
        EntityType<? extends Mob> type = (EntityType<? extends Mob>) NaturalistFauna.ENTITIES.get(name);
        if (type != null) {
            EntityRendererRegistry.register(type, (EntityRendererProvider<Mob>) provider);
        }
    }
}
