package folk.sisby.antique_atlas.util;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

public record GuiAtlasCanvas(GuiGraphicsExtractor graphics) implements AtlasCanvas {
	@Override
	public void pushPose() {
		this.graphics.pose().pushMatrix();
	}

	@Override
	public void popPose() {
		this.graphics.pose().popMatrix();
	}

	@Override
	public void translate(double x, double y, double z) {
		this.graphics.pose().translate((float) x, (float) y);
	}

	@Override
	public void scale(float x, float y, float z) {
		this.graphics.pose().scale(x, y);
	}

	@Override
	public void rotate(float degrees) {
		this.graphics.pose().rotate(degrees * (float) (Math.PI / 180.0));
	}

	@Override
	public void quad(Identifier texture, int textureWidth, int textureHeight, float x, float y, float z, float width, float height, float u, float v, float regionWidth, float regionHeight, int argb, boolean transparent, int light) {
		this.graphics.blit(RenderPipelines.GUI_TEXTURED, texture, Math.round(x), Math.round(y), u, v, Math.round(width), Math.round(height), textureWidth, textureHeight, argb);
	}

	@Override
	public void fill(float z, int light, int x1, int y1, int x2, int y2, float alpha, float[] color) {
		this.graphics.fill(x1, y1, x2, y2, ARGB.color((int) (alpha * 255.0F), (int) (color[0] * 255.0F), (int) (color[1] * 255.0F), (int) (color[2] * 255.0F)));
	}
}
