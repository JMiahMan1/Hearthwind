package de.keksuccino.loadmyresources.mixin;

import de.keksuccino.loadmyresources.pack.PackHandler;
import java.util.Iterator;
import java.util.stream.Stream;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({TransferableSelectionList.class})
public class MixinTransferableSelectionList {
   @Inject(at = {@At("TAIL")}, method = {"updateList"})
   private void onUpdatePackList(Stream<PackSelectionModel.Entry> packs, PackSelectionModel.EntryBase base, CallbackInfo info) {
      Iterator<TransferableSelectionList.Entry> iterator = ((TransferableSelectionList)(Object)this).children().iterator();

      while (iterator.hasNext()) {
         if (PackHandler.PACK_NAME.equals(iterator.next().getPackId())) {
            iterator.remove();
         }
      }
   }
}
