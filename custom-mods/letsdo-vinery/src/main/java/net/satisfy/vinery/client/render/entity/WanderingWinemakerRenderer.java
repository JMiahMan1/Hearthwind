package net.satisfy.vinery.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.resources.Identifier;
import net.satisfy.vinery.core.Vinery;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class WanderingWinemakerRenderer extends WanderingTraderRenderer {
	private static final Identifier TEXTURE = Vinery.identifier("textures/entity/wandering_winemaker.png");
	
	public WanderingWinemakerRenderer(EntityRendererProvider.Context context) {
		super(context);
	}
	
	@Override
	public @NotNull Identifier getTextureLocation(net.minecraft.client.renderer.entity.state.VillagerRenderState state) {
		return TEXTURE;
	}
}