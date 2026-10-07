package com.github.legoatoom.connectiblechains.client.render.entity.state;

import java.util.HashSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

public class ChainKnotEntityRenderState extends EntityRenderState {
   public HashSet<ChainKnotEntityRenderState.ChainData> chainDataSet = new HashSet<>();
   public Item sourceItem;

   @Environment(EnvType.CLIENT)
   public static class ChainData {
      public boolean useBaked;
      public Item sourceItem;
      public Vec3 offset = Vec3.ZERO;
      public Vec3 startPos = Vec3.ZERO;
      public Vec3 endPos = Vec3.ZERO;
      public int chainedEntityBlockLight = 0;
      public int chainHolderBlockLight = 0;
      public int chainedEntitySkyLight = 15;
      public int chainHolderSkyLight = 15;
   }
}
