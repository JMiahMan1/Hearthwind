package io.github.hexagonnico.spidercaves.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.hexagonnico.spidercaves.SpiderCaves;
import io.github.hexagonnico.spidercaves.entities.BlackRecluse;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class BlackRecluseRenderer extends net.minecraft.client.renderer.entity.SpiderRenderer<BlackRecluse> {
   private static final Identifier TEXTURE_LOCATION = Identifier.fromNamespaceAndPath(SpiderCaves.modId(), "textures/entity/black_recluse.png");

   public BlackRecluseRenderer(Context context) {
      super(context);
   }

   @NotNull
   public Identifier getTextureLocation(@NotNull net.minecraft.client.renderer.entity.state.LivingEntityRenderState state) {
      return TEXTURE_LOCATION;
   }
}
