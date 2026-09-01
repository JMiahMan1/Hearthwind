package dev.jmiahman.hearthwind.flora.blockentity;

import dev.jmiahman.hearthwind.flora.FloraStatusEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class FarmMachineryBlockEntities {

    private static Item getItem(String ns, String path) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.fromNamespaceAndPath(ns, path)).orElse(Items.AIR);
    }

    public static class WaterSprinklerBlockEntity extends BlockEntity {
        public WaterSprinklerBlockEntity(BlockPos pos, BlockState state) {
            super(FloraBlockEntities.SPRINKLER, pos, state);
        }

        public static void tick(Level level, BlockPos pos, BlockState state, WaterSprinklerBlockEntity be) {
            if (level.isClientSide()) {
                if (level.getRandom().nextFloat() < 0.4f) {
                    level.addParticle(ParticleTypes.SPLASH,
                            pos.getX() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5,
                            pos.getY() + 0.8,
                            pos.getZ() + 0.5 + (level.getRandom().nextDouble() - 0.5) * 1.5,
                            (level.getRandom().nextDouble() - 0.5) * 0.1, 0.1, (level.getRandom().nextDouble() - 0.5) * 0.1);
                }
                return;
            }

            if (level.getGameTime() % 40 == 0) {
                hydrateArea(level, pos);
            }
        }

        public static void hydrateArea(Level level, BlockPos pos) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    for (int dy = -1; dy <= 0; dy++) {
                        BlockPos target = pos.offset(dx, dy, dz);
                        BlockState bs = level.getBlockState(target);
                        if (bs.is(Blocks.FARMLAND) && bs.hasProperty(BlockStateProperties.MOISTURE) && bs.getValue(BlockStateProperties.MOISTURE) < 7) {
                            level.setBlock(target, bs.setValue(BlockStateProperties.MOISTURE, 7), 2);
                        }
                    }
                }
            }
        }
    }

    public static class FeedingTroughBlockEntity extends BlockEntity {
        private final NonNullList<ItemStack> feed = NonNullList.withSize(2, ItemStack.EMPTY);

        public FeedingTroughBlockEntity(BlockPos pos, BlockState state) {
            super(FloraBlockEntities.FEEDING_TROUGH, pos, state);
        }

        public static void tick(Level level, BlockPos pos, BlockState state, FeedingTroughBlockEntity be) {
            if (level.isClientSide() || level.getGameTime() % 60 != 0) return;

            ItemStack stored = be.feed.get(0);
            if (stored.isEmpty()) return;

            AABB box = new AABB(pos).inflate(4.0);
            List<Animal> animals = level.getEntitiesOfClass(Animal.class, box);
            for (Animal animal : animals) {
                if (animal.getAge() == 0 && animal.canFallInLove() && animal.isFood(stored)) {
                    animal.setInLove(null);
                    stored.shrink(1);
                    level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 0.8f, 1.0f);
                    be.setChanged();
                    break;
                }
            }
        }

        public InteractionResult interact(Player player, InteractionHand hand) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(Items.WHEAT) || held.is(Items.WHEAT_SEEDS) || held.is(Items.CARROT) || held.is(getItem("farm_and_charm", "barley"))) {
                if (feed.get(0).isEmpty()) {
                    feed.set(0, held.split(Math.min(16, held.getCount())));
                    setChanged();
                    if (level != null) level.playSound(null, worldPosition, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.8f, 1.2f);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        }
    }

    public static class ChickenCoopBlockEntity extends BlockEntity {
        private int eggProgress = 0;
        private static final int EGG_INTERVAL = 1200;

        public ChickenCoopBlockEntity(BlockPos pos, BlockState state) {
            super(FloraBlockEntities.CHICKEN_COOP, pos, state);
        }

        public static void tick(Level level, BlockPos pos, BlockState state, ChickenCoopBlockEntity be) {
            if (level.isClientSide()) return;

            be.eggProgress++;
            if (be.eggProgress >= EGG_INTERVAL) {
                be.eggProgress = 0;
                Block.popResource(level, pos, new ItemStack(Items.EGG, 1));
                if (level.getRandom().nextFloat() < 0.3f) {
                    Block.popResource(level, pos, new ItemStack(Items.FEATHER, 1));
                }
                level.playSound(null, pos, SoundEvents.CHICKEN_EGG, SoundSource.BLOCKS, 0.8f, 1.0f);
                be.setChanged();
            }
        }
    }

    public static class DinnerBellBlockEntity extends BlockEntity {
        public DinnerBellBlockEntity(BlockPos pos, BlockState state) {
            super(FloraBlockEntities.DINNER_BELL, pos, state);
        }

        public InteractionResult ring(Player player) {
            if (level == null) return InteractionResult.PASS;
            level.playSound(null, worldPosition, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 1.5f, 1.0f);

            AABB radius = new AABB(worldPosition).inflate(16.0);
            List<Player> nearbyPlayers = level.getEntitiesOfClass(Player.class, radius);
            for (Player p : nearbyPlayers) {
                p.addEffect(new MobEffectInstance(FloraStatusEffects.WELL_SERVED, 1200, 0));
                p.addEffect(new MobEffectInstance(FloraStatusEffects.REFRESHED, 1200, 0));
            }
            return InteractionResult.SUCCESS;
        }
    }
}
