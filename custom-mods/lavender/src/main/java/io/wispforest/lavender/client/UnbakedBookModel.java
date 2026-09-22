package io.wispforest.lavender.client;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModels;
import org.joml.Matrix4fc;

public class UnbakedBookModel implements ItemModel.Unbaked {

    public static final MapCodec<UnbakedBookModel> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            ItemModels.CODEC.fieldOf("default").forGetter(UnbakedBookModel::defaultModel)
        ).apply(instance, UnbakedBookModel::new)
    );

    private final ItemModel.Unbaked defaultModel;

    public UnbakedBookModel(ItemModel.Unbaked defaultModel) {
        this.defaultModel = defaultModel;
    }

    public ItemModel.Unbaked defaultModel() {
        return this.defaultModel;
    }

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type() {
        return CODEC;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        this.defaultModel.resolveDependencies(resolver);
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
        return new BookBakedModel(this.defaultModel.bake(context, transformation));
    }
}
