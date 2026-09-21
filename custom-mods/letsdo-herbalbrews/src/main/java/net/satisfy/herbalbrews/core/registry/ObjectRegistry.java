package net.satisfy.herbalbrews.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.satisfy.herbalbrews.HerbalBrews;
import net.satisfy.herbalbrews.core.blocks.*;
import net.satisfy.herbalbrews.core.blocks.CauldronBlock;
import net.satisfy.herbalbrews.core.items.*;
import net.satisfy.herbalbrews.core.util.HerbalBrewsIdentifier;
import net.satisfy.herbalbrews.core.util.HerbalBrewsUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ObjectRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(HerbalBrews.MOD_ID, Registries.ITEM);
    public static final Registrar<Item> ITEM_REGISTRAR = ITEMS.getRegistrar();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(HerbalBrews.MOD_ID, Registries.BLOCK);
    public static final Registrar<Block> BLOCK_REGISTRAR = BLOCKS.getRegistrar();

    public static final RegistrySupplier<Block> STOVE = registerWithItem("stove", () -> new StoveBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BRICKS).setId(blockKey(HerbalBrewsIdentifier.identifier("stove"))).lightLevel(state -> state.getValue(StoveBlock.LIT) ? 13 : 0).randomTicks()));
    public static final RegistrySupplier<Block> TEA_LEAF_CRATE = registerWithItem("tea_leaf_crate", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.RED)).setId(blockKey(HerbalBrewsIdentifier.identifier("tea_leaf_crate")))));
    public static final RegistrySupplier<Block> GREEN_TEA_LEAF_BLOCK = registerWithItem("green_tea_leaf_block", () -> new TeaLeafBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES).setId(blockKey(HerbalBrewsIdentifier.identifier("green_tea_leaf_block")))));
    public static final RegistrySupplier<Block> DRIED_GREEN_TEA_LEAF_BLOCK = registerWithItem("dried_green_tea_leaf_block", () -> new TeaLeafBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES).setId(blockKey(HerbalBrewsIdentifier.identifier("dried_green_tea_leaf_block")))));
    public static final RegistrySupplier<Block> DRIED_OUT_GREEN_TEA_LEAF_BLOCK = registerWithItem("dried_out_green_tea_leaf_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES).setId(blockKey(HerbalBrewsIdentifier.identifier("dried_out_green_tea_leaf_block")))));
    public static final RegistrySupplier<Block> BLACK_TEA_LEAF_BLOCK = registerWithItem("black_tea_leaf_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES).setId(blockKey(HerbalBrewsIdentifier.identifier("black_tea_leaf_block")))));
    public static final RegistrySupplier<Block> MIXED_TEA_LEAF_BLOCK = registerWithItem("mixed_tea_leaf_block", () -> new TeaLeafBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES).setId(blockKey(HerbalBrewsIdentifier.identifier("mixed_tea_leaf_block")))));
    public static final RegistrySupplier<Block> OOLONG_TEA_LEAF_BLOCK = registerWithItem("oolong_tea_leaf_block", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.ACACIA_LEAVES).setId(blockKey(HerbalBrewsIdentifier.identifier("oolong_tea_leaf_block")))));
    public static final RegistrySupplier<Block> WILD_COFFEE_PLANT = registerWithItem("wild_coffee_plant", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(HerbalBrewsIdentifier.identifier("wild_coffee_plant")))));
    public static final RegistrySupplier<Block> COFFEE_PLANT = registerWithoutItem("coffee_plant", () -> new CoffeeCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).setId(blockKey(HerbalBrewsIdentifier.identifier("coffee_plant")))));
    public static final RegistrySupplier<Block> WILD_ROOIBOS_PLANT = registerWithItem("wild_rooibos_plant", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(HerbalBrewsIdentifier.identifier("wild_rooibos_plant")))));
    public static final RegistrySupplier<Block> ROOIBOS_PLANT = registerWithoutItem("rooibos_plant", () -> new RooibosCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).setId(blockKey(HerbalBrewsIdentifier.identifier("rooibos_plant")))));
    public static final RegistrySupplier<Block> WILD_YERBA_MATE_PLANT = registerWithItem("wild_yerba_mate_plant", () -> new FlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(HerbalBrewsIdentifier.identifier("wild_yerba_mate_plant")))));
    public static final RegistrySupplier<Block> YERBA_MATE_PLANT = registerWithoutItem("yerba_mate_plant", () -> new YerbaMateCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).setId(blockKey(HerbalBrewsIdentifier.identifier("yerba_mate_plant")))));
    public static final RegistrySupplier<Block> TEA_PLANT = registerWithoutItem("tea_plant", () -> new TeaCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SWEET_BERRY_BUSH).setId(blockKey(HerbalBrewsIdentifier.identifier("tea_plant")))));
    public static final RegistrySupplier<Block> HIBISCUS = registerWithItem("hibiscus", () -> new BonemealableFlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(HerbalBrewsIdentifier.identifier("hibiscus")))));
    public static final RegistrySupplier<Block> LAVENDER = registerWithItem("lavender", () -> new BonemealableFlowerBlock(MobEffects.INSTANT_HEALTH, 1, BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).setId(blockKey(HerbalBrewsIdentifier.identifier("lavender")))));
    public static final RegistrySupplier<Block> JUG = registerWithoutItem("jug", () -> new JugBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(HerbalBrewsIdentifier.identifier("jug")))));
    public static final RegistrySupplier<Item> JUG_ITEM = registerItem("jug", () -> new JugItem(JUG.get(), getSettings(HerbalBrewsIdentifier.identifier("jug"))));
    public static final RegistrySupplier<Block> COPPER_TEA_KETTLE = registerWithItem("copper_tea_kettle", () -> new TeaKettleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(HerbalBrewsIdentifier.identifier("copper_tea_kettle")))));
    public static final RegistrySupplier<Block> TEA_KETTLE = registerWithItem("tea_kettle", () -> new TeaKettleBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(HerbalBrewsIdentifier.identifier("tea_kettle")))));
    public static final RegistrySupplier<Block> CAULDRON = registerWithItem("cauldron", () -> new CauldronBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).setId(blockKey(HerbalBrewsIdentifier.identifier("cauldron"))).lightLevel((blockState) -> 11)));
    public static final RegistrySupplier<Item> FLASK = registerItem("flask", () -> new FlaskItem(getSettings(HerbalBrewsIdentifier.identifier("flask"))));
    public static final RegistrySupplier<Item> TEA_BLOSSOM = registerItem("tea_blossom", () -> new BlockItem(TEA_PLANT.get(), getSettings(HerbalBrewsIdentifier.identifier("tea_blossom")).food(snackFood(1, 0.1f), snackConsumable())));
    public static final RegistrySupplier<Item> GREEN_TEA_LEAF = registerItem("green_tea_leaf", () -> new Item(getSettings(HerbalBrewsIdentifier.identifier("green_tea_leaf"))));
    public static final RegistrySupplier<Item> YERBA_MATE_LEAF = registerItem("yerba_mate_leaf", () -> new BlockItem(YERBA_MATE_PLANT.get(), getSettings(HerbalBrewsIdentifier.identifier("yerba_mate_leaf")).food(snackFood(1, 0.1f), snackConsumable())));
    public static final RegistrySupplier<Item> ROOIBOS_LEAF = registerItem("rooibos_leaf", () -> new BlockItem(ROOIBOS_PLANT.get(), getSettings(HerbalBrewsIdentifier.identifier("rooibos_leaf")).food(snackFood(1, 0.1f), snackConsumable())));
    public static final RegistrySupplier<Item> LAVENDER_BLOSSOM = registerItem("lavender_blossom", () -> new Item(getSettings(HerbalBrewsIdentifier.identifier("lavender_blossom"))));
    public static final RegistrySupplier<Item> DRIED_GREEN_TEA = registerItem("dried_green_tea", () -> new Item(getSettings(HerbalBrewsIdentifier.identifier("dried_green_tea")).food(snackFood(1, 0.1f), snackConsumable())));
    public static final RegistrySupplier<Item> DRIED_BLACK_TEA = registerItem("dried_black_tea", () -> new Item(getSettings(HerbalBrewsIdentifier.identifier("dried_black_tea")).food(snackFood(1, 0.1f), snackConsumable())));
    public static final RegistrySupplier<Item> DRIED_OOLONG_TEA = registerItem("dried_oolong_tea", () -> new Item(getSettings(HerbalBrewsIdentifier.identifier("dried_oolong_tea")).food(snackFood(1, 0.1f), snackConsumable())));
    public static final RegistrySupplier<Item> COFFEE_BEANS = registerItem("coffee_beans", () -> new BlockItem(COFFEE_PLANT.get(), getSettings(HerbalBrewsIdentifier.identifier("coffee_beans")).food(snackFood(1, 0.1f), snackConsumable())));
    public static final RegistrySupplier<Block> GREEN_TEA_BLOCK = registerWithoutItem("green_tea_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("green_tea_block"))));
    public static final RegistrySupplier<Block> BLACK_TEA_BLOCK = registerWithoutItem("black_tea_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("black_tea_block"))));
    public static final RegistrySupplier<Block> LAVENDER_TEA_BLOCK = registerWithoutItem("lavender_tea_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("lavender_tea_block"))));
    public static final RegistrySupplier<Block> YERBA_MATE_TEA_BLOCK = registerWithoutItem("yerba_mate_tea_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("yerba_mate_tea_block"))));
    public static final RegistrySupplier<Block> OOLONG_TEA_BLOCK = registerWithoutItem("oolong_tea_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("oolong_tea_block"))));
    public static final RegistrySupplier<Block> ROOIBOS_TEA_BLOCK = registerWithoutItem("rooibos_tea_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("rooibos_tea_block"))));
    public static final RegistrySupplier<Block> HIBISCUS_TEA_BLOCK = registerWithoutItem("hibiscus_tea_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("hibiscus_tea_block"))));
    public static final RegistrySupplier<Block> MILK_COFFEE_BLOCK = registerWithoutItem("milk_coffee_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("milk_coffee_block"))));
    public static final RegistrySupplier<Block> COFFEE_BLOCK = registerWithoutItem("coffee_block", () -> new TeaCupBlock(getTeaSettings(HerbalBrewsIdentifier.identifier("coffee_block"))));
    public static final RegistrySupplier<Item> GREEN_TEA = registerItem("green_tea", () -> new DrinkBlockItem(GREEN_TEA_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("green_tea")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> BLACK_TEA = registerItem("black_tea", () -> new DrinkBlockItem(BLACK_TEA_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("black_tea")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> LAVENDER_TEA = registerItem("lavender_tea", () -> new DrinkBlockItem(LAVENDER_TEA_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("lavender_tea")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> YERBA_MATE_TEA = registerItem("yerba_mate_tea", () -> new DrinkBlockItem(YERBA_MATE_TEA_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("yerba_mate_tea")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> OOLONG_TEA = registerItem("oolong_tea", () -> new DrinkBlockItem(OOLONG_TEA_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("oolong_tea")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> ROOIBOS_TEA = registerItem("rooibos_tea", () -> new DrinkBlockItem(ROOIBOS_TEA_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("rooibos_tea")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> HIBISCUS_TEA = registerItem("hibiscus_tea", () -> new DrinkBlockItem(HIBISCUS_TEA_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("hibiscus_tea")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> MILK_COFFEE = registerItem("milk_coffee", () -> new DrinkBlockItem(MILK_COFFEE_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("milk_coffee")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> COFFEE = registerItem("coffee", () -> new DrinkBlockItem(COFFEE_BLOCK.get(), getSettings(HerbalBrewsIdentifier.identifier("coffee")).food(drinkFood(), drinkConsumable())));
    public static final RegistrySupplier<Item> WITCH_HAT = registerItem("witch_hat", () -> new HatItem(ArmorMaterialRegistry.HERBALBREWS_HAT, ArmorType.HELMET, getSettings(HerbalBrewsIdentifier.identifier("witch_hat")).rarity(Rarity.EPIC), HerbalBrewsIdentifier.identifier("textures/models/armor/witch_hat.png")));
    public static final RegistrySupplier<Item> TOP_HAT = registerItem("top_hat", () -> new HatItem(ArmorMaterialRegistry.HERBALBREWS_HAT, ArmorType.HELMET, getSettings(HerbalBrewsIdentifier.identifier("top_hat")).rarity(Rarity.EPIC), HerbalBrewsIdentifier.identifier("textures/models/armor/top_hat.png")));
    public static final RegistrySupplier<Block> HERBALBREWS_BANNER = registerWithItem("herbalbrews_banner", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(HerbalBrewsIdentifier.identifier("herbalbrews_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> HERBALBREWS_WALL_BANNER = registerWithoutItem("herbalbrews_wall_banner", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of().setId(blockKey(HerbalBrewsIdentifier.identifier("herbalbrews_wall_banner"))).strength(1F).instrument(NoteBlockInstrument.BASS).noCollision().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> POTTED_LAVENDER = registerWithoutItem("potted_lavender", () -> new FlowerPotBlock(LAVENDER.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(HerbalBrewsIdentifier.identifier("potted_lavender")))));
    public static final RegistrySupplier<Block> POTTED_HIBISCUS = registerWithoutItem("potted_hibiscus", () -> new FlowerPotBlock(HIBISCUS.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(HerbalBrewsIdentifier.identifier("potted_hibiscus")))));
    public static final RegistrySupplier<Block> POTTED_WILD_ROOIBOS = registerWithoutItem("potted_wild_rooibos", () -> new FlowerPotBlock(WILD_ROOIBOS_PLANT.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(HerbalBrewsIdentifier.identifier("potted_wild_rooibos")))));
    public static final RegistrySupplier<Block> POTTED_WILD_COFFEE = registerWithoutItem("potted_wild_coffee", () -> new FlowerPotBlock(WILD_COFFEE_PLANT.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(HerbalBrewsIdentifier.identifier("potted_wild_coffee")))));
    public static final RegistrySupplier<Block> POTTED_WILD_YERBA_MATE = registerWithoutItem("potted_wild_yerba_mate", () -> new FlowerPotBlock(WILD_YERBA_MATE_PLANT.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.FLOWER_POT).setId(blockKey(HerbalBrewsIdentifier.identifier("potted_wild_yerba_mate")))));
    public static final RegistrySupplier<Item> HERBAL_INFUSION = registerItem("herbal_infusion", () -> new Item(getSettings(HerbalBrewsIdentifier.identifier("herbal_infusion"))));

    public static void init() {
        ITEMS.register();
        BLOCKS.register();
    }

    private static FoodProperties snackFood(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build();
    }

    private static Consumable snackConsumable() {
        return Consumable.builder().animation(ItemUseAnimation.EAT).consumeSeconds(0.8F).build();
    }

    private static FoodProperties drinkFood() {
        return new FoodProperties.Builder().nutrition(Foods.SWEET_BERRIES.nutrition()).saturationModifier(Foods.SWEET_BERRIES.saturation()).alwaysEdible().build();
    }

    private static Consumable drinkConsumable() {
        return Consumable.builder().animation(ItemUseAnimation.DRINK).sound(SoundEvents.HONEY_DRINK).build();
    }

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
        return getSettings(id, settings -> {
        });
    }

    public static <T extends Block> RegistrySupplier<T> registerWithItem(String name, Supplier<T> block) {
        Identifier id = HerbalBrewsIdentifier.identifier(name);
        RegistrySupplier<T> toReturn = HerbalBrewsUtil.registerWithoutItem(BLOCKS, BLOCK_REGISTRAR, id, block);
        HerbalBrewsUtil.registerItem(ITEMS, ITEM_REGISTRAR, id, () -> new BlockItem(toReturn.get(), new Item.Properties().setId(itemKey(id))));
        return toReturn;
    }

    public static <T extends Block> RegistrySupplier<T> registerWithoutItem(String path, Supplier<T> block) {
        return HerbalBrewsUtil.registerWithoutItem(BLOCKS, BLOCK_REGISTRAR, HerbalBrewsIdentifier.identifier(path), block);
    }

    public static <T extends Item> RegistrySupplier<T> registerItem(String path, Supplier<T> itemSupplier) {
        return HerbalBrewsUtil.registerItem(ITEMS, ITEM_REGISTRAR, HerbalBrewsIdentifier.identifier(path), itemSupplier);
    }

    private static BlockBehaviour.Properties getTeaSettings(Identifier id) {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).setId(blockKey(id)).noOcclusion().instabreak();
    }
}
