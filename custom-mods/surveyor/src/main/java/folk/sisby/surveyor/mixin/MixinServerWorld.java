package folk.sisby.surveyor.mixin;

import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class MixinServerWorld {
	@Inject(method = "method_66017(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/registry/entry/Holder;)V", at = @At("HEAD"))
	public void onPointOfInterestAdded(BlockPos blockPos, Holder<PoiType> poiType, CallbackInfo ci) {
		ServerLevel self = (ServerLevel) (Object) this;
		WorldLandmarks landmarks = WorldLandmarks.of(self);
		if (landmarks == null) return;
		if (poiType.getKey().isEmpty() || !Surveyor.CONFIG.builtins.poiLandmarks.contains(poiType.getKey().get().getValue().toString())) return;
		Identifier poi = poiType.getKey().get().getValue();
		landmarks.put(Landmark.global(
			Identifier.fromNamespaceAndPath(poi.getNamespace(), "poi/%s/%s/%s/%s".formatted(poi.getPath(), blockPos.getX(), blockPos.getY(), blockPos.getZ())),
			builder -> LandmarkComponentTypes.forBlock(builder, self, blockPos)
		));
	}

	@Inject(method = "method_66019(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/registry/entry/Holder;)V", at = @At("HEAD"))
	public void onPointOfInterestRemoved(BlockPos blockPos, Holder<PoiType> oldPoiType, CallbackInfo ci) {
		ServerLevel self = (ServerLevel) (Object) this;
		WorldLandmarks landmarks = WorldLandmarks.of(self);
		if (landmarks == null) return;
		landmarks.removeAll(l -> l.owner().equals(WorldLandmarks.GLOBAL)
			&& l.id().getPath().startsWith("poi")
			&& l.components().contains(LandmarkComponentTypes.POS)
			&& l.components().get(LandmarkComponentTypes.POS).equals(blockPos)
		);
	}
}
