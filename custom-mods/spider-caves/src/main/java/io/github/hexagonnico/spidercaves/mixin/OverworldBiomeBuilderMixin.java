package io.github.hexagonnico.spidercaves.mixin;

import com.mojang.datafixers.util.Pair;
import io.github.hexagonnico.spidercaves.SpiderCaves;
import io.github.phantomloader.library.platform.PlatformHelper;
import java.util.function.Consumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import net.minecraft.world.level.biome.Climate.Parameter;
import net.minecraft.world.level.biome.Climate.ParameterPoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({OverworldBiomeBuilder.class})
public class OverworldBiomeBuilderMixin {
   private static final Parameter TEMPERATURE = Parameter.span(-0.9F, 0.3F);
   private static final Parameter HUMIDITY = Parameter.span(-1.0F, -0.4F);
   private static final Parameter CONTINENTALNESS = Parameter.span(0.5F, 1.0F);
   private static final Parameter EROSION = Parameter.span(-1.0F, 1.0F);
   private static final Parameter DEPTH = Parameter.span(0.2F, 0.9F);
   private static final Parameter WEIRDNESS = Parameter.span(0.7F, 1.0F);

   @Inject(
      at = {@At("RETURN")},
      method = {"addUndergroundBiomes"}
   )
   public void addUndergroundBiomes(
      Consumer<Pair<net.minecraft.world.level.biome.Climate.ParameterPoint, net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>>> consumer,
      CallbackInfo ci
   ) {
      if (!PlatformHelper.isModLoaded("terrablender")) {
         net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> undergroundJungleKey = ResourceKey.create(
            Registries.BIOME, Identifier.fromNamespaceAndPath(SpiderCaves.modId(), "spider_cave")
         );
         ParameterPoint undergroundJungleClimate = Climate.parameters(TEMPERATURE, HUMIDITY, CONTINENTALNESS, EROSION, DEPTH, WEIRDNESS, 0.0F);
         consumer.accept(Pair.of(undergroundJungleClimate, undergroundJungleKey));
      }
   }
}
