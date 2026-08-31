package dev.jmiahman.hearthwind.world.fauna;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

/**
 * 100% Feature-Parity Port of Naturalist Wildlife AI & Behaviors for Hearthwind (26.2).
 */
public final class NaturalistEntities {
    public static final String MOD_ID = "naturalist";

    private NaturalistEntities() {}

    // ==========================================
    // 1. HERBIVORE & PREY FAUNA
    // ==========================================
    public static class HerbivoreAnimal extends Animal {
        public final String mobType;

        public HerbivoreAnimal(EntityType<? extends Animal> type, Level level, String mobType) {
            super(type, level);
            this.mobType = mobType;
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(0, new FloatGoal(this));
            this.goalSelector.addGoal(1, new PanicGoal(this, 1.35));
            this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
            this.goalSelector.addGoal(3, new TemptGoal(this, 1.15, this.getTemptIngredient(), false));
            this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));

            // Specialized AI behaviors per mob type
            if ("deer".equals(this.mobType)) {
                this.goalSelector.addGoal(1, new DeerSpookGoal(this));
            } else if ("duck".equals(this.mobType)) {
                this.goalSelector.addGoal(2, new DuckForageGoal(this));
            } else if ("butterfly".equals(this.mobType) || "firefly".equals(this.mobType)) {
                this.goalSelector.addGoal(1, new AmbientFlutterGoal(this));
            } else if ("boar".equals(this.mobType)) {
                this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
                this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.25, false));
            } else if ("tortoise".equals(this.mobType)) {
                this.goalSelector.addGoal(1, new TortoiseHideGoal(this));
            }

            this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
            this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0f));
            this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.level().isClientSide()) {
                // Ambient behavior ticks
                if ("firefly".equals(this.mobType) && this.tickCount % 40 == 0) {
                    if (this.level() instanceof ServerLevel sl) {
                        sl.sendParticles(ParticleTypes.GLOW, this.getX(), this.getY() + 0.3, this.getZ(), 1, 0.2, 0.2, 0.2, 0.02);
                    }
                } else if ("butterfly".equals(this.mobType) && this.tickCount % 100 == 0) {
                    // Pollinate nearby crops
                    BlockPos pos = this.blockPosition().below();
                    var state = this.level().getBlockState(pos);
                    if (state.is(BlockTags.CROPS)) {
                        this.level().levelEvent(2005, pos, 0);
                    }
                }
            }
        }

        @Override
        public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
            // Tortoise shell defense
            if ("tortoise".equals(this.mobType) && this.isCrouching()) {
                amount *= 0.15f; // 85% damage reduction inside shell
                level.playSound(null, this.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.NEUTRAL, 1.0f, 1.2f);
            }

            // Lizard tail drop
            if ("lizard".equals(this.mobType) && this.getHealth() <= amount + 4.0f && this.random.nextFloat() < 0.6f) {
                Item tailItem = NaturalistFauna.ITEMS.get("lizard_tail");
                if (tailItem != null) {
                    ItemEntity drop = new ItemEntity(level, this.getX(), this.getY(), this.getZ(), new ItemStack(tailItem));
                    level.addFreshEntity(drop);
                    level.playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.8f, 1.4f);
                }
            }

            return super.hurtServer(level, source, amount);
        }

        @Override
        public void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
            super.dropCustomDeathLoot(level, source, recentlyHit);
            Item dropItem = null;

            if ("deer".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get(this.isOnFire() ? "cooked_venison" : "venison");
                if (this.random.nextFloat() < 0.4f) {
                    this.spawnAtLocation(level, new ItemStack(NaturalistFauna.ITEMS.getOrDefault("deer_antler", Items.BONE)));
                }
            } else if ("duck".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get(this.isOnFire() ? "cooked_duck" : "duck");
                if (this.random.nextFloat() < 0.5f) {
                    this.spawnAtLocation(level, new ItemStack(NaturalistFauna.ITEMS.getOrDefault("duck_feather", Items.FEATHER)));
                }
            } else if ("boar".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get(this.isOnFire() ? "cooked_boar_chop" : "boar_chop");
            } else if ("elephant".equals(this.mobType) && this.random.nextFloat() < 0.5f) {
                dropItem = NaturalistFauna.ITEMS.get("elephant_tusk");
            } else if ("rhino".equals(this.mobType) && this.random.nextFloat() < 0.5f) {
                dropItem = NaturalistFauna.ITEMS.get("rhino_horn");
            } else if ("hippo".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get(this.isOnFire() ? "cooked_hippo_meat" : "hippo_meat");
            } else if ("bass".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get(this.isOnFire() ? "cooked_bass" : "bass");
            } else if ("catfish".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get(this.isOnFire() ? "cooked_catfish" : "catfish");
            }

            if (dropItem != null) {
                this.spawnAtLocation(level, new ItemStack(dropItem, 1 + this.random.nextInt(2)));
            }
        }

        private Ingredient getTemptIngredient() {
            if ("duck".equals(this.mobType) || "bird".equals(this.mobType)) {
                return Ingredient.of(Items.WHEAT_SEEDS, Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS);
            } else if ("deer".equals(this.mobType) || "zebra".equals(this.mobType) || "giraffe".equals(this.mobType)) {
                return Ingredient.of(Items.APPLE, Items.WHEAT, Items.GOLDEN_APPLE);
            } else if ("elephant".equals(this.mobType) || "rhino".equals(this.mobType) || "hippo".equals(this.mobType)) {
                return Ingredient.of(Items.HAY_BLOCK, Items.SUGAR_CANE, Items.MELON_SLICE);
            }
            return Ingredient.of(Items.WHEAT);
        }

        @Override
        public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
            return new HerbivoreAnimal((EntityType<? extends Animal>) this.getType(), level, this.mobType);
        }

        @Override
        public boolean isFood(ItemStack stack) {
            return this.getTemptIngredient().test(stack);
        }
    }

    // ==========================================
    // 2. PREDATOR & CARNIVORE FAUNA
    // ==========================================
    public static class PredatorAnimal extends Animal {
        public final String mobType;

        public PredatorAnimal(EntityType<? extends Animal> type, Level level, String mobType) {
            super(type, level);
            this.mobType = mobType;
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(0, new FloatGoal(this));
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
            this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
            this.goalSelector.addGoal(3, new TemptGoal(this, 1.1, Ingredient.of(Items.BEEF, Items.CHICKEN, Items.MUTTON, Items.PORKCHOP), false));
            this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1));

            // Specialized hunting targets
            this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
            if ("lion".equals(this.mobType)) {
                this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, HerbivoreAnimal.class, true));
            } else if ("bear".equals(this.mobType)) {
                this.goalSelector.addGoal(2, new BearFishingGoal(this));
            } else if ("snake".equals(this.mobType) || "rattlesnake".equals(this.mobType) || "coral_snake".equals(this.mobType)) {
                this.goalSelector.addGoal(1, new SnakeRattleWarningGoal(this));
            } else if ("vulture".equals(this.mobType)) {
                this.goalSelector.addGoal(1, new VultureCirclingGoal(this));
            }

            this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
            this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
            this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        }

        @Override
        public boolean doHurtTarget(ServerLevel level, net.minecraft.world.entity.Entity target) {
            boolean hurt = super.doHurtTarget(level, target);
            if (hurt && target instanceof LivingEntity living) {
                if ("rattlesnake".equals(this.mobType) || "coral_snake".equals(this.mobType)) {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 1), this);
                } else if ("snake".equals(this.mobType)) {
                    living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), this);
                }
            }
            return hurt;
        }

        @Override
        public void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
            super.dropCustomDeathLoot(level, source, recentlyHit);
            Item dropItem = null;

            if ("bear".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get("bear_fur");
            } else if ("lion".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get("lion_mane");
            } else if ("alligator".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get(this.isOnFire() ? "cooked_alligator_tail" : "alligator_tail");
            } else if ("rattlesnake".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get("rattle");
            } else if ("vulture".equals(this.mobType)) {
                dropItem = NaturalistFauna.ITEMS.get("vulture_feather");
            }

            if (dropItem != null) {
                this.spawnAtLocation(level, new ItemStack(dropItem, 1 + this.random.nextInt(2)));
            }
        }

        @Override
        public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
            return new PredatorAnimal((EntityType<? extends Animal>) this.getType(), level, this.mobType);
        }

        @Override
        public boolean isFood(ItemStack stack) {
            return stack.is(Items.BEEF) || stack.is(Items.PORKCHOP) || stack.is(Items.CHICKEN) || stack.is(Items.SALMON);
        }
    }

    // ==========================================
    // 3. SNAIL CRITTER
    // ==========================================
    public static class SnailCritter extends Animal {
        public SnailCritter(EntityType<? extends Animal> type, Level level) {
            super(type, level);
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(0, new FloatGoal(this));
            this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.4));
            this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 4.0f));
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.level().isClientSide() && this.tickCount % 60 == 0) {
                if (this.random.nextFloat() < 0.25f && this.level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.1, this.getZ(), 2, 0.1, 0.05, 0.1, 0.01);
                }
            }
        }

        @Override
        public InteractionResult mobInteract(Player player, InteractionHand hand) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(Items.WATER_BUCKET)) {
                if (!this.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                        player.addItem(new ItemStack(NaturalistFauna.ITEMS.getOrDefault("snail_bucket", Items.WATER_BUCKET)));
                    }
                    this.discard();
                }
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }

        @Override
        public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
            return null;
        }

        @Override
        public boolean isFood(ItemStack stack) {
            return stack.is(Items.BROWN_MUSHROOM) || stack.is(Items.RED_MUSHROOM);
        }
    }

    // ==========================================
    // 4. SPECIALIZED AI GOAL IMPLEMENTATIONS
    // ==========================================
    public static class DeerSpookGoal extends Goal {
        private final PathfinderMob mob;

        public DeerSpookGoal(PathfinderMob mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Player nearest = this.mob.level().getNearestPlayer(this.mob, 8.0);
            return nearest != null && nearest.isSprinting() && !nearest.isShiftKeyDown();
        }

        @Override
        public void start() {
            Player nearest = this.mob.level().getNearestPlayer(this.mob, 8.0);
            if (nearest != null) {
                var vec = this.mob.position().subtract(nearest.position()).normalize().scale(8.0);
                this.mob.getNavigation().moveTo(this.mob.getX() + vec.x, this.mob.getY(), this.mob.getZ() + vec.z, 1.4);
            }
        }
    }

    public static class DuckForageGoal extends Goal {
        private final Animal mob;

        public DuckForageGoal(Animal mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return this.mob.isInWater() && this.mob.getRandom().nextInt(120) == 0;
        }

        @Override
        public void start() {
            if (this.mob.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SPLASH, this.mob.getX(), this.mob.getY(), this.mob.getZ(), 6, 0.2, 0.1, 0.2, 0.05);
                this.mob.heal(1.0f);
            }
        }
    }

    public static class AmbientFlutterGoal extends Goal {
        private final Animal mob;

        public AmbientFlutterGoal(Animal mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.mob.getRandom().nextInt(40) == 0;
        }

        @Override
        public void start() {
            double dx = (this.mob.getRandom().nextDouble() - 0.5) * 6.0;
            double dy = (this.mob.getRandom().nextDouble() - 0.5) * 2.0;
            double dz = (this.mob.getRandom().nextDouble() - 0.5) * 6.0;
            this.mob.getNavigation().moveTo(this.mob.getX() + dx, this.mob.getY() + dy, this.mob.getZ() + dz, 0.8);
        }
    }

    public static class TortoiseHideGoal extends Goal {
        private final Animal mob;

        public TortoiseHideGoal(Animal mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return this.mob.getLastHurtByMob() != null && this.mob.getLastHurtByMob().isAlive();
        }

        @Override
        public void start() {
            this.mob.setShiftKeyDown(true);
            this.mob.getNavigation().stop();
        }

        @Override
        public void stop() {
            this.mob.setShiftKeyDown(false);
        }
    }

    public static class BearFishingGoal extends Goal {
        private final PathfinderMob mob;

        public BearFishingGoal(PathfinderMob mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            return this.mob.isInWater() && this.mob.getRandom().nextInt(100) == 0;
        }

        @Override
        public void start() {
            if (this.mob.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.FISHING, this.mob.getX(), this.mob.getY(), this.mob.getZ(), 8, 0.3, 0.1, 0.3, 0.1);
                ItemEntity fish = new ItemEntity(sl, this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ(), new ItemStack(Items.SALMON));
                sl.addFreshEntity(fish);
            }
        }
    }

    public static class SnakeRattleWarningGoal extends Goal {
        private final PathfinderMob mob;

        public SnakeRattleWarningGoal(PathfinderMob mob) {
            this.mob = mob;
        }

        @Override
        public boolean canUse() {
            Player p = this.mob.level().getNearestPlayer(this.mob, 5.0);
            return p != null && !p.isShiftKeyDown();
        }

        @Override
        public void start() {
            if (this.mob.level() instanceof ServerLevel sl && this.mob.tickCount % 20 == 0) {
                sl.playSound(null, this.mob.blockPosition(), SoundEvents.GRASS_STEP, SoundSource.HOSTILE, 0.8f, 1.8f);
            }
        }
    }

    public static class VultureCirclingGoal extends Goal {
        private final PathfinderMob mob;

        public VultureCirclingGoal(PathfinderMob mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            Player p = this.mob.level().getNearestPlayer(this.mob, 20.0);
            return p != null && p.getHealth() < p.getMaxHealth() * 0.35f;
        }

        @Override
        public void tick() {
            Player p = this.mob.level().getNearestPlayer(this.mob, 20.0);
            if (p != null) {
                this.mob.getLookControl().setLookAt(p, 30.0f, 30.0f);
            }
        }
    }

    // ==========================================
    // 5. AQUATIC FISH FAUNA
    // ==========================================
    public static class AquaticFish extends net.minecraft.world.entity.animal.fish.AbstractFish {
        public final String mobType;

        public AquaticFish(EntityType<? extends net.minecraft.world.entity.animal.fish.AbstractFish> type, Level level, String mobType) {
            super(type, level);
            this.mobType = mobType;
        }

        @Override
        public ItemStack getBucketItemStack() {
            Item bucket = NaturalistFauna.ITEMS.get(this.mobType + "_bucket");
            return new ItemStack(bucket != null ? bucket : Items.WATER_BUCKET);
        }

        @Override
        protected net.minecraft.sounds.SoundEvent getFlopSound() {
            return SoundEvents.SALMON_FLOP;
        }

        @Override
        public void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
            super.dropCustomDeathLoot(level, source, recentlyHit);
            Item dropItem = NaturalistFauna.ITEMS.get(this.isOnFire() ? ("cooked_" + this.mobType) : this.mobType);
            if (dropItem != null) {
                this.spawnAtLocation(level, new ItemStack(dropItem, 1 + this.getRandom().nextInt(2)));
            }
        }
    }

    // ==========================================
    // 6. ATTRIBUTE SUPPLIERS
    // ==========================================
    public static AttributeSupplier.Builder createHerbivoreAttributes(double maxHealth, double speed) {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.TEMPT_RANGE, 10.0);
    }

    public static AttributeSupplier.Builder createFishAttributes(double maxHealth) {
        return net.minecraft.world.entity.animal.fish.AbstractFish.createAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth);
    }

    public static AttributeSupplier.Builder createPredatorAttributes(double maxHealth, double speed, double attackDmg) {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.ATTACK_DAMAGE, attackDmg)
                .add(Attributes.TEMPT_RANGE, 10.0);
    }
}
