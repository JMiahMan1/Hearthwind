package net.jmb19905.niftycarts.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.jmb19905.niftycarts.NiftyCarts;
import net.jmb19905.niftycarts.NiftyCartsConfig;
import net.jmb19905.niftycarts.client.renderer.entity.model.CargoCartModel;
import net.jmb19905.niftycarts.entity.AbstractCargoCart;
import net.jmb19905.niftycarts.util.NiftyItemUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentClientInfo.LayerType;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public abstract class CargoCartRenderer<T extends AbstractCargoCart, M extends CargoCartModel<CargoCartRenderState>>
   extends DrawnRenderer<T, CargoCartRenderState, M> {
   private final net.minecraft.client.renderer.entity.ArmorModelSet<net.minecraft.client.model.object.armorstand.ArmorStandArmorModel> armorSet;
   private final Context ctx;
   private final EquipmentLayerRenderer equipmentRenderer;
   private final ItemModelResolver itemModelResolver;

   protected CargoCartRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context ctx, M model) {
      super(ctx, model);
      this.armorSet = ArmorModelSet.bake(ModelLayers.ARMOR_STAND_ARMOR, ctx.getModelSet(), ArmorStandArmorModel::new);
      this.ctx = ctx;
      this.shadowRadius = 1.0F;
      this.equipmentRenderer = ctx.getEquipmentRenderer();
      this.itemModelResolver = ctx.getItemModelResolver();
   }

   @NotNull
   public CargoCartRenderState createRenderState() {
      return new CargoCartRenderState();
   }

   public void extractRenderState(T entity, CargoCartRenderState state, float delta) {
      super.extractRenderState(entity, state, delta);
      state.cargo = NonNullList.create();
      state.cargoStates = NonNullList.create();

      for (int i = 0; i < entity.getCargo().size(); i++) {
         state.cargo.add(i, (ItemStack)entity.getCargo().get(i));
         ItemStackRenderState cargoState = new ItemStackRenderState();
         this.itemModelResolver.updateForNonLiving(cargoState, (ItemStack)entity.getCargo().get(i), ItemDisplayContext.NONE, entity);
         state.cargoStates.add(i, cargoState);
      }

      state.rngSeed = entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
      state.level = entity.level();
      state.extraWheel = false;
      state.flowerBasket = false;
      state.armorRenderState = new ArmorStandRenderState();
      state.armorRenderState.isCrouching = false;
      state.armorRenderState.isUsingItem = false;
      state.armorRenderState.isVisuallySwimming = false;
      state.armorRenderState2 = new ArmorStandRenderState();
      state.armorRenderState2.isCrouching = false;
      state.armorRenderState2.isUsingItem = false;
      state.armorRenderState2.isVisuallySwimming = false;
   }

   // $VF: Could not properly define all variable types!
   // Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
   protected void submitContents(CargoCartRenderState state, PoseStack stack, SubmitNodeCollector collector) {
      CargoCartRenderer.Contents contents = CargoCartRenderer.Contents.SUPPLIES;

      Iterator<ItemStack> it = state.cargo.iterator();

      outer:
      while (it.hasNext()) {
         ItemStack s = it.next();
         if (!s.isEmpty()) {
            for (CargoCartRenderer.Contents c : CargoCartRenderer.Contents.values()) {
               if (c.predicate.test(s)) {
                  contents = c;
                  break outer;
               }
            }
         }
      }

      while (contents != CargoCartRenderer.Contents.SUPPLIES && it.hasNext()) {
         ItemStack sx = (ItemStack)it.next();
         if (!sx.isEmpty() && !contents.predicate.test(sx)) {
            contents = CargoCartRenderer.Contents.SUPPLIES;
            if (!contents.predicate.test(sx)) {
               contents = CargoCartRenderer.Contents.NONE;
            }
         }
      }

      stack.pushPose();
      this.model.getBody().translateAndRotate(stack);
      switch (contents) {
         case FLOWERS:
            this.submitFlowers(state, stack, collector, state.cargo);
            break;
         case PAINTINGS:
            this.submitPaintings(state, stack, collector, state.cargo);
            break;
         case WHEEL:
            state.extraWheel = true;
            break;
         case SUPPLIES:
            this.submitSupplies(state, stack, collector, state.cargo);
      }

      if (state.bannerColor != null) {
         stack.translate(0.0, -0.6, 1.5);
         this.submitBanner(state, stack, collector);
      }

      stack.popPose();
   }

   void submitFlowers(
      CargoCartRenderState state,
      com.mojang.blaze3d.vertex.PoseStack stack,
      net.minecraft.client.renderer.SubmitNodeCollector collector,
      net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> cargo
   ) {
      state.flowerBasket = true;

      for (int i = 0; i < cargo.size(); i++) {
         ItemStack itemStack = (ItemStack)cargo.get(i);
         if (itemStack.getItem() instanceof BlockItem) {
            int ix = i % 2;
            int iz = i / 2;
            BlockState defaultState = ((BlockItem)itemStack.getItem()).getBlock().defaultBlockState();
            BlockState blockState = defaultState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
               ? (BlockState)defaultState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER)
               : defaultState;
            BlockStateModel model = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(blockState);
            java.util.List<net.minecraft.client.renderer.block.dispatch.BlockStateModelPart> parts = new java.util.ArrayList<>();
            model.collectParts(net.minecraft.util.RandomSource.create(0), parts);
            int[] tintValues = Minecraft.getInstance().getBlockColors().getTintSources(blockState).stream().mapToInt(t -> t.color(blockState)).toArray();
            stack.pushPose();
            stack.translate(0.0, -0.7, this.getFlowerOffsetZ());
            stack.scale(0.65F, 0.65F, 0.65F);
            stack.translate(ix, 0.5, iz - 1.0);
            stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            collector.submitBlockModel(
               stack,
               RenderTypes.cutoutMovingBlock(),
               parts,
               tintValues,
               state.lightCoords,
               OverlayTexture.NO_OVERLAY,
               -1
            );
            stack.popPose();
         }
      }
   }

   private void submitPaintings(
      CargoCartRenderState state,
      com.mojang.blaze3d.vertex.PoseStack stack,
      net.minecraft.client.renderer.SubmitNodeCollector collector,
      net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> cargo
   ) {
      Random rng = new Random(state.rngSeed);
      int count = 0;

      for (ItemStack itemStack : cargo) {
         if (!itemStack.isEmpty()) {
            count++;
         }
      }

      stack.pushPose();
      stack.translate(0.0, -0.15625, 0.0);
      stack.mulPose(Axis.XP.rotationDegrees(-90.0F));
      RegistryAccess registryAccess = state.level.registryAccess();
      ObjectList<net.minecraft.world.entity.decoration.painting.PaintingVariant> variants = new ObjectArrayList();
      registryAccess.lookupOrThrow(Registries.PAINTING_VARIANT).getTagOrEmpty(PaintingVariantTags.PLACEABLE).forEach(variantHolder -> {
         if (((PaintingVariant)variantHolder.value()).area() == 1) {
            variants.add((PaintingVariant)variantHolder.value());
         }
      });
      ObjectLists.shuffle(variants, rng);
      int i = 0;

      for (int n = 0; i < cargo.size(); i++) {
         ItemStack itemStackx = (ItemStack)cargo.get(i);
         if (!itemStackx.isEmpty()) {
            net.minecraft.core.Holder<net.minecraft.world.entity.decoration.painting.PaintingVariant> holder = (Holder)itemStackx.get(
               DataComponents.PAINTING_VARIANT
            );
            PaintingVariant paintingVariant = null;
            if (holder != null) {
               paintingVariant = (PaintingVariant)holder.value();
            }

            if (paintingVariant == null || paintingVariant.area() > 1) {
               if (variants.isEmpty()) {
                  continue;
               }

               paintingVariant = (PaintingVariant)variants.get(i % variants.size());
            }

            stack.pushPose();
            stack.translate(this.getPaintingOffset(i, n, count));
            n++;
            stack.mulPose(Axis.ZP.rotation(rng.nextFloat() * (float) Math.PI * this.getPaintingAngleFactor()));
            TextureAtlas atlas = this.ctx.getAtlas(AtlasIds.PAINTINGS);
            TextureAtlasSprite back = atlas.getSprite(CargoRenderUtil.BACK_SPRITE_LOCATION);
            CargoRenderUtil.renderPainting(this.ctx, paintingVariant, stack, RenderTypes.entitySolidZOffsetForward(back.atlasLocation()), collector, state.lightCoords);
            stack.popPose();
         }
      }

      stack.popPose();
   }

   protected boolean shouldRender(ItemStack stack) {
      ConfigValue<ArrayList<String>> blacklist = NiftyCartsConfig.getClient().renderBlacklist;

      for (String item : (ArrayList<String>) blacklist.get()) {
         if (item.startsWith("#")) {
            net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag = TagKey.create(
               Registries.ITEM, Identifier.parse(item.substring(1))
            );
            if (stack.is(tag)) {
               return false;
            }
         } else if (item.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) {
            return false;
         }
      }

      return true;
   }

   private void submitSupplies(
      CargoCartRenderState state,
      com.mojang.blaze3d.vertex.PoseStack stack,
      net.minecraft.client.renderer.SubmitNodeCollector collector,
      net.minecraft.core.NonNullList<net.minecraft.world.item.ItemStack> cargo
   ) {
      Random rng = new Random();

      for (int i = 0; i < cargo.size(); i++) {
         ItemStack itemStack = (ItemStack)cargo.get(i);
         if (!itemStack.isEmpty()) {
            int ix = i % 2;
            int iz = i / 2;
            if ((i >= cargo.size() - 2 || !((ItemStack)cargo.get(i + 2)).is(ItemTags.BEDS))
               && (i < 2 || !((ItemStack)cargo.get(i - 2)).is(ItemTags.BEDS))) {
               double x = this.getSuppliesOffsetX(ix);
               double z = this.getSuppliesOffsetZ(iz);
               stack.pushPose();
               ItemStackRenderState cargoState = (ItemStackRenderState)state.cargoStates.get(i);
               if (this.shouldRender(itemStack)) {
                  if (cargoState.usesBlockLight() && itemStack.getItem() != Items.TRIDENT && (Boolean)NiftyCartsConfig.getClient().renderSupplyGear.get()) {
                     stack.translate(x, -0.46, z);
                     stack.scale(this.getBlockSize(), this.getBlockSize(), this.getBlockSize());
                     stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                     if (iz < 1 && itemStack.is(ItemTags.BEDS)) {
                        stack.translate(0.0, 0.0, 1.0);
                     }

                     cargoState.submit(stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                  } else {
                     rng.setSeed(32L * i + Objects.hashCode(BuiltInRegistries.ITEM.getKey(itemStack.getItem())));
                     stack.translate(x, -0.15 + ((ix + iz) % 2 == 0 ? 0.0 : 1.0E-4), z);
                     if (NiftyItemUtil.isHumanoidArmor(itemStack) && (Boolean)NiftyCartsConfig.getClient().renderSupplyGear.get()) {
                        stack.scale(this.getArmorSize(), this.getArmorSize(), this.getArmorSize());
                        this.renderArmor(state, stack, collector, itemStack, ix);
                     } else {
                        if (itemStack.getItem() == Items.SHIELD) {
                           stack.scale(this.getBlockSize(), this.getBlockSize(), this.getBlockSize());
                           stack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                           stack.scale(1.2F, 1.2F, 1.2F);
                           stack.mulPose(Axis.YP.rotationDegrees(ix == 0 ? -90.0F : 90.0F));
                           stack.translate(0.5, this.getShieldOffsetY(), -0.05);
                           stack.mulPose(Axis.XP.rotationDegrees(-22.5F));
                        } else {
                           stack.scale(this.getItemSize(), this.getItemSize(), this.getItemSize());
                           stack.mulPose(Axis.YP.rotation(rng.nextFloat() * (float) Math.PI));
                           stack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                        }

                        int copies = Math.min(itemStack.getCount(), (itemStack.getCount() - 1) / 16 + 2);
                        cargoState.submit(stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

                        for (int n = 1; n < copies; n++) {
                           stack.pushPose();
                           stack.mulPose(Axis.ZP.rotation(rng.nextFloat() * (float) Math.PI));
                           stack.translate((rng.nextFloat() * 2.0F - 1.0F) * 0.05F, (rng.nextFloat() * 2.0F - 1.0F) * 0.05F, -0.1 * n);
                           cargoState.submit(stack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
                           stack.popPose();
                        }
                     }
                  }
               }

               stack.popPose();
            }
         }
      }
   }

   private void renderArmor(CargoCartRenderState state, PoseStack stack, SubmitNodeCollector collector, ItemStack itemStack, int ix) {
      Equippable equippable = (Equippable)itemStack.get(DataComponents.EQUIPPABLE);
      if (equippable != null) {
         EquipmentSlot slot = equippable.slot();
         ArmorStandArmorModel model = (ArmorStandArmorModel)this.armorSet.get(slot);
         stack.mulPose(Axis.YP.rotation(ix == 0 ? (float) (Math.PI / 2) : (float) (-Math.PI / 2)));
         ArmorStandRenderState armorRenderState = null;
         switch (slot) {
            case HEAD:
               stack.translate(0.0, 0.105, 0.0);
               state.armorRenderState.headPose = new Rotations(14.323944F, 0.0F, 0.0F);
               armorRenderState = state.armorRenderState;
               break;
            case CHEST:
               stack.translate(0.0, -0.3, -0.27);
               stack.mulPose(Axis.XP.rotation(0.9F));
               state.armorRenderState.leftArmPose = new Rotations(-42.971832F, 0.0F, 0.0F);
               state.armorRenderState.rightArmPose = new Rotations(-42.971832F, 0.0F, 0.0F);
               armorRenderState = state.armorRenderState;
               break;
            case LEGS:
               stack.translate(0.0, -0.7, -0.226);
               state.armorRenderState.bodyPose = new Rotations(0.0F, 0.0F, 0.0F);
               state.armorRenderState.rightLegPose = new Rotations(68.75494F, -17.188734F, 0.0F);
               state.armorRenderState.leftLegPose = new Rotations(68.75494F, 17.188734F, 0.0F);
               armorRenderState = state.armorRenderState;
               break;
            case FEET:
               stack.translate(0.0, -1.096, -0.19);
               state.armorRenderState2.rightLegPose = new Rotations(0.0F, -5.729578F, 0.0F);
               state.armorRenderState2.leftLegPose = new Rotations(0.0F, 0.0F, 0.0F);
               armorRenderState = state.armorRenderState2;
         }

         assert armorRenderState != null;

         model.setupAnim(armorRenderState);
         stack.scale(0.75F, 0.75F, 0.75F);
         boolean usesInnerModel = slot == EquipmentSlot.LEGS;
         LayerType layerType = usesInnerModel ? LayerType.HUMANOID_LEGGINGS : LayerType.HUMANOID;
         this.equipmentRenderer
            .renderLayers(
               layerType,
               (ResourceKey)equippable.assetId().orElseThrow(),
               model,
               armorRenderState,
               itemStack,
               stack,
               collector,
               state.lightCoords,
               state.outlineColor
            );
      }
   }

   protected abstract double getFlowerOffsetZ();

   protected abstract Vec3 getWheelOffset();

   protected abstract Vec3 getPaintingOffset(int var1, int var2, int var3);

   protected abstract float getPaintingAngleFactor();

   protected abstract double getSuppliesOffsetX(int var1);

   protected abstract double getSuppliesOffsetZ(int var1);

   protected abstract float getBlockSize();

   protected abstract float getArmorSize();

   protected abstract float getItemSize();

   protected abstract double getShieldOffsetY();

   @Environment(EnvType.CLIENT)
   private static enum Contents {
      FLOWERS(
         s -> s.getItem() instanceof BlockItem && s.is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, net.minecraft.resources.Identifier.withDefaultNamespace("small_flowers"))) && (Boolean)NiftyCartsConfig.getClient().renderSupplyFlowers.get()
      ),
      PAINTINGS(s -> s.getItem() == Items.PAINTING && (Boolean)NiftyCartsConfig.getClient().renderSupplyPaintings.get()),
      WHEEL(
         s -> s.getItem() == NiftyCarts.WHEEL
            && (Boolean)NiftyCartsConfig.getClient().renderSupplyWheel.get()
            && (Boolean)NiftyCartsConfig.getClient().renderSupplies.get()
      ),
      SUPPLIES(s -> (Boolean)NiftyCartsConfig.getClient().renderSupplies.get()),
      NONE(s -> true);

      private final Predicate<? super net.minecraft.world.item.ItemStack> predicate;

      private Contents(final Predicate<? super net.minecraft.world.item.ItemStack> predicate) {
         this.predicate = predicate;
      }
   }
}
