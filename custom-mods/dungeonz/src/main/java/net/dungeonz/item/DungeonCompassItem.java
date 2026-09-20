package net.dungeonz.item;

import java.util.List;
import java.util.Optional;

import net.dungeonz.init.ItemInit;
import net.dungeonz.item.component.DungeonCompassComponent;
import net.dungeonz.network.DungeonServerPacket;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.GlobalPos;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.Nullable;

public class DungeonCompassItem extends Item {

    public DungeonCompassItem(Item.Properties settings) {
        super(settings);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        if (hasDungeon(stack) && world.getGameTime() % 100 == 0 && !hasDungeonStructure(stack)) {
            setCompassDungeonStructure(world, entity.blockPosition(), stack, stack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonType());
        }
        syncLodestoneTracker(world, stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos blockPos = context.getClickedPos();
        Level world = context.getLevel();

        if (world.getBlockState(blockPos).is(Blocks.CARTOGRAPHY_TABLE)) {
            if (!world.isClientSide()) {
                DungeonServerPacket.writeS2COpenCompassScreenPacket((ServerPlayer) context.getPlayer(),
                        context.getItemInHand().get(ItemInit.DUNGEON_COMPASS_DATA) != null ? context.getItemInHand().get(ItemInit.DUNGEON_COMPASS_DATA).dungeonType() : "");
            }
            return (world.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME);
        }
        return super.useOn(context);
    }

    public static boolean hasDungeon(ItemStack stack) {
        return stack.get(ItemInit.DUNGEON_COMPASS_DATA) != null;
    }

    public static boolean hasDungeonStructure(ItemStack itemStack) {
        if (itemStack.get(ItemInit.DUNGEON_COMPASS_DATA) != null && itemStack.get(ItemInit.DUNGEON_COMPASS_DATA).hasDungeon()) {
            return true;
        }
        return false;
    }

    @Nullable
    public static BlockPos getDungeonStructurePos(ItemStack itemStack) {
        if (itemStack.get(ItemInit.DUNGEON_COMPASS_DATA) != null && itemStack.get(ItemInit.DUNGEON_COMPASS_DATA).hasDungeon()) {
            return itemStack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().get();
        }
        return null;
    }

    @Nullable
    public static GlobalPos createGlobalDungeonStructurePos(Level world, ItemStack itemStack) {
        BlockPos pos = getDungeonStructurePos(itemStack);
        return pos != null ? GlobalPos.of(world.dimension(), pos) : null;
    }

    public static void setCompassDungeonStructure(ServerLevel world, BlockPos playerPos, ItemStack itemStack, String dungeonType) {
        if (itemStack.is(ItemInit.DUNGEON_COMPASS)) {
            BlockPos structurePos = getDungeonStructurePos(world, dungeonType, playerPos);
            if (structurePos == null) {
                structurePos = BlockPos.containing(0, 0, 0);
            }
            itemStack.set(ItemInit.DUNGEON_COMPASS_DATA, new DungeonCompassComponent(dungeonType, structurePos != null, Optional.of(structurePos)));
            syncLodestoneTracker(world, itemStack);
        }
    }

    static void syncLodestoneTracker(ServerLevel world, ItemStack itemStack) {
        BlockPos pos = getDungeonStructurePos(itemStack);
        if (pos != null) {
            LodestoneTracker tracker = new LodestoneTracker(Optional.of(GlobalPos.of(world.dimension(), pos)), true);
            if (!tracker.equals(itemStack.get(DataComponents.LODESTONE_TRACKER))) {
                itemStack.set(DataComponents.LODESTONE_TRACKER, tracker);
            }
        } else {
            itemStack.remove(DataComponents.LODESTONE_TRACKER);
        }
    }

    @Nullable
    private static BlockPos getDungeonStructurePos(ServerLevel world, String dungeonType, BlockPos playerPos) {
        return world.findNearestMapStructure(TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("dungeonz", dungeonType)), playerPos, 100, false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay displayComponent, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
        super.appendHoverText(stack, context, displayComponent, tooltip, type);
        if (stack.get(ItemInit.DUNGEON_COMPASS_DATA) != null) {
            tooltip.accept(Component.translatable("dungeon." + stack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonType()));
            if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.canUseGameMasterBlocks() && stack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().isPresent()) {
                tooltip.accept(Component.nullToEmpty(stack.get(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().get().toShortString()));
            }
        } else {
            tooltip.accept(Component.translatable("compass.compass_item.cartography"));
        }
    }

}
