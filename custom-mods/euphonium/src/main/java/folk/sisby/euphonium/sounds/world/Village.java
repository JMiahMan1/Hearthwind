package folk.sisby.euphonium.sounds.world;

import folk.sisby.euphonium.EuphoniumClient;
import folk.sisby.euphonium.helper.WorldHelper;
import folk.sisby.euphonium.sound.ISoundType;
import folk.sisby.euphonium.sound.SoundHandler;
import folk.sisby.euphonium.sound.SurfaceWorldSound;
import folk.sisby.euphonium.sound.WorldSound;
import java.util.List;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class Village implements ISoundType<WorldSound> {
   public static SoundEvent SOUND;

   public Village() {
      SOUND = SoundEvent.createVariableRangeEvent(EuphoniumClient.id("world.village"));
   }

   @Override
   public void addSounds(SoundHandler<WorldSound> handler) {
      if (EuphoniumClient.CONFIG.worldAmbience.village) {
         handler.getSounds().add(new SurfaceWorldSound(handler.getPlayer()) {
            @Override
            public boolean isValidSituationCondition() {
               AABB bb = new AABB(this.player.blockPosition()).inflate(32.0);
               List<net.minecraft.world.entity.npc.villager.Villager> villagers = this.level.getEntitiesOfClass(Villager.class, bb);
               if (villagers.size() >= 2) {
                  Villager villager = (Villager)villagers.get(this.player.getRandom().nextInt(villagers.size()));
                  this.setPos(villager.blockPosition());
                  return true;
               } else {
                  return false;
               }
            }

            @Override
            public boolean isValidPlayerCondition() {
               return super.isValidPlayerCondition() && !WorldHelper.isNight(this.player);
            }

            @Nullable
            @Override
            public SoundEvent getSound() {
               return Village.SOUND;
            }

            @Override
            public int getDelay() {
               return this.level.getRandom().nextInt(400) + 320;
            }

            @Override
            public float getVolume() {
               return 0.82F;
            }
         });
      }
   }
}
