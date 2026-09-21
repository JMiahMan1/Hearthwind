package net.satisfy.candlelight.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.resources.Identifier;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.core.block.*;
import net.satisfy.candlelight.core.item.*;
import net.satisfy.candlelight.core.util.CandlelightFoods;
import net.satisfy.farm_and_charm.core.block.*;
import net.satisfy.farm_and_charm.core.item.food.EffectBlockItem;
import net.satisfy.farm_and_charm.core.item.food.EffectFoodBlockItem;
import net.satisfy.farm_and_charm.core.item.food.EffectFoodItem;
import net.satisfy.farm_and_charm.core.item.food.EffectItem;
import net.satisfy.farm_and_charm.core.registry.ArmorMaterialRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ObjectRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Candlelight.MOD_ID, Registries.ITEM);
    public static final Registrar<Item> ITEM_REGISTRAR = ITEMS.getRegistrar();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Candlelight.MOD_ID, Registries.BLOCK);
    public static final Registrar<Block> BLOCK_REGISTRAR = BLOCKS.getRegistrar();

    public static final RegistrySupplier<Item> TOMATO_SOUP = registerItem("tomato_soup", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.TOMATO_SOUP), 1));
    public static final RegistrySupplier<Item> MUSHROOM_SOUP = registerItem("mushroom_soup", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.MUSHROOM_SOUP), 1));
    public static final RegistrySupplier<Item> PASTA_WITH_MOZZARELLA = registerItem("pasta_with_mozzarella", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.PASTA), 2));
    public static final RegistrySupplier<Item> BOLOGNESE = registerItem("bolognese", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.BOLOGNESE), 1));
    public static final RegistrySupplier<Item> BEEF_WITH_MUSHROOM_IN_WINE_AND_POTATOES = registerItem("beef_with_mushroom_in_wine_and_potatoes", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.BEEF_WITH_MUSHROOM_IN_WINE_AND_POTATOES), 2));
    public static final RegistrySupplier<Item> PASTA_WITH_BOLOGNESE = registerItem("pasta_with_bolognese", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.HARVEST_PLATE), 1));
    public static final RegistrySupplier<Item> ROASTBEEF_WITH_GLAZED_CARROTS = registerItem("roastbeef_with_glazed_carrots", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.ROASTBEEF_WITH_GLAZED_CARROTS), 2));
    public static final RegistrySupplier<Item> ROASTED_LAMB_WITH_LETTUCE = registerItem("roasted_lamb_with_lettuce", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.ROASTED_LAMB_WITH_LETTUCE), 2));
    public static final RegistrySupplier<Item> FILLET_STEAK = registerItem("fillet_steak", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.FILLET_STEAK), 2));
    public static final RegistrySupplier<Item> TROPICAL_FISH_SUPREME = registerItem("tropical_fish_supreme", () -> new EffectFoodItem(getSettings().food(Foods.GOLDEN_CARROT), 1));
    public static final RegistrySupplier<Item> CHICKEN_ALFREDO = registerItem("chicken_alfredo", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.HARVEST_PLATE), 1));
    public static final RegistrySupplier<Item> SALMON_ON_WHITE_WINE = registerItem("salmon_on_white_wine", () -> new EffectFoodItem(getSettings().food(CandlelightFoods.SALMON_ON_WHITE_WINE), 2));
    public static final RegistrySupplier<Item> CHICKEN_WITH_VEGETABLES = registerItem("chicken_with_vegetables", () -> new EffectFoodItem(getSettings().food(Foods.GOLDEN_CARROT), 2));
    public static final RegistrySupplier<Block> FRESH_GARDEN_SALAD_BLOCK = registerWithoutItem("fresh_garden_salad_block", () -> new EffectFoodTrayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Candlelight.identifier("fresh_garden_salad_block"))), 4, new FoodProperties.Builder().nutrition(6).saturationModifier(0.9F).build()));
    public static final RegistrySupplier<Block> LASAGNE_BLOCK = registerWithoutItem("lasagne_block", () -> new CEffectFoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Candlelight.identifier("lasagne_block"))), 3, CandlelightFoods.LASAGNE));
    public static final RegistrySupplier<Block> BEEF_WELLINGTON_BLOCK = registerWithoutItem("beef_wellington_block", () -> new CEffectFoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Candlelight.identifier("beef_wellington_block"))), 2, CandlelightFoods.BEEF_WELLINGTON));
    public static final RegistrySupplier<Block> PORK_RIBS_BLOCK = registerWithoutItem("pork_ribs_block", () -> new CEffectFoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Candlelight.identifier("pork_ribs_block"))), 2, CandlelightFoods.PORK_RIBS));
    public static final RegistrySupplier<Block> TOMATO_MOZZARELLA_BLOCK = registerWithoutItem("tomato_mozzarella_block", () -> new EffectFoodTrayBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Candlelight.identifier("tomato_mozzarella_block"))), 4, new FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).build()));
    public static final RegistrySupplier<Item> PORK_RIBS = registerItem("pork_ribs", () -> new EffectFoodBlockItem(PORK_RIBS_BLOCK.get(), getSettings().food(CandlelightFoods.PORK_RIBS), 2));
    public static final RegistrySupplier<Item> LASAGNE = registerItem("lasagne", () -> new EffectFoodBlockItem(LASAGNE_BLOCK.get(), getSettings().food(CandlelightFoods.LASAGNE), 3));
    public static final RegistrySupplier<Item> BEEF_WELLINGTON = registerItem("beef_wellington", () -> new EffectFoodBlockItem(BEEF_WELLINGTON_BLOCK.get(), getSettings().food(CandlelightFoods.BEEF_WELLINGTON), 2));
    public static final RegistrySupplier<Item> CLOCHE = registerItem("cloche", () -> new Item(getSettings()));
    public static final RegistrySupplier<Item> NAPKIN = registerItem("napkin", () -> new Item(getSettings()));
    public static final RegistrySupplier<Item> MOZZARELLA = registerItem("mozzarella", () -> new Item(getSettings().food(Foods.BREAD)));
    public static final RegistrySupplier<Item> KHINKALI = registerItem("khinkali", () -> new Item(getSettings().food(Foods.GOLDEN_CARROT)));
    public static final RegistrySupplier<Item> BEETROOT_SALAD = registerItem("beetroot_salad", () -> new EffectItem(getFoodItemSettings(5, 0.6f, MobEffectRegistry.REFRESHED.get(), 2400), 2400, true));
    public static final RegistrySupplier<Item> CHICKEN_TERIYAKI = registerItem("chicken_teriyaki", () -> new EffectItem(getFoodItemSettings(8, 0.8f, MobEffectRegistry.WELL_SERVED.get(), 6000), 6000, true));
    public static final RegistrySupplier<Item> SALAD = registerItem("salad", () -> new EffectItem(getFoodItemSettings(5, 0.7f, MobEffectRegistry.REFRESHED.get(), 3600), 3600, true));
    public static final RegistrySupplier<Item> BEEF_TARTARE = registerItem("beef_tartare", () -> new EffectItem(getFoodItemSettings(8, 0.9f, MobEffectRegistry.WELL_SERVED.get(), 3000), 3000, true));
    public static final RegistrySupplier<Item> PASTA_WITH_LETTUCE = registerItem("pasta_with_lettuce", () -> new EffectItem(getFoodItemSettings(8, 0.9f, MobEffectRegistry.REFRESHED.get(), 3600), 3600, true));
    public static final RegistrySupplier<Item> OMELET = registerItem("omelet", () -> new EffectItem(getFoodItemSettings(8, 0.4f, MobEffectRegistry.WELL_SERVED.get(), 4800), 4800, true));
    public static final RegistrySupplier<Item> HARVEST_PLATE = registerItem("harvest_plate", () -> new EffectItem(getFoodItemSettings(7, 0.8f, MobEffectRegistry.REFRESHED.get(), 4800), 4800, true));
    public static final RegistrySupplier<Item> CHOCOLATE_MOUSSE = registerItem("chocolate_mousse", () -> new EffectItem(getFoodItemSettings(4, 0.3f, MobEffectRegistry.WELL_SERVED.get(), 2400), 2400, true));
    public static final RegistrySupplier<Item> GOLD_RING = registerItem("gold_ring", () -> new RingItem(ArmorMaterialRegistry.JEWELRY, ArmorType.CHESTPLATE, getSettings().rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> COOKING_HAT = registerItem("cooking_hat", () -> new CandlelightHatItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/cooking_hat.png")), ArmorType.HELMET, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/cooking_hat.png")));
    public static final RegistrySupplier<Item> CHEFS_JACKET = registerItem("chefs_jacket", () -> new CandlelightChestItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/cook.png")), ArmorType.CHESTPLATE, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/cook.png")));
    public static final RegistrySupplier<Item> CHEFS_PANTS = registerItem("chefs_pants", () -> new CandlelightLegsItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/cook.png")), ArmorType.LEGGINGS, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/cook.png")));
    public static final RegistrySupplier<Item> CHEFS_BOOTS = registerItem("chefs_boots", () -> new CandlelightBootsItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/cook.png")), ArmorType.BOOTS, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/cook.png")));
    public static final RegistrySupplier<Item> FLOWER_CROWN = registerItem("flower_crown", () -> new CandlelightHatItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/flower_crown.png")), ArmorType.HELMET, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/flower_crown.png")));
    public static final RegistrySupplier<Item> DRESS = registerItem("dress", () -> new DyeableCandlelightArmorItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/dress.png")), ArmorType.CHESTPLATE, 16744576, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/dress.png")));
    public static final RegistrySupplier<Item> SHIRT = registerItem("shirt", () -> new CandlelightChestItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/shirt.png")), ArmorType.CHESTPLATE, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/shirt.png")));
    public static final RegistrySupplier<Item> FORMAL_SHIRT = registerItem("formal_shirt", () -> new CandlelightChestItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/formal_shirt.png")), ArmorType.CHESTPLATE, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/formal_shirt.png")));
    public static final RegistrySupplier<Item> TROUSERS_AND_VEST = registerItem("trousers_and_vest", () -> new DyeableCandlelightArmorItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/suit.png")), ArmorType.LEGGINGS, 0x333399, getSettings().rarity(Rarity.UNCOMMON), Candlelight.identifier("textures/models/armor/suit.png")));
    public static final RegistrySupplier<Item> NECKTIE = registerItem("necktie", () -> new CandlelightHatItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Candlelight.identifier("textures/models/armor/tie.png")), ArmorType.HELMET, getSettings().rarity(Rarity.COMMON), Candlelight.identifier("textures/models/armor/tie.png")));
    public static final RegistrySupplier<Item> NOTE_PAPER_WRITEABLE = registerItem("note_paper_writeable", () -> new WriteablePaperItem(getSettings().stacksTo(1)));
    public static final RegistrySupplier<Item> NOTE_PAPER_WRITTEN = registerItem("note_paper_written", () -> new WrittenPaperItem(getSettingsWithoutTab()));
    public static final RegistrySupplier<Item> LETTER_OPEN = registerItem("letter_open", () -> new LetterItem(getSettings()));
    public static final RegistrySupplier<Item> LETTER_CLOSED = registerItem("letter_closed", () -> new ClosedLetterItem(getSettingsWithoutTab().stacksTo(1)));
    public static final RegistrySupplier<Item> LOVE_LETTER_OPEN = registerItem("love_letter_open", () -> new LetterItem(getSettings()));
    public static final RegistrySupplier<Item> LOVE_LETTER_CLOSED = registerItem("love_letter", () -> new ClosedLetterItem(getSettingsWithoutTab().stacksTo(1)));
    public static final RegistrySupplier<Block> CANDLELIGHT_BANNER = registerWithItem("candlelight_banner", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(Candlelight.identifier("candlelight_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> CANDLELIGHT_WALL_BANNER = registerWithoutItem("candlelight_wall_banner", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(Candlelight.identifier("candlelight_wall_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> FLOORBOARD = registerWithItem("floorboard", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("floorboard")))));
    public static final RegistrySupplier<Block> DRAWER = registerWithItem("drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> CABINET = registerWithItem("cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> SIDEBOARD = registerWithItem("sideboard", () -> new SideBoardBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("sideboard"))).strength(2.5f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> CHAIR = registerWithItem("chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SOFA = registerWithItem("sofa", () -> new SofaBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("sofa"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> TABLE = registerWithItem("table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("table")))));
    public static final RegistrySupplier<Block> LAMP = registerWithItem("lamp", () -> new LampBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN).setId(blockKey(Candlelight.identifier("lamp"))).lightLevel(s -> s.getValue(LampBlock.LUMINANCE) ? 15 : 0).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SIDE_TABLE = registerWithItem("side_table", () -> new SideTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("side_table")))));
    public static final RegistrySupplier<Block> DINNER_BELL = registerWithItem("dinner_bell", () -> new DinnerBellBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(Candlelight.identifier("dinner_bell"))).noOcclusion()));
    public static final RegistrySupplier<Block> COOKING_POT = registerWithItem("cooking_pot", () -> new LargeCookingPotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(Candlelight.identifier("cooking_pot"))).noOcclusion()));
    public static final RegistrySupplier<Block> COOKING_PAN = registerWithoutItem("cooking_pan", () -> new CookingPanBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(Candlelight.identifier("cooking_pan")))));
    public static final RegistrySupplier<Item> COOKING_PAN_ITEM = registerItem("cooking_pan", () -> new CookingPanItem(COOKING_PAN.get(), getSettings().attributes(CookingPanItem.createAttributes())));
    public static final RegistrySupplier<Block> TABLE_SET = registerWithoutItem("table_set", () -> new TableSetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Candlelight.identifier("table_set")))));
    public static final RegistrySupplier<Item> PLATE = registerItem("plate", () -> new TableSetBlockItem(TABLE_SET.get(), getSettings(), TableSetBlock.PlateType.PLATE));
    public static final RegistrySupplier<Item> BOWL = registerItem("bowl", () -> new TableSetBlockItem(TABLE_SET.get(), getSettings(), TableSetBlock.PlateType.BOWL));
    public static final RegistrySupplier<Block> GLASS_BLOCK = registerWithoutItem("glass", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Candlelight.identifier("glass"))).noTerrainParticles(), 4));
    public static final RegistrySupplier<Item> GLASS = registerItem("glass", () -> new BlockItem(GLASS_BLOCK.get(), getSettings()));
    public static final RegistrySupplier<Block> WINE_GLASS_BLOCK = registerWithoutItem("wine_glass", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(Candlelight.identifier("wine_glass"))).noTerrainParticles(), 4));
    public static final RegistrySupplier<Item> WINE_GLASS = registerItem("wine_glass", () -> new BlockItem(WINE_GLASS_BLOCK.get(), getSettings()));
    public static final RegistrySupplier<Item> FRESH_GARDEN_SALAD = registerItem("fresh_garden_salad", () -> new EffectBlockItem(FRESH_GARDEN_SALAD_BLOCK.get(), getFoodItemSettings(6, 0.9f, MobEffectRegistry.WELL_SERVED.get(), 3600)));
    public static final RegistrySupplier<Item> TOMATO_MOZZARELLA_SALAD = registerItem("tomato_mozzarella_salad", () -> new EffectBlockItem(TOMATO_MOZZARELLA_BLOCK.get(), getFoodItemSettings(5, 0.7f, MobEffectRegistry.WELL_SERVED.get(), 4800)));
    public static final RegistrySupplier<Block> TABLE_SIGN = registerWithItem("table_sign", () -> new BoardBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Candlelight.identifier("table_sign")))));
    public static final RegistrySupplier<Block> PAINTING = registerWithItem("painting", () -> new SmallPaintingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Candlelight.identifier("painting"))).noCollision()));
    public static final RegistrySupplier<Block> HEART = registerWithItem("heart", () -> new WallDecorationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Candlelight.identifier("heart"))).noCollision()));
    public static final RegistrySupplier<Block> ROSE = registerWithItem("rose", () -> new BonemealableFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(Candlelight.identifier("rose")))));
    public static final RegistrySupplier<Block> POTTED_ROSE = registerWithoutItem("potted_rose", () -> new FlowerPotBlock(ROSE.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_POPPY).setId(blockKey(Candlelight.identifier("potted_rose")))));
    public static final RegistrySupplier<Block> JEWELRY_BOX = registerWithItem("jewelry_box", () -> new JewelryBoxBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Candlelight.identifier("jewelry_box")))));
    public static final RegistrySupplier<Block> CHOCOLATE_BOX = registerWithItem("chocolate_box", () -> new EatableBoxBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Candlelight.identifier("chocolate_box")))));
    public static final RegistrySupplier<Block> TYPEWRITER_IRON = registerWithItem("typewriter_iron", () -> new TypewriterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(Candlelight.identifier("typewriter_iron"))).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> TYPEWRITER_GOLD = registerWithItem("typewriter_gold", () -> new TypewriterBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_BLOCK).setId(blockKey(Candlelight.identifier("typewriter_gold"))).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> NOTE_PAPER_BLOCK = registerWithoutItem("note_paper", () -> new StackableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DRIED_KELP_BLOCK).setId(blockKey(Candlelight.identifier("note_paper"))), 8));
    public static final RegistrySupplier<Item> NOTE_PAPER = registerItem("note_paper", () -> new BlockItem(NOTE_PAPER_BLOCK.get(), getSettings()));
    public static final RegistrySupplier<Block> COBBLESTONE_STOVE = registerWithItem("cobblestone_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("cobblestone_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> COBBLESTONE_KITCHEN_SINK = registerWithItem("cobblestone_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("cobblestone_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> COBBLESTONE_COUNTER = registerWithItem("cobblestone_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("cobblestone_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> OAK_CABINET = registerWithItem("oak_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("oak_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> OAK_DRAWER = registerWithItem("oak_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("oak_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> OAK_TABLE = registerWithItem("oak_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("oak_table")))));
    public static final RegistrySupplier<Block> OAK_CHAIR = registerWithItem("oak_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("oak_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> OAK_SHELF = registerWithItem("oak_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("oak_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> OAK_BIG_TABLE = registerWithItem("oak_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("oak_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> SANDSTONE_STOVE = registerWithItem("sandstone_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("sandstone_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> SANDSTONE_KITCHEN_SINK = registerWithItem("sandstone_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("sandstone_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> SANDSTONE_COUNTER = registerWithItem("sandstone_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("sandstone_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> BIRCH_CABINET = registerWithItem("birch_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("birch_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> BIRCH_DRAWER = registerWithItem("birch_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("birch_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> BIRCH_TABLE = registerWithItem("birch_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("birch_table")))));
    public static final RegistrySupplier<Block> BIRCH_CHAIR = registerWithItem("birch_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("birch_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BIRCH_SHELF = registerWithItem("birch_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("birch_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> BIRCH_BIG_TABLE = registerWithItem("birch_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("birch_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> STONE_BRICKS_STOVE = registerWithItem("stone_bricks_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("stone_bricks_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> STONE_BRICKS_KITCHEN_SINK = registerWithItem("stone_bricks_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("stone_bricks_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> STONE_BRICKS_COUNTER = registerWithItem("stone_bricks_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("stone_bricks_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> SPRUCE_CABINET = registerWithItem("spruce_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("spruce_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> SPRUCE_DRAWER = registerWithItem("spruce_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("spruce_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> SPRUCE_TABLE = registerWithItem("spruce_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("spruce_table")))));
    public static final RegistrySupplier<Block> SPRUCE_CHAIR = registerWithItem("spruce_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("spruce_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> SPRUCE_SHELF = registerWithItem("spruce_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("spruce_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> SPRUCE_BIG_TABLE = registerWithItem("spruce_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("spruce_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> DEEPSLATE_STOVE = registerWithItem("deepslate_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("deepslate_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> DEEPSLATE_KITCHEN_SINK = registerWithItem("deepslate_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("deepslate_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> DEEPSLATE_COUNTER = registerWithItem("deepslate_counter", () -> new FacingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("deepslate_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> DARK_OAK_CABINET = registerWithItem("dark_oak_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("dark_oak_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> DARK_OAK_DRAWER = registerWithItem("dark_oak_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("dark_oak_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> DARK_OAK_TABLE = registerWithItem("dark_oak_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_PLANKS).setId(blockKey(Candlelight.identifier("dark_oak_table")))));
    public static final RegistrySupplier<Block> DARK_OAK_CHAIR = registerWithItem("dark_oak_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("dark_oak_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> DARK_OAK_SHELF = registerWithItem("dark_oak_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("dark_oak_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> DARK_OAK_BIG_TABLE = registerWithItem("dark_oak_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("dark_oak_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> GRANITE_STOVE = registerWithItem("granite_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("granite_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> GRANITE_KITCHEN_SINK = registerWithItem("granite_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("granite_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> GRANITE_COUNTER = registerWithItem("granite_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("granite_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> ACACIA_CABINET = registerWithItem("acacia_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("acacia_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> ACACIA_DRAWER = registerWithItem("acacia_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("acacia_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> ACACIA_TABLE = registerWithItem("acacia_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_PLANKS).setId(blockKey(Candlelight.identifier("acacia_table")))));
    public static final RegistrySupplier<Block> ACACIA_CHAIR = registerWithItem("acacia_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("acacia_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> ACACIA_SHELF = registerWithItem("acacia_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("acacia_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> ACACIA_BIG_TABLE = registerWithItem("acacia_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("acacia_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> END_STOVE = registerWithItem("end_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("end_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> END_KITCHEN_SINK = registerWithItem("end_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("end_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> END_COUNTER = registerWithItem("end_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("end_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> JUNGLE_CABINET = registerWithItem("jungle_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("jungle_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> JUNGLE_DRAWER = registerWithItem("jungle_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("jungle_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> JUNGLE_TABLE = registerWithItem("jungle_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("jungle_table")))));
    public static final RegistrySupplier<Block> JUNGLE_CHAIR = registerWithItem("jungle_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("jungle_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> JUNGLE_SHELF = registerWithItem("jungle_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("jungle_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> JUNGLE_BIG_TABLE = registerWithItem("jungle_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("jungle_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> MUD_STOVE = registerWithItem("mud_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("mud_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> MUD_KITCHEN_SINK = registerWithItem("mud_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("mud_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> MUD_COUNTER = registerWithItem("mud_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("mud_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> MANGROVE_CABINET = registerWithItem("mangrove_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("mangrove_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> MANGROVE_DRAWER = registerWithItem("mangrove_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("mangrove_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> MANGROVE_TABLE = registerWithItem("mangrove_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("mangrove_table")))));
    public static final RegistrySupplier<Block> MANGROVE_CHAIR = registerWithItem("mangrove_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("mangrove_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> MANGROVE_SHELF = registerWithItem("mangrove_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("mangrove_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> MANGROVE_BIG_TABLE = registerWithItem("mangrove_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("mangrove_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> QUARTZ_STOVE = registerWithItem("quartz_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("quartz_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> QUARTZ_KITCHEN_SINK = registerWithItem("quartz_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("quartz_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> QUARTZ_COUNTER = registerWithItem("quartz_counter", () -> new FacingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("quartz_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> WARPED_CABINET = registerWithItem("warped_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("warped_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> WARPED_DRAWER = registerWithItem("warped_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("warped_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> WARPED_TABLE = registerWithItem("warped_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("warped_table")))));
    public static final RegistrySupplier<Block> WARPED_CHAIR = registerWithItem("warped_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("warped_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> WARPED_SHELF = registerWithItem("warped_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("warped_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> WARPED_BIG_TABLE = registerWithItem("warped_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("warped_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> RED_NETHER_BRICKS_STOVE = registerWithItem("red_nether_bricks_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(Candlelight.identifier("red_nether_bricks_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> RED_NETHER_BRICKS_KITCHEN_SINK = registerWithItem("red_nether_bricks_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("red_nether_bricks_kitchen_sink"))).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> RED_NETHER_BRICKS_COUNTER = registerWithItem("red_nether_bricks_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("red_nether_bricks_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> CRIMSON_CABINET = registerWithItem("crimson_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("crimson_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> CRIMSON_DRAWER = registerWithItem("crimson_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("crimson_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> CRIMSON_TABLE = registerWithItem("crimson_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("crimson_table")))));
    public static final RegistrySupplier<Block> CRIMSON_CHAIR = registerWithItem("crimson_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("crimson_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> CRIMSON_SHELF = registerWithItem("crimson_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Candlelight.identifier("crimson_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> CRIMSON_BIG_TABLE = registerWithItem("crimson_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).setId(blockKey(Candlelight.identifier("crimson_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> BASALT_STOVE = registerWithItem("basalt_stove", () -> new CStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT).setId(blockKey(Candlelight.identifier("basalt_stove"))).lightLevel(s -> 12)));
    public static final RegistrySupplier<Block> BASALT_KITCHEN_SINK = registerWithItem("basalt_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT).setId(blockKey(Candlelight.identifier("basalt_kitchen_sink"))).noOcclusion()));
    public static final RegistrySupplier<Block> BASALT_COUNTER = registerWithItem("basalt_counter", () -> new LineConnectingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BASALT).setId(blockKey(Candlelight.identifier("basalt_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> CHERRY_CABINET = registerWithItem("cherry_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS).setId(blockKey(Candlelight.identifier("cherry_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> CHERRY_DRAWER = registerWithItem("cherry_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS).setId(blockKey(Candlelight.identifier("cherry_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> CHERRY_TABLE = registerWithItem("cherry_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS).setId(blockKey(Candlelight.identifier("cherry_table")))));
    public static final RegistrySupplier<Block> CHERRY_CHAIR = registerWithItem("cherry_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS).setId(blockKey(Candlelight.identifier("cherry_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> CHERRY_SHELF = registerWithItem("cherry_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS).setId(blockKey(Candlelight.identifier("cherry_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> CHERRY_BIG_TABLE = registerWithItem("cherry_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CHERRY_PLANKS).setId(blockKey(Candlelight.identifier("cherry_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> BAMBOO_STOVE = registerWithItem("bamboo_stove", () -> new BambooStoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_stove"))).lightLevel(s -> 10)));
    public static final RegistrySupplier<Block> BAMBOO_KITCHEN_SINK = registerWithItem("bamboo_kitchen_sink", () -> new SinkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_kitchen_sink"))).noOcclusion()));
    public static final RegistrySupplier<Block> BAMBOO_COUNTER = registerWithItem("bamboo_counter", () -> new FacingBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_counter"))).noOcclusion()));
    public static final RegistrySupplier<Block> BAMBOO_CABINET = registerWithItem("bamboo_cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> BAMBOO_DRAWER = registerWithItem("bamboo_drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> BAMBOO_TABLE = registerWithItem("bamboo_table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_table")))));
    public static final RegistrySupplier<Block> BAMBOO_CHAIR = registerWithItem("bamboo_chair", () -> new ChairBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_chair"))).strength(2.0f, 3.0f).sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BAMBOO_SHELF = registerWithItem("bamboo_shelf", () -> new ShelfBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_shelf"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> BAMBOO_BIG_TABLE = registerWithItem("bamboo_big_table", () -> new LargeTableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BAMBOO_PLANKS).setId(blockKey(Candlelight.identifier("bamboo_big_table"))).strength(2.0F, 2.0F).pushReaction(PushReaction.IGNORE)));

    public static void init() {
        ITEMS.register();
        BLOCKS.register();
    }

    private static net.minecraft.resources.ResourceKey<Item> itemKey(Identifier id) {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ITEM, id);
    }
    private static net.minecraft.resources.ResourceKey<Block> blockKey(Identifier id) {
        return net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.BLOCK, id);
    }

    private static Identifier currentId;

    private static Item.Properties getSettings(Consumer<Item.Properties> consumer) {
        Item.Properties settings = new Item.Properties();
        if (currentId != null) {
            settings.setId(itemKey(currentId));
        }
        consumer.accept(settings);
        return settings;
    }

    private static Item.Properties getSettingsWithoutTab(Consumer<Item.Properties> consumer) {
        Item.Properties settings = new Item.Properties();
        if (currentId != null) {
            settings.setId(itemKey(currentId));
        }
        consumer.accept(settings);
        return settings;
    }

    private static Item.Properties getSettings() {
        return getSettings(settings -> {
        });
    }

    private static Item.Properties getSettingsWithoutTab() {
        return getSettingsWithoutTab(settings -> {
        });
    }

    private static Item.Properties getFoodItemSettings(int nutrition, float saturationModifier, MobEffect effect, int duration) {
        return getFoodItemSettings(nutrition, saturationModifier, effect, duration, true, false);
    }

    @SuppressWarnings("all")
    private static Item.Properties getFoodItemSettings(int nutrition, float saturationModifier, MobEffect effect, int duration, boolean alwaysEat, boolean fast) {
        Item.Properties props = getSettings().food(createFood(nutrition, saturationModifier, alwaysEat));
        if (effect != null) {
            java.util.List<net.minecraft.world.effect.MobEffectInstance> effects = java.util.Arrays.asList(
                new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration)
            );
            net.minecraft.world.item.component.Consumable consumable = net.minecraft.world.item.component.Consumable.builder()
                .onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(effects))
                .build();
            props.component(net.minecraft.core.component.DataComponents.CONSUMABLE, consumable);
        }
        return props;
    }

    private static FoodProperties createFood(int nutrition, float saturationModifier, boolean alwaysEat) {
        FoodProperties.Builder food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturationModifier);
        if (alwaysEat) food.alwaysEdible();
        return food.build();
    }

    private static BlockBehaviour.Properties getLogBlockSettings() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2.0F).sound(SoundType.WOOD);
    }

    private static BlockBehaviour.Properties getWineSettings() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).noOcclusion().instabreak();
    }

    public static <T extends Block> RegistrySupplier<T> registerWithItem(String name, Supplier<T> block) {
        return GeneralUtil.registerWithItem(BLOCKS, BLOCK_REGISTRAR, ITEMS, ITEM_REGISTRAR, Candlelight.identifier(name), block);
    }

    public static <T extends Block> RegistrySupplier<T> registerWithoutItem(String path, Supplier<T> block) {
        return GeneralUtil.registerWithoutItem(BLOCKS, BLOCK_REGISTRAR, Candlelight.identifier(path), block);
    }

    public static <T extends Item> RegistrySupplier<T> registerItem(String path, Supplier<T> itemSupplier) {
        Identifier id = Candlelight.identifier(path);
        currentId = id;
        try {
            return GeneralUtil.registerItem(ITEMS, ITEM_REGISTRAR, id, itemSupplier);
        } finally {
            currentId = null;
        }
    }
}
