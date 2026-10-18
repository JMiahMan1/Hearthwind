package elocindev.grassoverhaul;

import elocindev.grassoverhaul.blocks.SodBlock;
import elocindev.grassoverhaul.blocks.SodPatch;
import elocindev.grassoverhaul.blocks.SodSlab;
import elocindev.grassoverhaul.blocks.SodStair;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class GOBlocks {
   public static final SodPatch DIRT_SOD_PATCH = new SodPatch(props(Blocks.DIRT, "dirt_sod_patch"));
   public static final SodPatch GRASS_SOD_PATCH = new SodPatch(props(Blocks.GRASS_BLOCK, "grass_sod_patch"));
   public static final SodPatch PODZOL_SOD_PATCH = new SodPatch(props(Blocks.PODZOL, "podzol_sod_patch"));
   public static final SodPatch MYCELIUM_SOD_PATCH = new SodPatch(props(Blocks.MYCELIUM, "mycelium_sod_patch"));
   public static final SodPatch PATH_SOD_PATCH = new SodPatch(props(Blocks.GRASS_BLOCK, "path_sod_patch"));
   public static final SodPatch CRIMSON_NYLIUM_SOD_PATCH = new SodPatch(props(Blocks.CRIMSON_NYLIUM, "crimson_nylium_sod_patch"));
   public static final SodPatch WARPED_NYLIUM_SOD_PATCH = new SodPatch(props(Blocks.WARPED_NYLIUM, "warped_nylium_sod_patch"));
   public static final SodSlab DIRT_SOD_SLAB = new SodSlab(props(Blocks.DIRT, "dirt_sod_slab"));
   public static final SodSlab GRASS_SOD_SLAB = new SodSlab(props(Blocks.GRASS_BLOCK, "grass_sod_slab"));
   public static final SodSlab PODZOL_SOD_SLAB = new SodSlab(props(Blocks.PODZOL, "podzol_sod_slab"));
   public static final SodSlab MYCELIUM_SOD_SLAB = new SodSlab(props(Blocks.MYCELIUM, "mycelium_sod_slab"));
   public static final SodSlab PATH_SOD_SLAB = new SodSlab(props(Blocks.GRASS_BLOCK, "path_sod_slab"));
   public static final SodSlab CRIMSON_NYLIUM_SOD_SLAB = new SodSlab(props(Blocks.CRIMSON_NYLIUM, "crimson_nylium_sod_slab"));
   public static final SodSlab WARPED_NYLIUM_SOD_SLAB = new SodSlab(props(Blocks.WARPED_NYLIUM, "warped_nylium_sod_slab"));
   public static final SodBlock GRASS_SOD_BLOCK = new SodBlock(props(Blocks.GRASS_BLOCK, "grass_sod_block"));
   public static final SodBlock PODZOL_SOD_BLOCK = new SodBlock(props(Blocks.PODZOL, "podzol_sod_block"));
   public static final SodBlock MYCELIUM_SOD_BLOCK = new SodBlock(props(Blocks.MYCELIUM, "mycelium_sod_block"));
   public static final SodBlock PATH_SOD_BLOCK = new SodBlock(props(Blocks.GRASS_BLOCK, "path_sod_block"));
   public static final SodBlock CRIMSON_NYLIUM_SOD_BLOCK = new SodBlock(props(Blocks.CRIMSON_NYLIUM, "crimson_nylium_sod_block"));
   public static final SodBlock WARPED_NYLIUM_SOD_BLOCK = new SodBlock(props(Blocks.WARPED_NYLIUM, "warped_nylium_sod_block"));
   public static final Block DIRT_SOD_STAIRS = new SodStair(Blocks.DIRT, "dirt_sod_stairs");
   public static final Block GRASS_SOD_STAIRS = new SodStair(GRASS_SOD_BLOCK, "grass_sod_stairs");
   public static final Block PODZOL_SOD_STAIRS = new SodStair(PODZOL_SOD_BLOCK, "podzol_sod_stairs");
   public static final Block MYCELIUM_SOD_STAIRS = new SodStair(MYCELIUM_SOD_BLOCK, "mycelium_sod_stairs");
   public static final Block PATH_SOD_STAIRS = new SodStair(PATH_SOD_BLOCK, "path_sod_stairs");
   public static final Block CRIMSON_NYLIUM_SOD_STAIRS = new SodStair(CRIMSON_NYLIUM_SOD_BLOCK, "crimson_nylium_sod_stairs");
   public static final Block WARPED_NYLIUM_SOD_STAIRS = new SodStair(WARPED_NYLIUM_SOD_BLOCK, "warped_nylium_sod_stairs");
   public static Block[] GOBlocks = new Block[]{
      DIRT_SOD_STAIRS,
      DIRT_SOD_SLAB,
      DIRT_SOD_PATCH,
      GRASS_SOD_BLOCK,
      GRASS_SOD_SLAB,
      GRASS_SOD_PATCH,
      GRASS_SOD_STAIRS,
      PODZOL_SOD_BLOCK,
      PODZOL_SOD_SLAB,
      PODZOL_SOD_PATCH,
      PODZOL_SOD_STAIRS,
      MYCELIUM_SOD_BLOCK,
      MYCELIUM_SOD_SLAB,
      MYCELIUM_SOD_PATCH,
      MYCELIUM_SOD_STAIRS,
      PATH_SOD_BLOCK,
      PATH_SOD_SLAB,
      PATH_SOD_PATCH,
      PATH_SOD_STAIRS,
      CRIMSON_NYLIUM_SOD_BLOCK,
      CRIMSON_NYLIUM_SOD_SLAB,
      CRIMSON_NYLIUM_SOD_PATCH,
      CRIMSON_NYLIUM_SOD_STAIRS,
      WARPED_NYLIUM_SOD_BLOCK,
      WARPED_NYLIUM_SOD_SLAB,
      WARPED_NYLIUM_SOD_PATCH,
      WARPED_NYLIUM_SOD_STAIRS
   };
   public static String[] BlocksID = new String[]{
      "dirt_sod_stairs",
      "dirt_sod_slab",
      "dirt_sod_patch",
      "grass_sod_block",
      "grass_sod_slab",
      "grass_sod_patch",
      "grass_sod_stairs",
      "podzol_sod_block",
      "podzol_sod_slab",
      "podzol_sod_patch",
      "podzol_sod_stairs",
      "mycelium_sod_block",
      "mycelium_sod_slab",
      "mycelium_sod_patch",
      "mycelium_sod_stairs",
      "path_sod_block",
      "path_sod_slab",
      "path_sod_patch",
      "path_sod_stairs",
      "crimson_nylium_sod_block",
      "crimson_nylium_sod_slab",
      "crimson_nylium_sod_patch",
      "crimson_nylium_sod_stairs",
      "warped_nylium_sod_block",
      "warped_nylium_sod_slab",
      "warped_nylium_sod_patch",
      "warped_nylium_sod_stairs"
   };
   public static Block[] TintedBlocks = new Block[]{GRASS_SOD_BLOCK, GRASS_SOD_SLAB, GRASS_SOD_PATCH, GRASS_SOD_STAIRS};

   public static BlockBehaviour.Properties props(Block copy, String path) {
      return BlockBehaviour.Properties.ofFullCopy(copy)
         .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("grassoverhaul", path)));
   }

   public static void blockReg(String MODID) {
      for (int i = 0; i < GOBlocks.length; i++) {
         Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(MODID, BlocksID[i]), GOBlocks[i]);
      }
   }

   public static void itemReg(String MODID) {
      for (int i = 0; i < GOBlocks.length; i++) {
         Registry.register(
            BuiltInRegistries.ITEM,
            Identifier.fromNamespaceAndPath(MODID, BlocksID[i]),
            new BlockItem(
               GOBlocks[i],
               new Item.Properties()
                  .setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, BlocksID[i])))
                  .useBlockDescriptionPrefix()
            )
         );
      }
   }

   public static void init() {
      blockReg("grassoverhaul");
      itemReg("grassoverhaul");
   }

   @Environment(EnvType.CLIENT)
   public static void colorreg() {
      BlockTintSource grassTint = new BlockTintSource() {
         @Override
         public int color(BlockState state) {
            return 7516266;
         }

         @Override
         public int colorInWorld(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return BiomeColors.getAverageGrassColor(level, pos);
         }

         @Override
         public int colorAsTerrainParticle(BlockState state, BlockAndTintGetter level, BlockPos pos) {
            return BiomeColors.getAverageGrassColor(level, pos);
         }
      };
      BlockColorRegistry.register(List.of(grassTint), TintedBlocks);
   }
}
