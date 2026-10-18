package folk.sisby.antique_atlas.util;

import net.minecraft.util.ARGB;

public class ColorUtil {
   public static float[] componentsFromRgb(int color) {
      return new float[]{ARGB.red(color) / 255.0F, ARGB.green(color) / 255.0F, ARGB.blue(color) / 255.0F};
   }

   public static int rgbFromComponents(float[] components) {
      return ARGB.color(255, (int)(255.0F * components[0]), (int)(255.0F * components[1]), (int)(255.0F * components[2]));
   }
}
