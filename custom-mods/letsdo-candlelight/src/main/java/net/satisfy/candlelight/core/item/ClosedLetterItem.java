package net.satisfy.candlelight.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.satisfy.candlelight.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClosedLetterItem extends Item {
    public ClosedLetterItem(Properties settings) {
        super(settings);
    }


    @Override
    public @NotNull InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack in = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            CompoundTag sealed = in.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
            CompoundTag payload = sealed.contains("sealed_payload") ? sealed.getCompoundOrEmpty("sealed_payload") : new CompoundTag();

            ItemStack out = new ItemStack(ObjectRegistry.NOTE_PAPER_WRITTEN.get());
            out.set(DataComponents.CUSTOM_DATA, CustomData.of(payload.copy()));

            if (this == ObjectRegistry.LOVE_LETTER_CLOSED.get()) {
                ((net.minecraft.server.level.ServerLevel) level).sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 20, 0.3, 0.2, 0.3, 0.05);
            }

            player.setItemInHand(hand, out);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResult.CONSUME;
    }
}