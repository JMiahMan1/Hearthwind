package ht.treechop.common.config;

import ht.treechop.TreeChop;
import ht.treechop.api.IChoppingItem;
import ht.treechop.common.config.resource.ResourceIdentifier;
import ht.treechop.common.platform.ModLoader;
import ht.treechop.common.settings.ChopSettings;
import ht.treechop.common.settings.Permissions;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import ht.treechop.common.util.AxeAccessor;
import ht.treechop.compat.HugeFungusHandler;
import ht.treechop.compat.HugeMushroomHandler;
import ht.treechop.compat.LeafDecayOverrides;
import ht.treechop.compat.ProblematicLeavesTreeHandler;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.Builder;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.EnumValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.jetbrains.annotations.NotNull;

public class ConfigHandler {
   public static final ConfigHandler.Common COMMON;
   public static final ModConfigSpec COMMON_SPEC;
   public static final ConfigHandler.Client CLIENT;
   public static final ModConfigSpec CLIENT_SPEC;

   public static final Signal<Lazy<?>> RELOAD = new Signal<>(Lazy::reset);
   private static final Signal<Lazy<?>> UPDATE_TAGS = new Signal<>(Lazy::reset);
   static {
      Pair<ConfigHandler.Common, ModConfigSpec> commonPair = new Builder().configure(ConfigHandler.Common::new);
      COMMON_SPEC = commonPair.getRight();
      COMMON = commonPair.getLeft();
      Pair<ConfigHandler.Client, ModConfigSpec> clientPair = new Builder().configure(ConfigHandler.Client::new);
      CLIENT_SPEC = clientPair.getRight();
      CLIENT = clientPair.getLeft();
   }
   public static final Lazy<ChopSettings> defaultChopSettings = new Lazy<>(
      RELOAD,
      () -> {
         ChopSettings chopSettings = new ChopSettings();
         Permissions permissions = getServerPermissions();
         chopSettings.forEach(
            (field, value) -> {
               if (!permissions.isPermitted(field, value)) {
                  chopSettings.set(
                     field, field.getValues().stream().filter(candidate -> permissions.isPermitted(new Setting(field, candidate))).findFirst().orElse(value)
                  );
               }
            }
         );
         return chopSettings;
      }
   );
   public static final Lazy<ChopSettings> fakePlayerChopSettings = new Lazy<>(
      RELOAD,
      () -> new ChopSettings()
         .setChoppingEnabled((Boolean)COMMON.fakePlayerChoppingEnabled.get())
         .setTreesMustHaveLeaves((Boolean)COMMON.fakePlayerTreesMustHaveLeaves.get())
   );
   public static Lazy<Boolean> removeBarkOnInteriorLogs = new Lazy<>(RELOAD, () -> {
      try {
         return (Boolean)CLIENT.removeBarkOnInteriorLogs.get();
      } catch (IllegalStateException var1x) {
         return false;
      }
   });
   public static Lazy<Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState>> inferredStrippedStates = new Lazy<>(
      UPDATE_TAGS, ConfigHandler::inferStrippedStates
   );
   private static Level logLevel = Level.ALL;

   public static void onReload() {
      updateLogLevel();
      RELOAD.run();
      updateTags();
   }

   public static void updateTags() {
      UPDATE_TAGS.run();
   }

   private static void updateLogLevel() {
      Level newLevel = COMMON.enableLogging.get() ? Level.ALL : Level.OFF;
      if (newLevel != logLevel) {
         Configurator.setLevel("treechop", Level.ALL);
         TreeChop.LOGGER.info("Changing log level to {}", newLevel.name());
         Configurator.setLevel("treechop", newLevel);
         logLevel = newLevel;
      }
   }

   @NotNull
   private static Map<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> inferStrippedStates() {
      Set<net.minecraft.world.level.block.Block> choppableBlocks = COMMON.choppableBlocks.get();
      HashMap<net.minecraft.world.level.block.Block, net.minecraft.world.level.block.state.BlockState> map = new HashMap<>();
      choppableBlocks.forEach(block -> {
         Block unstripped = inferUnstripped(block);
         if (unstripped != Blocks.AIR && !AxeAccessor.isStrippable(unstripped)) {
            map.put(unstripped, block.defaultBlockState());
         }
      });
      return map;
   }

   private static Block inferUnstripped(Block block) {
      Identifier resource = BuiltInRegistries.BLOCK.getKey(block);
      return inferUnstripped(resource);
   }

