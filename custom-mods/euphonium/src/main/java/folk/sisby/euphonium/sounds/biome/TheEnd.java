package folk.sisby.euphonium.sounds.biome;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.sound.BiomeSound;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import org.jetbrains.annotations.Nullable;

public class TheEnd implements ISoundType<BiomeSound> {
   public static SoundEvent SOUND;
   public static Predicate<net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome>> VALID_BIOME = holder -> holder.is(
      BiomeTags.IS_END
   );

   public TheEnd() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("biome.the_end"));
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
                  return TheEnd.VALID_BIOME.test(holder);
               }

               @Override
               public boolean isValidPlayerCondition() {
                  return true;
               }

               @Nullable
               @Override
               public SoundEvent getSound() {
                  return TheEnd.SOUND;
               }
            }
         );
   }
}
