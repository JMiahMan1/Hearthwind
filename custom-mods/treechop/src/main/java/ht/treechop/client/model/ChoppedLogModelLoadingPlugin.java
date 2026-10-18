package ht.treechop.client.model;

import ht.treechop.common.registry.FabricModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

@Environment(EnvType.CLIENT)
public class ChoppedLogModelLoadingPlugin implements ModelLoadingPlugin {
   @Override
   public void initialize(Context context) {
      context.modifyBlockModelBeforeBake().register(
         (model, ctx) -> ctx.state().is(FabricModBlocks.CHOPPED_LOG) ? new ChoppedLogUnbakedModel() : model
      );
   }
}
