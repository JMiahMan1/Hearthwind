package net.medievalweapons.init;

import net.medievalweapons.MedievalMain;
import net.medievalweapons.effect.BleedEffect;
import net.medievalweapons.effect.StunEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectCategory;

public class EffectInit {
   public static final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> STUN_EFFECT = register(
      MedievalMain.identifierOf("stun"), new StunEffect(MobEffectCategory.HARMFUL, 12221440)
   );
   public static final net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> BLEED_EFFECT = register(
      MedievalMain.identifierOf("bleed"), new BleedEffect(MobEffectCategory.HARMFUL, 10303293)
   );

   public static void init() {
   }

   private static net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> register(
      Identifier id, net.minecraft.world.effect.MobEffect statusEffect
   ) {
      return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, id, statusEffect);
   }
}
