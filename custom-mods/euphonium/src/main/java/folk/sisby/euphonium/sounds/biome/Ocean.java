package folk.sisby.euphonium.sounds.biome;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import folk.sisby.euphonium.sound.BiomeSound;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.SurfaceBiomeSound;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import org.jetbrains.annotations.Nullable;

public class Ocean implements ISoundType<BiomeSound> {
   public static final Predicate<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> VALID_BIOME = holder -> holder.is(
         BiomeTags.IS_OCEAN
      )
      || holder.is(ConventionalBiomeTags.IS_OCEAN);
   public static SoundEvent DAY_SOUND;
   public static SoundEvent NIGHT_SOUND;

   public Ocean() {
      DAY_SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.ocean.day"));
      NIGHT_SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.ocean.night"));
   }

   @Override
   public void addSounds(SoundHandler<BiomeSound> handler) {
      handler.getSounds()
         .add(
            new SurfaceBiomeSound(handler.getPlayer(), true) {
               @Nullable
               @Override
               public SoundEvent getSound() {
                  return Ocean.DAY_SOUND;
               }

               @Override
               public boolean isValidPlayerCondition() {
                  return super.isValidPlayerCondition() && WorldHelper.isDay(this.player);
               }

               @Override
               public boolean isValidBiomeCondition(
                  net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder,
                  net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key
               ) {
                  return Ocean.VALID_BIOME.test(holder);
               }
            }
         );
      handler.getSounds()
         .add(
            new SurfaceBiomeSound(handler.getPlayer(), true) {
               @Nullable
               @Override
               public SoundEvent getSound() {
                  return Ocean.NIGHT_SOUND;
               }

               @Override
               public boolean isValidPlayerCondition() {
                  return super.isValidPlayerCondition() && WorldHelper.isNight(this.player);
               }

               @Override
               public boolean isValidBiomeCondition(
                  net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder,
                  net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key
               ) {
                  return Ocean.VALID_BIOME.test(holder);
               }
            }
         );
   }
}
