package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.block.BenchBlock;
import com.starfish_studios.another_furniture.block.ChairBlock;
import com.starfish_studios.another_furniture.block.CurtainBlock;
import com.starfish_studios.another_furniture.block.DrawerBlock;
import com.starfish_studios.another_furniture.block.FlowerBoxBlock;
import com.starfish_studios.another_furniture.block.LampBlock;
import com.starfish_studios.another_furniture.block.LampConnectorBlock;
import com.starfish_studios.another_furniture.block.ServiceBellBlock;
import com.starfish_studios.another_furniture.block.ShelfBlock;
import com.starfish_studios.another_furniture.block.ShutterBlock;
import com.starfish_studios.another_furniture.block.SofaBlock;
import com.starfish_studios.another_furniture.block.StoolBlock;
import com.starfish_studios.another_furniture.block.TableBlock;
import com.starfish_studios.another_furniture.block.TallStoolBlock;
import com.starfish_studios.another_furniture.block.properties.ModBlockStateProperties;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class AFBlocks {
   public static final Supplier<net.minecraft.world.level.block.Block> OAK_CHAIR = registerBlock(
      "oak_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(1, AFBlocks.Properties.oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> SPRUCE_CHAIR = registerBlock(
      "spruce_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(2, AFBlocks.Properties.spruce_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BIRCH_CHAIR = registerBlock(
      "birch_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(3, AFBlocks.Properties.birch_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> JUNGLE_CHAIR = registerBlock(
      "jungle_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(4, AFBlocks.Properties.jungle_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ACACIA_CHAIR = registerBlock(
      "acacia_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(5, AFBlocks.Properties.acacia_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> DARK_OAK_CHAIR = registerBlock(
      "dark_oak_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(6, AFBlocks.Properties.dark_oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MANGROVE_CHAIR = registerBlock(
      "mangrove_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(7, AFBlocks.Properties.mangrove_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CHERRY_CHAIR = registerBlock(
      "cherry_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(8, AFBlocks.Properties.cherry_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BAMBOO_CHAIR = registerBlock(
      "bamboo_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(9, AFBlocks.Properties.bamboo_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CRIMSON_CHAIR = registerBlock(
      "crimson_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(10, AFBlocks.Properties.crimson_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WARPED_CHAIR = registerBlock(
      "warped_chair", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ChairBlock(11, AFBlocks.Properties.warped_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> OAK_SHELF = registerBlock(
      "oak_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> SPRUCE_SHELF = registerBlock(
      "spruce_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.spruce_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BIRCH_SHELF = registerBlock(
      "birch_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.birch_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> JUNGLE_SHELF = registerBlock(
      "jungle_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.jungle_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ACACIA_SHELF = registerBlock(
      "acacia_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.acacia_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> DARK_OAK_SHELF = registerBlock(
      "dark_oak_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.dark_oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MANGROVE_SHELF = registerBlock(
      "mangrove_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.mangrove_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CHERRY_SHELF = registerBlock(
      "cherry_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.cherry_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BAMBOO_SHELF = registerBlock(
      "bamboo_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.bamboo_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CRIMSON_SHELF = registerBlock(
      "crimson_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.crimson_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WARPED_SHELF = registerBlock(
      "warped_shelf", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShelfBlock(AFBlocks.Properties.warped_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> OAK_TABLE = registerBlock(
      "oak_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> SPRUCE_TABLE = registerBlock(
      "spruce_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.spruce_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BIRCH_TABLE = registerBlock(
      "birch_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.birch_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> JUNGLE_TABLE = registerBlock(
      "jungle_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.jungle_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ACACIA_TABLE = registerBlock(
      "acacia_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.acacia_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> DARK_OAK_TABLE = registerBlock(
      "dark_oak_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.dark_oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MANGROVE_TABLE = registerBlock(
      "mangrove_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.mangrove_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CHERRY_TABLE = registerBlock(
      "cherry_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.cherry_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BAMBOO_TABLE = registerBlock(
      "bamboo_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.bamboo_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CRIMSON_TABLE = registerBlock(
      "crimson_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.crimson_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WARPED_TABLE = registerBlock(
      "warped_table", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TableBlock(AFBlocks.Properties.warped_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WHITE_STOOL = registerBlock(
      "white_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ORANGE_STOOL = registerBlock(
      "orange_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MAGENTA_STOOL = registerBlock(
      "magenta_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_BLUE_STOOL = registerBlock(
      "light_blue_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> YELLOW_STOOL = registerBlock(
      "yellow_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIME_STOOL = registerBlock(
      "lime_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PINK_STOOL = registerBlock(
      "pink_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GRAY_STOOL = registerBlock(
      "gray_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_GRAY_STOOL = registerBlock(
      "light_gray_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CYAN_STOOL = registerBlock(
      "cyan_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PURPLE_STOOL = registerBlock(
      "purple_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLUE_STOOL = registerBlock(
      "blue_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BROWN_STOOL = registerBlock(
      "brown_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GREEN_STOOL = registerBlock(
      "green_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> RED_STOOL = registerBlock(
      "red_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLACK_STOOL = registerBlock(
      "black_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new StoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> OAK_SHUTTER = registerBlock(
      "oak_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(1, AFBlocks.Properties.oak_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> SPRUCE_SHUTTER = registerBlock(
      "spruce_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(2, AFBlocks.Properties.spruce_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BIRCH_SHUTTER = registerBlock(
      "birch_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(3, AFBlocks.Properties.birch_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> JUNGLE_SHUTTER = registerBlock(
      "jungle_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(4, AFBlocks.Properties.jungle_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ACACIA_SHUTTER = registerBlock(
      "acacia_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(5, AFBlocks.Properties.acacia_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> DARK_OAK_SHUTTER = registerBlock(
      "dark_oak_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(6, AFBlocks.Properties.dark_oak_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MANGROVE_SHUTTER = registerBlock(
      "mangrove_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(7, AFBlocks.Properties.mangrove_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CHERRY_SHUTTER = registerBlock(
      "cherry_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(8, AFBlocks.Properties.cherry_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BAMBOO_SHUTTER = registerBlock(
      "bamboo_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(9, AFBlocks.Properties.bamboo_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CRIMSON_SHUTTER = registerBlock(
      "crimson_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(10, AFBlocks.Properties.crimson_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WARPED_SHUTTER = registerBlock(
      "warped_shutter", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ShutterBlock(11, AFBlocks.Properties.warped_wood().noOcclusion()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> OAK_FLOWER_BOX = registerBlock(
      "oak_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> SPRUCE_FLOWER_BOX = registerBlock(
      "spruce_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.spruce_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BIRCH_FLOWER_BOX = registerBlock(
      "birch_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.birch_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> JUNGLE_FLOWER_BOX = registerBlock(
      "jungle_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.jungle_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ACACIA_FLOWER_BOX = registerBlock(
      "acacia_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.acacia_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> DARK_OAK_FLOWER_BOX = registerBlock(
      "dark_oak_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.dark_oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MANGROVE_FLOWER_BOX = registerBlock(
      "mangrove_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.mangrove_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CHERRY_FLOWER_BOX = registerBlock(
      "cherry_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.cherry_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BAMBOO_FLOWER_BOX = registerBlock(
      "bamboo_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.bamboo_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CRIMSON_FLOWER_BOX = registerBlock(
      "crimson_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.crimson_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WARPED_FLOWER_BOX = registerBlock(
      "warped_flower_box", (Supplier<net.minecraft.world.level.block.Block>)(() -> new FlowerBoxBlock(AFBlocks.Properties.warped_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WHITE_CURTAIN = registerBlock(
      "white_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ORANGE_CURTAIN = registerBlock(
      "orange_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MAGENTA_CURTAIN = registerBlock(
      "magenta_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_BLUE_CURTAIN = registerBlock(
      "light_blue_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> YELLOW_CURTAIN = registerBlock(
      "yellow_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIME_CURTAIN = registerBlock(
      "lime_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PINK_CURTAIN = registerBlock(
      "pink_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GRAY_CURTAIN = registerBlock(
      "gray_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_GRAY_CURTAIN = registerBlock(
      "light_gray_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CYAN_CURTAIN = registerBlock(
      "cyan_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PURPLE_CURTAIN = registerBlock(
      "purple_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLUE_CURTAIN = registerBlock(
      "blue_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BROWN_CURTAIN = registerBlock(
      "brown_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GREEN_CURTAIN = registerBlock(
      "green_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> RED_CURTAIN = registerBlock(
      "red_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLACK_CURTAIN = registerBlock(
      "black_curtain", (Supplier<net.minecraft.world.level.block.Block>)(() -> new CurtainBlock(AFBlocks.Properties.curtain()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> SERVICE_BELL = registerBlock(
      "service_bell", (Supplier<net.minecraft.world.level.block.Block>)(() -> new ServiceBellBlock(AFBlocks.Properties.service_bell()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> OAK_BENCH = registerBlock(
      "oak_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> SPRUCE_BENCH = registerBlock(
      "spruce_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.spruce_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BIRCH_BENCH = registerBlock(
      "birch_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.birch_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> JUNGLE_BENCH = registerBlock(
      "jungle_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.jungle_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ACACIA_BENCH = registerBlock(
      "acacia_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.acacia_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> DARK_OAK_BENCH = registerBlock(
      "dark_oak_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.dark_oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MANGROVE_BENCH = registerBlock(
      "mangrove_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.mangrove_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CHERRY_BENCH = registerBlock(
      "cherry_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.cherry_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BAMBOO_BENCH = registerBlock(
      "bamboo_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.bamboo_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CRIMSON_BENCH = registerBlock(
      "crimson_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.crimson_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WARPED_BENCH = registerBlock(
      "warped_bench", (Supplier<net.minecraft.world.level.block.Block>)(() -> new BenchBlock(AFBlocks.Properties.warped_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> OAK_DRAWER = registerBlock(
      "oak_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> SPRUCE_DRAWER = registerBlock(
      "spruce_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.spruce_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BIRCH_DRAWER = registerBlock(
      "birch_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.birch_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> JUNGLE_DRAWER = registerBlock(
      "jungle_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.jungle_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ACACIA_DRAWER = registerBlock(
      "acacia_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.acacia_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> DARK_OAK_DRAWER = registerBlock(
      "dark_oak_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.dark_oak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MANGROVE_DRAWER = registerBlock(
      "mangrove_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.mangrove_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CHERRY_DRAWER = registerBlock(
      "cherry_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.cherry_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BAMBOO_DRAWER = registerBlock(
      "bamboo_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.bamboo_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CRIMSON_DRAWER = registerBlock(
      "crimson_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.crimson_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WARPED_DRAWER = registerBlock(
      "warped_drawer", (Supplier<net.minecraft.world.level.block.Block>)(() -> new DrawerBlock(AFBlocks.Properties.warped_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WHITE_LAMP = registerBlock(
      "white_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.WHITE, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ORANGE_LAMP = registerBlock(
      "orange_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.ORANGE, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MAGENTA_LAMP = registerBlock(
      "magenta_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.MAGENTA, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_BLUE_LAMP = registerBlock(
      "light_blue_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.LIGHT_BLUE, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> YELLOW_LAMP = registerBlock(
      "yellow_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.YELLOW, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIME_LAMP = registerBlock(
      "lime_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.LIME, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PINK_LAMP = registerBlock(
      "pink_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.PINK, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GRAY_LAMP = registerBlock(
      "gray_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.GRAY, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_GRAY_LAMP = registerBlock(
      "light_gray_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.LIGHT_GRAY, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CYAN_LAMP = registerBlock(
      "cyan_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.CYAN, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PURPLE_LAMP = registerBlock(
      "purple_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.PURPLE, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLUE_LAMP = registerBlock(
      "blue_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.BLUE, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BROWN_LAMP = registerBlock(
      "brown_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.BROWN, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GREEN_LAMP = registerBlock(
      "green_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.GREEN, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> RED_LAMP = registerBlock(
      "red_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.RED, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLACK_LAMP = registerBlock(
      "black_lamp", (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampBlock(DyeColor.BLACK, AFBlocks.Properties.lamp()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WHITE_LAMP_CONNECTOR = registerBlockOnly(
      "white_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.WHITE, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ORANGE_LAMP_CONNECTOR = registerBlockOnly(
      "orange_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.ORANGE, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MAGENTA_LAMP_CONNECTOR = registerBlockOnly(
      "magenta_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.MAGENTA, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_BLUE_LAMP_CONNECTOR = registerBlockOnly(
      "light_blue_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.LIGHT_BLUE, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> YELLOW_LAMP_CONNECTOR = registerBlockOnly(
      "yellow_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.YELLOW, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIME_LAMP_CONNECTOR = registerBlockOnly(
      "lime_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.LIME, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PINK_LAMP_CONNECTOR = registerBlockOnly(
      "pink_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.PINK, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GRAY_LAMP_CONNECTOR = registerBlockOnly(
      "gray_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.GRAY, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_GRAY_LAMP_CONNECTOR = registerBlockOnly(
      "light_gray_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.LIGHT_GRAY, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CYAN_LAMP_CONNECTOR = registerBlockOnly(
      "cyan_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.CYAN, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PURPLE_LAMP_CONNECTOR = registerBlockOnly(
      "purple_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.PURPLE, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLUE_LAMP_CONNECTOR = registerBlockOnly(
      "blue_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.BLUE, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BROWN_LAMP_CONNECTOR = registerBlockOnly(
      "brown_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.BROWN, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GREEN_LAMP_CONNECTOR = registerBlockOnly(
      "green_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.GREEN, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> RED_LAMP_CONNECTOR = registerBlockOnly(
      "red_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.RED, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLACK_LAMP_CONNECTOR = registerBlockOnly(
      "black_lamp_connector",
      (Supplier<net.minecraft.world.level.block.Block>)(() -> new LampConnectorBlock(DyeColor.BLACK, AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WHITE_SOFA = registerBlock(
      "white_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ORANGE_SOFA = registerBlock(
      "orange_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MAGENTA_SOFA = registerBlock(
      "magenta_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_BLUE_SOFA = registerBlock(
      "light_blue_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> YELLOW_SOFA = registerBlock(
      "yellow_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIME_SOFA = registerBlock(
      "lime_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PINK_SOFA = registerBlock(
      "pink_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GRAY_SOFA = registerBlock(
      "gray_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_GRAY_SOFA = registerBlock(
      "light_gray_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CYAN_SOFA = registerBlock(
      "cyan_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PURPLE_SOFA = registerBlock(
      "purple_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLUE_SOFA = registerBlock(
      "blue_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BROWN_SOFA = registerBlock(
      "brown_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GREEN_SOFA = registerBlock(
      "green_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> RED_SOFA = registerBlock(
      "red_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLACK_SOFA = registerBlock(
      "black_sofa", (Supplier<net.minecraft.world.level.block.Block>)(() -> new SofaBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> WHITE_TALL_STOOL = registerBlock(
      "white_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> ORANGE_TALL_STOOL = registerBlock(
      "orange_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> MAGENTA_TALL_STOOL = registerBlock(
      "magenta_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_BLUE_TALL_STOOL = registerBlock(
      "light_blue_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> YELLOW_TALL_STOOL = registerBlock(
      "yellow_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIME_TALL_STOOL = registerBlock(
      "lime_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PINK_TALL_STOOL = registerBlock(
      "pink_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GRAY_TALL_STOOL = registerBlock(
      "gray_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> LIGHT_GRAY_TALL_STOOL = registerBlock(
      "light_gray_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> CYAN_TALL_STOOL = registerBlock(
      "cyan_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> PURPLE_TALL_STOOL = registerBlock(
      "purple_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLUE_TALL_STOOL = registerBlock(
      "blue_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BROWN_TALL_STOOL = registerBlock(
      "brown_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> GREEN_TALL_STOOL = registerBlock(
      "green_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> RED_TALL_STOOL = registerBlock(
      "red_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );
   public static final Supplier<net.minecraft.world.level.block.Block> BLACK_TALL_STOOL = registerBlock(
      "black_tall_stool", (Supplier<net.minecraft.world.level.block.Block>)(() -> new TallStoolBlock(AFBlocks.Properties.weak_wood()))
   );

   public static <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlock(String name, Supplier<T> block) {
      AFBlocks.Properties.pushId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, com.starfish_studios.another_furniture.AnotherFurniture.res(name)));
      Supplier<T> supplier = AFRegistry.registerBlock(name, block);
      AFBlocks.Properties.popId();
      AFRegistry.registerItem(name, () -> new BlockItem((Block)supplier.get(), new net.minecraft.world.item.Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, com.starfish_studios.another_furniture.AnotherFurniture.res(name))).useBlockDescriptionPrefix()), "tab");
      return supplier;
   }

   public static <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlockHidden(String name, Supplier<T> block) {
      AFBlocks.Properties.pushId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, com.starfish_studios.another_furniture.AnotherFurniture.res(name)));
      Supplier<T> supplier = AFRegistry.registerBlock(name, block);
      AFBlocks.Properties.popId();
      AFRegistry.registerItem(name, () -> new BlockItem((Block)supplier.get(), new net.minecraft.world.item.Item.Properties().setId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, com.starfish_studios.another_furniture.AnotherFurniture.res(name))).useBlockDescriptionPrefix()), null);
      return supplier;
   }

   public static <T extends net.minecraft.world.level.block.Block> Supplier<T> registerBlockOnly(String name, Supplier<T> block) {
      AFBlocks.Properties.pushId(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, com.starfish_studios.another_furniture.AnotherFurniture.res(name)));
      Supplier<T> supplier = AFRegistry.registerBlock(name, block);
      AFBlocks.Properties.popId();
      return supplier;
   }

   public static void registerFlammables() {
      AFRegistry.setFlammable(OAK_CHAIR, 5, 20);
      AFRegistry.setFlammable(SPRUCE_CHAIR, 5, 20);
      AFRegistry.setFlammable(BIRCH_CHAIR, 5, 20);
      AFRegistry.setFlammable(JUNGLE_CHAIR, 5, 20);
      AFRegistry.setFlammable(ACACIA_CHAIR, 5, 20);
      AFRegistry.setFlammable(DARK_OAK_CHAIR, 5, 20);
      AFRegistry.setFlammable(MANGROVE_CHAIR, 5, 20);
      AFRegistry.setFlammable(CHERRY_CHAIR, 5, 20);
      AFRegistry.setFlammable(BAMBOO_CHAIR, 5, 20);
      AFRegistry.setFlammable(OAK_SHELF, 5, 20);
      AFRegistry.setFlammable(SPRUCE_SHELF, 5, 20);
      AFRegistry.setFlammable(BIRCH_SHELF, 5, 20);
      AFRegistry.setFlammable(JUNGLE_SHELF, 5, 20);
      AFRegistry.setFlammable(ACACIA_SHELF, 5, 20);
      AFRegistry.setFlammable(DARK_OAK_SHELF, 5, 20);
      AFRegistry.setFlammable(MANGROVE_SHELF, 5, 20);
      AFRegistry.setFlammable(CHERRY_SHELF, 5, 20);
      AFRegistry.setFlammable(BAMBOO_SHELF, 5, 20);
      AFRegistry.setFlammable(OAK_TABLE, 5, 20);
      AFRegistry.setFlammable(SPRUCE_TABLE, 5, 20);
      AFRegistry.setFlammable(BIRCH_TABLE, 5, 20);
      AFRegistry.setFlammable(JUNGLE_TABLE, 5, 20);
      AFRegistry.setFlammable(ACACIA_TABLE, 5, 20);
      AFRegistry.setFlammable(DARK_OAK_TABLE, 5, 20);
      AFRegistry.setFlammable(MANGROVE_TABLE, 5, 20);
      AFRegistry.setFlammable(CHERRY_TABLE, 5, 20);
      AFRegistry.setFlammable(BAMBOO_TABLE, 5, 20);
      AFRegistry.setFlammable(OAK_SHUTTER, 5, 20);
      AFRegistry.setFlammable(SPRUCE_SHUTTER, 5, 20);
      AFRegistry.setFlammable(BIRCH_SHUTTER, 5, 20);
      AFRegistry.setFlammable(JUNGLE_SHUTTER, 5, 20);
      AFRegistry.setFlammable(ACACIA_SHUTTER, 5, 20);
      AFRegistry.setFlammable(DARK_OAK_SHUTTER, 5, 20);
      AFRegistry.setFlammable(MANGROVE_SHUTTER, 5, 20);
      AFRegistry.setFlammable(CHERRY_SHUTTER, 5, 20);
      AFRegistry.setFlammable(BAMBOO_SHUTTER, 5, 20);
      AFRegistry.setFlammable(OAK_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(SPRUCE_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(BIRCH_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(JUNGLE_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(ACACIA_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(DARK_OAK_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(MANGROVE_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(CHERRY_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(BAMBOO_FLOWER_BOX, 5, 20);
      AFRegistry.setFlammable(WHITE_STOOL, 5, 20);
      AFRegistry.setFlammable(ORANGE_STOOL, 5, 20);
      AFRegistry.setFlammable(MAGENTA_STOOL, 5, 20);
      AFRegistry.setFlammable(LIGHT_BLUE_STOOL, 5, 20);
      AFRegistry.setFlammable(YELLOW_STOOL, 5, 20);
      AFRegistry.setFlammable(LIME_STOOL, 5, 20);
      AFRegistry.setFlammable(PINK_STOOL, 5, 20);
      AFRegistry.setFlammable(GRAY_STOOL, 5, 20);
      AFRegistry.setFlammable(LIGHT_GRAY_STOOL, 5, 20);
      AFRegistry.setFlammable(CYAN_STOOL, 5, 20);
      AFRegistry.setFlammable(PURPLE_STOOL, 5, 20);
      AFRegistry.setFlammable(BLUE_STOOL, 5, 20);
      AFRegistry.setFlammable(BROWN_STOOL, 5, 20);
      AFRegistry.setFlammable(GREEN_STOOL, 5, 20);
      AFRegistry.setFlammable(RED_STOOL, 5, 20);
      AFRegistry.setFlammable(BLACK_STOOL, 5, 20);
      AFRegistry.setFlammable(WHITE_CURTAIN, 5, 20);
      AFRegistry.setFlammable(ORANGE_CURTAIN, 5, 20);
      AFRegistry.setFlammable(MAGENTA_CURTAIN, 5, 20);
      AFRegistry.setFlammable(LIGHT_BLUE_CURTAIN, 5, 20);
      AFRegistry.setFlammable(YELLOW_CURTAIN, 5, 20);
      AFRegistry.setFlammable(LIME_CURTAIN, 5, 20);
      AFRegistry.setFlammable(PINK_CURTAIN, 5, 20);
      AFRegistry.setFlammable(GRAY_CURTAIN, 5, 20);
      AFRegistry.setFlammable(LIGHT_GRAY_CURTAIN, 5, 20);
      AFRegistry.setFlammable(CYAN_CURTAIN, 5, 20);
      AFRegistry.setFlammable(PURPLE_CURTAIN, 5, 20);
      AFRegistry.setFlammable(BLUE_CURTAIN, 5, 20);
      AFRegistry.setFlammable(BROWN_CURTAIN, 5, 20);
      AFRegistry.setFlammable(GREEN_CURTAIN, 5, 20);
      AFRegistry.setFlammable(RED_CURTAIN, 5, 20);
      AFRegistry.setFlammable(BLACK_CURTAIN, 5, 20);
      AFRegistry.setFlammable(OAK_BENCH, 5, 20);
      AFRegistry.setFlammable(SPRUCE_BENCH, 5, 20);
      AFRegistry.setFlammable(BIRCH_BENCH, 5, 20);
      AFRegistry.setFlammable(JUNGLE_BENCH, 5, 20);
      AFRegistry.setFlammable(ACACIA_BENCH, 5, 20);
      AFRegistry.setFlammable(DARK_OAK_BENCH, 5, 20);
      AFRegistry.setFlammable(MANGROVE_BENCH, 5, 20);
      AFRegistry.setFlammable(CHERRY_BENCH, 5, 20);
      AFRegistry.setFlammable(BAMBOO_BENCH, 5, 20);
      AFRegistry.setFlammable(OAK_DRAWER, 5, 20);
      AFRegistry.setFlammable(SPRUCE_DRAWER, 5, 20);
      AFRegistry.setFlammable(BIRCH_DRAWER, 5, 20);
      AFRegistry.setFlammable(JUNGLE_DRAWER, 5, 20);
      AFRegistry.setFlammable(ACACIA_DRAWER, 5, 20);
      AFRegistry.setFlammable(DARK_OAK_DRAWER, 5, 20);
      AFRegistry.setFlammable(MANGROVE_DRAWER, 5, 20);
      AFRegistry.setFlammable(CHERRY_DRAWER, 5, 20);
      AFRegistry.setFlammable(BAMBOO_DRAWER, 5, 20);
      AFRegistry.setFlammable(WHITE_LAMP, 5, 20);
      AFRegistry.setFlammable(ORANGE_LAMP, 5, 20);
      AFRegistry.setFlammable(MAGENTA_LAMP, 5, 20);
      AFRegistry.setFlammable(LIGHT_BLUE_LAMP, 5, 20);
      AFRegistry.setFlammable(YELLOW_LAMP, 5, 20);
      AFRegistry.setFlammable(LIME_LAMP, 5, 20);
      AFRegistry.setFlammable(PINK_LAMP, 5, 20);
      AFRegistry.setFlammable(GRAY_LAMP, 5, 20);
      AFRegistry.setFlammable(LIGHT_GRAY_LAMP, 5, 20);
      AFRegistry.setFlammable(CYAN_LAMP, 5, 20);
      AFRegistry.setFlammable(PURPLE_LAMP, 5, 20);
      AFRegistry.setFlammable(BLUE_LAMP, 5, 20);
      AFRegistry.setFlammable(BROWN_LAMP, 5, 20);
      AFRegistry.setFlammable(GREEN_LAMP, 5, 20);
      AFRegistry.setFlammable(RED_LAMP, 5, 20);
      AFRegistry.setFlammable(BLACK_LAMP, 5, 20);
      AFRegistry.setFlammable(WHITE_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(ORANGE_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(MAGENTA_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(LIGHT_BLUE_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(YELLOW_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(LIME_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(PINK_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(GRAY_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(LIGHT_GRAY_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(CYAN_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(PURPLE_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(BLUE_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(BROWN_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(GREEN_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(RED_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(BLACK_LAMP_CONNECTOR, 5, 20);
      AFRegistry.setFlammable(WHITE_SOFA, 5, 20);
      AFRegistry.setFlammable(ORANGE_SOFA, 5, 20);
      AFRegistry.setFlammable(MAGENTA_SOFA, 5, 20);
      AFRegistry.setFlammable(LIGHT_BLUE_SOFA, 5, 20);
      AFRegistry.setFlammable(YELLOW_SOFA, 5, 20);
      AFRegistry.setFlammable(LIME_SOFA, 5, 20);
      AFRegistry.setFlammable(PINK_SOFA, 5, 20);
      AFRegistry.setFlammable(GRAY_SOFA, 5, 20);
      AFRegistry.setFlammable(LIGHT_GRAY_SOFA, 5, 20);
      AFRegistry.setFlammable(CYAN_SOFA, 5, 20);
      AFRegistry.setFlammable(PURPLE_SOFA, 5, 20);
      AFRegistry.setFlammable(BLUE_SOFA, 5, 20);
      AFRegistry.setFlammable(BROWN_SOFA, 5, 20);
      AFRegistry.setFlammable(GREEN_SOFA, 5, 20);
      AFRegistry.setFlammable(RED_SOFA, 5, 20);
      AFRegistry.setFlammable(BLACK_SOFA, 5, 20);
      AFRegistry.setFlammable(WHITE_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(ORANGE_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(MAGENTA_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(LIGHT_BLUE_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(YELLOW_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(LIME_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(PINK_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(GRAY_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(LIGHT_GRAY_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(CYAN_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(PURPLE_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(BLUE_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(BROWN_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(GREEN_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(RED_TALL_STOOL, 5, 20);
      AFRegistry.setFlammable(BLACK_TALL_STOOL, 5, 20);
   }

   public static void init() {
   }

   public static class Properties {
      private static final ThreadLocal<net.minecraft.resources.ResourceKey<net.minecraft.world.level.block.Block>> CURRENT_BLOCK = new ThreadLocal<>();

      public static void pushId(net.minecraft.resources.ResourceKey<net.minecraft.world.level.block.Block> id) {
         CURRENT_BLOCK.set(id);
      }

      public static void popId() {
         CURRENT_BLOCK.remove();
      }

      private static net.minecraft.world.level.block.state.BlockBehaviour.Properties withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties props) {
         net.minecraft.resources.ResourceKey<net.minecraft.world.level.block.Block> id = CURRENT_BLOCK.get();
         if (id != null) {
            props.setId(id);
         }
         return props;
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties oak_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.WOOD)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties spruce_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.PODZOL)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties birch_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.SAND)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties jungle_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.DIRT)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties acacia_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.COLOR_ORANGE)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties dark_oak_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.COLOR_BROWN)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties mangrove_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.COLOR_RED)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties crimson_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.CRIMSON_STEM)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.NETHER_WOOD));
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties warped_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.WARPED_STEM)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.NETHER_WOOD));
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties bamboo_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.COLOR_YELLOW)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.BAMBOO_WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties cherry_wood() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .mapColor(MapColor.TERRACOTTA_WHITE)
         .instrument(NoteBlockInstrument.BASS)
         .strength(2.0F, 3.0F)
         .sound(SoundType.CHERRY_WOOD)
         .ignitedByLava());
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties weak_wood() {
         return withId(oak_wood().strength(1.0F, 3.0F)
         .sound(SoundType.WOOD)
         .ignitedByLava()
         .mapColor(MapColor.WOOD));
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties curtain() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .strength(0.1F)
         .noOcclusion()
         .sound(SoundType.WOOL)
         .mapColor(MapColor.WOOL)
         .pushReaction(PushReaction.DESTROY));
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties service_bell() {
         return withId(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
         .strength(2.0F, 3.0F)
         .sound(SoundType.METAL)
         .mapColor(MapColor.METAL));
      }

      public static net.minecraft.world.level.block.state.BlockBehaviour.Properties lamp() {
         return withId(weak_wood().lightLevel(
         blockState -> blockState.hasProperty(BlockStateProperties.LIT) && blockState.getValue(BlockStateProperties.LIT)
            ? (Integer)blockState.getValue(ModBlockStateProperties.LEVEL_1_3) * 5
            : 0
      ));
      }

   }
}
