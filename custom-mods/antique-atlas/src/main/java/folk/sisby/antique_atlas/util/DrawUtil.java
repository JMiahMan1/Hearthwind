package folk.sisby.antique_atlas.util;

import net.minecraft.resources.Identifier;

public class DrawUtil {
	public static void drawCenteredWithRotation(AtlasCanvas canvas, Identifier texture, double x, double y, float z, float scale, int textureWidth, int textureHeight, float rotation, int light, int argb) {
		canvas.pushPose();
		canvas.translate(x, y, 0.0);
		canvas.scale(scale, scale, 1.0F);
		canvas.rotate(180.0F + rotation);
		canvas.translate(-textureWidth / 2.0F, -textureHeight / 2.0F, 0.0);
		DrawBatcher.drawSingle(canvas, texture, textureWidth, textureHeight, light, 0, 0, z, textureWidth, textureHeight, 0, 0, textureWidth, textureHeight, argb, true);
		canvas.popPose();
	}

	public static void fill(AtlasCanvas canvas, float z, int light, int x1, int y1, int x2, int y2, float alpha, float[] color) {
		canvas.fill(z, light, x1, y1, x2, y2, alpha, color);
	}
}
