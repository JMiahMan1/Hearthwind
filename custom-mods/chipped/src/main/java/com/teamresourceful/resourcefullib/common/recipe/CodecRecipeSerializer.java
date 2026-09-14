package com.teamresourceful.resourcefullib.common.recipe;

import com.mojang.serialization.MapCodec;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

public class CodecRecipeSerializer<T extends Recipe<?>> {
    public CodecRecipeSerializer(RecipeType<T> type, MapCodec<T> codec, ByteCodec<T> networkCodec) {}
}
