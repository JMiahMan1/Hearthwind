package net.satisfy.vinery.core.item;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.satisfy.vinery.core.entity.DarkCherryBoatEntity;
import net.satisfy.vinery.core.entity.DarkCherryChestBoatEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Predicate;

public class DarkCherryBoatItem extends BoatItem {
    private static final Predicate<Entity> ENTITY_PREDICATE = EntitySelector.NO_SPECTATORS.and(Entity::isPickable);
    private final DarkCherryBoatEntity.Type type;
    private final boolean hasChest;

    public DarkCherryBoatItem(boolean hasChest, DarkCherryBoatEntity.Type type, Properties pProperties) {
        super(hasChest ? net.minecraft.world.entity.EntityTypes.CHERRY_CHEST_BOAT : net.minecraft.world.entity.EntityTypes.CHERRY_BOAT, pProperties);
        this.hasChest = hasChest;
        this.type = type;
    }

    @Override
    public @NotNull InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        HitResult hitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.ANY);
        if (hitresult.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        } else {
            Vec3 vec3 = pPlayer.getViewVector(1.0F);
            List<Entity> list = pLevel.getEntities(pPlayer, pPlayer.getBoundingBox().expandTowards(vec3.scale(5.0D)).inflate(1.0D), ENTITY_PREDICATE);
            if (!list.isEmpty()) {
                Vec3 vec31 = pPlayer.getEyePosition();

                for(Entity entity : list) {
                    AABB aabb = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                    if (aabb.contains(vec31)) {
                        return InteractionResult.PASS;
                    }
                }
            }

            if (hitresult.getType() == HitResult.Type.BLOCK) {
                AbstractBoat boat = this.getBoat(pLevel, hitresult);
                if(boat instanceof DarkCherryChestBoatEntity chestBoat) {
                    chestBoat.setWoodType(this.type);
                } else if(boat instanceof DarkCherryBoatEntity) {
                    ((DarkCherryBoatEntity)boat).setWoodType(this.type);
                }
                boat.setYRot(pPlayer.getYRot());
                if (!pLevel.noCollision(boat, boat.getBoundingBox())) {
                    return InteractionResult.FAIL;
                } else {
                    if (!pLevel.isClientSide()) {
                        pLevel.addFreshEntity(boat);
                        pLevel.gameEvent(pPlayer, GameEvent.ENTITY_PLACE, hitresult.getLocation());
                        if (!pPlayer.getAbilities().instabuild) {
                            itemstack.shrink(1);
                        }
                    }

                    pPlayer.awardStat(Stats.ITEM_USED.get(this));
                    return (pLevel.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
                }
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    private AbstractBoat getBoat(Level level, HitResult hitResult) {
        return this.hasChest ? new DarkCherryChestBoatEntity(level, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z) : new DarkCherryBoatEntity(level, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
    }
}