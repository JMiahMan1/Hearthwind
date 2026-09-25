package earth.terrarium.chipped;

import earth.terrarium.chipped.common.network.NetworkHandler;
import earth.terrarium.chipped.common.registry.*;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

public class Chipped {
    public static final String MOD_ID = "chipped";

    public static void init() {
        NetworkHandler.init();

        ModBlocks.BLOCKS.init();
        // 26.x validates a block entity against its type's allowed blocks.
        // Chipped's barrel/crate palette are plain BarrelBlocks reusing
        // vanilla's BARREL entity; without this, placing one crashes with
        // "Invalid block entity minecraft:barrel".
        FabricBlockEntityType barrel = (FabricBlockEntityType) BlockEntityTypes.BARREL;
        BuiltInRegistries.BLOCK.entrySet().forEach(e -> {
            if (e.getKey().identifier().getNamespace().equals(MOD_ID) && e.getValue() instanceof BarrelBlock) {
                barrel.addValidBlock(e.getValue());
            }
        });
        ModItems.ITEMS.init();
        ModItems.TABS.init();
        ModMenuTypes.MENUS.init();
        ModRecipeTypes.RECIPE_TYPES.init();
        ModRecipeSerializers.RECIPE_SERIALIZERS.init();
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.BOTANIST_WORKBENCH.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.GLASSBLOWER.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.CARPENTERS_TABLE.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.LOOM_TABLE.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.MASON_TABLE.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.ALCHEMY_BENCH.get());
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipeSerializers.TINKERING_TABLE.get());
    }
}
