package net.dungeonz.item;

import java.util.List;
import java.util.Optional;

import net.dungeonz.init.ItemInit;
import net.dungeonz.item.component.DungeonCompassComponent;
import net.dungeonz.network.DungeonServerPacket;
import net.minecraft.class_1269;
import net.minecraft.class_1297;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_1838;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3218;
import net.minecraft.class_3222;
import net.minecraft.class_4208;
import net.minecraft.class_6862;
import net.minecraft.class_7924;
import org.jetbrains.annotations.Nullable;

public class DungeonCompassItem extends class_1792 {

    public DungeonCompassItem(class_1792.class_1793 settings) {
        super(settings);
    }

    @Override
    public void method_7888(class_1799 stack, class_1937 world, class_1297 entity, int slot, boolean selected) {
        if (world.method_8608()) {
            return;
        }
        if (hasDungeon(stack) && world.method_8510() % 100 == 0 && !hasDungeonStructure(stack)) {
            setCompassDungeonStructure((class_3218) world, entity.method_24515(), stack, stack.method_57824(ItemInit.DUNGEON_COMPASS_DATA).dungeonType());
        }
    }

    @Override
    public class_1269 method_7884(class_1838 context) {
        class_2338 blockPos = context.method_8037();
        class_1937 world = context.method_8045();

        if (world.method_8320(blockPos).method_27852(class_2246.field_16336)) {
            if (!world.method_8608()) {
                DungeonServerPacket.writeS2COpenCompassScreenPacket((class_3222) context.method_8036(),
                        context.method_8041().method_57824(ItemInit.DUNGEON_COMPASS_DATA) != null ? context.method_8041().method_57824(ItemInit.DUNGEON_COMPASS_DATA).dungeonType() : "");
            }
            return class_1269.method_29236(world.method_8608());
        }
        return super.method_7884(context);
    }

    public static boolean hasDungeon(class_1799 stack) {
        return stack.method_57824(ItemInit.DUNGEON_COMPASS_DATA) != null;
    }

    public static boolean hasDungeonStructure(class_1799 itemStack) {
        if (itemStack.method_57824(ItemInit.DUNGEON_COMPASS_DATA) != null && itemStack.method_57824(ItemInit.DUNGEON_COMPASS_DATA).hasDungeon()) {
            return true;
        }
        return false;
    }

    @Nullable
    public static class_2338 getDungeonStructurePos(class_1799 itemStack) {
        if (itemStack.method_57824(ItemInit.DUNGEON_COMPASS_DATA) != null && itemStack.method_57824(ItemInit.DUNGEON_COMPASS_DATA).hasDungeon()) {
            return itemStack.method_57824(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().get();
        }
        return null;
    }

    @Nullable
    public static class_4208 createGlobalDungeonStructurePos(class_1937 world, class_1799 itemStack) {
        class_2338 pos = getDungeonStructurePos(itemStack);
        return pos != null ? class_4208.method_19443(world.method_27983(), pos) : null;
    }

    public static void setCompassDungeonStructure(class_3218 world, class_2338 playerPos, class_1799 itemStack, String dungeonType) {
        if (itemStack.method_31574(ItemInit.DUNGEON_COMPASS)) {
            class_2338 structurePos = getDungeonStructurePos(world, dungeonType, playerPos);
            if (structurePos == null) {
                structurePos = class_2338.method_49637(0, 0, 0);
            }
            itemStack.method_57379(ItemInit.DUNGEON_COMPASS_DATA, new DungeonCompassComponent(dungeonType, structurePos != null, Optional.of(structurePos)));
        }
    }

    @Nullable
    private static class_2338 getDungeonStructurePos(class_3218 world, String dungeonType, class_2338 playerPos) {
        return world.method_8487(class_6862.method_40092(class_7924.field_41246, class_2960.method_60655("dungeonz", dungeonType)), playerPos, 100, false);
    }

    @Override
    public void method_7851(class_1799 stack, class_9635 context, List<class_2561> tooltip, class_1836 type) {
        super.method_7851(stack, context, tooltip, type);
        if (stack.method_57824(ItemInit.DUNGEON_COMPASS_DATA) != null) {
            tooltip.add(class_2561.method_43471("dungeon." + stack.method_57824(ItemInit.DUNGEON_COMPASS_DATA).dungeonType()));
            if (class_310.method_1551().field_1724 != null && class_310.method_1551().field_1724.method_7338() && stack.method_57824(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().isPresent()) {
                tooltip.add(class_2561.method_30163(stack.method_57824(ItemInit.DUNGEON_COMPASS_DATA).dungeonPos().get().method_23854()));
            }
        } else {
            tooltip.add(class_2561.method_43471("compass.compass_item.cartography"));
        }
    }

}
