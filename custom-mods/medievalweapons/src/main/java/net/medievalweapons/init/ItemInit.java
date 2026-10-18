package net.medievalweapons.init;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.medievalweapons.MedievalMain;
import net.medievalweapons.entity.FranciscaEntity;
import net.medievalweapons.entity.JavelinEntity;
import net.medievalweapons.item.BigAxeItem;
import net.medievalweapons.item.DaggerItem;
import net.medievalweapons.item.FranciscaItem;
import net.medievalweapons.item.HealingStaffItem;
import net.medievalweapons.item.HeroStaffItem;
import net.medievalweapons.item.HeroSwordItem;
import net.medievalweapons.item.JavelinItem;
import net.medievalweapons.item.LanceItem;
import net.medievalweapons.item.LongBowItem;
import net.medievalweapons.item.LongSwordItem;
import net.medievalweapons.item.MaceItem;
import net.medievalweapons.item.NinjatoItem;
import net.medievalweapons.item.RapierItem;
import net.medievalweapons.item.RecurveBowItem;
import net.medievalweapons.item.SickleItem;
import net.medievalweapons.item.SmallAxeItem;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.Nullable;

public class ItemInit {
   public static final ResourceKey<CreativeModeTab> MEDIEVALWEAPONS_ITEM_GROUP = ResourceKey.create(
      Registries.CREATIVE_MODE_TAB, MedievalMain.identifierOf("item_group")
   );
   public static final ArrayList<String> MATERIAL_STRINGS = new ArrayList<>(Arrays.asList("wooden", "stone", "iron", "golden", "diamond", "netherite"));
   public static final ArrayList<String> ITEM_STRINGS = new ArrayList<>(
      Arrays.asList("small_axe", "long_sword", "dagger", "francisca", "big_axe", "javelin", "lance", "healing_staff", "mace", "ninjato", "sickle", "rapier")
   );
   public static final Set<Item> CUSTOM_MODEL_ITEMS = new HashSet<>();
   private static final List<Map.Entry<ToolMaterial, String>> MATERIALS = List.of(
      Map.entry(ToolMaterial.WOOD, "wooden"),
      Map.entry(ToolMaterial.STONE, "stone"),
      Map.entry(ToolMaterial.IRON, "iron"),
      Map.entry(ToolMaterial.DIAMOND, "diamond"),
      Map.entry(ToolMaterial.GOLD, "golden"),
      Map.entry(ToolMaterial.NETHERITE, "netherite")
   );
   public static final List<ItemInit.WeaponType> TYPES = List.of(
      new ItemInit.WeaponType(
         "small_axe",
         (material, settings, id) -> new SmallAxeItem(material, settings),
         ConfigInit.CONFIG.battle_axe_attack_damage,
         ConfigInit.CONFIG.battle_axe_attack_speed,
         false,
         null,
         null,
         null,
         new String[]{"iii", "ipi", " s "}
      ),
      new ItemInit.WeaponType(
         "long_sword",
         (material, settings, id) -> new LongSwordItem(material, settings),
         ConfigInit.CONFIG.long_sword_attack_damage,
         ConfigInit.CONFIG.long_sword_attack_speed,
         false,
         Attributes.ENTITY_INTERACTION_RANGE,
         new AttributeModifier(LongSwordItem.ATTACK_BONUS_MODIFIER_ID, ConfigInit.CONFIG.long_sword_range, Operation.ADD_VALUE),
         EquipmentSlotGroup.MAINHAND,
         new String[]{"i", "s"}
      ),
      new ItemInit.WeaponType(
         "dagger",
         (material, settings, id) -> new DaggerItem(material, settings),
         ConfigInit.CONFIG.dagger_attack_damage,
         ConfigInit.CONFIG.dagger_attack_speed,
         false,
         Attributes.ENTITY_INTERACTION_RANGE,
         new AttributeModifier(DaggerItem.ATTACK_BONUS_MODIFIER_ID, ConfigInit.CONFIG.dagger_range, Operation.ADD_VALUE),
         EquipmentSlotGroup.MAINHAND,
         new String[]{"i", "i", "p"}
      ),
      new ItemInit.WeaponType(
         "big_axe",
         (material, settings, id) -> new BigAxeItem(material, settings),
         ConfigInit.CONFIG.war_axe_attack_damage,
         ConfigInit.CONFIG.war_axe_attack_speed,
         false,
         Attributes.SWEEPING_DAMAGE_RATIO,
         new AttributeModifier(BigAxeItem.ATTACK_BONUS_MODIFIER_ID, ConfigInit.CONFIG.war_axe_sweeping_ratio, Operation.ADD_VALUE),
         EquipmentSlotGroup.MAINHAND,
         new String[]{" i ", " i ", "isi"}
      ),
      new ItemInit.WeaponType(
         "lance",
         (material, settings, id) -> new LanceItem(material, settings),
         ConfigInit.CONFIG.lance_attack_damage,
         ConfigInit.CONFIG.lance_attack_speed,
         false,
         Attributes.ENTITY_INTERACTION_RANGE,
         new AttributeModifier(LanceItem.ATTACK_BONUS_MODIFIER_ID, ConfigInit.CONFIG.lance_range, Operation.ADD_VALUE),
         EquipmentSlotGroup.MAINHAND,
         new String[]{" i", "ii", "is"}
      ),
      new ItemInit.WeaponType(
         "healing_staff",
         (material, settings, id) -> new HealingStaffItem(material, -1, settings),
         ConfigInit.CONFIG.healing_staff_attack_damage,
         ConfigInit.CONFIG.healing_staff_attack_speed,
         false,
         Attributes.ENTITY_INTERACTION_RANGE,
         new AttributeModifier(HealingStaffItem.ATTACK_BONUS_MODIFIER_ID, ConfigInit.CONFIG.healing_staff_range, Operation.ADD_VALUE),
         EquipmentSlotGroup.MAINHAND,
         new String[]{" i", "ii", "s "}
      ),
      new ItemInit.WeaponType(
         "mace",
         (material, settings, id) -> new MaceItem(material, -1, settings),
         ConfigInit.CONFIG.mace_attack_damage,
         ConfigInit.CONFIG.mace_attack_speed,
         false,
         Attributes.ENTITY_INTERACTION_RANGE,
         new AttributeModifier(MaceItem.ATTACK_BONUS_MODIFIER_ID, ConfigInit.CONFIG.mace_range, Operation.ADD_VALUE),
         EquipmentSlotGroup.MAINHAND,
         new String[]{"b", "i", "s"}
      ),
      new ItemInit.WeaponType(
         "ninjato",
         (material, settings, id) -> new NinjatoItem(material, settings),
         ConfigInit.CONFIG.ninjato_attack_damage,
         ConfigInit.CONFIG.ninjato_attack_speed,
         false,
         null,
         null,
         null,
         new String[]{"  i", " i ", "s  "}
      ),
      new ItemInit.WeaponType(
         "sickle",
         (material, settings, id) -> new SickleItem(material, settings),
         ConfigInit.CONFIG.sickle_attack_damage,
         ConfigInit.CONFIG.sickle_attack_speed,
         false,
         null,
         null,
         null,
         new String[]{" ii", "iip", "i s"}
      ),
      new ItemInit.WeaponType(
         "rapier",
         (material, settings, id) -> new RapierItem(material, -1, settings),
         ConfigInit.CONFIG.rapier_attack_damage,
         ConfigInit.CONFIG.rapier_attack_speed,
         false,
         null,
         null,
         null,
         new String[]{"i", "p"}
      ),
      new ItemInit.WeaponType(
         "francisca",
         (material, settings, id) -> new FranciscaItem(
            material, () -> (EntityType<FranciscaEntity>)EntityInit.ENTITY_TYPES.get(MedievalMain.identifierOf(id)), settings
         ),
         ConfigInit.CONFIG.francisca_attack_damage,
         ConfigInit.CONFIG.francisca_attack_speed,
         true,
         null,
         null,
         null,
         new String[]{"xi", "s "}
      ),
      new ItemInit.WeaponType(
         "javelin",
         (material, settings, id) -> new JavelinItem(
            material, () -> (EntityType<JavelinEntity>)EntityInit.ENTITY_TYPES.get(MedievalMain.identifierOf(id)), settings
         ),
         ConfigInit.CONFIG.javelin_attack_damage,
         ConfigInit.CONFIG.javelin_attack_speed,
         true,
         Attributes.ENTITY_INTERACTION_RANGE,
         new AttributeModifier(JavelinItem.ATTACK_BONUS_MODIFIER_ID, ConfigInit.CONFIG.javelin_range, Operation.ADD_VALUE),
         EquipmentSlotGroup.MAINHAND,
         new String[]{"i", "x", "p"}
      )
   );
   public static final Item THALLEOUS_SWORD = register(
      "thalleous_sword",
      key -> new HeroSwordItem(
         ToolMaterial.DIAMOND,
         heroSettings(
            key,
            ConfigInit.CONFIG.thalleous_sword_attack_damage,
            ConfigInit.CONFIG.thalleous_sword_attack_speed,
            ConfigInit.CONFIG.thalleous_sword_range,
            ConfigInit.CONFIG.thalleous_sword_sweeping_ratio
         )
      )
   );
   public static final Item DEATHSINGER_SWORD = register(
      "deathsinger_sword",
      key -> new HeroSwordItem(
         ToolMaterial.DIAMOND,
         heroSettings(
            key,
            ConfigInit.CONFIG.deathsinger_sword_attack_damage,
            ConfigInit.CONFIG.deathsinger_sword_attack_speed,
            ConfigInit.CONFIG.deathsinger_sword_range,
            ConfigInit.CONFIG.deathsinger_sword_sweeping_ratio
         )
      )
   );
   public static final Item TIDESINGER_STAFF = register(
      "tidesinger_staff",
      key -> new HeroStaffItem(
         ToolMaterial.DIAMOND,
         heroSettings(
            key,
            ConfigInit.CONFIG.tidesinger_staff_attack_damage,
            ConfigInit.CONFIG.tidesinger_staff_attack_speed,
            ConfigInit.CONFIG.tidesinger_staff_range,
            ConfigInit.CONFIG.tidesinger_staff_sweeping_ratio
         )
      )
   );
   public static final Item LONG_BOW_ITEM = register("long_bow", key -> new LongBowItem(new Properties().setId(key).durability(443)));
   public static final Item RECURVE_BOW_ITEM = register("recurve_bow", key -> new RecurveBowItem(new Properties().setId(key).durability(361)));

