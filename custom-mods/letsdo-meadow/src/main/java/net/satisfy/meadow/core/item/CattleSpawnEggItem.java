package net.satisfy.meadow.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import org.jetbrains.annotations.NotNull;
import net.satisfy.meadow.core.entity.WoolyCowEntity;
import net.satisfy.meadow.core.entity.WoolyCowVariant;

import java.util.function.Supplier;

public class CattleSpawnEggItem extends SpawnEggItem {
    private final Supplier<EntityType<WoolyCowEntity>> type;
    private final int variantId;

    public CattleSpawnEggItem(Supplier<EntityType<WoolyCowEntity>> type, Item.Properties properties, int variantId) {
        super(properties);
        this.type = type;
        this.variantId = variantId;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        EntityType<?> entityType = type.get();

        CompoundTag entityTag = new CompoundTag();
        if (entityType != null) {
            entityTag.putString("id", EntityType.getKey(entityType).toString());
        }
        entityTag.putInt("Variant", variantId);

        stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(entityType, entityTag));
        return stack;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel server)) return super.useOn(ctx);

        Direction face = ctx.getClickedFace();
        BlockPos spawnPos = ctx.getClickedPos().relative(face);
        EntityType<?> entityType = type.get();
        Entity e = entityType.create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (e == null) return InteractionResult.PASS;

        e.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, server.getRandom().nextFloat() * 360f, 0f);

        if (e instanceof WoolyCowEntity cow) {
            cow.setVariant(WoolyCowVariant.byId(variantId));
        }

        if (e instanceof Mob mob) {
            DifficultyInstance diff = server.getCurrentDifficultyAt(spawnPos);
            mob.finalizeSpawn(server, diff, EntitySpawnReason.SPAWN_ITEM_USE, null);
        }

        server.addFreshEntity(e);

        Player p = ctx.getPlayer();
        if (p == null || !p.getAbilities().instabuild) {
            ItemStack inHand = ctx.getItemInHand();
            inHand.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
