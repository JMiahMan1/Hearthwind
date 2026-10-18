package folk.sisby.antique_atlas.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public record AtlasItemModel(ItemModel base, ItemModel atlas) implements ItemModel {
   @Override
   public void update(ItemStackRenderState stackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, ClientLevel clientLevel, ItemOwner itemOwner, int i) {
      (AntiqueAtlas.isHandheldAtlas(itemStack) ? this.atlas : this.base).update(stackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, itemOwner, i);
   }

   @Environment(EnvType.CLIENT)
   public record Unbaked(ItemModel.Unbaked base, ItemModel.Unbaked atlas) implements ItemModel.Unbaked {
      public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ItemModels.CODEC.fieldOf("base").forGetter(Unbaked::base), ItemModels.CODEC.fieldOf("atlas").forGetter(Unbaked::atlas)).apply(instance, Unbaked::new));

      @Override
      public MapCodec<Unbaked> type() {
         return MAP_CODEC;
      }

      @Override
      public ItemModel bake(ItemModel.BakingContext bakingContext, Matrix4fc matrix4fc) {
         return new AtlasItemModel(this.base.bake(bakingContext, matrix4fc), this.atlas.bake(bakingContext, matrix4fc));
      }

      @Override
      public void resolveDependencies(ResolvableModel.Resolver resolver) {
         this.base.resolveDependencies(resolver);
         this.atlas.resolveDependencies(resolver);
      }
   }

   public static void init() {
      ItemModels.ID_MAPPER.put(AntiqueAtlas.id("atlas"), Unbaked.MAP_CODEC);
   }
}
