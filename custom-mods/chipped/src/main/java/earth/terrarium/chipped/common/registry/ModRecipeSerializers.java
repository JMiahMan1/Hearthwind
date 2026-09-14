package earth.terrarium.chipped.common.registry;

import com.teamresourceful.resourcefullib.common.registry.RegistryEntry;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistries;
import com.teamresourceful.resourcefullib.common.registry.ResourcefulRegistry;
import earth.terrarium.chipped.Chipped;
import earth.terrarium.chipped.common.recipes.ChippedRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ModRecipeSerializers {
    public static final ResourcefulRegistry<RecipeSerializer<?>> RECIPE_SERIALIZERS = ResourcefulRegistries.create(BuiltInRegistries.RECIPE_SERIALIZER, Chipped.MOD_ID);

    public static final RegistryEntry<RecipeSerializer<ChippedRecipe>> WORKBENCH = RECIPE_SERIALIZERS.register("workbench", () ->
        new RecipeSerializer<>(ChippedRecipe.CODEC, StreamCodec.of(
            (RegistryFriendlyByteBuf buf, ChippedRecipe recipe) -> ChippedRecipe.NETWORK_CODEC.encode(recipe, buf),
            buf -> ChippedRecipe.NETWORK_CODEC.decode(buf)
        )));
}
