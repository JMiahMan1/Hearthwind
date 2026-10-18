package net.smitherz.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.libz.access.ScreenHandlerAccess;
import net.libz.network.LibzServerPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.smitherz.network.packet.ScreenPacket;
import net.smitherz.network.packet.SmithPacket;
import net.smitherz.network.packet.SmithUpdatePacket;
import net.smitherz.screen.GrinderScreenHandler;
import net.smitherz.screen.SmitherScreenHandler;

public class SmitherServerPacket {
   public static void init() {
      PayloadTypeRegistry.clientboundPlay().register(SmithUpdatePacket.PACKET_ID, SmithUpdatePacket.PACKET_CODEC);
      PayloadTypeRegistry.serverboundPlay().register(ScreenPacket.PACKET_ID, ScreenPacket.PACKET_CODEC);
      PayloadTypeRegistry.serverboundPlay().register(SmithPacket.PACKET_ID, SmithPacket.PACKET_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(
         ScreenPacket.PACKET_ID,
         (payload, context) -> {
            int mouseX = payload.mouseX();
            int mouseY = payload.mouseY();
            int screenId = payload.screenId();
            BlockPos pos = context.player().containerMenu instanceof ScreenHandlerAccess ? ((ScreenHandlerAccess)context.player().containerMenu).getPos() : null;
            if (pos != null) {
               context.server()
                  .execute(
                     () -> {
                        if (screenId == 1) {
                           context.player()
                              .openMenu(
                                 new SimpleMenuProvider(
                                    (syncId, playerInventory, playerx) -> new SmitherScreenHandler(
                                       syncId, playerInventory, ContainerLevelAccess.create(playerx.level(), pos)
                                    ),
                                    Component.translatable("container.link")
                                 )
                              );
                        } else if (screenId == 2) {
                           context.player()
                              .openMenu(
                                 new SimpleMenuProvider(
                                    (syncId, playerInventory, playerx) -> new SmithingMenu(
                                       syncId, playerInventory, ContainerLevelAccess.create(playerx.level(), pos)
                                    ),
                                    Component.translatable("container.upgrade")
                                 )
                              );
                        } else if (screenId == 3) {
                           context.player()
                              .openMenu(
                                 new SimpleMenuProvider(
                                    (syncId, playerInventory, playerx) -> new GrindstoneMenu(
                                       syncId, playerInventory, ContainerLevelAccess.create(playerx.level(), pos)
                                    ),
                                    Component.translatable("container.grindstone_title")
                                 )
                              );
                        } else if (screenId == 4) {
                           context.player()
                              .openMenu(
                                 new SimpleMenuProvider(
                                    (syncId, playerInventory, playerx) -> new GrinderScreenHandler(
                                       syncId, playerInventory, ContainerLevelAccess.create(playerx.level(), pos)
                                    ),
                                    Component.translatable("container.unlink")
                                 )
                              );
                        }

                        LibzServerPacket.writeS2CMousePositionPacket(context.player(), mouseX, mouseY);
                     }
                  );
            }
         }
      );
      ServerPlayNetworking.registerGlobalReceiver(SmithPacket.PACKET_ID, (payload, context) -> context.server().execute(() -> {
         if (context.player().containerMenu instanceof SmitherScreenHandler smitherScreenHandler) {
            smitherScreenHandler.smith();
         }
      }));
   }

   public static void writeS2CSmitherReadyPacket(ServerPlayer serverPlayerEntity, boolean disableButton) {
      ServerPlayNetworking.send(serverPlayerEntity, new SmithUpdatePacket(disableButton));
   }
}