   public static void init() {
      Registry.register(
         BuiltInRegistries.CREATIVE_MODE_TAB,
         MEDIEVALWEAPONS_ITEM_GROUP,
         FabricCreativeModeTab.builder().icon(() -> new ItemStack(THALLEOUS_SWORD)).title(Component.translatable("item.medievalweapons.item_group")).build()
      );

      for (Map.Entry<ToolMaterial, String> material : MATERIALS) {
         for (ItemInit.WeaponType type : TYPES) {
            String fullName = material.getValue() + "_" + type.suffix();
            Identifier identifier = MedievalMain.identifierOf(fullName);
            Properties settings = createWeaponSettings(ResourceKey.create(Registries.ITEM, identifier), material.getKey(), type);
            if (material.getKey() == ToolMaterial.NETHERITE) {
               settings = settings.fireResistant();
            }

            Item weapon = type.constructor().create(material.getKey(), settings, fullName);
            registerItem(identifier, weapon);
            if (type.hasEntity()) {
               registerWeaponEntity(fullName, weapon);
            }
         }
      }
   }

   public static Item register(String id, Function<ResourceKey<Item>, Item> factory) {
      Identifier identifier = MedievalMain.identifierOf(id);
      Item item = factory.apply(ResourceKey.create(Registries.ITEM, identifier));
      registerItem(identifier, item);
      return item;
   }

