package earth.terrarium.chipped.common.registry;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.chipped.Chipped;
import earth.terrarium.chipped.common.recipes.ChippedRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class ModRecipeSerializers {
    public static final ResourcefulRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = ResourcefulRegistries.create(BuiltInRegistries.RECIPE_SERIALIZER, Chipped.MOD_ID);

    public static final RegistryEntry<RecipeSerializer<ChippedRecipe>> BOTANIST_WORKBENCH = RECIPE_SERIALIZERS.register("botanist_workbench", () -> new ChippedRecipe.Serializer(ModRecipeTypes.BOTANIST_WORKBENCH, ModBlocks.BOTANIST_WORKBENCH).toRecipeSerializer());
    public static final RegistryEntry<RecipeSerializer<ChippedRecipe>> GLASSBLOWER = RECIPE_SERIALIZERS.register("glassblower", () -> new ChippedRecipe.Serializer(ModRecipeTypes.GLASSBLOWER, ModBlocks.GLASSBLOWER).toRecipeSerializer());
    public static final RegistryEntry<RecipeSerializer<ChippedRecipe>> CARPENTERS_TABLE = RECIPE_SERIALIZERS.register("carpenters_table", () -> new ChippedRecipe.Serializer(ModRecipeTypes.CARPENTERS_TABLE, ModBlocks.CARPENTERS_TABLE).toRecipeSerializer());
    public static final RegistryEntry<RecipeSerializer<ChippedRecipe>> LOOM_TABLE = RECIPE_SERIALIZERS.register("loom_table", () -> new ChippedRecipe.Serializer(ModRecipeTypes.LOOM_TABLE, ModBlocks.LOOM_TABLE).toRecipeSerializer());
    public static final RegistryEntry<RecipeSerializer<ChippedRecipe>> MASON_TABLE = RECIPE_SERIALIZERS.register("mason_table", () -> new ChippedRecipe.Serializer(ModRecipeTypes.MASON_TABLE, ModBlocks.MASON_TABLE).toRecipeSerializer());
    public static final RegistryEntry<RecipeSerializer<ChippedRecipe>> ALCHEMY_BENCH = RECIPE_SERIALIZERS.register("alchemy_bench", () -> new ChippedRecipe.Serializer(ModRecipeTypes.ALCHEMY_BENCH, ModBlocks.ALCHEMY_BENCH).toRecipeSerializer());
    public static final RegistryEntry<RecipeSerializer<ChippedRecipe>> TINKERING_TABLE = RECIPE_SERIALIZERS.register("tinkering_table", () -> new ChippedRecipe.Serializer(ModRecipeTypes.TINKERING_TABLE, ModBlocks.TINKERING_TABLE).toRecipeSerializer());

    public static RecipeSerializer<ChippedRecipe> forType(RecipeType<?> type) {
        if (type == ModRecipeTypes.BOTANIST_WORKBENCH.get()) return BOTANIST_WORKBENCH.get();
        if (type == ModRecipeTypes.GLASSBLOWER.get()) return GLASSBLOWER.get();
        if (type == ModRecipeTypes.CARPENTERS_TABLE.get()) return CARPENTERS_TABLE.get();
        if (type == ModRecipeTypes.LOOM_TABLE.get()) return LOOM_TABLE.get();
        if (type == ModRecipeTypes.MASON_TABLE.get()) return MASON_TABLE.get();
        if (type == ModRecipeTypes.ALCHEMY_BENCH.get()) return ALCHEMY_BENCH.get();
        if (type == ModRecipeTypes.TINKERING_TABLE.get()) return TINKERING_TABLE.get();
        throw new IllegalArgumentException("Unknown chipped recipe type");
    }
}
