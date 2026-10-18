package folk.sisby.antique_atlas.util;

import net.minecraft.resources.Identifier;

public interface AtlasCanvas {
	void pushPose();

	void popPose();

	void translate(double x, double y, double z);

	void scale(float x, float y, float z);

	void rotate(float degrees);

	void quad(Identifier texture, int textureWidth, int textureHeight, float x, float y, float z, float width, float height, float u, float v, float regionWidth, float regionHeight, int argb, boolean transparent, int light);

	void fill(float z, int light, int x1, int y1, int x2, int y2, float alpha, float[] color);
}
