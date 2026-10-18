package com.chailotl.particular.compat;

import com.chailotl.particular.Main;
import com.chailotl.particular.Particles;
import java.awt.Color;
import net.minecraft.resources.Identifier;

public class Traverse {
   private static String MOD_ID = "traverse";

   private static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath(MOD_ID, path);
   }

   public static void addLeaves() {
      Main.registerLeafData(id("brown_autumnal_leaves"), new Main.LeafData(Particles.WHITE_OAK_LEAF, new Color(7555879)));
      Main.registerLeafData(id("red_autumnal_leaves"), new Main.LeafData(Particles.WHITE_OAK_LEAF, new Color(11945008)));
      Main.registerLeafData(id("orange_autumnal_leaves"), new Main.LeafData(Particles.WHITE_OAK_LEAF, new Color(15699741)));
      Main.registerLeafData(id("yellow_autumnal_leaves"), new Main.LeafData(Particles.WHITE_OAK_LEAF, new Color(15323441)));
      Main.registerLeafData(id("fir_leaves"), new Main.LeafData(Particles.WHITE_SPRUCE_LEAF, new Color(1787673)));
   }
}
