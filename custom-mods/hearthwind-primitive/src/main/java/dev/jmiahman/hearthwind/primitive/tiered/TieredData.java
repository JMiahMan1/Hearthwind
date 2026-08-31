package dev.jmiahman.hearthwind.primitive.tiered;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public final class TieredData {
    public static final String TIER_TAG = "Tier";

    private TieredData() {}

    public static String getTierId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            return data.copyTag().getStringOr(TIER_TAG, null);
        }
        return null;
    }

    public static TierDefinition getTier(ItemStack stack) {
        String id = getTierId(stack);
        if (id == null) return null;
        return TierRegistry.get(Identifier.parse(id));
    }

    public static void setTier(ItemStack stack, TierDefinition tier) {
        if (stack == null || stack.isEmpty()) return;
        if (tier == null) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.remove(TIER_TAG));
            return;
        }
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(TIER_TAG, tier.id().toString()));
        ItemAttributeModifiers modifiers = tier.buildModifiers(stack);
        if (!modifiers.modifiers().isEmpty()) {
            stack.set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers);
        }
    }

    public static boolean isEligible(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (stack.getMaxDamage() <= 0 && !stack.has(DataComponents.MAX_DAMAGE)) {
            return false;
        }
        if (stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || stack.is(ItemTags.HEAD_ARMOR)
                || stack.is(ItemTags.CHEST_ARMOR)
                || stack.is(ItemTags.LEG_ARMOR)
                || stack.is(ItemTags.FOOT_ARMOR)
                || stack.is(Items.BOW)
                || stack.is(Items.CROSSBOW)
                || stack.is(Items.FISHING_ROD)
                || stack.is(Items.TRIDENT)
                || stack.is(Items.SHIELD)) {
            return true;
        }
        String path = stack.getItem().toString().toLowerCase();
        return path.contains("sword")
                || path.contains("axe")
                || path.contains("pickaxe")
                || path.contains("shovel")
                || path.contains("hoe")
                || path.contains("helmet")
                || path.contains("chestplate")
                || path.contains("leggings")
                || path.contains("boots")
                || path.contains("shield")
                || path.contains("bow");
    }

    public static boolean applyRandomTierIfEligible(ItemStack stack, RandomSource random) {
        if (!isEligible(stack)) {
            return false;
        }
        if (getTierId(stack) != null) {
            return false; // already tiered
        }
        TierDefinition rolled = TierRegistry.rollTier(stack, random);
        if (rolled != null) {
            setTier(stack, rolled);
            return true;
        }
        return false;
    }
}
