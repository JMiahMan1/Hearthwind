package folk.sisby.euphonium.sounds.biome;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.sound.BiomeSound;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.SurfaceBiomeSound;
import java.util.function.BiPredicate;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

public class River implements ISoundType<BiomeSound> {
   public static SoundEvent SOUND;
   public static BiPredicate<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>, net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome>> VALID_BIOME = (holder, key) -> key.equals(
         Biomes.RIVER
      )
      || holder.is(ConventionalBiomeTags.IS_RIVER);

   public River() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.river"));
   }

   @Override
   public void addSounds(SoundHandler<BiomeSound> handler) {
      handler.getSounds()
         .add(
            new SurfaceBiomeSound(handler.getPlayer(), true) {
               @Nullable
               @Override
               public SoundEvent getSound() {
                  return River.SOUND;
               }

               @Override
               public boolean isValidBiomeCondition(
                  net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> holder,
                  net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> key
               ) {
                  return River.VALID_BIOME.test(holder, key);
               }
            }
         );
   }
}
