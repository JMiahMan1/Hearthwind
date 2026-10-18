package folk.sisby.antique_atlas.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public record WorldAtlasCanvas(PoseStack stack, SubmitNodeCollector collector) implements AtlasCanvas {
	@Override
	public void pushPose() {
		this.stack.pushPose();
	}

	@Override
	public void popPose() {
		this.stack.popPose();
	}

	@Override
	public void translate(double x, double y, double z) {
		this.stack.translate(x, y, z);
	}

	@Override
	public void scale(float x, float y, float z) {
		this.stack.scale(x, y, z);
	}

	@Override
	public void rotate(float degrees) {
		this.stack.mulPose(Axis.ZP.rotationDegrees(degrees));
	}

	@Override
	public void quad(Identifier texture, int textureWidth, int textureHeight, float x, float y, float z, float width, float height, float u, float v, float regionWidth, float regionHeight, int argb, boolean transparent, int light) {
		this.collector.submitCustomGeometry(this.stack, RenderTypes.text(texture), (pose, vertexConsumer) -> {
			float u0 = u / textureWidth;
			float u1 = (u + regionWidth) / textureWidth;
			float v0 = v / textureHeight;
			float v1 = (v + regionHeight) / textureHeight;
			float x1 = x + width;
			float y1 = y + height;
			vertexConsumer.addVertex(pose, x, y, z).setColor(argb).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vertexConsumer.addVertex(pose, x, y1, z).setColor(argb).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vertexConsumer.addVertex(pose, x1, y1, z).setColor(argb).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
			vertexConsumer.addVertex(pose, x1, y, z).setColor(argb).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
		});
	}

	@Override
	public void fill(float z, int light, int x1, int y1, int x2, int y2, float alpha, float[] color) {
		this.collector.submitCustomGeometry(this.stack, RenderTypes.textBackgroundSeeThrough(), (pose, vertexConsumer) -> {
			vertexConsumer.addVertex(pose, x1, y1, z).setColor(color[0], color[1], color[2], alpha).setLight(light);
			vertexConsumer.addVertex(pose, x1, y2, z).setColor(color[0], color[1], color[2], alpha).setLight(light);
			vertexConsumer.addVertex(pose, x2, y2, z).setColor(color[0], color[1], color[2], alpha).setLight(light);
			vertexConsumer.addVertex(pose, x2, y1, z).setColor(color[0], color[1], color[2], alpha).setLight(light);
		});
	}
}