   private static void registerItem(Identifier id, Item item) {
      addCustomModelItem(item);
      CreativeModeTabEvents.modifyOutputEvent(MEDIEVALWEAPONS_ITEM_GROUP).register(output -> output.accept(item));
      Registry.register(BuiltInRegistries.ITEM, id, item);
   }

   private static Properties heroSettings(ResourceKey<Item> key, int damage, float speed, double range, double sweepingRatio) {
      Properties settings = new Properties().setId(key);
      ToolMaterial.DIAMOND.applySwordProperties(settings, 0, 0);
      ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
         .add(
            Attributes.ATTACK_DAMAGE,
            new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, damage, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .add(
            Attributes.ATTACK_SPEED,
            new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, speed, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .build()
         .withModifierAdded(
            Attributes.ENTITY_INTERACTION_RANGE,
            new AttributeModifier(LongSwordItem.ATTACK_BONUS_MODIFIER_ID, range, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         )
         .withModifierAdded(
            Attributes.SWEEPING_DAMAGE_RATIO,
            new AttributeModifier(HeroSwordItem.ATTACK_BONUS_MODIFIER_ID, sweepingRatio, Operation.ADD_VALUE),
            EquipmentSlotGroup.MAINHAND
         );
      settings.attributes(modifiers);
      return settings;
   }

   private static Properties createWeaponSettings(ResourceKey<Item> key, ToolMaterial mat, ItemInit.WeaponType type) {
      Properties settings = new Properties().setId(key);
      mat.applySwordProperties(settings, type.damage(), type.speed());
      if (type.attribute() != null && type.modifier() != null) {
         ItemAttributeModifiers modifiers = ItemAttributeModifiers.builder()
            .add(
               Attributes.ATTACK_DAMAGE,
               new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, type.damage() + mat.attackDamageBonus(), Operation.ADD_VALUE),
               EquipmentSlotGroup.MAINHAND
            )
            .add(
               Attributes.ATTACK_SPEED,
               new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, type.speed(), Operation.ADD_VALUE),
               EquipmentSlotGroup.MAINHAND
            )
            .build()
            .withModifierAdded(type.attribute(), type.modifier(), type.slot());
         settings.attributes(modifiers);
      }

      return settings;
   }

   private static void registerWeaponEntity(String id, Item item) {
      Identifier identifier = MedievalMain.identifierOf(id);
      ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, identifier);
      EntityType<?> type;
      if (item instanceof FranciscaItem francisca) {
         type = EntityType.Builder.of((EntityType.EntityFactory<FranciscaEntity>)(entity, world) -> new FranciscaEntity(entity, world, francisca), MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .build(key);
      } else if (item instanceof JavelinItem javelin) {
         type = EntityType.Builder.of((EntityType.EntityFactory<JavelinEntity>)(entity, world) -> new JavelinEntity(entity, world, javelin), MobCategory.MISC)
            .sized(0.5F, 0.5F)
            .build(key);
      } else {
         return;
      }

      EntityInit.register(identifier, type);
   }

   private static void addCustomModelItem(Item item) {
      if (item instanceof LongSwordItem
         || item instanceof SmallAxeItem
         || item instanceof BigAxeItem
         || item instanceof LanceItem
         || item instanceof HealingStaffItem
         || item instanceof JavelinItem
         || item instanceof MaceItem
         || item instanceof SickleItem
         || item instanceof RapierItem
         || item instanceof HeroSwordItem
         || item instanceof HeroStaffItem) {
         CUSTOM_MODEL_ITEMS.add(item);
      }
   }

   @FunctionalInterface
   interface WeaponFactory {
      Item create(ToolMaterial var1, Properties var2, String var3);
   }

   public record WeaponType(
      String suffix,
      ItemInit.WeaponFactory constructor,
      int damage,
      float speed,
      boolean hasEntity,
      @Nullable Holder<Attribute> attribute,
      @Nullable AttributeModifier modifier,
      @Nullable EquipmentSlotGroup slot,
      String[] pattern
   ) {
   }
}
