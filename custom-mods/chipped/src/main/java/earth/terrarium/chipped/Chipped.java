package earth.terrarium.chipped;

import earth.terrarium.chipped.common.network.NetworkHandler;
import earth.terrarium.chipped.common.registry.*;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class Chipped {
    public static final String MOD_ID = "chipped";

    public static void init() {
        NetworkHandler.init();

        ModBlocks.BLOCKS.init();
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
