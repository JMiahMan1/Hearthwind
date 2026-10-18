package net.voidz.init;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.voidz.block.InfestedVoidStoneBlock;
import net.voidz.block.PortalBlock;
import net.voidz.block.VoidStoneBlock;
import net.voidz.block.entity.PortalBlockEntity;
import net.voidz.block.entity.VoidStoneBlockEntity;

public class BlockInit {
   public static final Block PORTAL = register(
      "void_portal",
      id -> new PortalBlock(
         Properties.of()
            .mapColor(MapColor.COLOR_BLACK)
            .noOcclusion()
            .lightLevel(state -> 15)
            .strength(-1.0F, 3600000.0F)
            .noLootTable()
            .pushReaction(PushReaction.BLOCK)
            .setId(ResourceKey.create(Registries.BLOCK, id))
      )
   );
   public static final Block VOID_STONE = register(
      "void_stone",
      id -> new VoidStoneBlock(Properties.ofFullCopy(Blocks.END_STONE).setId(ResourceKey.create(Registries.BLOCK, id)))
   );
   public static final Block INFESTED_VOID = register(
      "infested_void_stone",
      id -> new InfestedVoidStoneBlock(Properties.ofFullCopy(Blocks.END_STONE).setId(ResourceKey.create(Registries.BLOCK, id)))
   );
   public static BlockEntityType<PortalBlockEntity> PORTAL_BLOCK_ENTITY;
   public static BlockEntityType<VoidStoneBlockEntity> VOID_BLOCK_ENTITY;
   public static final ResourceKey<net.minecraft.world.damagesource.DamageType> INFESTED = ResourceKey.create(
      Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("voidz", "infested")
   );

   private static Block register(String id, java.util.function.Function<Identifier, Block> factory) {
      return register(Identifier.fromNamespaceAndPath("voidz", id), factory);
   }

   private static Block register(Identifier id, java.util.function.Function<Identifier, Block> factory) {
      Block block = factory.apply(id);
      Item item = Registry.register(
         BuiltInRegistries.ITEM, id, new BlockItem(block, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).useBlockDescriptionPrefix())
      );
      CreativeModeTabEvents.modifyOutputEvent(ItemInit.VOIDZ_ITEM_GROUP).register(entries -> entries.accept(item));
      return Registry.register(BuiltInRegistries.BLOCK, id, block);
   }

   public static void init() {
      PORTAL_BLOCK_ENTITY = Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE,
         Identifier.fromNamespaceAndPath("voidz", "void_portal_entity"),
         new BlockEntityType<PortalBlockEntity>(PortalBlockEntity::new, java.util.Set.of(PORTAL))
      );
      VOID_BLOCK_ENTITY = Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE,
         Identifier.fromNamespaceAndPath("voidz", "void_block_entity"),
         new BlockEntityType<VoidStoneBlockEntity>(VoidStoneBlockEntity::new, java.util.Set.of(VOID_STONE))
      );
   }
}
