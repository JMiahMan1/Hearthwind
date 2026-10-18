package folk.sisby.antique_atlas.util;

import net.minecraft.client.renderer.Rect2i;
import org.joml.Vector2d;

public class MathUtil {
   public static int roundToBase(int a, int base) {
      return a - a % base;
   }

   public static int roundToBase(double a, double base) {
      return (int)(a - a % base);
   }

   public static double innerDistanceToEdge(Rect2i rect, Vector2d point) {
      double dx = Math.min(point.x - rect.getX(), rect.getX() + rect.getWidth() - point.x);
      double dy = Math.min(point.y - rect.getY(), rect.getY() + rect.getHeight() - point.y);
      return Math.min(dx, dy);
   }
}
