package folk.sisby.euphonium.sounds.biome;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.sound.BiomeSound;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

public class Caves implements ISoundType<BiomeSound> {
   public static final Predicate<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> VALID_BIOME = holder -> holder.is(
      BiomeTags.IS_BEACH
   );
   public static SoundEvent DEEP_DARK;
   public static SoundEvent DRIPSTONE;
   public static SoundEvent LUSH;

   public Caves() {
      DEEP_DARK = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.caves.deep_dark"));
      DRIPSTONE = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.caves.dripstone"));
      LUSH = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.caves.lush"));
   }

   @Override
   public void addSounds(SoundHandler<BiomeSound> handler) {
      handler.getSounds()
         .add(
            new BiomeSound(handler.getPlayer()) {
               @Override
               public boolean isValidBiomeCondition(
                  net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder,
                  net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key
               ) {
                  return key.equals(Biomes.DEEP_DARK);
               }

               @Override
               public boolean isValidPlayerCondition() {
                  return true;
               }

               @Nullable
               @Override
               public SoundEvent getSound() {
                  return Caves.DEEP_DARK;
               }
            }
         );
      handler.getSounds()
         .add(
            new BiomeSound(handler.getPlayer()) {
               @Override
               public boolean isValidBiomeCondition(
                  net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder,
                  net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key
               ) {
                  return key.equals(Biomes.DRIPSTONE_CAVES);
               }

               @Override
               public boolean isValidPlayerCondition() {
                  return true;
               }

               @Nullable
               @Override
               public SoundEvent getSound() {
                  return Caves.DRIPSTONE;
               }
            }
         );
      handler.getSounds()
         .add(
            new BiomeSound(handler.getPlayer()) {
               @Override
               public boolean isValidBiomeCondition(
                  net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder,
                  net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key
               ) {
                  return key.equals(Biomes.LUSH_CAVES);
               }

               @Override
               public boolean isValidPlayerCondition() {
                  return true;
               }

               @Nullable
               @Override
               public SoundEvent getSound() {
                  return Caves.LUSH;
               }
            }
         );
   }
}
