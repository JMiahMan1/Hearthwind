package net.satisfy.candlelight.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.candlelight.core.block.CookingPanBlock;
import net.satisfy.farm_and_charm.core.util.EquipmentTiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CookingPanItem extends BlockItem {
    public static final EquipmentTiers COOKING_PAN_TIER = EquipmentTiers.COPPER;

    public CookingPanItem(Block block, Item.Properties properties) {
        super(block, properties
                .durability(COOKING_PAN_TIER.material().durability())
                .enchantable(COOKING_PAN_TIER.material().enchantmentValue())
                .repairable(COOKING_PAN_TIER.material().repairItems()));
    }

    public static ItemAttributeModifiers createAttributes() {
        float attackDamage = 2.0F + COOKING_PAN_TIER.material().attackDamageBonus();
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState blockState, BlockPos pos, LivingEntity entity) {
        if (!level.isClientSide() && blockState.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public @NotNull InteractionResult place(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            return super.place(context);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, net.minecraft.server.level.ServerLevel serverLevel, Entity entity, EquipmentSlot slot) {
        super.inventoryTick(itemStack, serverLevel, entity, slot);
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(BlockPlaceContext blockPlaceContext) {
        BlockState state = super.getPlacementState(blockPlaceContext);
        if (state != null)
            state = state.setValue(CookingPanBlock.DAMAGE, blockPlaceContext.getItemInHand().getDamageValue());
        return state;
    }
}