package dev.jmiahman.hearthwind.primitive.tiered;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

@Environment(EnvType.CLIENT)
public final class TieredTooltips {
    private TieredTooltips() {}

    public static void init() {
        ItemTooltipCallback.EVENT.register(TieredTooltips::onTooltip);
    }

    private static void onTooltip(ItemStack stack, net.minecraft.world.item.Item.TooltipContext context,
            TooltipFlag tooltipType, List<Component> lines) {
        if (stack == null || stack.isEmpty()) return;

        TierDefinition tier = TieredData.getTier(stack);
        if (tier != null && !lines.isEmpty()) {
            ChatFormatting color = tier.getFormatting();
            String name = tier.getDisplayName();

            MutableComponent badge = Component.literal("★ " + name + " ★")
                    .withStyle(color, ChatFormatting.BOLD);

            // Insert tier badge right below item title
            if (lines.size() >= 1) {
                lines.add(1, badge);
            } else {
                lines.add(badge);
            }
        }
    }
}
