package folk.sisby.antique_atlas.util;

import net.minecraft.resources.Identifier;

public class DrawBatcher implements AutoCloseable {
	private final AtlasCanvas canvas;
	private final Identifier texture;
	private final int textureWidth;
	private final int textureHeight;
	private final int light;
	private final boolean transparent;

	public static void drawSingle(AtlasCanvas canvas, Identifier texture, int textureWidth, int textureHeight, int light, int x, int y, float z, int width, int height, int u, int v, int regionWidth, int regionHeight, int argb, boolean transparent) {
		try (DrawBatcher batcher = new DrawBatcher(canvas, texture, textureWidth, textureHeight, light, transparent)) {
			batcher.add(x, y, z, width, height, u, v, regionWidth, regionHeight, argb);
		}
	}

	public DrawBatcher(AtlasCanvas canvas, Identifier texture, int textureWidth, int textureHeight, int light, boolean transparent) {
		this.canvas = canvas;
		this.texture = texture;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.light = light;
		this.transparent = transparent;
	}

	public void add(int x, int y, float z, int width, int height, int u, int v, int regionWidth, int regionHeight, int argb) {
		this.canvas.quad(this.texture, this.textureWidth, this.textureHeight, x, y, z, width, height, u, v, regionWidth, regionHeight, argb, this.transparent, this.light);
	}

	@Override
	public void close() {
	}
}
