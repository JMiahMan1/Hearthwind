package net.satisfy.farm_and_charm.core.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.NotNull;

// 26.2: Block.appendHoverText does not exist (tooltips are Item-only).
// Full parity: every block tooltip body moved here verbatim, dispatched by block class.
public class TooltipBlockItem extends BlockItem {
    public TooltipBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack itemStack, @NotNull Item.TooltipContext tooltipContext,
            @NotNull net.minecraft.world.item.component.TooltipDisplay display,
            @NotNull java.util.function.Consumer<Component> tooltip, @NotNull TooltipFlag tooltipFlag) {
        Block block = getBlock();
        if (block instanceof net.satisfy.farm_and_charm.core.block.CattlegridBlock) {

                int earthyColor = 0xFFD966;
                int goldColor = 0xFFD700;

                if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.cattlegrid.info_0").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthyColor))));
                } else {
                    Component shiftKey = Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(goldColor)));
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.tooltip_information.hold", shiftKey).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthyColor))));
                }
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.ChickenNestBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.CookingPotBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.CraftingBowlBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.EatableBoxBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.FeedingTroughBlock) {

                int earthy = 0xFFD966;
                int gold = 0xFFD700;

                if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.feeding_trough.info_0")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                } else {
                    tooltip.accept(Component.translatable(
                            "tooltip.farm_and_charm.tooltip_information.hold",
                            Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)))
                    ).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                }
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.FertilizedFarmlandBlock) {

                int earthy = 0xFFD966;
                int gold = 0xFFD700;

                if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.fertilized_farmland.info_0")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                } else {
                    tooltip.accept(Component.translatable(
                            "tooltip.farm_and_charm.tooltip_information.hold",
                            Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)))
                    ).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                }
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.FertilizedSoilBlock) {

                int earthy = 0xFFD966;
                int gold = 0xFFD700;

                if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.fertilized_soil.info_0")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.fertilized_soil.info_1")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                } else {
                    tooltip.accept(Component.translatable(
                            "tooltip.farm_and_charm.tooltip_information.hold",
                            Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)))
                    ).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                }
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.FoodBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.MincerBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.PetBowlBlock) {

                int earthy = 0xFFD966;
                int gold = 0xFFD700;

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
                tooltip.accept(Component.empty());

                if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.pet_bowl.info_0").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.pet_bowl.info_1").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                } else {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.tooltip_information.hold", Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)))
                    ).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                }
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.RoasterBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.ScarecrowBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.thankyou_1").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
                tooltip.accept(Component.empty());
                tooltip.accept(Component.translatable("tooltip.farm_and_charm.thankyou_2").withStyle(ChatFormatting.DARK_PURPLE));
                tooltip.accept(Component.translatable("tooltip.farm_and_charm.thankyou_4").withStyle(ChatFormatting.BLUE));
                tooltip.accept(Component.empty());
                tooltip.accept(Component.translatable("tooltip.farm_and_charm.thankyou_3").withStyle(ChatFormatting.GOLD));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.SiloBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.StackableBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.SturdyLadderBlock) {

                int earthy = 0xFFD966;
                int gold = 0xFFD700;

                if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.sturdy_ladder.info_0")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                } else {
                    tooltip.accept(Component.translatable(
                            "tooltip.farm_and_charm.tooltip_information.hold",
                            Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)))
                    ).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                }
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.TeaJugBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.TimberWellBlock) {

                tooltip.accept(Component.literal("Test item – not implemented. Nice that you found it!")
                        .withStyle(ChatFormatting.RED));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.ToolRackBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.WaterSprinklerBlock) {

                int earthy = 0xFFD966;
                int gold = 0xFFD700;

                if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.water_sprinkler.info_0")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                } else {
                    tooltip.accept(Component.translatable(
                            "tooltip.farm_and_charm.tooltip_information.hold",
                            Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)))
                    ).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                }
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.WaterTroughBlock) {

                int earthy = 0xFFD966;
                int gold = 0xFFD700;

                if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_LSHIFT) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(net.minecraft.client.Minecraft.getInstance().getWindow(), com.mojang.blaze3d.platform.InputConstants.KEY_RSHIFT)) {
                    tooltip.accept(Component.translatable("tooltip.farm_and_charm.water_trough.info_0")
                            .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                } else {
                    tooltip.accept(Component.translatable(
                            "tooltip.farm_and_charm.tooltip_information.hold",
                            Component.literal("[SHIFT]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(gold)))
                    ).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(earthy))));
                }
            return;
        }
        if (block instanceof net.satisfy.farm_and_charm.core.block.WindowSillBlock) {

                tooltip.accept(Component.translatable("tooltip.farm_and_charm.canbeplaced").withStyle(ChatFormatting.GRAY));
            return;
        }
    }
}
