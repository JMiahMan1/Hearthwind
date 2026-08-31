package dev.jmiahman.hearthwind.primitive.tiered;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record TierDefinition(
        Identifier id,
        List<Verifier> verifiers,
        int weight,
        String colorName,
        List<AffixAttribute> attributes) {

    public boolean matches(ItemStack stack) {
        if (stack == null || stack.isEmpty() || verifiers.isEmpty()) {
            return false;
        }
        for (Verifier v : verifiers) {
            if (v.matches(stack)) {
                return true;
            }
        }
        return false;
    }

    public ChatFormatting getFormatting() {
        if (colorName == null) {
            return ChatFormatting.WHITE;
        }
        return switch (colorName.toLowerCase().trim()) {
            case "gold", "yellow" -> ChatFormatting.GOLD;
            case "dark_purple", "purple" -> ChatFormatting.DARK_PURPLE;
            case "light_purple", "magenta", "pink" -> ChatFormatting.LIGHT_PURPLE;
            case "blue", "dark_blue" -> ChatFormatting.BLUE;
            case "aqua", "cyan" -> ChatFormatting.AQUA;
            case "dark_green", "green" -> ChatFormatting.DARK_GREEN;
            case "gray", "grey" -> ChatFormatting.GRAY;
            case "dark_gray", "dark_grey" -> ChatFormatting.DARK_GRAY;
            case "red", "dark_red" -> ChatFormatting.RED;
            default -> ChatFormatting.WHITE;
        };
    }

    public String getDisplayName() {
        String path = id.getPath();
        if (path.startsWith("legendary_")) {
            return switch (path.substring(path.length() - 1)) {
                case "1" -> "Godly";
                case "2" -> "Peerless";
                case "3" -> "Eternal";
                default -> "Legendary";
            };
        } else if (path.startsWith("epic_")) {
            return switch (path.substring(path.length() - 1)) {
                case "1" -> "Masterwork";
                case "2" -> "Heroic";
                case "3" -> "Furious";
                default -> "Epic";
            };
        } else if (path.startsWith("rare_")) {
            return switch (path.substring(path.length() - 1)) {
                case "1" -> "Keen";
                case "2" -> "Tempered";
                case "3" -> "Honed";
                default -> "Rare";
            };
        } else if (path.startsWith("uncommon_")) {
            return switch (path.substring(path.length() - 1)) {
                case "1" -> "Sharp";
                case "2" -> "Polished";
                case "3" -> "Swift";
                default -> "Uncommon";
            };
        } else if (path.startsWith("common_")) {
            return switch (path.substring(path.length() - 1)) {
                case "1" -> "Dull";
                case "2" -> "Rusty";
                case "3" -> "Chipped";
                default -> "Common";
            };
        } else if (path.startsWith("unique_")) {
            return switch (path.substring(path.length() - 1)) {
                case "1" -> "Mythic";
                case "2" -> "Relic";
                case "3" -> "Curseforged";
                default -> "Unique";
            };
        }
        return "Tiered";
    }

    public ItemAttributeModifiers buildModifiers(ItemStack stack) {
        ItemAttributeModifiers base = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();

        // Copy original base weapon/tool/armor modifiers
        for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }

        // Apply tier modifiers
        for (AffixAttribute attr : attributes) {
            Holder<Attribute> holder = TierRegistry.resolveAttribute(attr.type());
            if (holder != null) {
                Identifier modId = Identifier.fromNamespaceAndPath("tiered",
                        id.getPath() + "_" + attr.modifierName().replace(':', '_'));
                AttributeModifier modifier = new AttributeModifier(modId, attr.amount(), attr.operation());
                for (EquipmentSlotGroup slot : attr.slots()) {
                    builder.add(holder, modifier, slot);
                }
            }
        }
        return builder.build();
    }

    public record Verifier(String tag, String item) {
        public boolean matches(ItemStack stack) {
            if (tag != null && !tag.isEmpty()) {
                Identifier tagId = Identifier.parse(tag);
                TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
                if (stack.is(tagKey)) {
                    return true;
                }
                // Tag compatibility aliases for 26.2
                if (tag.equals("c:swords") && (stack.is(ItemTags.SWORDS) || stack.getItem().toString().contains("sword"))) return true;
                if (tag.equals("c:axes") && (stack.is(ItemTags.AXES) || stack.getItem().toString().contains("axe"))) return true;
                if (tag.equals("c:pickaxes") && (stack.is(ItemTags.PICKAXES) || stack.getItem().toString().contains("pickaxe"))) return true;
                if (tag.equals("c:shovels") && (stack.is(ItemTags.SHOVELS) || stack.getItem().toString().contains("shovel"))) return true;
                if (tag.equals("c:hoes") && (stack.is(ItemTags.HOES) || stack.getItem().toString().contains("hoe"))) return true;
                if (tag.equals("c:helmets") && (stack.is(ItemTags.HEAD_ARMOR) || stack.getItem().toString().contains("helmet") || stack.getItem().toString().contains("cap"))) return true;
                if (tag.equals("c:chestplates") && (stack.is(ItemTags.CHEST_ARMOR) || stack.getItem().toString().contains("chestplate") || stack.getItem().toString().contains("tunic"))) return true;
                if (tag.equals("c:leggings") && (stack.is(ItemTags.LEG_ARMOR) || stack.getItem().toString().contains("leggings") || stack.getItem().toString().contains("pants"))) return true;
                if (tag.equals("c:boots") && (stack.is(ItemTags.FOOT_ARMOR) || stack.getItem().toString().contains("boots"))) return true;
                if (tag.equals("c:shields") && (stack.is(Items.SHIELD) || stack.getItem().toString().contains("shield"))) return true;
                if (tag.equals("c:bows") && (stack.is(Items.BOW) || stack.is(Items.CROSSBOW) || stack.getItem().toString().contains("bow"))) return true;
                if (tag.equals("aged:melee") && (stack.is(ItemTags.SWORDS) || stack.is(ItemTags.AXES) || stack.getItem().toString().contains("sword") || stack.getItem().toString().contains("axe"))) return true;
            }
            if (item != null && !item.isEmpty()) {
                Identifier itemId = Identifier.parse(item);
                if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId)) {
                    return true;
                }
            }
            return false;
        }
    }

    public record AffixAttribute(
            String type,
            String modifierName,
            Operation operation,
            double amount,
            List<EquipmentSlotGroup> slots) {}
}
