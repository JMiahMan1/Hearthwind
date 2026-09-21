package net.satisfy.candlelight.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.satisfy.candlelight.core.util.CandlelightUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WrittenPaperItem extends Item {
    public WrittenPaperItem(Properties settings) {
        super(settings);
    }

    @Override
    public @NotNull InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (world.isClientSide()) {
            CandlelightUtil.setSignedPaperScreen(itemStack);
        } else {
            user.containerMenu.broadcastChanges();
        }
        user.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.CONSUME;
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        CompoundTag nbtCompound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (nbtCompound != null) {
            String string = nbtCompound.getStringOr("title", "");
            if (!StringUtil.isNullOrEmpty(string)) {
                return Component.literal(string);
            }
        }

        return super.getName(stack);
    }

}