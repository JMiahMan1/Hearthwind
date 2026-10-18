package net.smitherz.init;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponentType.Builder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item.Properties;
import net.smitherz.item.Gem;
import net.smitherz.item.Hammer;
import net.smitherz.item.component.GemComponent;

public class ItemInit {
   public static final net.minecraft.resources.ResourceKey<net.minecraft.world.item.CreativeModeTab> SMITHERZ_ITEM_GROUP = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath("smitherz", "item_group")
   );
   public static final ArrayList<net.minecraft.world.item.Item> ITEMS = new ArrayList<>();
   public static final Item SMITHER_HAMMER_1 = register("smither_hammer_1", new Hammer(new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "smither_hammer_1"))).rarity(Rarity.UNCOMMON), 5.0F));
   public static final Item SMITHER_HAMMER_2 = register("smither_hammer_2", new Hammer(new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "smither_hammer_2"))).rarity(Rarity.UNCOMMON), 10.0F));
   public static final Item SMITHER_HAMMER_3 = register("smither_hammer_3", new Hammer(new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "smither_hammer_3"))).rarity(Rarity.UNCOMMON), 20.0F));
   public static final Item SMITHER_HAMMER_4 = register("smither_hammer_4", new Hammer(new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "smither_hammer_4"))).rarity(Rarity.UNCOMMON), 50.0F));
   public static final Item EXTRACTION_HAMMER_1 = register("extraction_hammer_1", new Hammer(new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "extraction_hammer_1"))).rarity(Rarity.UNCOMMON), 5.0F));
   public static final Item EXTRACTION_HAMMER_2 = register("extraction_hammer_2", new Hammer(new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "extraction_hammer_2"))).rarity(Rarity.UNCOMMON), 10.0F));
   public static final Item EXTRACTION_HAMMER_3 = register("extraction_hammer_3", new Hammer(new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "extraction_hammer_3"))).rarity(Rarity.UNCOMMON), 20.0F));
   public static final Item EXTRACTION_HAMMER_4 = register("extraction_hammer_4", new Hammer(new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "extraction_hammer_4"))).rarity(Rarity.UNCOMMON), 50.0F));
   public static Item STRENGTH_I;
   public static Item STRENGTH_II;
   public static Item STRENGTH_III;
   public static Item STRENGTH_IV;
   public static Item STRENGTH_V;
   public static Item DEFENSE_I;
   public static Item DEFENSE_II;
   public static Item DEFENSE_III;
   public static Item DEFENSE_IV;
   public static Item DEFENSE_V;
   public static Item HEALTH_I;
   public static Item HEALTH_II;
   public static Item HEALTH_III;
   public static Item HEALTH_IV;
   public static Item HEALTH_V;
   public static Item SPEED_I;
   public static Item SPEED_II;
   public static Item SPEED_III;
   public static Item SPEED_IV;
   public static Item SPEED_V;
   public static Item ATTACK_SPEED_I;
   public static Item ATTACK_SPEED_II;
   public static Item ATTACK_SPEED_III;
   public static Item ATTACK_SPEED_IV;
   public static Item ATTACK_SPEED_V;
   public static Item HASTE_I;
   public static Item HASTE_II;
   public static Item HASTE_III;
   public static Item HASTE_IV;
   public static Item HASTE_V;
   public static Item REACH_I;
   public static Item REACH_II;
   public static Item REACH_III;
   public static Item REACH_IV;
   public static Item REACH_V;
   public static final net.minecraft.core.component.DataComponentType<GemComponent> GEMS = registerComponent(
      "gems", builder -> builder.persistent(GemComponent.CODEC).networkSynchronized(GemComponent.PACKET_CODEC)
   );

   public static Item register(String id, Item item) {
      ITEMS.add(item);
      return register(Identifier.fromNamespaceAndPath("smitherz", id), item);
   }

   private static Item register(Identifier id, Item item) {
      CreativeModeTabEvents.modifyOutputEvent(SMITHERZ_ITEM_GROUP).register(entries -> entries.accept(item));
      return (Item)Registry.register(BuiltInRegistries.ITEM, id, item);
   }

   private static <T> net.minecraft.core.component.DataComponentType<T> registerComponent(
      String id, UnaryOperator<net.minecraft.core.component.DataComponentType.Builder<T>> builderOperator
   ) {
      return (DataComponentType)Registry.register(
         BuiltInRegistries.DATA_COMPONENT_TYPE, Identifier.fromNamespaceAndPath("smitherz", id), ((Builder)builderOperator.apply(DataComponentType.builder())).build()
      );
   }

   public static void init() {
      if (ConfigInit.CONFIG.defaultGems) {
         STRENGTH_I = register(
            "strength_1_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "strength_1_gem"))),
               Map.of(
                  Attributes.ATTACK_DAMAGE,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_DAMAGE.unwrapKey().get()).identifier(), 2.0, Operation.ADD_VALUE)
               ),
               0.45F,
               0.0F,
               0.5F,
               ItemTags.SWORDS
            )
         );
         STRENGTH_II = register(
            "strength_2_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "strength_2_gem"))),
               Map.of(
                  Attributes.ATTACK_DAMAGE,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_DAMAGE.unwrapKey().get()).identifier(), 4.0, Operation.ADD_VALUE)
               ),
               0.33F,
               0.0F,
               0.3F,
               ItemTags.SWORDS
            )
         );
         STRENGTH_III = register(
            "strength_3_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "strength_3_gem"))),
               Map.of(
                  Attributes.ATTACK_DAMAGE,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_DAMAGE.unwrapKey().get()).identifier(), 6.0, Operation.ADD_VALUE)
               ),
               0.25F,
               0.0F,
               0.2F,
               ItemTags.SWORDS
            )
         );
         STRENGTH_IV = register(
            "strength_4_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "strength_4_gem"))),
               Map.of(
                  Attributes.ATTACK_DAMAGE,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_DAMAGE.unwrapKey().get()).identifier(), 8.0, Operation.ADD_VALUE)
               ),
               0.09F,
               0.01F,
               0.05F,
               ItemTags.SWORDS
            )
         );
         STRENGTH_V = register(
            "strength_5_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "strength_5_gem"))),
               Map.of(
                  Attributes.ATTACK_DAMAGE,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_DAMAGE.unwrapKey().get()).identifier(), 12.0, Operation.ADD_VALUE)
               ),
               0.01F,
               0.05F,
               0.01F,
               ItemTags.SWORDS
            )
         );
         DEFENSE_I = register(
            "defense_1_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "defense_1_gem"))),
               Map.of(
                  Attributes.ARMOR,
                  new AttributeModifier(((ResourceKey)Attributes.ARMOR.unwrapKey().get()).identifier(), 2.0, Operation.ADD_VALUE)
               ),
               0.45F,
               0.0F,
               0.5F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         DEFENSE_II = register(
            "defense_2_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "defense_2_gem"))),
               Map.of(
                  Attributes.ARMOR,
                  new AttributeModifier(((ResourceKey)Attributes.ARMOR.unwrapKey().get()).identifier(), 4.0, Operation.ADD_VALUE)
               ),
               0.33F,
               0.0F,
               0.3F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         DEFENSE_III = register(
            "defense_3_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "defense_3_gem"))),
               Map.of(
                  Attributes.ARMOR,
                  new AttributeModifier(((ResourceKey)Attributes.ARMOR.unwrapKey().get()).identifier(), 6.0, Operation.ADD_VALUE)
               ),
               0.25F,
               0.0F,
               0.2F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         DEFENSE_IV = register(
            "defense_4_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "defense_4_gem"))),
               Map.of(
                  Attributes.ARMOR,
                  new AttributeModifier(((ResourceKey)Attributes.ARMOR.unwrapKey().get()).identifier(), 8.0, Operation.ADD_VALUE)
               ),
               0.09F,
               0.01F,
               0.05F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         DEFENSE_V = register(
            "defense_5_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "defense_5_gem"))),
               Map.of(
                  Attributes.ARMOR,
                  new AttributeModifier(((ResourceKey)Attributes.ARMOR.unwrapKey().get()).identifier(), 10.0, Operation.ADD_VALUE)
               ),
               0.01F,
               0.05F,
               0.01F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         HEALTH_I = register(
            "health_1_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "health_1_gem"))),
               Map.of(
                  Attributes.MAX_HEALTH,
                  new AttributeModifier(((ResourceKey)Attributes.MAX_HEALTH.unwrapKey().get()).identifier(), 2.0, Operation.ADD_VALUE)
               ),
               0.45F,
               0.0F,
               0.5F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         HEALTH_II = register(
            "health_2_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "health_2_gem"))),
               Map.of(
                  Attributes.MAX_HEALTH,
                  new AttributeModifier(((ResourceKey)Attributes.MAX_HEALTH.unwrapKey().get()).identifier(), 4.0, Operation.ADD_VALUE)
               ),
               0.33F,
               0.0F,
               0.3F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         HEALTH_III = register(
            "health_3_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "health_3_gem"))),
               Map.of(
                  Attributes.MAX_HEALTH,
                  new AttributeModifier(((ResourceKey)Attributes.MAX_HEALTH.unwrapKey().get()).identifier(), 6.0, Operation.ADD_VALUE)
               ),
               0.25F,
               0.0F,
               0.2F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         HEALTH_IV = register(
            "health_4_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "health_4_gem"))),
               Map.of(
                  Attributes.MAX_HEALTH,
                  new AttributeModifier(((ResourceKey)Attributes.MAX_HEALTH.unwrapKey().get()).identifier(), 8.0, Operation.ADD_VALUE)
               ),
               0.09F,
               0.01F,
               0.05F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         HEALTH_V = register(
            "health_5_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "health_5_gem"))),
               Map.of(
                  Attributes.MAX_HEALTH,
                  new AttributeModifier(((ResourceKey)Attributes.MAX_HEALTH.unwrapKey().get()).identifier(), 12.0, Operation.ADD_VALUE)
               ),
               0.01F,
               0.05F,
               0.01F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         SPEED_I = register(
            "speed_1_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "speed_1_gem"))),
               Map.of(
                  Attributes.MOVEMENT_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.MOVEMENT_SPEED.unwrapKey().get()).identifier(), 0.05, Operation.ADD_VALUE)
               ),
               0.45F,
               0.0F,
               0.5F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         SPEED_II = register(
            "speed_2_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "speed_2_gem"))),
               Map.of(
                  Attributes.MOVEMENT_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.MOVEMENT_SPEED.unwrapKey().get()).identifier(), 0.1, Operation.ADD_VALUE)
               ),
               0.33F,
               0.0F,
               0.3F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         SPEED_III = register(
            "speed_3_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "speed_3_gem"))),
               Map.of(
                  Attributes.MOVEMENT_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.MOVEMENT_SPEED.unwrapKey().get()).identifier(), 0.15, Operation.ADD_VALUE)
               ),
               0.25F,
               0.0F,
               0.2F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         SPEED_IV = register(
            "speed_4_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "speed_4_gem"))),
               Map.of(
                  Attributes.MOVEMENT_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.MOVEMENT_SPEED.unwrapKey().get()).identifier(), 0.2, Operation.ADD_VALUE)
               ),
               0.09F,
               0.01F,
               0.05F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         SPEED_V = register(
            "speed_5_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "speed_5_gem"))),
               Map.of(
                  Attributes.MOVEMENT_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.MOVEMENT_SPEED.unwrapKey().get()).identifier(), 0.3, Operation.ADD_VALUE)
               ),
               0.01F,
               0.05F,
               0.01F,
               ItemTags.ARMOR_ENCHANTABLE
            )
         );
         ATTACK_SPEED_I = register(
            "attack_speed_1_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "attack_speed_1_gem"))),
               Map.of(
                  Attributes.ATTACK_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_SPEED.unwrapKey().get()).identifier(), 0.05, Operation.ADD_VALUE)
               ),
               0.45F,
               0.0F,
               0.5F,
               ItemTags.BREAKS_DECORATED_POTS
            )
         );
         ATTACK_SPEED_II = register(
            "attack_speed_2_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "attack_speed_2_gem"))),
               Map.of(
                  Attributes.ATTACK_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_SPEED.unwrapKey().get()).identifier(), 0.1, Operation.ADD_VALUE)
               ),
               0.33F,
               0.0F,
               0.3F,
               ItemTags.BREAKS_DECORATED_POTS
            )
         );
         ATTACK_SPEED_III = register(
            "attack_speed_3_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "attack_speed_3_gem"))),
               Map.of(
                  Attributes.ATTACK_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_SPEED.unwrapKey().get()).identifier(), 0.15, Operation.ADD_VALUE)
               ),
               0.25F,
               0.0F,
               0.2F,
               ItemTags.BREAKS_DECORATED_POTS
            )
         );
         ATTACK_SPEED_IV = register(
            "attack_speed_4_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "attack_speed_4_gem"))),
               Map.of(
                  Attributes.ATTACK_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_SPEED.unwrapKey().get()).identifier(), 0.2, Operation.ADD_VALUE)
               ),
               0.09F,
               0.01F,
               0.05F,
               ItemTags.BREAKS_DECORATED_POTS
            )
         );
         ATTACK_SPEED_V = register(
            "attack_speed_5_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "attack_speed_5_gem"))),
               Map.of(
                  Attributes.ATTACK_SPEED,
                  new AttributeModifier(((ResourceKey)Attributes.ATTACK_SPEED.unwrapKey().get()).identifier(), 0.3, Operation.ADD_VALUE)
               ),
               0.01F,
               0.05F,
               0.01F,
               ItemTags.BREAKS_DECORATED_POTS
            )
         );
         HASTE_I = register(
            "haste_1_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "haste_1_gem"))),
               Map.of(
                  Attributes.MINING_EFFICIENCY,
                  new AttributeModifier(((ResourceKey)Attributes.MINING_EFFICIENCY.unwrapKey().get()).identifier(), 0.5, Operation.ADD_VALUE)
               ),
               0.45F,
               0.0F,
               0.5F,
               ItemTags.MINING_ENCHANTABLE
            )
         );
         HASTE_II = register(
            "haste_2_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "haste_2_gem"))),
               Map.of(
                  Attributes.MINING_EFFICIENCY,
                  new AttributeModifier(((ResourceKey)Attributes.MINING_EFFICIENCY.unwrapKey().get()).identifier(), 1.0, Operation.ADD_VALUE)
               ),
               0.33F,
               0.0F,
               0.3F,
               ItemTags.MINING_ENCHANTABLE
            )
         );
         HASTE_III = register(
            "haste_3_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "haste_3_gem"))),
               Map.of(
                  Attributes.MINING_EFFICIENCY,
                  new AttributeModifier(((ResourceKey)Attributes.MINING_EFFICIENCY.unwrapKey().get()).identifier(), 1.5, Operation.ADD_VALUE)
               ),
               0.25F,
               0.0F,
               0.2F,
               ItemTags.MINING_ENCHANTABLE
            )
         );
         HASTE_IV = register(
            "haste_4_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "haste_4_gem"))),
               Map.of(
                  Attributes.MINING_EFFICIENCY,
                  new AttributeModifier(((ResourceKey)Attributes.MINING_EFFICIENCY.unwrapKey().get()).identifier(), 2.0, Operation.ADD_VALUE)
               ),
               0.09F,
               0.01F,
               0.05F,
               ItemTags.MINING_ENCHANTABLE
            )
         );
         HASTE_V = register(
            "haste_5_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "haste_5_gem"))),
               Map.of(
                  Attributes.MINING_EFFICIENCY,
                  new AttributeModifier(((ResourceKey)Attributes.MINING_EFFICIENCY.unwrapKey().get()).identifier(), 3.0, Operation.ADD_VALUE)
               ),
               0.01F,
               0.05F,
               0.01F,
               ItemTags.MINING_ENCHANTABLE
            )
         );
         REACH_I = register(
            "reach_1_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "reach_1_gem"))),
               Map.of(
                  Attributes.ENTITY_INTERACTION_RANGE,
                  new AttributeModifier(((ResourceKey)Attributes.ENTITY_INTERACTION_RANGE.unwrapKey().get()).identifier(), 0.5, Operation.ADD_VALUE)
               ),
               0.45F,
               0.0F,
               0.5F,
               ItemTags.SWORDS
            )
         );
         REACH_II = register(
            "reach_2_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "reach_2_gem"))),
               Map.of(
                  Attributes.ENTITY_INTERACTION_RANGE,
                  new AttributeModifier(((ResourceKey)Attributes.ENTITY_INTERACTION_RANGE.unwrapKey().get()).identifier(), 1.0, Operation.ADD_VALUE)
               ),
               0.33F,
               0.0F,
               0.3F,
               ItemTags.SWORDS
            )
         );
         REACH_III = register(
            "reach_3_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "reach_3_gem"))),
               Map.of(
                  Attributes.ENTITY_INTERACTION_RANGE,
                  new AttributeModifier(((ResourceKey)Attributes.ENTITY_INTERACTION_RANGE.unwrapKey().get()).identifier(), 1.5, Operation.ADD_VALUE)
               ),
               0.25F,
               0.0F,
               0.2F,
               ItemTags.SWORDS
            )
         );
         REACH_IV = register(
            "reach_4_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "reach_4_gem"))),
               Map.of(
                  Attributes.ENTITY_INTERACTION_RANGE,
                  new AttributeModifier(((ResourceKey)Attributes.ENTITY_INTERACTION_RANGE.unwrapKey().get()).identifier(), 2.0, Operation.ADD_VALUE)
               ),
               0.09F,
               0.01F,
               0.05F,
               ItemTags.SWORDS
            )
         );
         REACH_V = register(
            "reach_5_gem",
            new Gem(
               new Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("smitherz", "reach_5_gem"))),
               Map.of(
                  Attributes.ENTITY_INTERACTION_RANGE,
                  new AttributeModifier(((ResourceKey)Attributes.ENTITY_INTERACTION_RANGE.unwrapKey().get()).identifier(), 3.0, Operation.ADD_VALUE)
               ),
               0.01F,
               0.05F,
               0.01F,
               ItemTags.SWORDS
            )
         );
      }

      Registry.register(
         BuiltInRegistries.CREATIVE_MODE_TAB,
         SMITHERZ_ITEM_GROUP,
         FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(SMITHER_HAMMER_4))
            .title(Component.translatable("item.smitherz.item_group"))
            .build()
      );
   }
}
