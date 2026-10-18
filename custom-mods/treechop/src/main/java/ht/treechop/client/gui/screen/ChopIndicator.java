package ht.treechop.client.gui.screen;

import ht.treechop.TreeChop;
import ht.treechop.TreeChopException;
import ht.treechop.client.Client;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.settings.ClientChopSettings;
import ht.treechop.common.chop.ChopUtil;
import ht.treechop.common.config.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;

public class ChopIndicator {
   private static final double IMAGE_SCALE = 1.0;

   public static void render(GuiGraphicsExtractor gui, int windowWidth, int windowHeight) {
      Minecraft minecraft = Minecraft.getInstance();
      HitResult mouseOver = minecraft.hitResult;
      Player player = minecraft.player;

      try {
         if (Client.isChoppingIndicatorEnabled()
            && player != null
            && !player.isSpectator()
            && minecraft.level != null
            && minecraft.gui.screen() == null
            && mouseOver != null
            && mouseOver.getType() == Type.BLOCK
            && mouseOver instanceof BlockHitResult
            && ChopUtil.playerWantsToChop(player, Client.getChopSettings())) {
            BlockPos blockPos = ((BlockHitResult)mouseOver).getBlockPos();
            if (blockCanBeChopped(blockPos)) {
               boolean mirror = player.getMainArm() == HumanoidArm.LEFT;
               int indicatorCenterX = windowWidth / 2 + (Integer)ConfigHandler.CLIENT.indicatorXOffset.get() * (mirror ? -1 : 1);
               int indicatorCenterY = windowHeight / 2 + (Integer)ConfigHandler.CLIENT.indicatorYOffset.get();
               Sprite sprite = Sprite.CHOP_INDICATOR;
               int imageWidth = (int)(sprite.width * 1.0);
               int imageHeight = (int)(sprite.height * 1.0);
               sprite.blit(gui, net.minecraft.client.renderer.RenderPipelines.CROSSHAIR, indicatorCenterX - imageWidth / 2, indicatorCenterY - imageHeight / 2, imageWidth, imageHeight, mirror, 1.0F);
            }
         }
      } catch (Exception var13) {
         TreeChop.cry(var13);
      }
   }

   public static boolean blockCanBeChopped(BlockPos pos) throws TreeChopException {
      Minecraft minecraft = Minecraft.getInstance();
      LocalPlayer player = minecraft.player;
      ClientLevel level = minecraft.level;
      ClientChopSettings chopSettings = Client.getChopSettings();
      if (player != null && level != null) {
         boolean wantToChop = ChopUtil.canChopWithTool(player, level, pos) && ChopUtil.playerWantsToChop(minecraft.player, chopSettings);
         return wantToChop ? isAProperTree(pos, level, chopSettings) : false;
      } else {
         return false;
      }
   }

   private static boolean isAProperTree(BlockPos pos, ClientLevel level, ClientChopSettings chopSettings) throws TreeChopException {
      try {
         return Client.treeCache.getTree(level, pos).isAProperTree(chopSettings.getTreesMustHaveLeaves());
      } catch (Exception var4) {
         throw new TreeChopException(String.format("Parameters: %s, %s, %s", pos, level, chopSettings), var4);
      }
   }
}
