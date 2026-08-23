package dev.jmiahman.aged.survival;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

/**
 * Items of the original environmentz mod, re-registered under its namespace so
 * the migrated tuning tags (warm_armor, non_affecting_armor, insolating_item,
 * ice_items) resolve. Wolf/wanderer armor are leather-tier with custom
 * equipment assets (placeholder visuals server-side).
 */
public final class EnvironmentzItems {
    public static final TagKey<Item> WARM_ARMOR =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("environmentz", "warm_armor"));
    public static final TagKey<Item> NON_AFFECTING_ARMOR =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("environmentz", "non_affecting_armor"));
    public static final TagKey<Item> INSOLATING_ITEM =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("environmentz", "insolating_item"));
    public static final TagKey<Item> ICE_ITEMS =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("environmentz", "ice_items"));

    private static final TagKey<Item> WOLF_REPAIR =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("environmentz", "repairs_wolf_armor"));
    private static final TagKey<Item> WANDERER_REPAIR =
            TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("environmentz", "repairs_wanderer_armor"));

    private static ArmorMaterial leatherLike(TagKey<Item> repair, String assetPath) {
        ArmorMaterial base = net.minecraft.world.item.equipment.ArmorMaterials.LEATHER;
        ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID,
                Identifier.fromNamespaceAndPath("environmentz", assetPath));
        return new ArmorMaterial(base.durability(), base.defense(), base.enchantmentValue(),
                base.equipSound(), 0.0f, 0.0f, repair, asset);
    }

    private static final ArmorMaterial WOLF = leatherLike(WOLF_REPAIR, "wolf");
    private static final ArmorMaterial WANDERER = leatherLike(WANDERER_REPAIR, "wanderer");

    public static final Item WOLF_PELT = plain("wolf_pelt");
    public static final Item POLAR_BEAR_FUR = plain("polar_bear_fur");
    public static final Item ICE_PACK = new TempControlItem(
            new Item.Properties().durability(5).setId(key("ice_pack")), -3.0);
    public static final Item HEATING_STONES = new TempControlItem(
            new Item.Properties().durability(5).setId(key("heating_stones")), 3.0);

    public static final Item WOLF_HELMET = armor("wolf_helmet", WOLF, ArmorType.HELMET);
    public static final Item WOLF_CHESTPLATE = armor("wolf_chestplate", WOLF, ArmorType.CHESTPLATE);
    public static final Item WOLF_LEGGINGS = armor("wolf_leggings", WOLF, ArmorType.LEGGINGS);
    public static final Item WOLF_BOOTS = armor("wolf_boots", WOLF, ArmorType.BOOTS);

    public static final Item WANDERER_HELMET = armor("wanderer_helmet", WANDERER, ArmorType.HELMET);
    public static final Item WANDERER_CHESTPLATE = armor("wanderer_chestplate", WANDERER, ArmorType.CHESTPLATE);
    public static final Item WANDERER_LEGGINGS = armor("wanderer_leggings", WANDERER, ArmorType.LEGGINGS);
    public static final Item WANDERER_BOOTS = armor("wanderer_boots", WANDERER, ArmorType.BOOTS);

    private static Item plain(String path) {
        return new Item(new Item.Properties().setId(key(path)));
    }

    private static Item armor(String path, ArmorMaterial material, ArmorType type) {
        return new Item(new Item.Properties().humanoidArmor(material, type).setId(key(path)));
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("environmentz", path));
    }

    public static void registerAll(Consumer<String> log) {
        Map<String, Item> items = Map.ofEntries(
                Map.entry("wolf_pelt", WOLF_PELT),
                Map.entry("polar_bear_fur", POLAR_BEAR_FUR),
                Map.entry("ice_pack", ICE_PACK),
                Map.entry("heating_stones", HEATING_STONES),
                Map.entry("wolf_helmet", WOLF_HELMET),
                Map.entry("wolf_chestplate", WOLF_CHESTPLATE),
                Map.entry("wolf_leggings", WOLF_LEGGINGS),
                Map.entry("wolf_boots", WOLF_BOOTS),
                Map.entry("wanderer_helmet", WANDERER_HELMET),
                Map.entry("wanderer_chestplate", WANDERER_CHESTPLATE),
                Map.entry("wanderer_leggings", WANDERER_LEGGINGS),
                Map.entry("wanderer_boots", WANDERER_BOOTS));
        items.forEach((path, item) -> Registry.register(BuiltInRegistries.ITEM, key(path), item));
        log.accept("[aged-survival] environmentz items registered (" + items.size() + ")");
    }

    /** Shared sound holder for temperature control use feedback. */
    static Holder<net.minecraft.sounds.SoundEvent> drinkSound() {
        return SoundEvents.GENERIC_DRINK;
    }

    private EnvironmentzItems() {}
}
