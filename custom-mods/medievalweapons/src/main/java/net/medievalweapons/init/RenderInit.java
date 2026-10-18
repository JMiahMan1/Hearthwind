package net.medievalweapons.init;

import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.medievalweapons.entity.JavelinEntity;
import net.medievalweapons.entity.FranciscaEntity;
import net.medievalweapons.entity.renderer.FranciscaEntityRenderer;
import net.medievalweapons.entity.renderer.JavelinEntityRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

@Environment(EnvType.CLIENT)
public class RenderInit {
   @SuppressWarnings("unchecked")
   public static void init() {
      for (Entry<Identifier, EntityType<?>> entry : EntityInit.ENTITY_TYPES.entrySet()) {
         if (entry.getKey().getPath().contains("javelin")) {
            EntityRendererRegistry.register((EntityType<JavelinEntity>)entry.getValue(), JavelinEntityRenderer::new);
         } else if (entry.getKey().getPath().contains("francisca")) {
            EntityRendererRegistry.register((EntityType<FranciscaEntity>)entry.getValue(), FranciscaEntityRenderer::new);
         }
      }

      ParticleProviderRegistry.getInstance().register(ParticleInit.HEALING_AURA_PARTICLE, ParticleInit.HealingAuraParticle.Factory::new);
   }
}
