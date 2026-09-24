package net.adventurez.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.adventurez.entity.PiglinBeastEntity;
import net.adventurez.init.ConfigInit;
import net.adventurez.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@SuppressWarnings("deprecation")
@Mixin(DropExperienceBlock.class)
public abstract class ExperienceDroppingBlockMixin extends Block {

    public ExperienceDroppingBlockMixin(Properties settings) {
        super(settings);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool) {
        if (!level.isClientSide() && level instanceof ServerLevel serverWorld && state.is(Blocks.NETHER_GOLD_ORE) && ConfigInit.CONFIG.piglin_beast_ore_spawn_chance != 0) {
            if (!player.isCreative() && level.dimension() == Level.NETHER
                    && player.getMainHandItem().getEnchantments().keySet().stream().anyMatch(holder -> holder.is(Enchantments.SILK_TOUCH))) {
                if (level.getEntitiesOfClass(PiglinBeastEntity.class, player.getBoundingBox().inflate(40D), EntitySelector.NO_SPECTATORS).isEmpty()) {
                    int spawnChanceInt = level.getRandom().nextInt(ConfigInit.CONFIG.piglin_beast_ore_spawn_chance) + 1;
                    if (spawnChanceInt == 1) {
                        PiglinBeastEntity beastEntity = EntityInit.PIGLIN_BEAST.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                        if (beastEntity == null) {
                            super.playerDestroy(level, player, pos, state, blockEntity, tool);
                            return;
                        }
                        int posYOfPlayer = player.blockPosition().getY();
                        for (int counter = 0; counter < 100; counter++) {
                            float randomFloat = level.getRandom().nextFloat() * 6.2831855F;
                            int posX = pos.getX() + Mth.floor(Mth.cos(randomFloat) * 18.0F + level.getRandom().nextInt(16));
                            int posZ = pos.getZ() + Mth.floor(Mth.sin(randomFloat) * 18.0F + level.getRandom().nextInt(16));
                            int posY = posYOfPlayer - 20 + level.getRandom().nextInt(40);
                            BlockPos spawnPos = new BlockPos(posX, posY, posZ);

                            if (level.hasChunksAt(spawnPos.getX() - 4, spawnPos.getY() - 4, spawnPos.getZ() - 4, spawnPos.getX() + 4, spawnPos.getY() + 4, spawnPos.getZ() + 4)
                                    && SpawnPlacements.isSpawnPositionOk(EntityInit.PIGLIN_BEAST, serverWorld, spawnPos)
                                    && SpawnPlacements.checkSpawnRules(EntityInit.PIGLIN_BEAST, serverWorld, EntitySpawnReason.EVENT, spawnPos, level.getRandom())) {
                                beastEntity.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
                                beastEntity.setYRot(0.0F);
                                beastEntity.setXRot(0.0F);
                                beastEntity.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(spawnPos), EntitySpawnReason.EVENT, null);
                                level.addFreshEntity(beastEntity);
                                beastEntity.spawnAnim();
                                break;
                            }
                        }
                    }
                }
            }
        }
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
    }

}
