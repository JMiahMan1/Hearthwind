package io.wispforest.lmft.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.wispforest.lmft.LMFTCommon;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = TagLoader.class, priority = 800)
public abstract class TagGroupLoaderMixin<T> {
    @WrapOperation(method = "tryBuildTag(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    private boolean preventTagsFromFailingToLoad(List list2, Operation<Boolean> original){
        LMFTCommon.handleAndLogInvalidEntries(list2);

        return original.call(list2);
    }

    // 26.2: the per-tag build body is lambda$build$1 - its args are
    // (TagEntry$Lookup, Map, Identifier, SortingEntry), so the ordinal-0 Identifier
    // below is still the tag id. 1.21.11 called this lambda$build$6.
    @Inject(method = "lambda$build$1", at = @At("HEAD"), require = 1, allow = 1)
    private void saveTagId(CallbackInfo ci, @Local(ordinal = 0, argsOnly = true) Identifier id){
        LMFTCommon.setTagId(id);
    }
}
