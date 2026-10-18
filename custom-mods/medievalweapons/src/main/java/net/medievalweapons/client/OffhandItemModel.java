package net.medievalweapons.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.medievalweapons.MedievalMain;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;

@Environment(EnvType.CLIENT)
public record OffhandItemModel(ItemModel base, ItemModel offhand) implements ItemModel {
   @Override
   public void update(ItemStackRenderState stackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, ClientLevel clientLevel, ItemOwner itemOwner, int i) {
      LivingEntity livingEntity = itemOwner == null ? null : itemOwner.asLivingEntity();
      boolean bl = itemDisplayContext != ItemDisplayContext.GUI && livingEntity != null && !livingEntity.getOffhandItem().isEmpty();
      (bl ? this.offhand : this.base).update(stackRenderState, itemStack, itemModelResolver, itemDisplayContext, clientLevel, itemOwner, i);
   }

   @Environment(EnvType.CLIENT)
   public record Unbaked(ItemModel.Unbaked base, ItemModel.Unbaked offhand) implements ItemModel.Unbaked {
      public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
         instance -> instance.group(
            ItemModels.CODEC.fieldOf("base").forGetter(Unbaked::base),
            ItemModels.CODEC.fieldOf("offhand").forGetter(Unbaked::offhand)
         ).apply(instance, Unbaked::new)
      );

      @Override
      public MapCodec<Unbaked> type() {
         return MAP_CODEC;
      }

      @Override
      public ItemModel bake(ItemModel.BakingContext bakingContext, Matrix4fc matrix4fc) {
         return new OffhandItemModel(this.base.bake(bakingContext, matrix4fc), this.offhand.bake(bakingContext, matrix4fc));
      }

      @Override
      public void resolveDependencies(ResolvableModel.Resolver resolver) {
         this.base.resolveDependencies(resolver);
         this.offhand.resolveDependencies(resolver);
      }
   }

   public static void init() {
      ItemModels.ID_MAPPER.put(MedievalMain.identifierOf("offhand"), Unbaked.MAP_CODEC);
   }
}
