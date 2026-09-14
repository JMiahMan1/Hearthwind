package net.dungeonz.util;

import net.minecraft.class_2680;
import net.minecraft.class_2738;
import net.minecraft.class_2741;

public class PropertyUtil {

    public static int getHorizontalFacing(class_2680 blockState) {
        if (blockState.method_28498(class_2741.field_12481)) {
            return blockState.method_11654(class_2741.field_12481).method_10161();
        }
        return 0;
    }

    public static int getBlockFacing(class_2680 blockState) {
        if (blockState.method_28498(class_2741.field_12555)) {
            return switch (blockState.method_11654(class_2741.field_12555)) {
                case field_12475 -> 1;
                case field_12471 -> 2;
                case field_12473 -> 3;
                default -> 0;
            };
        }
        return 0;
    }

    public static class_2738 getBlockFacing(int blockFacing) {
        return switch (blockFacing) {
            case 1 -> class_2738.field_12475;
            case 2 -> class_2738.field_12471;
            case 3 -> class_2738.field_12473;
            default -> class_2738.field_12475;
        };
    }
}