   private static Block inferUnstripped(Identifier resource) {
      if (resource != null) {
         Identifier unstripped = getFilteredResourceLocation(resource, "stripped");
         if (unstripped != null) {
            return (Block)BuiltInRegistries.BLOCK.getValue(unstripped);
         }
      }

      return Blocks.AIR;
   }

   private static Identifier getFilteredResourceLocation(Identifier resource, String filterTerm) {
      if (resource != null) {
         String strippedPath = resource.getPath();
         String unstrippedPath = Arrays.stream(strippedPath.split("_")).filter(token -> !token.equals(filterTerm)).collect(Collectors.joining("_"));
         if (!strippedPath.equals(unstrippedPath)) {
            return Identifier.fromNamespaceAndPath(resource.getNamespace(), unstrippedPath);
         }
      }

      return null;
   }

   public static Stream<net.minecraft.world.item.Item> getIdentifiedItems(String stringId) {
      ResourceIdentifier id = ResourceIdentifier.from(stringId);
      return id.resolve(BuiltInRegistries.ITEM);
   }

   public static Stream<net.minecraft.world.level.block.Block> getIdentifiedBlocks(String stringId) {
      ResourceIdentifier id = ResourceIdentifier.from(stringId);
      return id.resolve(BuiltInRegistries.BLOCK);
   }

   public static Stream<net.minecraft.world.level.block.Block> getIdentifiedBlocks(List<? extends String> strings) {
      return strings.stream().flatMap(ConfigHandler::getIdentifiedBlocks);
   }

   public static boolean canChopWithTool(Player player, ItemStack tool, net.minecraft.world.level.Level level, BlockPos pos, BlockState blockState) {
      IChoppingItem choppingItem = TreeChop.api.getRegisteredChoppingItemBehavior(tool.getItem());
      return choppingItem != null ? choppingItem.canChop(player, tool, level, pos, blockState) : choppingItemIsBlacklisted(tool.getItem());
   }

   private static boolean choppingItemIsBlacklisted(Item item) {
      return ((ListType)COMMON.itemsBlacklistOrWhitelist.get()).accepts(COMMON.choppingItemsList.get().contains(item));
   }

   public static Permissions getServerPermissions() {
      return new Permissions(
         COMMON.rawPermissions
            .stream()
            .filter(settingAndConfig -> (Boolean)((BooleanValue)settingAndConfig.getValue()).get())
            .<Setting>map(Pair::getKey)
            .collect(Collectors.toSet())
      );
   }

   private static <T> ConfigHandler.InitializedSupplier<T> defaultValue(T defaultValue) {
      return new ConfigHandler.InitializedSupplier<>(() -> defaultValue);
   }

   public static String getCommonTagId(String path) {
      return String.format("#%s:%s", TreeChop.platform.uses(ModLoader.FORGE) ? "forge" : "c", path);
   }

   public static class Client {
      public final BooleanValue choppingEnabled;
      public final EnumValue<SneakBehavior> sneakBehavior;
      public final BooleanValue treesMustHaveLeaves;
      public final BooleanValue chopInCreativeMode;
      public final BooleanValue showChoppingIndicators;
      private final BooleanValue removeBarkOnInteriorLogs;
      public final IntValue indicatorXOffset;
      public final IntValue indicatorYOffset;
      public final BooleanValue showFeedbackMessages;
      public final BooleanValue showTooltips;

