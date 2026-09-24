package net.adventurez.item;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.blaze3d.platform.InputConstants;

import net.adventurez.entity.nonliving.GildedBlackstoneShardEntity;
import net.adventurez.init.ConfigInit;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class GildedBlackstoneShard extends Item {

    private final Supplier<EntityType<GildedBlackstoneShardEntity>> typeSupplier;
    private EntityType<GildedBlackstoneShardEntity> cachedType;

    public GildedBlackstoneShard(Item.Properties settings, Supplier<EntityType<GildedBlackstoneShardEntity>> typeSupplier) {
        super(settings);
        this.typeSupplier = typeSupplier;
    }

    public EntityType<GildedBlackstoneShardEntity> getType() {
        if (cachedType == null) {
            cachedType = typeSupplier.get();
        }
        return cachedType;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (ConfigInit.CONFIG.allow_extra_tooltips) {
            if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), 340)) {
                tooltip.accept(Component.translatable("item.adventurez.gilded_blackstone_shard.tooltip"));
            } else {
                tooltip.accept(Component.translatable("item.adventurez.moreinfo.tooltip"));
            }
        }
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (ConfigInit.CONFIG.allow_gilded_blackstone_shard_throw && !user.isShiftKeyDown()) {
            level.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F,
                    0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!level.isClientSide()) {
                GildedBlackstoneShardEntity gildedStoneEntity = new GildedBlackstoneShardEntity(level, user.getX(), user.getY() + 1.6D, user.getZ());
                gildedStoneEntity.setItem(itemStack);
                gildedStoneEntity.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0F, 1.4F, 0.0F);
                level.addFreshEntity(gildedStoneEntity);
            }
            if (!user.isCreative()) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }
}
