package net.adventurez.block.entity;

import net.adventurez.entity.DragonEntity;
import net.adventurez.entity.TheEyeEntity;
import net.adventurez.init.BlockInit;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.EffectInit;
import net.adventurez.init.EntityInit;
import net.adventurez.init.TagInit;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class DragonEggEntity extends BlockEntity {
    private int overallSummoningTick;
    private int overallHatchingTick;
    private int hatchTick;
    private int summoningTick;
    private boolean isHatchAble;

    public DragonEggEntity(BlockPos pos, BlockState state) {
        super(BlockInit.DRAGON_EGG_ENTITY, pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        hatchTick = input.getIntOr("Hatch_Tick", 0);
        isHatchAble = input.getBooleanOr("Hatch_Able", false);
        summoningTick = input.getIntOr("Summoning_Tick", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Hatch_Able", isHatchAble);
        output.putInt("Hatch_Tick", hatchTick);
        output.putInt("Summoning_Tick", summoningTick);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, DragonEggEntity blockEntity) {
        blockEntity.tick();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DragonEggEntity blockEntity) {
        blockEntity.tick();
    }

    @Override
    public void setChanged() {
        super.setChanged();
        sendUpdate();
    }

    private void sendUpdate() {
        if (this.getLevel() != null) {
            BlockState state = this.getLevel().getBlockState(this.getBlockPos());
            this.getLevel().sendBlockUpdated(this.getBlockPos(), state, state, 3);
        }
    }

    private boolean isValid(Level level, BlockPos pos, BlockState state) {
        int stoneCounter;
        int stoneCounter2;
        int stoneCounter3 = 0;
        int stoneCounter4;
        int stoneCounter5;
        BlockState rod_1 = level.getBlockState(pos.north().east());
        BlockState rod_2 = level.getBlockState(pos.north().west());
        BlockState rod_3 = level.getBlockState(pos.south().east());
        BlockState rod_4 = level.getBlockState(pos.south().west());
        if (rod_1.equals(Blocks.END_ROD.defaultBlockState()) && rod_2.equals(Blocks.END_ROD.defaultBlockState()) && rod_3.equals(Blocks.END_ROD.defaultBlockState())
                && rod_4.equals(Blocks.END_ROD.defaultBlockState())) {
            for (stoneCounter4 = -1; stoneCounter4 < 2; stoneCounter4++) {
                for (stoneCounter5 = -1; stoneCounter5 < 2; stoneCounter5++) {
                    BlockState middleState = level.getBlockState(pos.north(stoneCounter4).east(stoneCounter5).below());
                    if (middleState.getBlock().equals(Blocks.CRYING_OBSIDIAN)) {
                        stoneCounter3++;
                    }
                }
            }
            for (stoneCounter = -2; stoneCounter < 3; stoneCounter++) {
                for (stoneCounter2 = -2; stoneCounter2 < 3; stoneCounter2++) {
                    BlockState baseState = level.getBlockState(pos.north(stoneCounter).east(stoneCounter2).below(2));
                    if (baseState.is(TagInit.PLATFORM_END_BLOCKS)) {
                        stoneCounter3++;
                    }
                }
            }
        }
        if (stoneCounter3 == 34) {
            return true;
        } else {
            return false;
        }
    }

    private void tick() {
        this.updateTheEyeSummoning();
        this.updateDragonHatching();
    }

    private void updateDragonHatching() {
        if (ConfigInit.CONFIG.allow_dragon_hatching && !this.getLevel().isClientSide()) {
            overallHatchingTick++;
            if (overallHatchingTick == 20) {
                if (!this.isHatchAble) {
                    if (((ServerLevel) this.getLevel()).getDragonFight() != null && !((ServerLevel) this.getLevel()).getDragonFight().hasPreviouslyKilledDragon())
                        return;
                    Player playerEntity = this.getLevel().getNearestPlayer(this.getBlockPos().getX() + 0.5D, this.getBlockPos().getY(), this.getBlockPos().getZ() + 0.5D, 2.5D, player -> true);
                    if (playerEntity != null && playerEntity.hasEffect(EffectInit.FAME))
                        enableEggHatching();
                } else {
                    this.hatchTick++;
                    if (this.hatchTick % 60 == 0) {
                        for (int i = 0; i < 20; i++) {
                    double d = (double) this.getBlockPos().getX() + (double) this.getLevel().getRandom().nextFloat();
                    double e = (double) this.getBlockPos().getY() + (double) this.getLevel().getRandom().nextFloat();
                    double f = (double) this.getBlockPos().getZ() + (double) this.getLevel().getRandom().nextFloat();
                    ((ServerLevel) this.getLevel()).sendParticles(ParticleTypes.HAPPY_VILLAGER, d, e, f, 0, 0.0D, 0.0D, 0.0D, 0.01D);
                }
            }
            if (this.hatchTick >= 598) {
                this.getLevel().destroyBlock(this.getBlockPos(), false);
                        DragonEntity dragonEntity = EntityInit.DRAGON.create(this.getLevel(), net.minecraft.world.entity.EntitySpawnReason.STRUCTURE);
                        dragonEntity.setPos((double) this.getBlockPos().getX() + 0.5D, (double) this.getBlockPos().getY() + 0.55D, (double) this.getBlockPos().getZ() + 0.5D); dragonEntity.setYRot(90.0F); dragonEntity.setXRot(0.0F);
                        dragonEntity.finalizeSpawn(((ServerLevel) this.getLevel()), ((ServerLevel) this.getLevel()).getCurrentDifficultyAt(this.getBlockPos()), EntitySpawnReason.STRUCTURE, null);
                        dragonEntity.setSize(1);
                        this.getLevel().addFreshEntity(dragonEntity);
                    }
                }

            }
            if (overallHatchingTick >= 20) {
                overallHatchingTick = 0;
            }
        }
    }

    private void updateTheEyeSummoning() {
        if (ConfigInit.CONFIG.allow_the_eye_summoning) {
            if (this.getLevel().getBlockState(this.getBlockPos().below()).equals(Blocks.CRYING_OBSIDIAN.defaultBlockState()) && this.getLevel().dimension() == Level.END) {
                overallSummoningTick++;
                BlockState state = this.getBlockState();
                if (overallSummoningTick == 20 && this.isValid(this.getLevel(), this.getBlockPos(), state)) {
                    summoningTick++;
                    if (!this.getLevel().isClientSide() && summoningTick >= 60) {
                        TheEyeEntity theEyeEntity = (TheEyeEntity) EntityInit.THE_EYE.create(this.getLevel(), net.minecraft.world.entity.EntitySpawnReason.STRUCTURE);
                        theEyeEntity.setPos((double) this.getBlockPos().getX() + 0.5D, (double) this.getBlockPos().getY() + 0.55D, (double) this.getBlockPos().getZ() + 0.5D); theEyeEntity.setYRot(90.0F); theEyeEntity.setXRot(0.0F);
                        theEyeEntity.finalizeSpawn(((ServerLevel) this.getLevel()), ((ServerLevel) this.getLevel()).getCurrentDifficultyAt(this.getBlockPos()), EntitySpawnReason.STRUCTURE, null);
                        theEyeEntity.setEyeInvulnerabletime();
                        this.getLevel().addFreshEntity(theEyeEntity);
                        this.getLevel().destroyBlock(this.getBlockPos(), false);
                    }
                }
                if (overallSummoningTick >= 20) {
                    overallSummoningTick = 0;
                }
                if (this.getLevel().isClientSide() && summoningTick != 0) {
                    if (summoningTick % 10 == 0) {
                        this.dragonAltartParticle(1);
                    }
                    if (summoningTick % 5 == 0) {
                        this.dragonAltartParticle(2);
                    }
                }

            }
        }
    }

    private void dragonAltartParticle(int distance) {
        Level world = this.getLevel();
        double d = (double) this.getBlockPos().east(distance).north(distance).getX() + (double) world.getRandom().nextFloat();
        double e = (double) this.getBlockPos().above(1 - distance).getY() + (double) world.getRandom().nextFloat() * 2.0D;
        double f = (double) this.getBlockPos().east(distance).north(distance).getZ() + (double) world.getRandom().nextFloat();
        double d1 = (double) this.getBlockPos().west(distance).north(distance).getX() + (double) world.getRandom().nextFloat();
        double e1 = (double) this.getBlockPos().above(1 - distance).getY() + (double) world.getRandom().nextFloat() * 2.0D;
        double f1 = (double) this.getBlockPos().west(distance).north(distance).getZ() + (double) world.getRandom().nextFloat();
        double d2 = (double) this.getBlockPos().south(distance).east(distance).getX() + (double) world.getRandom().nextFloat();
        double e2 = (double) this.getBlockPos().above(1 - distance).getY() + (double) world.getRandom().nextFloat() * 2.0D;
        double f2 = (double) this.getBlockPos().south(distance).east(distance).getZ() + (double) world.getRandom().nextFloat();
        double d3 = (double) this.getBlockPos().south(distance).west(distance).getX() + (double) world.getRandom().nextFloat();
        double e3 = (double) this.getBlockPos().above(1 - distance).getY() + (double) world.getRandom().nextFloat() * 2.0D;
        double f3 = (double) this.getBlockPos().south(distance).west(distance).getZ() + (double) world.getRandom().nextFloat();
        world.addParticle(ParticleTypes.END_ROD, d, e, f, 0.0D, 0.0D, 0.0D);
        world.addParticle(ParticleTypes.END_ROD, d1, e1, f1, 0.0D, 0.0D, 0.0D);
        world.addParticle(ParticleTypes.END_ROD, d2, e2, f2, 0.0D, 0.0D, 0.0D);
        world.addParticle(ParticleTypes.END_ROD, d3, e3, f3, 0.0D, 0.0D, 0.0D);
    }

    public void enableEggHatching() {
        this.isHatchAble = true;
    }

}