      public Client(Builder builder) {
         builder.push("chopping");
         this.choppingEnabled = builder.comment("Default setting for whether or not the user wishes to chop (can be toggled in-game)")
            .define("choppingEnabled", true);
         this.sneakBehavior = builder.comment("Default setting for the effect that sneaking has on chopping (can be cycled in-game)")
            .defineEnum("sneakBehavior", SneakBehavior.INVERT_CHOPPING);
         this.treesMustHaveLeaves = builder.comment("Ignore trees without connected leaves (can be toggled in-game)").define("treesMustHaveLeaves", true);
         this.chopInCreativeMode = builder.comment(
               "Enable chopping in creative mode (even when false, sneaking can still enable chopping) (can be toggled in-game)"
            )
            .define("chopInCreativeMode", false);
         builder.pop();
         builder.push("visuals");
         this.removeBarkOnInteriorLogs = builder.comment("Visually replace the interior sides of logs with a chopped texture instead of bark")
            .define("removeBarkOnInteriorLogs", true);
         builder.push("choppingIndicator");
         this.showChoppingIndicators = builder.comment("Show an on-screen indicator when a block will be chopped instead of broken (can be toggled in-game)")
            .define("enabled", true);
         this.indicatorXOffset = builder.comment(
               "Horizontal location of the indicator relative to the player's crosshairs; positive values move the indicator to the right"
            )
            .defineInRange("xOffset", 16, -256, 256);
         this.indicatorYOffset = builder.comment(
               "Vertical location of the indicator relative to the player's crosshairs; positive values move the indicator down"
            )
            .defineInRange("yOffset", 0, -256, 256);
         builder.pop();
         builder.pop();
         builder.push("settingsScreen");
         this.showFeedbackMessages = builder.comment("Show chat confirmations when using hotkeys to change chop settings (can be toggled in-game)")
            .define("showFeedbackMessages", true);
         this.showTooltips = builder.comment("Show tooltips in the settings screen (can be toggled in-game)").define("showTooltips", true);
         builder.pop();
      }

      public ChopSettings getChopSettings() {
         ChopSettings chopSettings = new ChopSettings();
         chopSettings.setChoppingEnabled((Boolean)ConfigHandler.CLIENT.choppingEnabled.get());
         chopSettings.setSneakBehavior((SneakBehavior)ConfigHandler.CLIENT.sneakBehavior.get());
         chopSettings.setTreesMustHaveLeaves((Boolean)ConfigHandler.CLIENT.treesMustHaveLeaves.get());
         chopSettings.setChopInCreativeMode((Boolean)ConfigHandler.CLIENT.chopInCreativeMode.get());
         return chopSettings;
      }
   }

