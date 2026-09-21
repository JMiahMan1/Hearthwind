package net.satisfy.brewery.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;
import net.satisfy.brewery.Brewery;
import net.satisfy.brewery.core.block.*;
import net.satisfy.brewery.core.block.property.BrewMaterial;
import net.satisfy.brewery.core.item.*;
import net.satisfy.farm_and_charm.core.block.BenchBlock;
import net.satisfy.farm_and_charm.core.block.BonemealableFlowerBlock;
import net.satisfy.farm_and_charm.core.block.BonemealableTallFlowerBlock;
import net.satisfy.farm_and_charm.core.block.FoodBlock;
import net.satisfy.farm_and_charm.core.item.food.EffectBlockItem;
import net.satisfy.farm_and_charm.core.item.food.EffectItem;
import net.satisfy.farm_and_charm.core.registry.ArmorMaterialRegistry;
import net.satisfy.farm_and_charm.core.util.GeneralUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Brewery.MOD_ID, Registries.BLOCK);
    public static final Registrar<Block> BLOCK_REGISTRAR = BLOCKS.getRegistrar();
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Brewery.MOD_ID, Registries.ITEM);
    public static final Registrar<Item> ITEM_REGISTRAR = ITEMS.getRegistrar();

    public static final RegistrySupplier<Item> BREATHALYZER = registerItem("breathalyzer", () -> new BreathalyzerItem(getSettings(Brewery.identifier("breathalyzer"))));
    public static final RegistrySupplier<Item> DARK_BREW = registerItem("dark_brew", () -> new DarkBrewItem(getSettings(Brewery.identifier("dark_brew"))));
    public static final RegistrySupplier<Item> SAUSAGE = registerItem("sausage", () -> new EffectItem(getFoodItemSettings(Brewery.identifier("sausage"), 6, 0.5f, MobEffectRegistry.STOUTHEART, 6000), 6000, true));
    public static final RegistrySupplier<Item> PRETZEL = registerItem("pretzel", () -> new EffectItem(getFoodItemSettings(Brewery.identifier("pretzel"), 3, 0.4f, MobEffectRegistry.STOUTHEART, 2000), 2000, false));
    // 26.2: lazy ITEMS.register (entity types register before items); the eager registrar path resolves too early.
    public static final RegistrySupplier<Item> BEER_ELEMENTAL_SPAWN_EGG = ITEMS.register("beer_elemental_spawn_egg", () -> new SpawnEggItem(getSettings(Brewery.identifier("beer_elemental_spawn_egg")).spawnEgg(EntityTypeRegistry.BEER_ELEMENTAL.get())));
    public static final RegistrySupplier<Item> BREWFEST_HAT = registerItem("brewfest_hat", () -> new BrewfestHatItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Brewery.identifier("models/armor/brewfest_hat")), ArmorType.HELMET, getSettings(Brewery.identifier("brewfest_hat")).rarity(Rarity.EPIC).durability(ArmorType.HELMET.getDurability(5)), Brewery.identifier("models/armor/brewfest_hat")));
    public static final RegistrySupplier<Item> BREWFEST_HAT_RED = registerItem("brewfest_hat_red", () -> new BrewfestHatItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Brewery.identifier("models/armor/brewfest_hat_red")), ArmorType.HELMET, getSettings(Brewery.identifier("brewfest_hat_red")).rarity(Rarity.EPIC).durability(ArmorType.HELMET.getDurability(5)), Brewery.identifier("models/armor/brewfest_hat_red")));
    public static final RegistrySupplier<Item> BREWFEST_REGALIA = registerItem("brewfest_regalia", () -> new BrewfestChestItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Brewery.identifier("models/armor/lederhosen")), ArmorType.CHESTPLATE, getSettings(Brewery.identifier("brewfest_regalia")).rarity(Rarity.EPIC).durability(ArmorType.CHESTPLATE.getDurability(5)), Brewery.identifier("models/armor/lederhosen")));

    public static final RegistrySupplier<Item> BREWFEST_TROUSERS = registerItem("brewfest_trousers", () -> new BrewfestLegsItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Brewery.identifier("models/armor/lederhosen")), ArmorType.LEGGINGS, getSettings(Brewery.identifier("brewfest_trousers")).rarity(Rarity.EPIC).durability(ArmorType.LEGGINGS.getDurability(5)), Brewery.identifier("models/armor/lederhosen")));
    public static final RegistrySupplier<Item> BREWFEST_BOOTS = registerItem("brewfest_boots", () -> new BrewfestBootsItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Brewery.identifier("models/armor/lederhosen")), ArmorType.BOOTS, getSettings(Brewery.identifier("brewfest_boots")).rarity(Rarity.RARE).durability(ArmorType.BOOTS.getDurability(5)), Brewery.identifier("models/armor/lederhosen")));
    public static final RegistrySupplier<Item> BREWFEST_DRESS = registerItem("brewfest_dress", () -> new BrewfestLegsItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Brewery.identifier("models/armor/dirndl")), ArmorType.LEGGINGS, getSettings(Brewery.identifier("brewfest_dress")).rarity(Rarity.RARE).durability(ArmorType.LEGGINGS.getDurability(5)), Brewery.identifier("models/armor/dirndl")));
    public static final RegistrySupplier<Item> BREWFEST_BLOUSE = registerItem("brewfest_blouse", () -> new BrewfestChestItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Brewery.identifier("models/armor/dirndl")), ArmorType.CHESTPLATE, getSettings(Brewery.identifier("brewfest_blouse")).rarity(Rarity.EPIC).durability(ArmorType.CHESTPLATE.getDurability(5)), Brewery.identifier("models/armor/dirndl")));
    public static final RegistrySupplier<Item> BREWFEST_SHOES = registerItem("brewfest_shoes", () -> new BrewfestBootsItem(ArmorMaterialRegistry.withTextureNoOverlay(ArmorMaterialRegistry.CLOTH, Brewery.identifier("models/armor/dirndl")), ArmorType.BOOTS, getSettings(Brewery.identifier("brewfest_shoes")).rarity(Rarity.RARE).durability(ArmorType.BOOTS.getDurability(5)), Brewery.identifier("models/armor/dirndl")));
    public static final RegistrySupplier<Block> WILD_HOPS = registerWithItem("wild_hops", () -> new BonemealableTallFlowerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ROSE_BUSH).setId(blockKey(Brewery.identifier("wild_hops")))));
    public static final RegistrySupplier<Block> HOPS_CROP = registerWithoutItem("hops_crop", () -> new HopsCropHeadBlock(getBushSettings(Brewery.identifier("hops_crop")).randomTicks()));
    public static final RegistrySupplier<Block> HOPS_CROP_BODY = registerWithoutItem("hops_crop_body", () -> new HopsCropBodyBlock(getBushSettings(Brewery.identifier("hops_crop_body")).randomTicks()));
    public static final RegistrySupplier<Item> HOPS = registerItem("hops", () -> new BlockItem(HOPS_CROP.get(), getSettings(Brewery.identifier("hops"))));

    public static final RegistrySupplier<Block> DRIED_WHEAT = registerWithItem("dried_wheat", () -> new BagBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.RED)).setId(blockKey(Brewery.identifier("dried_wheat")))));
    public static final RegistrySupplier<Block> DRIED_BARLEY = registerWithItem("dried_barley", () -> new BagBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.RED)).setId(blockKey(Brewery.identifier("dried_barley")))));
    public static final RegistrySupplier<Block> DRIED_CORN = registerWithItem("dried_corn", () -> new BagBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.RED)).setId(blockKey(Brewery.identifier("dried_corn")))));
    public static final RegistrySupplier<Block> DRIED_OAT = registerWithItem("dried_oat", () -> new BagBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.RED)).setId(blockKey(Brewery.identifier("dried_oat")))));
    public static final RegistrySupplier<Block> BENCH = registerWithItem("bench", () -> new BenchBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Brewery.identifier("bench")))));
    public static final RegistrySupplier<Block> TABLE = registerWithItem("table", () -> new TableBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Brewery.identifier("table")))));
    public static final RegistrySupplier<Block> PATTERNED_WOOL = registerWithItem("patterned_wool", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(DyeColor.BLACK)).setId(blockKey(Brewery.identifier("patterned_wool")))));
    public static final RegistrySupplier<Block> PATTERNED_CARPET_BLOCK = registerWithItem("patterned_carpet_block", () -> new CarpetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CARPET.pick(DyeColor.BLACK)).setId(blockKey(Brewery.identifier("patterned_carpet_block")))));
    public static final RegistrySupplier<Item> PATTERNED_CARPET = registerItem("patterned_carpet", () -> new BlockItem(PATTERNED_CARPET_BLOCK.get(), getSettings(Brewery.identifier("patterned_carpet"))));
    public static final RegistrySupplier<Block> CABINET = registerWithItem("cabinet", () -> new CabinetBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> DRAWER = registerWithItem("drawer", () -> new CabinetBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Brewery.identifier("drawer"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.DRAWER_OPEN.get(), SoundEventRegistry.DRAWER_CLOSE.get()));
    public static final RegistrySupplier<Block> BAR_COUNTER = registerWithItem("bar_counter", () -> new BarCounterBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("bar_counter"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> SIDEBOARD = registerWithItem("sideboard", () -> new SideBoardBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("sideboard"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN, SoundEventRegistry.CABINET_CLOSE));
    public static final RegistrySupplier<Block> WALL_CABINET = registerWithItem("wall_cabinet", () -> new CabinetWallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Brewery.identifier("wall_cabinet"))).strength(2.0F, 3.0F).sound(SoundType.WOOD), SoundEventRegistry.CABINET_OPEN.get(), SoundEventRegistry.CABINET_CLOSE.get()));
    public static final RegistrySupplier<Block> WOODEN_BREWINGSTATION = registerWithItem("wooden_brewingstation", () -> new BrewKettleBlock(BrewMaterial.WOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).setId(blockKey(Brewery.identifier("wooden_brewingstation"))).pushReaction(PushReaction.BLOCK)));
    public static final RegistrySupplier<Block> COPPER_BREWINGSTATION = registerWithItem("copper_brewingstation", () -> new BrewKettleBlock(BrewMaterial.COPPER, BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().pick(WeatheringCopper.WeatherState.UNAFFECTED)).setId(blockKey(Brewery.identifier("copper_brewingstation"))).pushReaction(PushReaction.BLOCK)));
    public static final RegistrySupplier<Block> NETHERITE_BREWINGSTATION = registerWithItem("netherite_brewingstation", () -> new BrewKettleBlock(BrewMaterial.NETHERITE, BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).setId(blockKey(Brewery.identifier("netherite_brewingstation"))).pushReaction(PushReaction.BLOCK)));
    public static final RegistrySupplier<Block> BREW_WHISTLE = registerWithoutItem("brew_whistle", () -> new BrewWhistleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().pick(WeatheringCopper.WeatherState.UNAFFECTED)).setId(blockKey(Brewery.identifier("brew_whistle"))).pushReaction(PushReaction.BLOCK)));
    public static final RegistrySupplier<Block> BREW_OVEN = registerWithoutItem("brew_oven", () -> new BrewOvenBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().pick(WeatheringCopper.WeatherState.UNAFFECTED)).setId(blockKey(Brewery.identifier("brew_oven"))).pushReaction(PushReaction.BLOCK)));
    public static final RegistrySupplier<Block> BREW_TIMER = registerWithoutItem("brew_timer", () -> new BrewTimerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_BLOCK.weathering().pick(WeatheringCopper.WeatherState.UNAFFECTED)).setId(blockKey(Brewery.identifier("brew_timer"))).pushReaction(PushReaction.BLOCK)));
    public static final RegistrySupplier<Block> BARREL_MAIN = registerWithItem("barrel_main", () -> new BigBarrelMainBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("barrel_main"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.IGNORE)));
    public static final RegistrySupplier<Block> BARREL_MAIN_HEAD = registerWithoutItem("barrel_main_head", () -> new BigBarrelMainHeadBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("barrel_main_head"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.IGNORE).noLootTable()));
    public static final RegistrySupplier<Block> BARREL_RIGHT = registerWithoutItem("barrel_right", () -> new BigBarrelRightBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("barrel_right"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.IGNORE).noLootTable()));
    public static final RegistrySupplier<Block> BARREL_HEAD_RIGHT = registerWithoutItem("barrel_head_right", () -> new BigBarrelRightHeadBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("barrel_head_right"))).strength(2.0F, 3.0F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.IGNORE).noLootTable()));
    public static final RegistrySupplier<Block> BEER_MUG = registerWithItem("beer_mug", () -> new BeerMugFlowerPotBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).setId(blockKey(Brewery.identifier("beer_mug")))));
    public static final RegistrySupplier<Block> BEER_WHEAT = registerWithItemeverage("beer_wheat", () -> new BeverageBlock(getMugSettings(Brewery.identifier("beer_wheat")), 2), MobEffectRegistry.SNOWWHITE);
    public static final RegistrySupplier<Block> BEER_HOPS = registerWithItemeverage("beer_hops", () -> new BeverageBlock(getMugSettings(Brewery.identifier("beer_hops")), 2), MobEffectRegistry.PARTYSTARTER);
    public static final RegistrySupplier<Block> BEER_BARLEY = registerWithItemeverage("beer_barley", () -> new BeverageBlock(getMugSettings(Brewery.identifier("beer_barley")), 2), MobEffectRegistry.PINTCHARISMA);
    public static final RegistrySupplier<Block> BEER_OAT = registerWithItemeverage("beer_oat", () -> new BeverageBlock(getMugSettings(Brewery.identifier("beer_oat")), 2), MobEffectRegistry.MINING);
    public static final RegistrySupplier<Block> BEER_NETTLE = registerWithItemeverage("beer_nettle", () -> new BeverageBlock(getMugSettings(Brewery.identifier("beer_nettle")), 2), MobEffectRegistry.PACIFY);
    public static final RegistrySupplier<Block> BEER_HALEY = registerWithItemeverage("beer_haley", () -> new BeverageBlock(getMugSettings(Brewery.identifier("beer_haley")), 2), MobEffectRegistry.HALEY);
    public static final RegistrySupplier<Block> WHISKEY_MAGGOALLAN = registerWithItemeverage("whiskey_maggoallan", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_maggoallan")), 1), MobEffectRegistry.HEALINGTOUCH);
    public static final RegistrySupplier<Block> WHISKEY_CARRASCONLABEL = registerWithItemeverage("whiskey_carrasconlabel", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_carrasconlabel")), 1), MobEffectRegistry.RENEWINGTOUCH);
    public static final RegistrySupplier<Block> WHISKEY_LILITUSINGLEMALT = registerWithItemeverage("whiskey_lilitusinglemalt", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_lilitusinglemalt")), 1), MobEffectRegistry.PARTYSTARTER);
    public static final RegistrySupplier<Block> WHISKEY_JOJANNIK = registerWithItemeverage("whiskey_jojannik", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_jojannik")), 1), MobEffectRegistry.TOXICTOUCH);
    public static final RegistrySupplier<Block> WHISKEY_CRISTELWALKER = registerWithItemeverage("whiskey_cristelwalker", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_cristelwalker")), 3), MobEffectRegistry.PROTECTIVETOUCH);
    public static final RegistrySupplier<Block> WHISKEY_AK = registerWithItemeverage("whiskey_ak", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_ak")), 3), MobEffectRegistry.LIGHTNING_STRIKE);
    public static final RegistrySupplier<Block> WHISKEY_HIGHLAND_HEARTH = registerWithItemeverage("whiskey_highland_hearth", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_highland_hearth")), 1), MobEffectRegistry.REPULSION);
    public static final RegistrySupplier<Block> WHISKEY_JAMESONS_MALT = registerWithItemeverage("whiskey_jamesons_malt", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_jamesons_malt")), 1), MobEffectRegistry.EXPLOSION);
    public static final RegistrySupplier<Block> WHISKEY_SMOKEY_REVERIE = registerWithItemeverage("whiskey_smokey_reverie", () -> new BeverageBlock(getBeverageSettings(Brewery.identifier("whiskey_smokey_reverie")), 2), MobEffectRegistry.COMBUSTION);
    // Sober-age staples: always brewed, even when alcohol is removed.
    public static final RegistrySupplier<Block> ROOT_BEER = registerWithItemeverage("root_beer", () -> new BeverageBlock(getMugSettings(Brewery.identifier("root_beer")), 2), MobEffectRegistry.STOUTHEART);
    public static final RegistrySupplier<Block> SMALL_BEER = registerWithItemeverage("small_beer", () -> new BeverageBlock(getMugSettings(Brewery.identifier("small_beer")), 2), MobEffectRegistry.HEALINGTOUCH);
    public static final RegistrySupplier<Block> KVASS = registerWithItemeverage("kvass", () -> new BeverageBlock(getMugSettings(Brewery.identifier("kvass")), 2), MobEffectRegistry.RENEWINGTOUCH);
    public static final RegistrySupplier<Block> COFFEE = registerWithItemeverage("coffee", () -> new BeverageBlock(getMugSettings(Brewery.identifier("coffee")), 2), MobEffectRegistry.MINING);
    public static final RegistrySupplier<Block> PORK_KNUCKLE_BLOCK = registerWithoutItem("pork_knuckle", () -> new FoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Brewery.identifier("pork_knuckle"))), 4, new FoodProperties.Builder().nutrition(9).saturationModifier(0.9F).build()));
    public static final RegistrySupplier<Item> PORK_KNUCKLE = registerItem("pork_knuckle", () -> new EffectBlockItem(PORK_KNUCKLE_BLOCK.get(), getFoodItemSettings(Brewery.identifier("pork_knuckle"), 6, 0.6f, MobEffectRegistry.STOUTHEART, 2000)));
    public static final RegistrySupplier<Block> FRIED_CHICKEN_BLOCK = registerWithoutItem("fried_chicken", () -> new FoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Brewery.identifier("fried_chicken"))), 4, new FoodProperties.Builder().nutrition(7).saturationModifier(0.7F).build()));
    public static final RegistrySupplier<Item> FRIED_CHICKEN = registerItem("fried_chicken", () -> new EffectBlockItem(FRIED_CHICKEN_BLOCK.get(), getFoodItemSettings(Brewery.identifier("fried_chicken"), 6, 0.6f, MobEffectRegistry.STOUTHEART, 1500)));
    public static final RegistrySupplier<Block> HALF_CHICKEN_BLOCK = registerWithoutItem("half_chicken", () -> new FoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Brewery.identifier("half_chicken"))), 4, new FoodProperties.Builder().nutrition(3).saturationModifier(0.4F).build()));
    public static final RegistrySupplier<Item> HALF_CHICKEN = registerItem("half_chicken", () -> new EffectBlockItem(HALF_CHICKEN_BLOCK.get(), getFoodItemSettings(Brewery.identifier("half_chicken"), 6, 0.6f, MobEffectRegistry.STOUTHEART, 900)));
    public static final RegistrySupplier<Block> MASHED_POTATOES_BLOCK = registerWithoutItem("mashed_potatoes", () -> new FoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Brewery.identifier("mashed_potatoes"))), 4, new FoodProperties.Builder().nutrition(4).saturationModifier(0.5F).build()));
    public static final RegistrySupplier<Item> MASHED_POTATOES = registerItem("mashed_potatoes", () -> new EffectBlockItem(MASHED_POTATOES_BLOCK.get(), getFoodItemSettings(Brewery.identifier("mashed_potatoes"), 3, 0.5f, MobEffectRegistry.STOUTHEART, 4000)));
    public static final RegistrySupplier<Block> POTATO_SALAD_BLOCK = registerWithoutItem("potato_salad", () -> new FoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Brewery.identifier("potato_salad"))), 4, new FoodProperties.Builder().nutrition(8).saturationModifier(0.8F).build()));
    public static final RegistrySupplier<Item> POTATO_SALAD = registerItem("potato_salad", () -> new EffectBlockItem(POTATO_SALAD_BLOCK.get(), getFoodItemSettings(Brewery.identifier("potato_salad"), 6, 0.7f, MobEffectRegistry.STOUTHEART, 6000)));
    public static final RegistrySupplier<Block> DUMPLINGS_BLOCK = registerWithoutItem("dumplings", () -> new FoodBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).setId(blockKey(Brewery.identifier("dumplings"))), 4, new FoodProperties.Builder().nutrition(7).saturationModifier(0.8F).build()));
    public static final RegistrySupplier<Item> DUMPLINGS = registerItem("dumplings", () -> new EffectBlockItem(DUMPLINGS_BLOCK.get(), getFoodItemSettings(Brewery.identifier("dumplings"), 6, 0.5f, MobEffectRegistry.STOUTHEART, 6000)));
    public static final RegistrySupplier<Block> GINGERBREAD = registerWithItem("gingerbread", () -> new WallDecorationBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(Brewery.identifier("gingerbread")))));
    public static final RegistrySupplier<Block> BREWERY_BANNER = registerWithItem("brewery_banner", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("brewery_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BREWERY_WALL_BANNER = registerWithoutItem("brewery_wall_banner", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(Brewery.identifier("brewery_wall_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));

    public static void init() {
        ITEMS.register();
        BLOCKS.register();
    }

    public static BlockBehaviour.Properties properties(float strength) {
        return properties(strength, strength);
    }

    public static BlockBehaviour.Properties properties(float breakSpeed, float explosionResist) {
        return BlockBehaviour.Properties.of().strength(breakSpeed, explosionResist);
    }

    // 26.2: Item.Properties.setId is MANDATORY before construction.
    private static ResourceKey<Item> itemKey(Identifier id) {
        return ResourceKey.create(Registries.ITEM, id);
    }

    private static ResourceKey<Block> blockKey(Identifier id) {
        return ResourceKey.create(Registries.BLOCK, id);
    }

    private static Item.Properties getSettings(Identifier id, Consumer<Item.Properties> consumer) {
        Item.Properties settings = new Item.Properties().setId(itemKey(id));
        consumer.accept(settings);
        return settings;
    }

    static Item.Properties getSettings(Identifier id) {
        return getSettings(id, s -> {});
    }

    // 26.2: FoodProperties.Builder.effect/fast removed; effects live on CONSUMABLE
    // (ApplyStatusEffectsConsumeEffect), fast = consumeSeconds(0.8). Same values preserved.
    private static Item.Properties foodWith(Identifier id, int nutrition, float saturationMod, boolean alwaysEat,
            java.util.List<MobEffectInstance> instances, float probability, boolean fast) {
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
        return getFoodItemSettings(id, nutrition, saturationMod, effect, duration, true, false);
    }

    private static Item.Properties getFoodItemSettings(Identifier id, int nutrition, float saturationMod, RegistrySupplier<MobEffect> effect, int duration, boolean alwaysEat, boolean fast) {
        return foodWith(id, nutrition, saturationMod, alwaysEat, effect == null ? java.util.List.of() : java.util.List.of(new MobEffectInstance(MobEffectRegistry.holder(effect), duration)), 1.0f, fast);
    }

    private static BlockBehaviour.Properties getBeverageSettings(Identifier id) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(id)).noOcclusion().instabreak();
    }

    private static BlockBehaviour.Properties getMugSettings(Identifier id) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD).setId(blockKey(id)).noOcclusion().instabreak();
    }

    private static BlockBehaviour.Properties getBushSettings(Identifier id) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).setId(blockKey(id));
    }

    private static <T extends Block> RegistrySupplier<T> registerWithItemeverage(String name, Supplier<T> block, RegistrySupplier<MobEffect> effect) {
        RegistrySupplier<T> toReturn = registerWithoutItem(name, block);
        registerItem(name, () -> new DrinkBlockItem(effect.get(), 600, toReturn.get(), getSettings(Brewery.identifier(name), settings -> settings.food(beverageFoodComponent()))));
        return toReturn;
    }

    private static FoodProperties beverageFoodComponent() {
        FoodProperties.Builder component = new FoodProperties.Builder().nutrition(2).saturationModifier(1);
        return component.build();
    }

    public static <T extends Block> RegistrySupplier<T> registerWithItem(String name, Supplier<T> block) {
        return GeneralUtil.registerWithItem(BLOCKS, BLOCK_REGISTRAR, ITEMS, ITEM_REGISTRAR, Brewery.identifier(name), block);
    }

    public static <T extends Block> RegistrySupplier<T> registerWithoutItem(String path, Supplier<T> block) {
        return GeneralUtil.registerWithoutItem(BLOCKS, BLOCK_REGISTRAR, Brewery.identifier(path), block);
    }

    public static <T extends Item> RegistrySupplier<T> registerItem(String path, Supplier<T> itemSupplier) {
        return GeneralUtil.registerItem(ITEMS, ITEM_REGISTRAR, Brewery.identifier(path), itemSupplier);
    }
}
