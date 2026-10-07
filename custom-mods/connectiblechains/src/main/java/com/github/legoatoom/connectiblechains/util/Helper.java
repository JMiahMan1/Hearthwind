package com.github.legoatoom.connectiblechains.util;

import com.github.legoatoom.connectiblechains.ConnectibleChains;
import com.google.common.collect.BiMap;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Helper {
   public static boolean isOxidizableSourceItem(Item item) {
      if (!(item instanceof BlockItem blockItem)) {
         return false;
      } else {
         Block block = blockItem.getBlock();
         return block instanceof WeatheringCopper || ((BiMap)HoneycombItem.WAX_OFF_BY_BLOCK.get()).containsKey(block);
      }
   }

   public static Identifier identifier(String name) {
      return Identifier.fromNamespaceAndPath("connectiblechains", name);
   }

   @Deprecated
   public static double drip(double x, double d) {
      double c = ConnectibleChains.runtimeConfig.getChainHangAmount();
      double b = -c / d;
      double a = c / (d * d);
      return a * (x * x) + b * x;
   }

   public static double drip2(double x, double d, double h) {
      double a = ConnectibleChains.runtimeConfig.getChainHangAmount();
      a += d * 0.3;
      double p1 = a * asinh(h / (2.0 * a) * (1.0 / Math.sinh(d / (2.0 * a))));
      double p2 = -a * Math.cosh((2.0 * p1 - d) / (2.0 * a));
      return p2 + a * Math.cosh((2.0 * x + 2.0 * p1 - d) / (2.0 * a));
   }

   private static double asinh(double x) {
      return Math.log(x + Math.sqrt(x * x + 1.0));
   }

   public static double drip2prime(double x, double d, double h) {
      double a = ConnectibleChains.runtimeConfig.getChainHangAmount();
      double p1 = a * asinh(h / (2.0 * a) * (1.0 / Math.sinh(d / (2.0 * a))));
      return Math.sinh((2.0 * x + 2.0 * p1 - d) / (2.0 * a));
   }

   @Deprecated
   public static Vec3 getChainOffset(Vec3 start, Vec3 end) {
      Vector3f offset = end.subtract(start).toVector3f();
      offset.set(offset.x(), 0.0F, offset.z());
      offset.normalize();
      offset.normalize(0.125F);
      return new Vec3(offset);
   }

   public static boolean hasShiftDown() {
      return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)
         || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 344);
   }
}