   public static class Common {
      public final BooleanValue enabled;
      public final BooleanValue enableLogging;
      public final BooleanValue dropLootForChoppedBlocks;
      public final BooleanValue dropLootOnFirstChop;
      public final IntValue maxNumTreeBlocks;
      public final IntValue maxNumLeavesBlocks;
      public final EnumValue<FellLeavesStrategy> fellLeavesStrategy;
      public final EnumValue<FellCreditStrategy> fellCreditStrategy;
      public final BooleanValue ignorePersistentLeaves;
      public final IntValue maxBreakLeavesDistance;
      public final EnumValue<ChopCountingAlgorithm> chopCountingAlgorithm;
      public final EnumValue<Rounder> chopCountRounding;
      public final BooleanValue canRequireMoreChopsThanBlocks;
      public final DoubleValue logarithmicA;
      public final DoubleValue linearM;
      public final DoubleValue linearB;
      public final EnumValue<ListType> itemsBlacklistOrWhitelist;
      public final BooleanValue mustUseCorrectToolForDrops;
      public final BooleanValue mustUseFastBreakingTool;
      public final BooleanValue preventChoppingOnRightClick;
      public final BooleanValue preventChopRecursion;
      public final BooleanValue fakePlayerChoppingEnabled;
      public final BooleanValue fakePlayerTreesMustHaveLeaves;
      public final ConfigHandler.InitializedSupplier<Boolean> compatForCarryOn = ConfigHandler.defaultValue(true);
      public final ConfigHandler.InitializedSupplier<Boolean> compatForProjectMMO = ConfigHandler.defaultValue(true);
      public final ConfigHandler.InitializedSupplier<Boolean> compatForTheOneProbe = ConfigHandler.defaultValue(true);
      public final ConfigHandler.InitializedSupplier<Boolean> compatForSilentGear = ConfigHandler.defaultValue(true);
      public final ConfigHandler.InitializedSupplier<Integer> silentGearSawChops = ConfigHandler.defaultValue(5);
      public final ConfigHandler.InitializedSupplier<Boolean> compatForTerraformers = ConfigHandler.defaultValue(true);
      public final ConfigHandler.InitializedSupplier<Boolean> compatForTinkersConstruct = ConfigHandler.defaultValue(true);
      public final ConfigHandler.InitializedSupplier<Boolean> compatForMultiMine = ConfigHandler.defaultValue(true);
      public final ConfigHandler.InitializedSupplier<Boolean> compatForApotheosis = ConfigHandler.defaultValue(true);
      public final ConfigHandler.InitializedSupplier<Integer> tinkersConstructTreeAOEChops = ConfigHandler.defaultValue(5);
      public final ConfigHandler.InitializedSupplier<Integer> tinkersConstructWoodAOEChops = ConfigHandler.defaultValue(5);
      public final ConfigHandler.InitializedSupplier<Double> tinkersConstructExpandedMultiplier = ConfigHandler.defaultValue(2.0);
      public final BooleanValue verboseAPI;
      protected final List<Pair<Setting, BooleanValue>> rawPermissions = new LinkedList<>();
      protected final ConfigValue<List<? extends String>> choppableBlocksList;
      protected final ConfigValue<List<? extends String>> choppableBlocksExceptionsList;
      public final Lazy<Set<net.minecraft.world.level.block.Block>> choppableBlocks = new Lazy<>(
         ConfigHandler.UPDATE_TAGS,
         () -> {
            Set<net.minecraft.world.level.block.Block> exceptions = ConfigHandler.getIdentifiedBlocks(
                  (List<? extends String>)ConfigHandler.COMMON.choppableBlocksExceptionsList.get()
               )
               .collect(Collectors.toSet());
            Set<net.minecraft.world.level.block.Block> blocks = ConfigHandler.getIdentifiedBlocks(
                  (List<? extends String>)ConfigHandler.COMMON.choppableBlocksList.get()
               )
               .filter(block -> !exceptions.contains(block))
               .collect(Collectors.toSet());
            TreeChop.api.getChoppableBlockOverrides().forEach(blockIsChoppable -> {
               if ((Boolean)blockIsChoppable.getValue()) {
                  blocks.add((Block)blockIsChoppable.getKey());
               } else {
                  blocks.remove(blockIsChoppable.getKey());
               }
            });
            return blocks;
         }
      );
      protected final ConfigValue<List<? extends String>> leavesBlocksList;
      protected final ConfigValue<List<? extends String>> leavesBlocksExceptionsList;
      public final Lazy<Map<net.minecraft.world.level.block.Block, TreeLeavesBehavior>> leavesBlocksAndBehaviors = new Lazy<>(
         ConfigHandler.UPDATE_TAGS,
         () -> {
            Set<net.minecraft.world.level.block.Block> exceptions = ConfigHandler.getIdentifiedBlocks(
                  (List<? extends String>)ConfigHandler.COMMON.leavesBlocksExceptionsList.get()
               )
               .collect(Collectors.toSet());
            Map<net.minecraft.world.level.block.Block, TreeLeavesBehavior> blocks = ConfigHandler.getIdentifiedBlocks(
                  (List<? extends String>)ConfigHandler.COMMON.leavesBlocksList.get()
               )
               .filter(block -> !exceptions.contains(block))
               .collect(Collectors.toConcurrentMap(k -> k, v -> TreeLeavesBehavior.DEFAULT, (a, b) -> b));
            TreeChop.api.getLeavesBlockOverrides().forEach(blockIsLeaves -> {
               if ((Boolean)blockIsLeaves.getValue()) {
                  blocks.put((Block)blockIsLeaves.getKey(), TreeLeavesBehavior.PROBLEMATIC);
               } else {
                  blocks.remove(blockIsLeaves.getKey());
               }
            });
            return blocks;
         }
      );
      protected final ConfigValue<List<? extends String>> choppingItemsToBlacklistOrWhitelist;
      public final Lazy<Set<net.minecraft.world.item.Item>> choppingItemsList = new Lazy<>(
         ConfigHandler.UPDATE_TAGS,
         () -> {
            Set<net.minecraft.world.item.Item> items = ((List<String>)ConfigHandler.COMMON.choppingItemsToBlacklistOrWhitelist.get())
               .stream()
               .flatMap(s -> ConfigHandler.getIdentifiedItems(s))
               .collect(Collectors.toSet());
            ListType blackListOrWhiteList = (ListType)ConfigHandler.COMMON.itemsBlacklistOrWhitelist.get();
            TreeChop.api.getChoppingItemOverrides().forEach(itemCanChop -> {
               if (blackListOrWhiteList.accepts((Boolean)itemCanChop.getValue())) {
                  items.add((Item)itemCanChop.getKey());
               } else {
                  items.remove(itemCanChop.getKey());
               }
            });
            return items;
         }
      );

