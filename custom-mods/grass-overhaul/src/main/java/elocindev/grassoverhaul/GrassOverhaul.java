package elocindev.grassoverhaul;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrassOverhaul implements ModInitializer {
   public static final String MODID = "grassoverhaul";
   public static final Logger LOGGER = LoggerFactory.getLogger("grassoverhaul");

   public void onInitialize() {
      GOBlocks.init();
      CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS)
         .register(output -> {
            output.insertAfter(Items.GRASS_BLOCK, GOBlocks.GRASS_SOD_BLOCK);
            output.insertAfter(GOBlocks.GRASS_SOD_BLOCK, GOBlocks.GRASS_SOD_STAIRS);
            output.insertAfter(GOBlocks.GRASS_SOD_STAIRS, GOBlocks.GRASS_SOD_SLAB);
            output.insertAfter(GOBlocks.GRASS_SOD_SLAB, GOBlocks.GRASS_SOD_PATCH);
            output.insertAfter(Items.DIRT, GOBlocks.DIRT_SOD_STAIRS);
            output.insertAfter(GOBlocks.DIRT_SOD_STAIRS, GOBlocks.DIRT_SOD_SLAB);
            output.insertAfter(GOBlocks.DIRT_SOD_SLAB, GOBlocks.DIRT_SOD_PATCH);
            output.insertAfter(Items.MYCELIUM, GOBlocks.MYCELIUM_SOD_BLOCK);
            output.insertAfter(GOBlocks.MYCELIUM_SOD_BLOCK, GOBlocks.MYCELIUM_SOD_STAIRS);
            output.insertAfter(GOBlocks.MYCELIUM_SOD_STAIRS, GOBlocks.MYCELIUM_SOD_SLAB);
            output.insertAfter(GOBlocks.MYCELIUM_SOD_SLAB, GOBlocks.MYCELIUM_SOD_PATCH);
            output.insertAfter(Items.PODZOL, GOBlocks.PODZOL_SOD_BLOCK);
            output.insertAfter(GOBlocks.PODZOL_SOD_BLOCK, GOBlocks.PODZOL_SOD_STAIRS);
            output.insertAfter(GOBlocks.PODZOL_SOD_STAIRS, GOBlocks.PODZOL_SOD_SLAB);
            output.insertAfter(GOBlocks.PODZOL_SOD_SLAB, GOBlocks.PODZOL_SOD_PATCH);
            output.insertAfter(Items.DIRT_PATH, GOBlocks.PATH_SOD_BLOCK);
            output.insertAfter(GOBlocks.PATH_SOD_BLOCK, GOBlocks.PATH_SOD_STAIRS);
            output.insertAfter(GOBlocks.PATH_SOD_STAIRS, GOBlocks.PATH_SOD_SLAB);
            output.insertAfter(GOBlocks.PATH_SOD_SLAB, GOBlocks.PATH_SOD_PATCH);
            output.insertAfter(Items.CRIMSON_NYLIUM, GOBlocks.CRIMSON_NYLIUM_SOD_BLOCK);
            output.insertAfter(GOBlocks.CRIMSON_NYLIUM_SOD_BLOCK, GOBlocks.CRIMSON_NYLIUM_SOD_STAIRS);
            output.insertAfter(GOBlocks.CRIMSON_NYLIUM_SOD_STAIRS, GOBlocks.CRIMSON_NYLIUM_SOD_SLAB);
            output.insertAfter(GOBlocks.CRIMSON_NYLIUM_SOD_SLAB, GOBlocks.CRIMSON_NYLIUM_SOD_PATCH);
            output.insertAfter(Items.WARPED_NYLIUM, GOBlocks.WARPED_NYLIUM_SOD_BLOCK);
            output.insertAfter(GOBlocks.WARPED_NYLIUM_SOD_BLOCK, GOBlocks.WARPED_NYLIUM_SOD_STAIRS);
            output.insertAfter(GOBlocks.WARPED_NYLIUM_SOD_STAIRS, GOBlocks.WARPED_NYLIUM_SOD_SLAB);
            output.insertAfter(GOBlocks.WARPED_NYLIUM_SOD_SLAB, GOBlocks.WARPED_NYLIUM_SOD_PATCH);
         });
   }
}
