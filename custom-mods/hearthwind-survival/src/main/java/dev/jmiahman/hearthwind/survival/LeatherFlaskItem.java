package dev.jmiahman.hearthwind.survival;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
 * Portable water vessel, five tiers (capacity {@code 2 + tier}), a
 * behaviour-for-behaviour port of Dehydration 1.3.6 {@code LeatherFlask}
 * (Aged 3.1.2):
 *
 * <ul>
 *   <li>Open water fills the flask to capacity in one use. River-biome
 *       water counts as purified (quality 0); other open water is dirty
 *       (quality 2); topping up a purified/impure flask outside a river
 *       downgrades it to impurified (quality 1).</li>
 *   <li>Vanilla cauldrons fill one unit per use and always with dirty
 *       water; sneaking pours one unit back.</li>
 *   <li>Sneaking at open water empties the flask while keeping purity.</li>
 *   <li>Drinking quenches 4 levels; dirty water rolls amplifier-1 thirst,
 *       impurified amplifier-0 (both at half the dirty chance).</li>
 * </ul>
 *
 * Item ids sit in the dehydration namespace so the migrated datapack gates
 * and job rewards resolve unchanged.
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

    /**
     * Upstream purity transition for filling from an open water source.
     *
     * @param currentQuality the quality carried before the fill
     * @param currentFill    the fill level before the fill
     * @param river          whether the source sits in a river biome
     */
    public static int openWaterQuality(int currentQuality, int currentFill, boolean river) {
        int quality = FlaskData.DIRTY;
        boolean empty = currentFill == 0;
        boolean dirty = currentQuality == FlaskData.DIRTY;
        if (!empty && !dirty) {
            quality = FlaskData.IMPURIFIED;
        }
        if (river && (empty || !dirty)) {
            quality = FlaskData.PURIFIED;
        }
        return quality;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        FlaskData data = data(stack);
        // Upstream finds open water with a source-only raycast inside use():
        // fill (or empty while sneaking) before considering a drink.
        if (player instanceof ServerPlayer sp && level instanceof ServerLevel server) {
            InteractionResult rayResult = tryWaterInteraction(sp, server, stack);
            if (rayResult != InteractionResult.PASS) {
                return rayResult;
            }
        }
        if (data != null && data.fillLevel() > 0) {
            Consumable drink = stack.get(DataComponents.CONSUMABLE);
            if (drink != null) {
                return drink.startConsuming(player, stack, hand);
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    /** Fill from, or (sneaking) empty into, an open water source. */
    private static InteractionResult tryWaterInteraction(ServerPlayer sp, ServerLevel server, ItemStack stack) {
        HitResult ray = sp.pick(sp.blockInteractionRange(), 0.0f, true);
        if (ray.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos pos = ((BlockHitResult) ray).getBlockPos();
        FluidState fluid = server.getFluidState(pos);
        if (!fluid.is(Fluids.WATER) || !fluid.isSource()) {
            return InteractionResult.PASS;
        }
        int fill = data(stack) == null ? 0 : data(stack).fillLevel();
        int quality = data(stack) == null ? FlaskData.DIRTY : data(stack).qualityLevel();
        if (sp.isShiftKeyDown() && fill > 0) {
            emptyFlask(stack, sp, server);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (fill < ((LeatherFlaskItem) stack.getItem()).capacity()) {
            boolean river = server.getBiome(pos).is(BiomeTags.IS_RIVER);
            setFilled(stack, sp, server,
                    ((LeatherFlaskItem) stack.getItem()).capacity(),
                    openWaterQuality(quality, fill, river));
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
        // Open water under the crosshair never reaches useOn in vanilla
        // (the ray lands on the ground below); run the fluid-including
        // raycast first so filling works at any shoreline. Solid hits
        // (cauldrons) fall through to the block logic below.
        if (player instanceof ServerPlayer sp && level instanceof ServerLevel server) {
            InteractionResult rayResult = tryWaterInteraction(sp, server, stack);
            if (rayResult != InteractionResult.PASS) {
                return rayResult;
            }
        }
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer sp)) {
            return state.is(Blocks.WATER_CAULDRON) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        int fill = data(stack) == null ? 0 : data(stack).fillLevel();
        int quality = data(stack) == null ? FlaskData.DIRTY : data(stack).qualityLevel();

        if (state.is(Blocks.WATER_CAULDRON)) {
            int cauldronLevel = state.getValue(LayeredCauldronBlock.LEVEL);
            if (player.isShiftKeyDown() && fill > 0) {
                // Pour one unit back, refilling the cauldron up to 3.
                if (cauldronLevel < 3) {
                    level.setBlock(pos, state.setValue(LayeredCauldronBlock.LEVEL, cauldronLevel + 1), 3);
                }
                setFillOrEmpty(stack, sp, server, fill - 1, quality);
                return InteractionResult.SUCCESS_SERVER;
            }
            if (cauldronLevel > 0 && fill < this.capacity) {
                // Upstream: vanilla cauldrons always hand over dirty water,
                // one unit per use.
                setFilled(stack, sp, server, fill + 1, FlaskData.DIRTY);
                int newLevel = cauldronLevel - 1;
                level.setBlock(pos, newLevel <= 0
                        ? Blocks.CAULDRON.defaultBlockState()
                        : state.setValue(LayeredCauldronBlock.LEVEL, newLevel), 3);
                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    /** Sets the flask content, plays the upstream fill feedback. */
    private static void setFilled(ItemStack stack, ServerPlayer sp, ServerLevel level, int fill, int quality) {
        stack.set(FlaskItems.FLASK_DATA, new FlaskData(fill, quality));
        stack.set(DataComponents.CONSUMABLE, FlaskItems.DRINK);
        level.playSound(null, sp.blockPosition(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 1.0f, 1.0f);
        sp.awardStat(Stats.ITEM_USED.get(stack.getItem()));
    }

    /** Decrements one unit; empties the flask when the last unit is gone. */
    private static void setFillOrEmpty(ItemStack stack, ServerPlayer sp, ServerLevel level, int fill, int quality) {
        if (fill <= 0) {
            emptyFlask(stack, sp, level);
            return;
        }
        stack.set(FlaskItems.FLASK_DATA, new FlaskData(fill, quality));
        stack.set(DataComponents.CONSUMABLE, FlaskItems.DRINK);
        level.playSound(null, sp.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    /** Upstream sneak-use: drops the contents but keeps the flask itself. */
    private static void emptyFlask(ItemStack stack, ServerPlayer sp, ServerLevel level) {
        stack.remove(FlaskItems.FLASK_DATA);
        stack.remove(DataComponents.CONSUMABLE);
        level.playSound(null, sp.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.0f, 1.0f);
        sp.awardStat(Stats.ITEM_USED.get(stack.getItem()));
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
            default -> "Impurified";
        };
        lines.accept(Component.literal(purity + " water")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
