package net.jmb19905.niftycarts;

import com.google.common.collect.ImmutableMap;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.Load;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.jmb19905.niftycarts.advancement.NCCriteriaTriggers;
import net.jmb19905.niftycarts.container.PlowMenu;
import net.jmb19905.niftycarts.container.SeedDrillMenu;
import net.jmb19905.niftycarts.entity.AnimalCartEntity;
import net.jmb19905.niftycarts.entity.HandCartEntity;
import net.jmb19905.niftycarts.entity.PlowEntity;
import net.jmb19905.niftycarts.entity.PostilionEntity;
import net.jmb19905.niftycarts.entity.ReaperCartEntity;
import net.jmb19905.niftycarts.entity.SeedDrillEntity;
import net.jmb19905.niftycarts.entity.SupplyCartEntity;
import net.jmb19905.niftycarts.entity.WagonEntity;
import net.jmb19905.niftycarts.entity.ai.goal.AvoidCartGoal;
import net.jmb19905.niftycarts.entity.ai.goal.PullCartGoal;
import net.jmb19905.niftycarts.entity.ai.goal.RideCartGoal;
import net.jmb19905.niftycarts.item.CartItem;
import net.jmb19905.niftycarts.network.clientbound.UpdateDrawnPayload;
import net.jmb19905.niftycarts.network.serverbound.ActionKeyPayload;
import net.jmb19905.niftycarts.network.serverbound.CoachmanMovePayload;
import net.jmb19905.niftycarts.network.serverbound.OpenSupplyCartPayload;
import net.jmb19905.niftycarts.network.serverbound.RequestCartUpdatePayload;
import net.jmb19905.niftycarts.network.serverbound.ToggleSlowPayload;
import net.jmb19905.niftycarts.util.NiftyGoalAdder;
import net.jmb19905.niftycarts.util.NiftyWorld;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType.Builder;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.fml.config.ModConfig.Type;
import oshi.util.tuples.Triplet;

public class NiftyCarts implements ModInitializer {
   public static final String MOD_ID = "niftycarts";
   public static final Item WHEEL = register("wheel", Item::new);
   private static final Function<Triplet<net.minecraft.world.level.block.state.properties.WoodType, String, net.minecraft.world.flag.FeatureFlag[]>, CartItem> CART_ITEM_SUPPLIER = triplet -> register(
      ((WoodType)triplet.getA()).name() + "_" + (String)triplet.getB(),
      (Function<net.minecraft.world.item.Item.Properties, CartItem>)(prop -> new CartItem(
         (WoodType)triplet.getA(), (String)triplet.getB(), prop.stacksTo(1).requiredFeatures((FeatureFlag[])triplet.getC())
      ))
   );
   public static final Map<net.minecraft.world.level.block.state.properties.WoodType, CartItem> SUPPLY_CART = new HashMap<>();
   public static final Map<net.minecraft.world.level.block.state.properties.WoodType, CartItem> HAND_CART = new HashMap<>();
   public static final Map<net.minecraft.world.level.block.state.properties.WoodType, CartItem> PLOW = new HashMap<>();
   public static final Map<net.minecraft.world.level.block.state.properties.WoodType, CartItem> ANIMAL_CART = new HashMap<>();
   public static final Map<net.minecraft.world.level.block.state.properties.WoodType, CartItem> SEED_DRILL = new HashMap<>();
   public static final Map<net.minecraft.world.level.block.state.properties.WoodType, CartItem> REAPER = new HashMap<>();
   public static final Map<net.minecraft.world.level.block.state.properties.WoodType, CartItem> WAGON = new HashMap<>();
   public static final WoodType[] VANILLA_WOOD_TYPES = new WoodType[]{
      WoodType.OAK,
      WoodType.SPRUCE,
      WoodType.BIRCH,
      WoodType.ACACIA,
      WoodType.CHERRY,
      WoodType.JUNGLE,
      WoodType.DARK_OAK,
      WoodType.PALE_OAK,
      WoodType.CRIMSON,
      WoodType.WARPED,
      WoodType.MANGROVE,
      WoodType.BAMBOO
   };
   public static MinecraftServer server;
   public static final Identifier ATTACH_SOUND_ID;
   public static final Identifier DETACH_SOUND_ID;
   public static final Identifier PLACE_SOUND_ID;
   public static SoundEvent ATTACH_SOUND;
   public static SoundEvent DETACH_SOUND;
   public static SoundEvent PLACE_SOUND;
   public static final net.minecraft.world.entity.EntityType<SupplyCartEntity> SUPPLY_CART_ENTITY;
   public static final net.minecraft.world.entity.EntityType<AnimalCartEntity> ANIMAL_CART_ENTITY;
   public static final net.minecraft.world.entity.EntityType<PlowEntity> PLOW_ENTITY;
   public static final net.minecraft.world.entity.EntityType<HandCartEntity> HAND_CART_ENTITY;
   public static final net.minecraft.world.entity.EntityType<SeedDrillEntity> SEED_DRILL_ENTITY;
   public static final net.minecraft.world.entity.EntityType<ReaperCartEntity> REAPER_ENTITY;
   public static final net.minecraft.world.entity.EntityType<WagonEntity> WAGON_ENTITY;
   public static final net.minecraft.world.entity.EntityType<PostilionEntity> POSTILION_ENTITY;
   public static final NiftyGoalAdder<net.minecraft.world.entity.Mob> MOB_GOAL_ADDER;
   public static final NiftyGoalAdder<net.minecraft.world.entity.PathfinderMob> PATHFINDER_GOAL_ADDER;
   public static final net.minecraft.world.inventory.MenuType<PlowMenu> PLOW_MENU_TYPE;
   public static final net.minecraft.world.inventory.MenuType<SeedDrillMenu> SEED_DRILL_MENU_TYPE;
   public static net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> CHEST_9x4_MENU_TYPE;
   public static net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> CHEST_9x8_MENU_TYPE;
   public static net.minecraft.world.inventory.MenuType<net.minecraft.world.inventory.ChestMenu> CHEST_9x12_MENU_TYPE;
   public static final Map<net.minecraft.world.entity.EntityType<?>, net.minecraft.resources.Identifier> CART_PULL_CM;
   public static final Identifier RIDE_CART_CM;
   public static final Identifier STEER_ANIMAL_CART_CM;
   public static final Identifier STEER_REAPER_CM;
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> PLOW_BREAKABLE_HOE;
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> PLOW_BREAKABLE_SHOVEL;
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> PLOW_BREAKABLE_AXE;
   public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> REAPER_HARVESTABLE;
   public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> SEED_DRILL_PLANTABLE;

