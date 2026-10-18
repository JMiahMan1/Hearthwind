package net.jmb19905.niftycarts;

import java.util.ArrayList;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import org.apache.commons.lang3.tuple.Pair;

public final class NiftyCartsConfig {
   public static NiftyCartsConfig.Common get() {
      return NiftyCartsConfig.Holder.COMMON;
   }

   public static ForgeConfigSpec spec() {
      return NiftyCartsConfig.Holder.COMMON_SPEC;
   }

   public static NiftyCartsConfig.Client getClient() {
      return NiftyCartsConfig.Holder.CLIENT;
   }

   public static ForgeConfigSpec clientSpec() {
      return NiftyCartsConfig.Holder.CLIENT_SPEC;
   }

   public static class CartConfig {
      public final ConfigValue<ArrayList<String>> pullEntities;
      public final DoubleValue pullSpeed;
      public final BooleanValue adventureModeInteract;

      CartConfig(Builder builder, String name, String description) {
         this(builder, name, description, new ArrayList<>(), 0.0);
      }

      CartConfig(Builder builder, String name, String description, ArrayList<String> defaultEntityList, double defaultPullSpeed) {
         builder.comment(description).push(name);
         this.pullEntities = builder.comment(
               "Entity that are able to pull this cart, such as [\"minecraft:horse\"]\nAn empty list defaults to all which may wear a saddle but not steered by an item"
            )
            .define("pull_animals", defaultEntityList);
         this.pullSpeed = builder.comment("Base speed modifier applied to animals (-0.5 = half normal speed)")
            .defineInRange("pull_speed", 0.0, -1.0, defaultPullSpeed);
         this.adventureModeInteract = builder.comment("Players in adventure mode can interact with cart").define("adventure_mode_interact", true);
         builder.pop();
      }
   }

   public static class Client {
      public final BooleanValue renderSupplies;
      public final BooleanValue renderSupplyGear;
      public final BooleanValue renderSupplyFlowers;
      public final BooleanValue renderSupplyPaintings;
      public final BooleanValue renderSupplyWheel;
      public final ConfigValue<ArrayList<String>> renderBlacklist;

      Client(Builder builder) {
         builder.comment("Configuration to disable the rendering of certain supplies in the supply cart");
         this.renderSupplies = builder.comment("Enables/Disables the rendering of all supplies").define("render_supplies", true);
         this.renderSupplyGear = builder.comment("Falls back to rendering as items if false").define("render_supply_gear", true);
         this.renderSupplyFlowers = builder.comment("Falls back to rendering as items if false").define("render_supply_flowers", true);
         this.renderSupplyPaintings = builder.comment("Falls back to rendering as items if false").define("render_supply_paintings", true);
         this.renderSupplyWheel = builder.comment("Falls back to rendering as items if false").define("render_supply_wheel", true);
         ArrayList<String> blacklist = new ArrayList<>();
         blacklist.add("minecraft:trident");
         blacklist.add("minecraft:decorated_pot");
         blacklist.add("#minecraft:buttons");
         blacklist.add("#minecraft:banners");
         this.renderBlacklist = builder.comment("Disables rendering for these blocks and items").define("render_item_blacklist", blacklist);
      }
   }

   public static class Common {
      public final DoubleValue slowSpeed;
      public final NiftyCartsConfig.CartConfig supplyCart;
      public final NiftyCartsConfig.CartConfig animalCart;
      public final NiftyCartsConfig.CartConfig plow;
      public final NiftyCartsConfig.CartConfig handCart;
      public final NiftyCartsConfig.CartConfig seedDrill;
      public final NiftyCartsConfig.CartConfig reaper;
      public final NiftyCartsConfig.CartConfig wagon;

      Common(Builder builder) {
         this.slowSpeed = builder.comment("Slow speed modifier toggled by the sprint key").defineInRange("slow_speed", -0.65, -1.0, 0.0);
         builder.comment("Configuration for all carts and cart-like vehicles, check log for automatic \"pull_animals\" list.").push("carts");
         this.supplyCart = new NiftyCartsConfig.CartConfig(builder, "supply_cart", "The Supply Cart, a type of cart that stores items");
         this.animalCart = new NiftyCartsConfig.CartConfig(builder, "animal_cart", "The Animal Cart, a type of cart to haul other animals");
         this.plow = new NiftyCartsConfig.CartConfig(builder, "plow", "The Plow, an animal pulled machine for tilling soil and creating paths");
         ArrayList<String> list = new ArrayList<>();
         list.add("minecraft:player");
         this.handCart = new NiftyCartsConfig.CartConfig(builder, "handCart", "The Hand Cart, a player pulled cart that stores items", list, 0.0);
         this.seedDrill = new NiftyCartsConfig.CartConfig(builder, "seedDrill", "The Seed Drill, a type of cart that plants crops");
         this.reaper = new NiftyCartsConfig.CartConfig(builder, "reaper", "The Reaper, a type of cart that harvests crops");
         this.wagon = new NiftyCartsConfig.CartConfig(
            builder, "wagon", "The Covered wagon, a horse drawn cart that multiple people and/or lots of items", new ArrayList<>(), -0.2
         );
         builder.pop();
      }
   }

   private static final class Holder {
      private static final NiftyCartsConfig.Common COMMON;
      private static final ForgeConfigSpec COMMON_SPEC;
      private static final NiftyCartsConfig.Client CLIENT;
      private static final ForgeConfigSpec CLIENT_SPEC;

      static {
         Pair<NiftyCartsConfig.Common, ForgeConfigSpec> specPair = new Builder().configure(NiftyCartsConfig.Common::new);
         COMMON = (NiftyCartsConfig.Common)specPair.getLeft();
         COMMON_SPEC = (ForgeConfigSpec)specPair.getRight();
         Pair<NiftyCartsConfig.Client, ForgeConfigSpec> clientSpecPair = new Builder().configure(NiftyCartsConfig.Client::new);
         CLIENT = (NiftyCartsConfig.Client)clientSpecPair.getLeft();
         CLIENT_SPEC = (ForgeConfigSpec)clientSpecPair.getRight();
      }
   }
}
