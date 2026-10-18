package ht.treechop.client.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class ChoppedLogUnbakedModel implements BlockStateModel.UnbakedRoot {
   public static final Identifier DEFAULT_TEXTURE_RESOURCE = Identifier.withDefaultNamespace("block/stripped_oak_log");

   @Override
   public BlockStateModel bake(BlockState state, ModelBaker baker) {
      Material.Baked material = baker.materials().get(new Material(DEFAULT_TEXTURE_RESOURCE), () -> "treechop");
      return new ChoppedLogModel(material);
   }

   @Override
   public void resolveDependencies(ResolvableModel.Resolver resolver) {
   }

   @Override
   public Object visualEqualityGroup(BlockState state) {
      return this;
   }
}