   private static void registerStat(Identifier id, StatFormatter formatter) {
      Registry.register(BuiltInRegistries.CUSTOM_STAT, id, id);
      Stats.CUSTOM.get(id, formatter);
   }

   public void onInitialize() {
      ConfigRegistry.INSTANCE.register("niftycarts", Type.COMMON, NiftyCartsConfig.spec());

      for (Identifier stat : CART_PULL_CM.values()) {
         registerStat(stat, StatFormatter.DISTANCE);
      }

      registerStat(RIDE_CART_CM, StatFormatter.DISTANCE);
      registerStat(STEER_ANIMAL_CART_CM, StatFormatter.DISTANCE);
      registerStat(STEER_REAPER_CM, StatFormatter.DISTANCE);
      NCCriteriaTriggers.register();
      Registry.register(BuiltInRegistries.MENU, resLoc("plow"), PLOW_MENU_TYPE);
      Registry.register(BuiltInRegistries.MENU, resLoc("seed_drill"), SEED_DRILL_MENU_TYPE);
      Registry.register(BuiltInRegistries.MENU, resLoc("chest_four_rows"), CHEST_9x4_MENU_TYPE);
      Registry.register(BuiltInRegistries.MENU, resLoc("chest_eight_rows"), CHEST_9x8_MENU_TYPE);
      Registry.register(BuiltInRegistries.MENU, resLoc("chest_quad"), CHEST_9x12_MENU_TYPE);
      CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> output.accept(WHEEL));
      CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
         .register(output -> Arrays.stream(VANILLA_WOOD_TYPES).forEach(woodType -> {
            output.accept((ItemLike)SUPPLY_CART.get(woodType));
            output.accept((ItemLike)PLOW.get(woodType));
            output.accept((ItemLike)SEED_DRILL.get(woodType));
            output.accept((ItemLike)REAPER.get(woodType));
            output.accept((ItemLike)ANIMAL_CART.get(woodType));
            output.accept((ItemLike)HAND_CART.get(woodType));
            output.accept((ItemLike)WAGON.get(woodType));
         }));
      Registry.register(BuiltInRegistries.SOUND_EVENT, ATTACH_SOUND_ID, ATTACH_SOUND);
      Registry.register(BuiltInRegistries.SOUND_EVENT, DETACH_SOUND_ID, DETACH_SOUND);
      Registry.register(BuiltInRegistries.SOUND_EVENT, PLACE_SOUND_ID, PLACE_SOUND);
      PayloadTypeRegistry.serverboundPlay().register(ActionKeyPayload.TYPE, ActionKeyPayload.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(OpenSupplyCartPayload.TYPE, OpenSupplyCartPayload.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(ToggleSlowPayload.TYPE, ToggleSlowPayload.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(RequestCartUpdatePayload.TYPE, RequestCartUpdatePayload.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(CoachmanMovePayload.TYPE, CoachmanMovePayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(UpdateDrawnPayload.TYPE, UpdateDrawnPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(ActionKeyPayload.TYPE, (payload, context) -> ActionKeyPayload.handle(context.player()));
      ServerPlayNetworking.registerGlobalReceiver(OpenSupplyCartPayload.TYPE, (payload, context) -> OpenSupplyCartPayload.handle(context.player()));
      ServerPlayNetworking.registerGlobalReceiver(ToggleSlowPayload.TYPE, (payload, context) -> ToggleSlowPayload.handle(context.player()));
      ServerPlayNetworking.registerGlobalReceiver(
         RequestCartUpdatePayload.TYPE, (payload, context) -> RequestCartUpdatePayload.handle(payload, context.player())
      );
      ServerPlayNetworking.registerGlobalReceiver(CoachmanMovePayload.TYPE, (payload, context) -> CoachmanMovePayload.handle(payload, context.player()));
      ServerLifecycleEvents.SERVER_STARTED.register((ServerStarted)s -> server = s);
      ServerLifecycleEvents.SERVER_STOPPED.register((ServerStopped)s -> server = null);
      ServerTickEvents.END_SERVER_TICK.register((EndTick)e -> {
         for (net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> levelKey : e.levelKeys()) {
            NiftyWorld.getServer(server, levelKey).tick(server.getLevel(levelKey));
         }
      });
      UseEntityCallback.EVENT.register((UseEntityCallback)(player, level, hand, entity, hitResult) -> {
         Entity rider = entity.getControllingPassenger();
         if (rider instanceof PostilionEntity) {
            rider.stopRiding();
         }

         return InteractionResult.PASS;
      });
      ServerEntityEvents.ENTITY_LOAD.register((Load)(entity, world) -> {
         MOB_GOAL_ADDER.onEntityJoinWorld(entity);
         PATHFINDER_GOAL_ADDER.onEntityJoinWorld(entity);
      });
      FabricDefaultAttributeRegistry.register(POSTILION_ENTITY, LivingEntity.createLivingAttributes());
   }

   public static <T extends net.minecraft.world.entity.Entity> net.minecraft.world.entity.EntityType<T> register(
      String id, net.minecraft.world.entity.EntityType.Builder<T> builder
   ) {
      net.minecraft.resources.ResourceKey<net.minecraft.world.entity.EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, resLoc(id));
      return (EntityType)Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
   }

   public static <I extends net.minecraft.world.item.Item> I register(String id, Function<net.minecraft.world.item.Item.Properties, I> function) {
      net.minecraft.resources.ResourceKey<net.minecraft.world.item.Item> key = ResourceKey.create(Registries.ITEM, resLoc(id));
      I item = (I)((Item)function.apply(new Properties().setId(key)));
      return (I)((Item)Registry.register(BuiltInRegistries.ITEM, key, item));
   }

   public static Identifier resLoc(String name) {
      return Identifier.fromNamespaceAndPath("niftycarts", name);
   }

   static {
      for (WoodType woodType : VANILLA_WOOD_TYPES) {
         FeatureFlag[] flags = new FeatureFlag[0];
         SUPPLY_CART.put(woodType, CART_ITEM_SUPPLIER.apply(new Triplet(woodType, "supply_cart", flags)));
         HAND_CART.put(woodType, CART_ITEM_SUPPLIER.apply(new Triplet(woodType, "hand_cart", flags)));
         PLOW.put(woodType, CART_ITEM_SUPPLIER.apply(new Triplet(woodType, "plow", flags)));
         SEED_DRILL.put(woodType, CART_ITEM_SUPPLIER.apply(new Triplet(woodType, "seed_drill", flags)));
         REAPER.put(woodType, CART_ITEM_SUPPLIER.apply(new Triplet(woodType, "reaper", flags)));
         ANIMAL_CART.put(woodType, CART_ITEM_SUPPLIER.apply(new Triplet(woodType, "animal_cart", flags)));
         WAGON.put(woodType, CART_ITEM_SUPPLIER.apply(new Triplet(woodType, "wagon", flags)));
      }

      server = null;
      ATTACH_SOUND_ID = resLoc("entity.cart.attach");
      DETACH_SOUND_ID = resLoc("entity.cart.detach");
      PLACE_SOUND_ID = resLoc("entity.cart.place");
      ATTACH_SOUND = SoundEvent.createVariableRangeEvent(ATTACH_SOUND_ID);
      DETACH_SOUND = SoundEvent.createVariableRangeEvent(DETACH_SOUND_ID);
      PLACE_SOUND = SoundEvent.createVariableRangeEvent(PLACE_SOUND_ID);
      SUPPLY_CART_ENTITY = register("supply_cart", Builder.of(SupplyCartEntity::new, MobCategory.MISC).sized(1.5F, 1.4F));
      ANIMAL_CART_ENTITY = register("animal_cart", Builder.of(AnimalCartEntity::new, MobCategory.MISC).sized(1.3F, 1.4F));
      PLOW_ENTITY = register("plow", Builder.of(PlowEntity::new, MobCategory.MISC).sized(1.3F, 1.4F));
      HAND_CART_ENTITY = register("hand_cart", Builder.of(HandCartEntity::new, MobCategory.MISC).sized(1.3F, 1.1F));
      SEED_DRILL_ENTITY = register("seed_drill", Builder.of(SeedDrillEntity::new, MobCategory.MISC).sized(1.3F, 1.4F));
      REAPER_ENTITY = register("reaper", Builder.of(ReaperCartEntity::new, MobCategory.MISC).sized(1.3F, 1.4F));
      WAGON_ENTITY = register("wagon", Builder.of(WagonEntity::new, MobCategory.MISC).sized(2.5F, 3.0F));
      POSTILION_ENTITY = register(
         "postilion", Builder.of(PostilionEntity::new, MobCategory.MISC).sized(0.25F, 0.25F).noSummon().noSave()
      );
      MOB_GOAL_ADDER = NiftyGoalAdder.mobGoal(Mob.class).add(1, PullCartGoal::new).add(1, RideCartGoal::new).build();
      PATHFINDER_GOAL_ADDER = NiftyGoalAdder.mobGoal(PathfinderMob.class)
         .add(3, mob -> new AvoidCartGoal<>(mob, SupplyCartEntity.class, 3.0F, 0.5))
         .add(3, mob -> new AvoidCartGoal<>(mob, PlowEntity.class, 3.0F, 0.5))
         .add(3, mob -> new AvoidCartGoal<>(mob, SeedDrillEntity.class, 3.0F, 0.5))
         .add(3, mob -> new AvoidCartGoal<>(mob, ReaperCartEntity.class, 3.0F, 0.5))
         .build();
      PLOW_MENU_TYPE = new MenuType(PlowMenu::new, FeatureFlags.DEFAULT_FLAGS);
      SEED_DRILL_MENU_TYPE = new MenuType(SeedDrillMenu::new, FeatureFlags.DEFAULT_FLAGS);
      CHEST_9x4_MENU_TYPE = new MenuType((i, inv) -> new ChestMenu(CHEST_9x4_MENU_TYPE, i, inv, new SimpleContainer(36), 4), FeatureFlags.DEFAULT_FLAGS);
      CHEST_9x8_MENU_TYPE = new MenuType((i, inv) -> new ChestMenu(CHEST_9x8_MENU_TYPE, i, inv, new SimpleContainer(72), 8), FeatureFlags.DEFAULT_FLAGS);
      CHEST_9x12_MENU_TYPE = new MenuType((i, inv) -> new ChestMenu(CHEST_9x12_MENU_TYPE, i, inv, new SimpleContainer(108), 12), FeatureFlags.DEFAULT_FLAGS);
      CART_PULL_CM = ImmutableMap.of(
         SUPPLY_CART_ENTITY,
         Identifier.fromNamespaceAndPath("niftycarts", "supply_cart_pull_cm"),
         HAND_CART_ENTITY,
         Identifier.fromNamespaceAndPath("niftycarts", "hand_cart_pull_cm"),
         ANIMAL_CART_ENTITY,
         Identifier.fromNamespaceAndPath("niftycarts", "animal_cart_pull_cm"),
         PLOW_ENTITY,
         Identifier.fromNamespaceAndPath("niftycarts", "plow_pull_cm"),
         REAPER_ENTITY,
         Identifier.fromNamespaceAndPath("niftycarts", "reaper_pull_cm"),
         SEED_DRILL_ENTITY,
         Identifier.fromNamespaceAndPath("niftycarts", "seed_drill_pull_cm"),
         WAGON_ENTITY,
         Identifier.fromNamespaceAndPath("niftycarts", "wagon_pull_cm")
      );
      RIDE_CART_CM = Identifier.fromNamespaceAndPath("niftycarts", "ride_cart_cm");
      STEER_ANIMAL_CART_CM = Identifier.fromNamespaceAndPath("niftycarts", "steer_animal_cart_cm");
      STEER_REAPER_CM = Identifier.fromNamespaceAndPath("niftycarts", "steer_reaper_cm");
      PLOW_BREAKABLE_HOE = TagKey.create(Registries.BLOCK, resLoc("plow_breakable/hoe"));
      PLOW_BREAKABLE_SHOVEL = TagKey.create(Registries.BLOCK, resLoc("plow_breakable/shovel"));
      PLOW_BREAKABLE_AXE = TagKey.create(Registries.BLOCK, resLoc("plow_breakable/axe"));
      REAPER_HARVESTABLE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("niftycarts", "reaper_harvestable"));
      SEED_DRILL_PLANTABLE = TagKey.create(Registries.ITEM, resLoc("seed_drill_plantable"));
   }
}
