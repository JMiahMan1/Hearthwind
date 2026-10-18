package net.medievalweapons.init;

import java.util.LinkedHashMap;
import java.util.Map;
import net.medievalweapons.MedievalMain;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;

public class EntityInit {
   public static final Map<Identifier, EntityType<?>> ENTITY_TYPES = new LinkedHashMap<>();
   public static final ResourceKey<net.minecraft.world.damagesource.DamageType> JAVELIN = ResourceKey.create(
      Registries.DAMAGE_TYPE, MedievalMain.identifierOf("javelin")
   );
   public static final ResourceKey<net.minecraft.world.damagesource.DamageType> FRANCISCA = ResourceKey.create(
      Registries.DAMAGE_TYPE, MedievalMain.identifierOf("francisca")
   );
   public static final ResourceKey<net.minecraft.world.damagesource.DamageType> BLEEDING = ResourceKey.create(
      Registries.DAMAGE_TYPE, MedievalMain.identifierOf("bleed")
   );

   public static void init() {
      for (Identifier id : ENTITY_TYPES.keySet()) {
         Registry.register(BuiltInRegistries.ENTITY_TYPE, id, ENTITY_TYPES.get(id));
      }
   }

   public static <T extends EntityType<?>> T register(Identifier id, T type) {
      ENTITY_TYPES.put(id, type);
      return type;
   }
}
