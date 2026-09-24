package net.adventurez.item;

import net.adventurez.entity.EnderWhaleEntity;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EnderWhaleFoodOnAStickItem extends Item {

    private final int consumeItemDamage;

    public EnderWhaleFoodOnAStickItem(int consumeItemDamage, Item.Properties properties) {
        super(properties);
        this.consumeItemDamage = consumeItemDamage;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        Entity vehicle = player.getControlledVehicle();
        if (player.isPassenger() && vehicle instanceof EnderWhaleEntity whale && whale.boost()) {
            ItemStack result = itemStack.hurtAndConvertOnBreak(consumeItemDamage, Items.FISHING_ROD, player, hand.asEquipmentSlot());
            return InteractionResult.SUCCESS_SERVER.heldItemTransformedTo(result);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.PASS;
    }
}
