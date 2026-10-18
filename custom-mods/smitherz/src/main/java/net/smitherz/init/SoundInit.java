package net.smitherz.init;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.smitherz.SmitherzMain;

public class SoundInit {
   public static SoundEvent LINKAGE_SUCCESS_EVENT = register("linkage_success");
   public static SoundEvent LINKAGE_FAILURE_EVENT = register("linkage_failure");

   private static SoundEvent register(String id) {
      return (SoundEvent)Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(SmitherzMain.identifierOf(id)));
   }

   public static void init() {
   }
}
