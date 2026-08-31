package dev.jmiahman.hearthwind.survival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

/**
 * Portable water vessel, five tiers (capacity 2 + tier). Fill by using it on
 * a water source or a cauldron (heated cauldrons boil the water clean);
 * drink with right-click (vanilla hold-to-drink flow); pour back into a
 * cauldron while sneaking. Item ids sit in the dehydration namespace so the
 * migrated datapack gates and job rewards resolve unchanged.
 */
public final class LeatherFlaskItem extends Item {
    private final int capacity;

    public LeatherFlaskItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public int capacity() {
        return this.capacity;
    }

    private static FlaskData data(ItemStack stack) {
        return stack.get(FlaskItems.FLASK_DATA);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        FlaskData data = data(stack);
        if (data != null && data.fillLevel() > 0) {
            Consumable drink = stack.get(DataComponents.CONSUMABLE);
            if (drink != null) {
                return drink.startConsuming(player, stack, hand);
            }
            return InteractionResult.PASS;
        }
        // Empty flask: vanilla crosshairs cannot target fluid blocks, so
        // raycast with fluids included (server-side) to find open water.
        if (player instanceof ServerPlayer sp && level instanceof ServerLevel server) {
            InteractionResult rayResult = tryFillOrPourFromRay(sp, server, stack);
            if (rayResult != InteractionResult.PASS) {
                return rayResult;
            }
        }
        return InteractionResult.PASS;
    }

    /**
     * Shared fluid raycast: filling an empty flask from, or dumping into,
     * open water. Returns PASS when the ray hits a solid block first or
     * there is no water interaction to do.
     */
    private static InteractionResult tryFillOrPourFromRay(ServerPlayer sp, ServerLevel server, ItemStack stack) {
        HitResult ray = sp.pick(sp.blockInteractionRange(), 0.0f, true);
        if (ray.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos pos = ((BlockHitResult) ray).getBlockPos();
        FluidState fluid = server.getFluidState(pos);
        if (!fluid.is(Fluids.WATER) || !fluid.isSource()) {
            return InteractionResult.PASS;
        }
        boolean sneak = sp.isShiftKeyDown();
        if (sneak && data(stack) != null && data(stack).fillLevel() > 0) {
            pour(stack, sp, server);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (data(stack) == null) {
            boolean river = server.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_RIVER);
            fill(stack, sp, server, river ? FlaskData.DIRTY : FlaskData.IMPURIFIED, false);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ItemStack stack = context.getItemInHand();
        // Vanilla crosshairs cannot target fluid blocks: when looking into
        // open water the ray actually lands on the ground BELOW the water
        // and arrives here. Run the fluid-including raycast first so filling
        // works at any shoreline; solid hits (cauldrons) fall through.
        if (player instanceof ServerPlayer sp && level instanceof ServerLevel server) {
            InteractionResult rayResult = tryFillOrPourFromRay(sp, server, stack);
            if (rayResult != InteractionResult.PASS) {
                return rayResult;
            }
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean sneak = player.isShiftKeyDown();

        if (state.is(Blocks.WATER_CAULDRON)) {
            if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.SUCCESS;
            }
            int cauldronLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            if (sneak && data(stack) != null && data(stack).fillLevel() > 0) {
                // Pour back into the cauldron
                pour(stack, sp, server);
                int newLevel = Math.min(3, cauldronLevel + 1);
                level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, newLevel), 3);
                return InteractionResult.SUCCESS_SERVER;
            }
            if (cauldronLevel > 0 && data(stack) == null) {
                boolean heated = BareHandDrinkHandler.isHeatedCauldron(level, pos);
                fill(stack, sp, server, heated ? FlaskData.PURIFIED : FlaskData.DIRTY, true);
                int newLevel = cauldronLevel - 1;
                if (newLevel <= 0) {
                    level.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), 3);
                } else {
                    level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, newLevel), 3);
                }
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.PASS;
        }

        FluidState fluid = state.getFluidState();
        boolean waterSource = fluid.is(Fluids.WATER) && fluid.isSource();
        if (waterSource) {
            if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer sp)) {
                return InteractionResult.SUCCESS;
            }
            if (sneak && data(stack) != null && data(stack).fillLevel() > 0) {
                // Dump carried water back into the source
                pour(stack, sp, server);
                return InteractionResult.SUCCESS_SERVER;
            }
            if (data(stack) == null) {
                boolean river = level.getBiome(pos).is(net.minecraft.tags.BiomeTags.IS_RIVER);
                fill(stack, sp, server, river ? FlaskData.DIRTY : FlaskData.IMPURIFIED, false);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.PASS;
    }

    private static void fill(ItemStack stack, ServerPlayer sp, ServerLevel level, int quality, boolean cauldron) {
        int capacity = ((LeatherFlaskItem) stack.getItem()).capacity();
        stack.set(FlaskItems.FLASK_DATA, new FlaskData(capacity, quality));
        stack.set(DataComponents.CONSUMABLE, FlaskItems.DRINK);
        String purity = switch (quality) {
            case FlaskData.PURIFIED -> "purified";
            case FlaskData.DIRTY -> "dirty";
            default -> "impure";
        };
        if (cauldron && quality == FlaskData.PURIFIED) {
            sp.sendOverlayMessage(Component.literal("You scoop steaming boiled water - purified!"));
        }
        level.playSound(null, sp.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        sp.awardStat(Stats.ITEM_USED.get(stack.getItem()));
        sp.sendOverlayMessage(Component.literal("Filled flask (" + purity + " water)"));
    }

    private static void pour(ItemStack stack, ServerPlayer sp, ServerLevel level) {
        stack.remove(FlaskItems.FLASK_DATA);
        stack.remove(DataComponents.CONSUMABLE);
        level.playSound(null, sp.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f);
        sp.sendOverlayMessage(Component.literal("Poured the flask out"));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
            net.minecraft.world.item.component.TooltipDisplay display,
            java.util.function.Consumer<Component> lines, TooltipFlag flag) {
        FlaskData data = data(stack);
        if (data == null) {
            lines.accept(Component.literal("Empty (" + this.capacity + " uses)")
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
            return;
        }
        lines.accept(Component.literal("Uses: " + data.fillLevel() + "/" + this.capacity)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        String purity = switch (data.qualityLevel()) {
            case FlaskData.PURIFIED -> "Purified";
            case FlaskData.DIRTY -> "Dirty";
            default -> "Impure";
        };
        net.minecraft.ChatFormatting color = switch (data.qualityLevel()) {
            case FlaskData.PURIFIED -> net.minecraft.ChatFormatting.AQUA;
            case FlaskData.DIRTY -> net.minecraft.ChatFormatting.GREEN;
            default -> net.minecraft.ChatFormatting.GRAY;
        };
        lines.accept(Component.literal(purity + " water").withStyle(color));
    }
}
