package folk.sisby.euphonium.sounds.biome;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import folk.sisby.euphonium.sound.BiomeSound;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.SurfaceBiomeSound;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import org.jetbrains.annotations.Nullable;

public class Taiga implements ISoundType<BiomeSound> {
   public static final Predicate<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> VALID_BIOME = holder -> holder.is(
      BiomeTags.IS_TAIGA
   );
   public static SoundEvent DAY_SOUND;
   public static SoundEvent NIGHT_SOUND;

   public Taiga() {
      DAY_SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.taiga.day"));
      NIGHT_SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.taiga.night"));
   }

   @Override
   public void addSounds(SoundHandler<BiomeSound> handler) {
      handler.getSounds()
         .add(
            new SurfaceBiomeSound(handler.getPlayer(), false) {
               @Nullable
               @Override
               public SoundEvent getSound() {
                  return Taiga.DAY_SOUND;
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
                  return Taiga.VALID_BIOME.test(holder);
               }
            }
         );
      handler.getSounds()
         .add(
            new SurfaceBiomeSound(handler.getPlayer(), false) {
               @Nullable
               @Override
               public SoundEvent getSound() {
                  return Taiga.NIGHT_SOUND;
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
                  return Taiga.VALID_BIOME.test(holder);
               }
            }
         );
   }
}
