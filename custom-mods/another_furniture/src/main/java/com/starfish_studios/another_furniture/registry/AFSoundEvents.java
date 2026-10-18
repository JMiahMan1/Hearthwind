package com.starfish_studios.another_furniture.registry;

import com.starfish_studios.another_furniture.AnotherFurniture;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;

public class AFSoundEvents {
   public static final Supplier<net.minecraft.sounds.SoundEvent> SERVICE_BELL = register("block.service_bell.use");
   public static final Supplier<net.minecraft.sounds.SoundEvent> CHAIR_TUCK = register("block.chair.tuck");
   public static final Supplier<net.minecraft.sounds.SoundEvent> CHAIR_UNTUCK = register("block.chair.untuck");
   public static final Supplier<net.minecraft.sounds.SoundEvent> CURTAIN = register("block.curtain.use");
   public static final Supplier<net.minecraft.sounds.SoundEvent> HAMMER_USE = register("item.furniture_hammer.use");

   public static Supplier<net.minecraft.sounds.SoundEvent> register(String name) {
      return AFRegistry.registerSoundEvent(name, (Supplier<net.minecraft.sounds.SoundEvent>)(() -> SoundEvent.createVariableRangeEvent(AnotherFurniture.res(name))));
   }

   public static void init() {
   }
}
