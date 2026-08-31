package dev.jmiahman.hearthwind.primitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Crafting rock: the earlystage knapping station.
 *
 * The top surface is divided into a 3x3 grid. Right-click with a usable item
 * (see the {@code earlystage:usable_crafting_rock_items} tag) while aiming at
 * a cell to place one into that cell; right-click with an empty hand to take
 * an ingredient back; right-click holding a {@code earlystage:rock} to hammer
 * the grid - once enough hits accumulate the grid is crafted (all 4 rotations
 * tried, vanilla crafting recipes) with the result landing in the middle
 * slot. After {@code craftRockMaxCraftHits} total hits the rock breaks.
 */
public class CraftingRockBlock extends Block implements EntityBlock {

    public static final ResourceKey<Block> KEY =
            ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath("earlystage", "crafting_rock"));

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape BOTTOM_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    public CraftingRockBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public net.minecraft.world.level.block.entity.BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CraftingRockBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return BOTTOM_SHAPE;
    }

    @Override
    protected boolean isCollisionShapeFullBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, net.minecraft.world.entity.player.Player player,
            InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) {
            return InteractionResult.PASS;
        }
        CraftingRockBlockEntity rock = (CraftingRockBlockEntity) be;

        double yFrac = Math.abs(hit.getLocation().y % 1.0);
        if (!(yFrac > 0.495 && yFrac < 0.505)) {
            return InteractionResult.PASS;
        }

        ItemStack held = player.getItemInHand(hand);

        if (held.is(HearthwindPrimitiveBlocks.ROCK.asItem())) {
            if (!rock.isEmpty()) {
                if (!level.isClientSide()) {
                    HearthwindPrimitiveConfig cfg = HearthwindPrimitiveConfig.get();
                    if (rock.getCraftHits() - 1 <= 0) {
                        tryCraftItem(level, player, rock);
                        rock.setCraftHits(cfg.craftRockCraftHits
                                + level.getRandom().nextInt(Math.max(1, cfg.craftRockCraftHits / 2)));
                    } else {
                        rock.decreaseCraftHits(player);
                    }
                }
                level.playSound(player, pos, net.minecraft.sounds.SoundEvents.STONE_HIT,
                        net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.0f);
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }

        double relX = ((hit.getLocation().x - pos.getX()) % 1.0 + 1.0) % 1.0;
        double relZ = ((hit.getLocation().z - pos.getZ()) % 1.0 + 1.0) % 1.0;
        int slot = getSlot(relX, relZ);
        if (rock.getItem(slot).isEmpty() && !held.isEmpty()
                && held.is(HearthwindPrimitiveTags.USABLE_CRAFTING_ROCK_ITEMS)) {
            if (!level.isClientSide()) {
                rock.setItem(slot, new ItemStack(held.getItem(), 1));
                if (!player.hasInfiniteMaterials()) {
                    held.shrink(1);
                }
                resetCraftHits(level, rock);
            }
            return InteractionResult.CONSUME;
        } else if (!rock.getItem(slot).isEmpty()) {
            if (!level.isClientSide()) {
                if (!player.getAbilities().instabuild) {
                    player.getInventory().placeItemBackInInventory(rock.getItem(slot));
                }
                rock.setItem(slot, new ItemStack(Items.AIR));
                resetCraftHits(level, rock);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private static int getSlot(double x, double z) {
        int slot = 0;
        for (int i = 2; i >= 0; i--) {
            for (int u = 2; u >= 0; u--) {
                if (x > i * 0.33D && z > u * 0.33D) {
                    return slot;
                }
                slot++;
            }
        }
        return 0;
    }

    private void resetCraftHits(Level level, CraftingRockBlockEntity rock) {
        HearthwindPrimitiveConfig cfg = HearthwindPrimitiveConfig.get();
        rock.setCraftHits(cfg.craftRockCraftHits
                + level.getRandom().nextInt(Math.max(1, cfg.craftRockCraftHits / 2)));
    }

    private void tryCraftItem(Level level, Player player, CraftingRockBlockEntity rock) {
        if (!level.isClientSide()) {
            CraftingInput craftingInput = null;
            Optional<RecipeHolder<CraftingRecipe>> optional = null;
            for (int i = 0; i < 4; i++) {
                List<ItemStack> mapped = new ArrayList<>(9);
                for (int s = 0; s < 9; s++) {
                    mapped.add(rock.getItem(getVariantSlot(i, s)).copy());
                }
                craftingInput = CraftingInput.of(3, 3, mapped);
                optional = ((net.minecraft.server.level.ServerLevel) level).recipeAccess()
                        .getRecipeFor(RecipeType.CRAFTING, craftingInput, level);
                if (optional.isPresent()) {
                    break;
                }
            }
            if (optional != null && optional.isPresent()
                    && canCraftRecipe(level, player, optional.get())) {
                rock.clearContent();
                ItemStack result = optional.get().value().assemble(craftingInput);
                dev.jmiahman.hearthwind.primitive.tiered.TieredData.applyRandomTierIfEligible(result, player.getRandom());
                rock.setItem(4, result);
            }
        }
    }

    private boolean canCraftRecipe(Level level, Player player, RecipeHolder<CraftingRecipe> holder) {
        if (holder.value().isSpecial()) {
            return true;
        }
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel
                && serverLevel.getGameRules().get(GameRules.LIMITED_CRAFTING)) {
            return player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && serverPlayer.getRecipeBook().contains(holder.id());
        }
        return true;
    }

    private static int getVariantSlot(int variant, int i) {
        if (variant == 0) {
            return i;
        } else if (variant == 1) {
            return Math.abs(i - 8);
        } else if (variant == 2) {
            return switch (i) {
                case 0 -> 6;
                case 1 -> 3;
                case 2 -> 0;
                case 3 -> 7;
                case 5 -> 1;
                case 6 -> 8;
                case 7 -> 5;
                case 8 -> 2;
                default -> 4;
            };
        } else if (variant == 3) {
            return switch (i) {
                case 0 -> 2;
                case 1 -> 5;
                case 2 -> 8;
                case 3 -> 1;
                case 5 -> 7;
                case 6 -> 0;
                case 7 -> 3;
                case 8 -> 6;
                default -> 4;
            };
        }
        return 0;
    }

    @Override
    public void destroy(net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CraftingRockBlockEntity rock) {
                net.minecraft.world.Containers.dropContents(serverLevel, pos, rock);
            }
        }
        super.destroy(level, pos, state);
    }

    public static class CraftingRockItem extends net.minecraft.world.item.BlockItem {
        public CraftingRockItem(Block block, Item.Properties props) {
            super(block, props);
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                TooltipDisplay display, java.util.function.Consumer<Component> tooltip,
                TooltipFlag flag) {
            if (HearthwindPrimitiveConfig.get().infoTooltips) {
                tooltip.accept(Component.translatable("block.earlystage.crafting_rock.tooltip"));
            }
        }
    }
}
