package net.jmb19905.niftycarts.client.mixin;

import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Environment(EnvType.CLIENT)
@Mixin({ModelPart.class})
public interface ModelPartMixin {
   @Accessor("children")
   Map<String, net.minecraft.client.model.geom.ModelPart> getChildren();
}
