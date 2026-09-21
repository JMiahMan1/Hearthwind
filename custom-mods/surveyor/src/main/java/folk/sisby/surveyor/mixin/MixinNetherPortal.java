package folk.sisby.surveyor.mixin;

import folk.sisby.surveyor.Surveyor;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PortalShape.class)
public class MixinNetherPortal {
	@Shadow @Final private @Nullable BlockPos lowerCorner;
	@Shadow @Final private int height;
	@Shadow @Final private Direction negativeDir;
	@Shadow @Final private int width;

	@Inject(method = "createPortal", at = @At("TAIL"))
	private void onCreatePortal(LevelAccessor world, CallbackInfo ci) {
		if (!Surveyor.CONFIG.builtins.netherPortalLandmarks) return;
		if (!(world instanceof ServerLevel serverWorld)) return;
		WorldLandmarks landmarks = WorldLandmarks.of(serverWorld);
		if (landmarks == null) return;
		Identifier id = Identifier.fromNamespaceAndPath(PoiTypes.NETHER_PORTAL.identifier().getNamespace(), "poi/%s/%s/%s/%s".formatted(PoiTypes.NETHER_PORTAL.identifier().getPath(), lowerCorner.getX(), lowerCorner.getY(), lowerCorner.getZ()));
		landmarks.put(Landmark.global(id, builder -> LandmarkComponentTypes.forBlock(builder, serverWorld, lowerCorner)
			.add(LandmarkComponentTypes.COLOR, DyeColor.PURPLE.getFireworkColor())
			.add(LandmarkComponentTypes.BOX, BoundingBox.create(lowerCorner, this.lowerCorner.offset(Direction.UP, this.height - 1).offset(negativeDir, width - 1)))
		));
	}
}