      public Common(Builder builder) {
         String blockIdsHelp = String.join(
            "\n",
            "For block lists, specify using registry names (mod:block), tags (#mod:tag), namespaces (@mod), and Java-style regular expressions",
            "Regular expressions must match the whole resource name, including the colon. Some simple examples are:",
            " - To match any block ending in _log: \".*_log\", where .* is a wildcard",
            " - You can also specify a mod: \"treemod:.*_log\"",
            " - To also match stripped versions: \".*_log(_stripped)?\", where ? means that the text in the parenthesis is optional",
            "For more help, see https://github.com/hammertater/treechop/wiki/Configuration"
         );
         builder.push("mod");
         this.enabled = builder.comment("Set to false to disable TreeChop without having to uninstall the mod").define("enabled", true);
         this.enableLogging = builder.comment("Let TreeChop print to the log").define("printDebugInfo", true);
         builder.pop();
         builder.push("permissions");

         for (SettingsField field : SettingsField.values()) {
            String fieldName = field.getConfigKey();

            for (Object value : field.getValues()) {
               String valueName = this.getPrettyValueName(value);
               BooleanValue configHandle = builder.define(fieldName + ".canBe" + valueName, true);
               this.rawPermissions.add(Pair.of(new Setting(field, value), configHandle));
            }
         }

         builder.pop();
         builder.push("general");
         this.dropLootForChoppedBlocks = builder.comment("If false, log items will be destroyed when chopping").define("dropLootForChoppedBlocks", true);
         this.dropLootOnFirstChop = builder.comment(
               "If true, chopped logs will drop a log item immediately instead of waiting for the tree to be felled, restoring legacy behavior. Does nothing if dropLootForChoppedBlocks is false"
            )
            .define("dropLootOnFirstChop", false);
         builder.pop();
         builder.push("treeDetection");
         this.maxNumTreeBlocks = builder.comment("Maximum number of log blocks that can be detected to belong to one tree")
            .defineInRange("maxTreeBlocks", 1024, 1, 8096);
         this.maxNumLeavesBlocks = builder.comment("Maximum number of leaves blocks that can destroyed when a tree is felled")
            .defineInRange("maxLeavesBlocks", 1024, 1, 8096);
         this.fellLeavesStrategy = builder.comment("What to do with leaves blocks when a tree is felled")
            .defineEnum("breakOrDecayLeaves", FellLeavesStrategy.DECAY);
         this.fellCreditStrategy = builder.comment(
               String.join(
                  "\n",
                  "Who to credit for breaking tree blocks (logs and leaves), which can change item drops, trigger enchantment effects, etc.",
                  "Use with caution: may cause unexpected interactions with other mods"
               )
            )
            .defineEnum("fellCredit", FellCreditStrategy.NONE);
         this.ignorePersistentLeaves = builder.comment("Non-decayable leaves are ignored when detecting leaves").define("ignorePersistentLeaves", true);
         this.maxBreakLeavesDistance = builder.comment("Maximum distance from log blocks to destroy leaves blocks when felling")
            .defineInRange("maxBreakLeavesDistance", 7, 0, 16);
         builder.push("logs");
         this.choppableBlocksList = builder.comment(String.join("\n", "Blocks that should be considered choppable", blockIdsHelp))
            .defineListAllowEmpty("blocks", List.of("#treechop:choppables", "#minecraft:logs"), always -> true);
         this.choppableBlocksExceptionsList = builder.comment(
               String.join(
                  "\n",
                  "Blocks that should never be chopped, even if included in the list above",
                  "Specify using registry names (mod:block), tags (#mod:tag), and namespaces (@mod)"
               )
            )
            .defineListAllowEmpty("exceptions", List.of("minecraft:bamboo", "#dynamictrees:branches", "dynamictrees:trunk_shell"), always -> true);
         builder.pop();
         builder.push("leaves");
         this.leavesBlocksList = builder.comment(
               String.join("\n", "Blocks that should be considered leaves", "Specify using registry names (mod:block), tags (#mod:tag), and namespaces (@mod)")
            )
            .defineListAllowEmpty("blocks", List.of("#treechop:leaves_like", "#minecraft:leaves", "pamhc2trees:pam[a-z]+"), always -> true);
         this.leavesBlocksExceptionsList = builder.comment(
               String.join(
                  "\n",
                  "Blocks that should never be considered leaves, even if included in the list above",
                  "Specify using registry names (mod:block), tags (#mod:tag), and namespaces (@mod)"
               )
            )
            .defineListAllowEmpty("exceptions", List.of(), always -> true);
         builder.pop();
         builder.pop();
         builder.push("chopCounting");
         this.chopCountingAlgorithm = builder.comment("Method to use for computing the number of chops needed to fell a tree")
            .defineEnum("algorithm", ChopCountingAlgorithm.LOGARITHMIC);
         this.chopCountRounding = builder.comment("How to round the number of chops needed to fell a tree; this is more meaningful for smaller trees")
            .defineEnum("rounding", Rounder.NEAREST);
         this.canRequireMoreChopsThanBlocks = builder.comment("Felling a tree can require more chops than the number of blocks in the tree")
            .define("canRequireMoreChopsThanBlocks", false);
         builder.comment("See https://github.com/hammertater/treechop/#logarithmic").push("logarithmic");
         this.logarithmicA = builder.comment("Determines the number of chops required to fell a tree; higher values require more chops for bigger trees")
            .defineInRange("a", 10.0, 0.0, 10000.0);
         builder.pop();
         builder.comment("See https://github.com/hammertater/treechop/#linear").push("linear");
         this.linearM = builder.comment(
               "The number of chops per block required to fell a tree; if chopsPerBlock = 0.5, it will take 50 chops to fell a 100 block tree"
            )
            .defineInRange("chopsPerBlock", 1.0, 0.0, 7.0);
         this.linearB = builder.comment("The base number of chops required to fell a tree regardless of its size")
            .defineInRange("baseNumChops", 0.0, -10000.0, 10000.0);
         builder.pop();
         builder.pop();
         builder.push("compatibility");
         builder.push("general");
         this.mustUseCorrectToolForDrops = builder.comment(
               "Only chop when using the correct tool for drops, if any (does nothing in vanilla, but some mods add tool requirements to logs"
            )
            .define("choppingRequiresCorrectToolForDrops", true);
         this.mustUseFastBreakingTool = builder.comment("Only chop when using a tool that increases block breaking speed (such as axes for logs)")
            .define("choppingRequiresFastBreakingTool", false);
         this.preventChoppingOnRightClick = builder.comment("Prevent chopping when right-clicking blocks").define("preventChoppingOnRightClick", false);
         this.preventChopRecursion = builder.comment("Prevent infinite loops when chopping; fixes crashes when using modded items that break multiple blocks")
            .define("preventChopRecursion", true);
         builder.push("blacklist");
         this.itemsBlacklistOrWhitelist = builder.comment("Whether the listed items should be blacklisted or whitelisted")
            .defineEnum("blacklistOrWhitelist", ListType.BLACKLIST);
         this.choppingItemsToBlacklistOrWhitelist = builder.comment(
               String.join(
                  "\n",
                  "List of item registry names (mod:item), tags (#mod:tag), and namespaces (@mod) for items that should not chop when used to break a log",
                  "- Items in this list that have special support for TreeChop will not be blacklisted; see https://github.com/hammertater/treechop/blob/main/docs/compatibility.md#blacklist"
               )
            )
            .defineListAllowEmpty(
               "items",
               Arrays.asList(
                  "botania:terra_axe",
                  "@lumberjack",
                  "mekanism:atomic_disassembler",
                  "practicaltools:diamond_greataxe",
                  "practicaltools:golden_greataxe",
                  "practicaltools:iron_greataxe",
                  "practicaltools:netherite_greataxe",
                  "twilightforest:giant_pickaxe"
               ),
               always -> true
            );
         builder.pop();
         builder.comment("The chop settings used by non-player entities, such as robots and machine blocks");
         builder.push("fakePlayerChopSettings");
         this.fakePlayerChoppingEnabled = builder.comment(
               "Use with caution! May cause conflicts with some mods, e.g. https://github.com/hammertater/treechop/issues/71"
            )
            .define("choppingEnabled", false);
         this.fakePlayerTreesMustHaveLeaves = builder.define("treesMustHaveLeaves", true);
         builder.pop();
         builder.pop();
         builder.comment(String.join("\n", "A set of alternate tree detection strategies for oddly shaped trees", blockIdsHelp));
         builder.push("trees");
         HugeMushroomHandler.MyConfigHandler.init(builder);
         HugeFungusHandler.MyConfigHandler.init(builder);
         ProblematicLeavesTreeHandler.MyConfigHandler.init(builder);
         LeafDecayOverrides.MyConfigHandler.init(builder);
         builder.pop();
         if (TreeChop.platform.uses(ModLoader.FORGE)) {
            this.compatForCarryOn
               .set(
                  builder.comment(new String[]{"https://www.curseforge.com/minecraft/mc-mods/carry-on", "https://modrinth.com/mod/carry-on", "Small fixes."})
                     .define("carryOn", true)::get
               );
            this.compatForProjectMMO
               .set(
                  builder.comment(
                        new String[]{
                           "https://www.curseforge.com/minecraft/mc-mods/project-mmo",
                           "https://modrinth.com/mod/project-mmo",
                           "Award woodcutting XP for chopping."
                        }
                     )
                     .define("projectMMO", true)::get
               );
            this.compatForTheOneProbe
               .set(
                  builder.comment(
                        new String[]{
                           "https://www.curseforge.com/minecraft/mc-mods/the-one-probe",
                           "https://modrinth.com/mod/the-one-probe",
                           "Shows the number of chops required to fell a tree and what loot will drop."
                        }
                     )
                     .define("theOneProbe", true)::get
               );
            this.compatForTerraformers
               .set(
                  builder.comment(
                        new String[]{
                           "https://github.com/TerraformersMC", "Fixes starting chop radius for small logs from Terraformers mods (Terrestria, etc.)"
                        }
                     )
                     .define("terraformers", true)::get
               );
            this.compatForMultiMine
               .set(
                  builder.comment(new String[]{"https://github.com/AtomicStryker/atomicstrykers-minecraft-mods", "Fixes bad behavior"})
                     .define("multiMine", true)::get
               );
            this.compatForApotheosis
               .set(
                  builder.comment(
                        new String[]{"https://www.curseforge.com/minecraft/mc-mods/apotheosis", "Adds compatibility with the \"chainsaw\" enchantment."}
                     )
                     .define("apotheosis", true)::get
               );
            builder.push("silentgear");
            this.compatForSilentGear
               .set(
                  builder.comment(
                        new String[]{
                           "https://www.curseforge.com/minecraft/mc-mods/tinkers-construct",
                           "https://modrinth.com/mod/tinkers-construct",
                           "Makes saws do more chops."
                        }
                     )
                     .define("enabled", true)::get
               );
            this.silentGearSawChops
               .set(builder.comment("Number of chops a saw should perform on a single block break").defineInRange("sawChops", 5, 1, 10000)::get);
            builder.pop();
            builder.push("tinkersConstruct");
            this.compatForTinkersConstruct
               .set(
                  builder.comment(
                        new String[]{
                           "https://www.curseforge.com/minecraft/mc-mods/tinkers-construct",
                           "https://modrinth.com/mod/tinkers-construct",
                           "Makes AOE tools do more chops."
                        }
                     )
                     .define("enabled", true)::get
               );
            this.tinkersConstructTreeAOEChops
               .set(
                  builder.comment("Number of chops that tree breaking tools (like broad axes) should perform on a single block break")
                     .defineInRange("treeBreakingTools", 5, 1, 10000)::get
               );
            this.tinkersConstructWoodAOEChops
               .set(
                  builder.comment("Number of chops that wood breaking tools (like hand axes) should perform on a single block break")
                     .defineInRange("woodBreakingTools", 1, 1, 10000)::get
               );
            this.tinkersConstructExpandedMultiplier
               .set(
                  builder.comment("The chop count multiplier for each level of the expanded upgrade").defineInRange("expandedMultiplier", 2.0, 1.0, 10000.0)::get
               );
            builder.pop();
         }

         builder.push("API");
         this.verboseAPI = builder.comment("Log information about TreeChop API usage. May be useful for debugging mod compatibility issues.")
            .define("verbose", false);
         builder.pop();
         builder.pop();
      }

      private String getPrettyValueName(Object value) {
         return Arrays.stream(value.toString().toLowerCase().split("_")).<CharSequence>map(WordUtils::capitalize).collect(Collectors.joining());
      }
   }

   public static class InitializedSupplier<T> implements Supplier<T> {
      private Supplier<T> supplier;

      public InitializedSupplier(Supplier<T> defaultSupplier) {
         this.supplier = defaultSupplier;
      }

      @Override
      public T get() {
         return this.supplier.get();
      }

      private void set(Supplier<T> newSupplier) {
         this.supplier = newSupplier;
      }
   }
}
