package net.medievalweapons.init;

import net.medievalweapons.MedievalMain;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class SoundInit {
   public static final Identifier MAGIC_HEAL_AURA = MedievalMain.identifierOf("magic_heal_aura");
   public static final Identifier MAGIC_SHOT = MedievalMain.identifierOf("magic_shot");
   public static final Identifier PARRYING = MedievalMain.identifierOf("parrying");
   public static final Identifier SWORD_PARRYING = MedievalMain.identifierOf("sword_parrying");
   public static SoundEvent SWORD_PARRYING_EVENT = SoundEvent.createVariableRangeEvent(SWORD_PARRYING);
   public static SoundEvent MAGIC_HEAL_AURA_EVENT = SoundEvent.createVariableRangeEvent(MAGIC_HEAL_AURA);
   public static SoundEvent MAGIC_SHOT_EVENT = SoundEvent.createVariableRangeEvent(MAGIC_SHOT);
   public static SoundEvent PARRYING_EVENT = SoundEvent.createVariableRangeEvent(PARRYING);

   public static void init() {
      Registry.register(BuiltInRegistries.SOUND_EVENT, MAGIC_HEAL_AURA, MAGIC_HEAL_AURA_EVENT);
      Registry.register(BuiltInRegistries.SOUND_EVENT, MAGIC_SHOT, MAGIC_SHOT_EVENT);
      Registry.register(BuiltInRegistries.SOUND_EVENT, PARRYING, PARRYING_EVENT);
      Registry.register(BuiltInRegistries.SOUND_EVENT, SWORD_PARRYING, SWORD_PARRYING_EVENT);
   }
}
