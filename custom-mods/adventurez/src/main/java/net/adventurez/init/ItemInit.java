package net.adventurez.init;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import com.mojang.serialization.Codec;

import net.adventurez.AdventureMain;
import net.adventurez.item.*;
import net.adventurez.item.component.GildedActivationComponent;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.registry.FabricPotionBrewingBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.ChatFormatting;

public class ItemInit {

    public static final ResourceKey<CreativeModeTab> ADVENTUREZ_ITEM_GROUP = ResourceKey.create(Registries.CREATIVE_MODE_TAB, AdventureMain.identifierOf("item_group"));

    public static final DataComponentType<Boolean> VOID_DROP = registerComponent("void_drop", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DataComponentType<Boolean> LAVA_LIGHT = registerComponent("lava_light", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
    public static final DataComponentType<GildedActivationComponent> GILDED_DATA = registerComponent("gilded_data",
            b -> b.persistent(GildedActivationComponent.CODEC).networkSynchronized(GildedActivationComponent.PACKET_CODEC));

    public static final Item GILDED_BLACKSTONE_SHARD = create("gilded_blackstone_shard", p -> new GildedBlackstoneShard(p.fireResistant(), () -> EntityInit.GILDED_BLACKSTONE_SHARD));
    public static final Item BLACKSTONE_GOLEM_HEART = create("blackstone_golem_heart", p -> new BlackstoneGolemHeart(p.fireResistant()));
    public static final Item BLACKSTONE_GOLEM_ARM = create("blackstone_golem_arm", p -> new BlackstoneGolemArm(p.durability(2506).fireResistant().repairable(net.minecraft.world.item.Items.NETHERITE_SCRAP)));
    public static final Item GILDED_NETHERITE_FRAGMENT = create("gilded_netherite_fragment", p -> new GildedNetheriteFragment(p.fireResistant()));
    public static final Item PRIME_EYE = create("prime_eye", p -> new PrimeEyeItem(p.durability(64)));
    public static final Item ORC_SKIN = create("orc_skin", Item::new);
    public static final Item DRAGON_SADDLE = create("dragon_saddle", p -> new Item(p.stacksTo(1)));
    public static final Item SOURCE_STONE = create("source_stone", p -> new SourceStone(p.stacksTo(1)));
    public static final Item CHORUS_FRUIT_ON_A_STICK = create("chorus_fruit_on_a_stick",
            p -> new EnderWhaleFoodOnAStickItem(1, p.durability(100)));
    public static final Item ENDER_FLUTE = create("ender_flute", p -> new EnderFlute(p.durability(32)));
    public static final Item IGUANA_HIDE = create("iguana_hide", Item::new);
    public static final Item MAMMOTH_LEATHER = create("mammoth_fur", Item::new);
    public static final Item ENDER_WHALE_SKIN = create("ender_whale_skin", Item::new);
    public static final Item IVORY_ARROW = create("ivory_arrow", Item::new);
    public static final Item MAMMOTH_TUSK = create("mammoth_tusk", Item::new);
    public static final Item RHINO_LEATHER = create("rhino_leather", Item::new);
    public static final Item WARTHOG_SHELL_PIECE = create("warthog_shell_piece", Item::new);
    public static final Item HANDBOOK = create("handbook", p -> new Handbook(p.stacksTo(1)));
    public static final Item GILDED_UPGRADE_SMITHING_TEMPLATE = create("gilded_upgrade_smithing_template",
            p -> new SmithingTemplateItem(
                    Component.translatable("item.minecraft.smithing_template.gilded_upgrade.applies_to").withStyle(ChatFormatting.BLUE),
                    Component.translatable("item.minecraft.smithing_template.gilded_upgrade.ingredients").withStyle(ChatFormatting.BLUE),
                    Component.translatable("item.adventurez.gilded_upgrade").withStyle(ChatFormatting.GRAY),
                    Component.translatable("item.smithing_template.gilded_upgrade.base_slot_description"),
                    List.of(
                            Identifier.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_helmet"),
                            Identifier.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_chestplate"),
                            Identifier.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_leggings"),
                            Identifier.fromNamespaceAndPath("minecraft", "item/empty_armor_slot_boots")),
                    List.of(Identifier.fromNamespaceAndPath("adventurez", "item/empty_slot_gilded_netherite_fragment")), p));

    // Food
    public static final Item MAMMOTH_MEAT = create("mammoth_meat", p -> food(p, 3, 0.3F));
    public static final Item COOKED_MAMMOTH_MEAT = create("cooked_mammoth_meat", p -> food(p, 8, 0.8F));
    public static final Item IGUANA_MEAT = create("iguana_meat", p -> food(p, 2, 0.3F));
    public static final Item COOKED_IGUANA_MEAT = create("cooked_iguana_meat", p -> food(p, 6, 0.6F));
    public static final Item ENDER_WHALE_MEAT = create("ender_whale_meat", p -> food(p, 3, 0.3F));
    public static final Item COOKED_ENDER_WHALE_MEAT = create("cooked_ender_whale_meat", p -> food(p, 8, 0.9F));
    public static final Item RHINO_MEAT = create("rhino_meat", p -> food(p, 3, 0.3F));
    public static final Item COOKED_RHINO_MEAT = create("cooked_rhino_meat", p -> food(p, 8, 0.8F));
    public static final Item RAW_VENISON = create("raw_venison", p -> food(p, 3, 0.3F));
    public static final Item COOKED_VENISON = create("cooked_venison", p -> food(p, 6, 0.8F));
    public static final Item WARTHOG_MEAT = create("warthog_meat", p -> food(p, 3, 0.3F));
    public static final Item COOKED_WARTHOG_MEAT = create("cooked_warthog_meat", p -> food(p, 6, 0.8F));
    public static final Item SKUNK_MEAT = create("skunk_meat", p -> food(p, 2, 0.3F));
    public static final Item COOKED_SKUNK_MEAT = create("cooked_skunk_meat", p -> food(p, 4, 0.8F));

    // Armor
    public static final Item GILDED_NETHERITE_HELMET = create("gilded_netherite_helmet",
            p -> new GildedNetheriteArmor(p.humanoidArmor(AdventureArmorMaterials.GILDED_NETHERITE, ArmorType.HELMET).fireResistant().repairable(GILDED_NETHERITE_FRAGMENT)));
    public static final Item GILDED_NETHERITE_CHESTPLATE = create("gilded_netherite_chestplate",
            p -> new GildedNetheriteArmor(p.humanoidArmor(AdventureArmorMaterials.GILDED_NETHERITE, ArmorType.CHESTPLATE).fireResistant().repairable(GILDED_NETHERITE_FRAGMENT)));
    public static final Item GILDED_NETHERITE_LEGGINGS = create("gilded_netherite_leggings",
            p -> new GildedNetheriteArmor(p.humanoidArmor(AdventureArmorMaterials.GILDED_NETHERITE, ArmorType.LEGGINGS).fireResistant().repairable(GILDED_NETHERITE_FRAGMENT)));
    public static final Item GILDED_NETHERITE_BOOTS = create("gilded_netherite_boots",
            p -> new GildedNetheriteArmor(p.humanoidArmor(AdventureArmorMaterials.GILDED_NETHERITE, ArmorType.BOOTS).fireResistant().repairable(GILDED_NETHERITE_FRAGMENT)));

    private static Item food(Item.Properties p, int nutrition, float saturation) {
        return new Item(p.food(new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build()));
    }

    private static Item create(String id, Function<Item.Properties, Item> factory) {
        Identifier identifier = AdventureMain.identifierOf(id);
        Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, identifier));
        Item item = factory.apply(properties);
        CreativeModeTabEvents.modifyOutputEvent(ADVENTUREZ_ITEM_GROUP).register(output -> output.accept(item));
        return Registry.register(BuiltInRegistries.ITEM, identifier, item);
    }

    private static <T> DataComponentType<T> registerComponent(String id, UnaryOperator<DataComponentType.Builder<T>> builderOperator) {
        return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, AdventureMain.identifierOf(id), builderOperator.apply(DataComponentType.builder()).build());
    }

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ADVENTUREZ_ITEM_GROUP,
                FabricCreativeModeTab.builder().icon(() -> new ItemStack(ItemInit.HANDBOOK)).title(Component.translatable("item.adventurez.item_group")).build());

        FabricPotionBrewingBuilder.BUILD.register(builder -> {
            builder.registerPotionRecipe(Potions.AWKWARD, net.minecraft.world.item.crafting.Ingredient.of(ItemInit.ORC_SKIN), Potions.TURTLE_MASTER);
            builder.registerPotionRecipe(Potions.AWKWARD, net.minecraft.world.item.crafting.Ingredient.of(ItemInit.ENDER_WHALE_SKIN), Potions.SLOW_FALLING);
        });
    }

}
