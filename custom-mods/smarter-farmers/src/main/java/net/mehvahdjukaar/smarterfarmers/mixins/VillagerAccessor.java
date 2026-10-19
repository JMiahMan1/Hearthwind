package net.mehvahdjukaar.smarterfarmers.mixins;

import java.util.Map;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Villager.class})
public interface VillagerAccessor {
   @Mutable
   @Accessor("FOOD_POINTS")
   static void setFoodPoints(Map<net.minecraft.world.item.Item, Integer> map) {
   }
}
