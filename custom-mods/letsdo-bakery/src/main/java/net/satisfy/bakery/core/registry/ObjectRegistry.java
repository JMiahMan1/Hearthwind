package net.satisfy.bakery.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.satisfy.bakery.Bakery;
import net.satisfy.bakery.core.block.*;
import net.satisfy.bakery.core.block.cake.*;
import net.satisfy.bakery.core.item.SmallCookingPotItem;
import net.satisfy.bakery.core.item.SugarRushEffectItem;
import net.satisfy.bakery.platform.PlatformHelper;
import net.satisfy.farm_and_charm.core.block.*;
import net.satisfy.farm_and_charm.core.item.food.EffectBlockItem;
import net.satisfy.farm_and_charm.core.item.food.EffectItem;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Bakery.MOD_ID, Registries.ITEM);
    public static final Registrar<Item> ITEM_REGISTRAR = ITEMS.getRegistrar();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Bakery.MOD_ID, Registries.BLOCK);
    public static final Registrar<Block> BLOCK_REGISTRAR = BLOCKS.getRegistrar();

    public static final RegistrySupplier<Block> BAKERY_BANNER = registerWithItem("bakery_banner", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(Bakery.identifier("bakery_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BAKERY_WALL_BANNER = registerWithoutItem("bakery_wall_banner", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(Bakery.identifier("bakery_wall_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> KITCHEN_SINK = registerWithItem("kitchen_sink", () -> new BrickSinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Bakery.identifier("kitchen_sink"))).noOcclusion()));
    public static final RegistrySupplier<Block> BAKER_STATION = registerWithItem("baker_station", () -> new BakerStationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Bakery.identifier("baker_station")))));
    public static final RegistrySupplier<Block> BRICK_COUNTER = registerWithItem("brick_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Bakery.identifier("brick_counter")))));
    public static final RegistrySupplier<Block> CABINET = registerWithItem("cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.of().setId(blockKey(Bakery.identifier("cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> DRAWER = registerWithItem("drawer", () -> new CabinetBlock(BlockBehaviour.Properties.of().setId(blockKey(Bakery.identifier("drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> WALL_CABINET = registerWithItem("wall_cabinet", () -> new CabinetWallBlock(BlockBehaviour.Properties.of().setId(blockKey(Bakery.identifier("wall_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> IRON_BENCH = registerWithItem("iron_bench", () -> new BenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(Bakery.identifier("iron_bench")))));
    public static final RegistrySupplier<Block> IRON_CHAIR = registerWithItem("iron_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(Bakery.identifier("iron_chair")))));
    public static final RegistrySupplier<Block> IRON_TABLE = registerWithItem("iron_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(Bakery.identifier("iron_table")))));
    public static final RegistrySupplier<Block> STREET_SIGN = registerWithItem("street_sign", () -> new StreetSignBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Bakery.identifier("street_sign")))));
    public static final RegistrySupplier<Block> CAKE_STAND = registerWithItem("cake_stand", () -> new CakeStandBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Bakery.identifier("cake_stand"))).sound(SoundType.GLASS)));
    public static final RegistrySupplier<Block> CAKE_DISPLAY = registerWithItem("cake_display", () -> new CakeDisplayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Bakery.identifier("cake_display"))).sound(SoundType.GLASS)));
    public static final RegistrySupplier<Block> CUPCAKE_DISPLAY = registerWithItem("cupcake_display", () -> new CupcakeDisplayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Bakery.identifier("cupcake_display"))).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BREADBOX = registerWithItem("breadbox", () -> new BreadBox(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Bakery.identifier("breadbox")))));
    public static final RegistrySupplier<Block> TRAY = registerWithItem("tray", () -> new TrayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Bakery.identifier("tray")))));
    public static final RegistrySupplier<Block> BREAD_CRATE = registerWithItem("bread_crate", () -> new BreadBasketBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Bakery.identifier("bread_crate")))));
    public static final RegistrySupplier<Block> WALL_DISPLAY = registerWithItem("wall_display", () -> new WallDisplayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Bakery.identifier("wall_display")))));
    public static final RegistrySupplier<Block> CHOCOLATE_BOX = registerWithItem("chocolate_box", () -> new EatableBoxBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("chocolate_box")))));
    public static final RegistrySupplier<Item> ROLLING_PIN = registerItem("rolling_pin", () -> new Item(getSettings(Bakery.identifier("rolling_pin")).rarity(Rarity.COMMON).sword(ToolMaterial.IRON, -1, -3.4F)));
    public static final RegistrySupplier<Item> BREAD_KNIFE = registerItem("bread_knife", () -> new Item(getSettings(Bakery.identifier("bread_knife")).rarity(Rarity.COMMON).sword(ToolMaterial.WOOD, -1, -3.4F)));
    public static final RegistrySupplier<Block> SMALL_COOKING_POT = registerWithoutItem("small_cooking_pot", () -> new SmallCookingPotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(Bakery.identifier("small_cooking_pot")))));
    public static final RegistrySupplier<Item> SMALL_COOKING_POT_ITEM = registerItem("small_cooking_pot", () -> new SmallCookingPotItem(SMALL_COOKING_POT.get(), getSettings(Bakery.identifier("small_cooking_pot")).attributes(SmallCookingPotItem.createAttributes())));
    public static final RegistrySupplier<Block> JAR = registerWithItem("jar", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Bakery.identifier("jar"))).instabreak().noOcclusion().sound(SoundType.GLASS), 4));
    public static final RegistrySupplier<Block> STRAWBERRY_JAM = registerWithItem("strawberry_jam", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Bakery.identifier("strawberry_jam"))).instabreak().noOcclusion().sound(SoundType.GLASS), 4), () -> JAR.get().asItem());
    public static final RegistrySupplier<Block> GLOWBERRY_JAM = registerWithItem("glowberry_jam", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Bakery.identifier("glowberry_jam"))).instabreak().noOcclusion().sound(SoundType.GLASS), 4), () -> JAR.get().asItem());
    public static final RegistrySupplier<Block> SWEETBERRY_JAM = registerWithItem("sweetberry_jam", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Bakery.identifier("sweetberry_jam"))).instabreak().noOcclusion().sound(SoundType.GLASS), 4), () -> JAR.get().asItem());
    public static final RegistrySupplier<Block> CHOCOLATE_JAM = registerWithItem("chocolate_jam", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Bakery.identifier("chocolate_jam"))).instabreak().noOcclusion().sound(SoundType.GLASS), 4), () -> JAR.get().asItem());
    public static final RegistrySupplier<Block> APPLE_JAM = registerWithItem("apple_jam", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Bakery.identifier("apple_jam"))).instabreak().sound(SoundType.GLASS).noOcclusion(), 4), () -> JAR.get().asItem());
    public static final RegistrySupplier<Block> CRUSTY_BREAD_BLOCK = registerWithoutItem("crusty_bread_block", () -> new StackableEatableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("crusty_bread_block"))), 3));
    public static final RegistrySupplier<Block> BREAD_BLOCK = registerWithoutItem("bread_block", () -> new StackableEatableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("bread_block"))), 3));
    public static final RegistrySupplier<Block> BAGUETTE_BLOCK = registerWithoutItem("baguette_block", () -> new StackableEatableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("baguette_block"))), 4));
    public static final RegistrySupplier<Block> TOAST_BLOCK = registerWithoutItem("toast_block", () -> new StackableEatableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("toast_block"))), 3));
    public static final RegistrySupplier<Block> BRAIDED_BREAD_BLOCK = registerWithoutItem("braided_bread_block", () -> new StackableEatableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("braided_bread_block"))), 3));
    public static final RegistrySupplier<Block> BUN_BLOCK = registerWithoutItem("bun_block", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("bun_block"))), 4));
    public static final RegistrySupplier<Block> WAFFLE_BLOCK = registerWithoutItem("waffle_block", () -> new StackableEatableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("waffle_block"))), 4));
    public static final RegistrySupplier<Item> CAKE_DOUGH = registerItem("cake_dough", () -> new Item(getSettings(Bakery.identifier("cake_dough")).food(Foods.SWEET_BERRIES)));
    public static final RegistrySupplier<Item> SWEET_DOUGH = registerItem("sweet_dough", () -> new Item(getSettings(Bakery.identifier("sweet_dough")).food(Foods.SWEET_BERRIES)));
    public static final RegistrySupplier<Item> CROISSANT = registerItem("croissant", () -> new EffectItem(getFoodItemSettings(Bakery.identifier("croissant"), PlatformHelper.getCroissantNutrition(), PlatformHelper.getCroissantSaturation(), MobEffectRegistry.VITALITY, 900), 400, false));
    public static final RegistrySupplier<Item> CRUSTY_BREAD = registerItem("crusty_bread", () -> new EffectBlockItem(CRUSTY_BREAD_BLOCK.get(), getFoodItemSettings(Bakery.identifier("crusty_bread"), PlatformHelper.getCrustyBreadNutrition(), PlatformHelper.getCrustyBreadSaturation(), MobEffectRegistry.VITALITY, 4800)));
    public static final RegistrySupplier<Item> BREAD = registerItem("bread", () -> new EffectBlockItem(BREAD_BLOCK.get(), getFoodItemSettings(Bakery.identifier("bread"), PlatformHelper.getBreadNutrition(), PlatformHelper.getBreadSaturation(), MobEffectRegistry.VITALITY, 4200)));
    public static final RegistrySupplier<Item> BAGUETTE = registerItem("baguette", () -> new EffectBlockItem(BAGUETTE_BLOCK.get(), getFoodItemSettings(Bakery.identifier("baguette"), PlatformHelper.getBaguetteNutrition(), PlatformHelper.getBaguetteSaturation(), MobEffectRegistry.VITALITY, 4200)));
    public static final RegistrySupplier<Item> TOAST = registerItem("toast", () -> new EffectBlockItem(TOAST_BLOCK.get(), getFoodItemSettings(Bakery.identifier("toast"), PlatformHelper.getToastNutrition(), PlatformHelper.getToastSaturation(), MobEffectRegistry.VITALITY, 5400)));
    public static final RegistrySupplier<Item> BRAIDED_BREAD = registerItem("braided_bread", () -> new EffectBlockItem(BRAIDED_BREAD_BLOCK.get(), getFoodItemSettings(Bakery.identifier("braided_bread"), PlatformHelper.getBraidedBreadNutrition(), PlatformHelper.getBraidedBreadSaturation(), MobEffectRegistry.VITALITY, 4200)));
    public static final RegistrySupplier<Item> SANDWICH = registerItem("sandwich", () -> new EffectItem(getFoodItemSettings(Bakery.identifier("sandwich"), PlatformHelper.getSandwichNutrition(), PlatformHelper.getSandwichSaturation(), MobEffectRegistry.VITALITY, 1500), 4800, false));
    public static final RegistrySupplier<Item> VEGETABLE_SANDWICH = registerItem("vegetable_sandwich", () -> new EffectItem(getFoodItemSettings(Bakery.identifier("vegetable_sandwich"), PlatformHelper.getVegetableSandwichNutrition(), PlatformHelper.getVegetableSandwichSaturation(), MobEffectRegistry.VITALITY, 1800), 4800, false));
    public static final RegistrySupplier<Item> GRILLED_SALMON_SANDWICH = registerItem("grilled_salmon_sandwich", () -> new EffectItem(getFoodItemSettings(Bakery.identifier("grilled_salmon_sandwich"), PlatformHelper.getGrilledSalmonSandwichNutrition(), PlatformHelper.getGrilledSalmonSandwichSaturation(), MobEffectRegistry.VITALITY, 1200), 4800, false));
    public static final RegistrySupplier<Item> GRILLED_BACON_SANDWICH = registerItem("grilled_bacon_sandwich", () -> new EffectItem(getFoodItemSettings(Bakery.identifier("grilled_bacon_sandwich"), PlatformHelper.getGrilledBaconSandwichNutrition(), PlatformHelper.getGrilledBaconSandwichSaturation(), MobEffectRegistry.VITALITY, 1200), 6000, false));
    public static final RegistrySupplier<Item> BREAD_WITH_JAM = registerItem("bread_with_jam", () -> new EffectItem(getFoodItemSettings(Bakery.identifier("bread_with_jam"), PlatformHelper.getBreadWithJamNutrition(), PlatformHelper.getBreadWithJamSaturation(), MobEffectRegistry.VITALITY, 400), 2500, false));
    public static final RegistrySupplier<Item> STRAWBERRY_CAKE_SLICE = registerItem("strawberry_cake_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("strawberry_cake_slice"), PlatformHelper.getStrawberryCakeSliceNutrition(), PlatformHelper.getStrawberryCakeSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> SWEETBERRY_CAKE_SLICE = registerItem("sweetberry_cake_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("sweetberry_cake_slice"), PlatformHelper.getSweetberryCakeSliceNutrition(), PlatformHelper.getSweetberryCakeSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> CHOCOLATE_CAKE_SLICE = registerItem("chocolate_cake_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("chocolate_cake_slice"), PlatformHelper.getChocolateCakeSliceNutrition(), PlatformHelper.getChocolateCakeSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> CHOCOLATE_GATEAU_SLICE = registerItem("chocolate_gateau_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("chocolate_gateau_slice"), PlatformHelper.getChocolateGateauSliceNutrition(), PlatformHelper.getChocolateGateauSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> BUNDT_CAKE_SLICE = registerItem("bundt_cake_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("bundt_cake_slice"), PlatformHelper.getBundtCakeSliceNutrition(), PlatformHelper.getBundtCakeSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> LINZER_TART_SLICE = registerItem("linzer_tart_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("linzer_tart_slice"), PlatformHelper.getLinzerTartSliceNutrition(), PlatformHelper.getLinzerTartSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> APPLE_PIE_SLICE = registerItem("apple_pie_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("apple_pie_slice"), PlatformHelper.getApplePieSliceNutrition(), PlatformHelper.getApplePieSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> GLOWBERRY_PIE_SLICE = registerItem("glowberry_pie_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("glowberry_pie_slice"), PlatformHelper.getGlowberryPieSliceNutrition(), PlatformHelper.getGlowberryPieSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> CHOCOLATE_TART_SLICE = registerItem("chocolate_tart_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("chocolate_tart_slice"), PlatformHelper.getChocolateTartSliceNutrition(), PlatformHelper.getChocolateTartSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> PUDDING_SLICE = registerItem("pudding_slice", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("pudding_slice"), PlatformHelper.getPuddingSliceNutrition(), PlatformHelper.getPuddingSliceSaturation(), MobEffectRegistry.SUGAR_RUSH, 600), MobEffectRegistry.SUGAR_RUSH, 600, 10, false));
    public static final RegistrySupplier<Item> STRAWBERRY_GLAZED_COOKIE = registerItem("strawberry_glazed_cookie", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("strawberry_glazed_cookie"), PlatformHelper.getStrawberryGlazedCookieNutrition(), PlatformHelper.getStrawberryGlazedCookieSaturation(), MobEffectRegistry.SUGAR_RUSH, 400), MobEffectRegistry.SUGAR_RUSH, 400, 10, false));
    public static final RegistrySupplier<Item> SWEETBERRY_GLAZED_COOKIE = registerItem("sweetberry_glazed_cookie", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("sweetberry_glazed_cookie"), PlatformHelper.getSweetberryGlazedCookieNutrition(), PlatformHelper.getSweetberryGlazedCookieSaturation(), MobEffectRegistry.SUGAR_RUSH, 400), MobEffectRegistry.SUGAR_RUSH, 400, 10, false));
    public static final RegistrySupplier<Item> CHOCOLATE_GLAZED_COOKIE = registerItem("chocolate_glazed_cookie", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("chocolate_glazed_cookie"), PlatformHelper.getChocolateGlazedCookieNutrition(), PlatformHelper.getChocolateGlazedCookieSaturation(), MobEffectRegistry.SUGAR_RUSH, 400), MobEffectRegistry.SUGAR_RUSH, 400, 10, false));
    public static final RegistrySupplier<Item> STRAWBERRY_CUPCAKE = registerItem("strawberry_cupcake", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("strawberry_cupcake"), PlatformHelper.getStrawberryCupcakeNutrition(), PlatformHelper.getStrawberryCupcakeSaturation(), MobEffectRegistry.SUGAR_RUSH, 400), MobEffectRegistry.SUGAR_RUSH, 400, 10, false));
    public static final RegistrySupplier<Item> SWEETBERRY_CUPCAKE = registerItem("sweetberry_cupcake", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("sweetberry_cupcake"), PlatformHelper.getSweetberryCupcakeNutrition(), PlatformHelper.getSweetberryCupcakeSaturation(), MobEffectRegistry.SUGAR_RUSH, 400), MobEffectRegistry.SUGAR_RUSH, 400, 10, false));
    public static final RegistrySupplier<Item> APPLE_CUPCAKE = registerItem("apple_cupcake", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("apple_cupcake"), PlatformHelper.getAppleCupcakeNutrition(), PlatformHelper.getAppleCupcakeSaturation(), MobEffectRegistry.SUGAR_RUSH, 400), MobEffectRegistry.SUGAR_RUSH, 400, 10, false));
    public static final RegistrySupplier<Item> CORNET = registerItem("cornet", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("cornet"), PlatformHelper.getCornetNutrition(), PlatformHelper.getCornetSaturation(), MobEffectRegistry.SUGAR_RUSH, 400), MobEffectRegistry.SUGAR_RUSH, 400, 10, false));
    public static final RegistrySupplier<Item> JAM_ROLL = registerItem("jam_roll", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("jam_roll"), PlatformHelper.getJamRollNutrition(), PlatformHelper.getJamRollSaturation(), MobEffectRegistry.SUGAR_RUSH, 400), MobEffectRegistry.SUGAR_RUSH, 400, 10, false));
    public static final RegistrySupplier<Item> CHOCOLATE_TRUFFLE = registerItem("chocolate_truffle", () -> new SugarRushEffectItem(getFoodItemSettings(Bakery.identifier("chocolate_truffle"), PlatformHelper.getChocolateTruffleNutrition(), PlatformHelper.getChocolateTruffleSaturation(), MobEffectRegistry.SUGAR_RUSH, 200), MobEffectRegistry.SUGAR_RUSH, 200, 10, false));
    public static final RegistrySupplier<Item> MISSLILITU_BISCUIT = registerItem("misslilitu_biscuit", () -> new EffectItem(getFoodItemSettings(Bakery.identifier("misslilitu_biscuit"), PlatformHelper.getMisslilituBiscuitNutrition(), PlatformHelper.getMisslilituBiscuitSaturation(), MobEffectRegistry.VITALITY, 900), 4200, false));
    public static final RegistrySupplier<Item> WAFFLE = registerItem("waffle", () -> new EffectBlockItem(WAFFLE_BLOCK.get(), getFoodItemSettings(Bakery.identifier("waffle"), PlatformHelper.getWaffleNutrition(), PlatformHelper.getWaffleSaturation(), MobEffectRegistry.VITALITY, 800)));
    public static final RegistrySupplier<Item> BUN = registerItem("bun", () -> new EffectBlockItem(BUN_BLOCK.get(), getFoodItemSettings(Bakery.identifier("bun"), PlatformHelper.getBunNutrition(), PlatformHelper.getBunSaturation(), MobEffectRegistry.VITALITY, 2800)));
    public static final RegistrySupplier<Block> CHOCOLATE_GATEAU = registerWithItem("chocolate_gateau", () -> new CakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("chocolate_gateau"))), CHOCOLATE_GATEAU_SLICE));
    public static final RegistrySupplier<Block> CHOCOLATE_TART = registerWithItem("chocolate_tart", () -> new ChocolateTart(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("chocolate_tart"))), CHOCOLATE_TART_SLICE));
    public static final RegistrySupplier<Block> BLANK_CAKE = registerWithoutItem("blank_cake", () -> new BlankCakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("blank_cake"))).forceSolidOn()));
    public static final RegistrySupplier<Block> APPLE_CUPCAKE_BLOCK = registerWithoutItem("apple_cupcake_block", () -> new CupcakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("apple_cupcake_block"))).instabreak().forceSolidOn()));
    public static final RegistrySupplier<Block> SWEETBERRY_CUPCAKE_BLOCK = registerWithoutItem("sweetberry_cupcake_block", () -> new CupcakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("sweetberry_cupcake_block"))).instabreak().forceSolidOn()));
    public static final RegistrySupplier<Block> STRAWBERRY_CUPCAKE_BLOCK = registerWithoutItem("strawberry_cupcake_block", () -> new CupcakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("strawberry_cupcake_block"))).instabreak().forceSolidOn()));
    public static final RegistrySupplier<Block> CHOCOLATE_COOKIE_BLOCK = registerWithoutItem("chocolate_cookie_block", () -> new CookieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("chocolate_cookie_block"))).instabreak().forceSolidOn()));
    public static final RegistrySupplier<Block> SWEETBERRY_COOKIE_BLOCK = registerWithoutItem("sweetberry_cookie_block", () -> new CookieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("sweetberry_cookie_block"))).instabreak().forceSolidOn()));
    public static final RegistrySupplier<Block> STRAWBERRY_COOKIE_BLOCK = registerWithoutItem("strawberry_cookie_block", () -> new CookieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("strawberry_cookie_block"))).instabreak().forceSolidOn()));
    public static final RegistrySupplier<Block> STRAWBERRY_CAKE = registerWithItem("strawberry_cake", () -> new CakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("strawberry_cake"))), ObjectRegistry.STRAWBERRY_CAKE_SLICE));
    public static final RegistrySupplier<Block> SWEETBERRY_CAKE = registerWithItem("sweetberry_cake", () -> new CakeBlock((BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("sweetberry_cake")))), ObjectRegistry.SWEETBERRY_CAKE_SLICE));
    public static final RegistrySupplier<Block> CHOCOLATE_CAKE = registerWithItem("chocolate_cake", () -> new CakeBlock((BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("chocolate_cake")))), ObjectRegistry.CHOCOLATE_CAKE_SLICE));
    public static final RegistrySupplier<Block> BUNDT_CAKE = registerWithItem("bundt_cake", () -> new BundtCakeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("bundt_cake"))), ObjectRegistry.BUNDT_CAKE_SLICE));
    public static final RegistrySupplier<Block> LINZER_TART = registerWithItem("linzer_tart", () -> new LinzerTartBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("linzer_tart"))), ObjectRegistry.LINZER_TART_SLICE));
    public static final RegistrySupplier<Block> APPLE_PIE = registerWithItem("apple_pie", () -> new ApplePieBlock((BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("apple_pie")))), ObjectRegistry.APPLE_PIE_SLICE));
    public static final RegistrySupplier<Block> GLOWBERRY_TART = registerWithItem("glowberry_tart", () -> new GlowberryTartBlock((BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("glowberry_tart")))), ObjectRegistry.GLOWBERRY_PIE_SLICE));
    public static final RegistrySupplier<Block> PUDDING = registerWithItem("pudding", () -> new PuddingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Bakery.identifier("pudding"))), ObjectRegistry.PUDDING_SLICE));

    public static void init() {
        BLOCKS.register();
        ITEMS.register();
    }

    private static net.minecraft.resources.ResourceKey<Item> itemKey(Identifier id) {
        return net.minecraft.resources.ResourceKey.create(Registries.ITEM, id);
    }

    private static net.minecraft.resources.ResourceKey<Block> blockKey(Identifier id) {
        return net.minecraft.resources.ResourceKey.create(Registries.BLOCK, id);
    }

    private static Item.Properties getSettings(Identifier id, Consumer<Item.Properties> consumer) {
        Item.Properties settings = new Item.Properties().setId(itemKey(id));
        consumer.accept(settings);
        return settings;
    }

    static Item.Properties getSettings(Identifier id) {
        return getSettings(id, settings -> {
        });
    }

    public static <T extends Block> RegistrySupplier<T> registerWithItem(String name, Supplier<T> block) {
        RegistrySupplier<T> toReturn = GeneralUtil.registerWithoutItem(BLOCKS, BLOCK_REGISTRAR, Bakery.identifier(name), block);
        GeneralUtil.registerItem(ITEMS, ITEM_REGISTRAR, Bakery.identifier(name), () -> new net.satisfy.bakery.core.item.BakeryTooltipBlockItem(toReturn.get(), new Item.Properties().setId(itemKey(Bakery.identifier(name)))));
        return toReturn;
    }

    private static <T extends Block> RegistrySupplier<T> registerWithItem(String name, Supplier<T> blockSupplier, Supplier<Item> craftingRemainderSupplier) {
        RegistrySupplier<T> registrySupplier = BLOCKS.register(name, blockSupplier);
        ITEMS.register(name, () -> new BlockItem(registrySupplier.get(), getSettings(Bakery.identifier(name)).craftRemainder(craftingRemainderSupplier.get())));
        return registrySupplier;
    }

    public static <T extends Block> RegistrySupplier<T> registerWithoutItem(String path, Supplier<T> block) {
        return GeneralUtil.registerWithoutItem(BLOCKS, BLOCK_REGISTRAR, Bakery.identifier(path), block);
    }

    public static <T extends Item> RegistrySupplier<T> registerItem(String path, Supplier<T> itemSupplier) {
        return GeneralUtil.registerItem(ITEMS, ITEM_REGISTRAR, Bakery.identifier(path), itemSupplier);
    }

    public static BlockBehaviour.Properties properties(float strength) {
        return properties(strength, strength);
    }

    public static BlockBehaviour.Properties properties(float breakSpeed, float explosionResist) {
        return BlockBehaviour.Properties.of().strength(breakSpeed, explosionResist);
    }

    // 26.2: FoodProperties.Builder.effect/fast removed; effects live on CONSUMABLE
    // (ApplyStatusEffectsConsumeEffect), fast = consumeSeconds(0.8). Same values preserved.
    private static Item.Properties foodWith(Identifier id, int nutrition, float saturationMod, boolean alwaysEat,
            java.util.List<net.minecraft.world.effect.MobEffectInstance> instances, float probability, boolean fast) {
        FoodProperties.Builder food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationMod);
        if (alwaysEat) food.alwaysEdible();
        FoodProperties fp = food.build();
        if (instances.isEmpty() && !fast) return new Item.Properties().setId(itemKey(id)).food(fp);
        var b = net.minecraft.world.item.component.Consumable.builder();
        if (fast) b.consumeSeconds(0.8F);
        for (var inst : instances) b.onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(inst, probability));
        return new Item.Properties().setId(itemKey(id)).food(fp, b.build());
    }

    private static Item.Properties getFoodItemSettings(Identifier id, int nutrition, float saturationMod, RegistrySupplier<MobEffect> effect, int duration) {
        return foodWith(id, nutrition, saturationMod, false, effect == null ? java.util.List.of() : java.util.List.of(new MobEffectInstance(MobEffectRegistry.holder(effect), duration)), 1.0f, false);
    }

}