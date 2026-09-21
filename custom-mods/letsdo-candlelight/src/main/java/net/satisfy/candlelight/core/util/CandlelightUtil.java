package net.satisfy.candlelight.core.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.satisfy.candlelight.client.gui.NotePaperGui;
import net.satisfy.candlelight.client.gui.SignedPaperGui;
import net.satisfy.candlelight.client.gui.TypeWriterGui;
import net.satisfy.candlelight.core.block.entity.TypewriterEntity;
import org.joml.Vector3i;

public class CandlelightUtil {
    public static void setNotePaperScreen(Player user, ItemStack stack, InteractionHand hand) {
        Minecraft.getInstance().setScreenAndShow(new NotePaperGui(user, stack, hand));
    }


    public static void setTypeWriterScreen(Player user, TypewriterEntity typeWriterEntity) {
        if (user instanceof LocalPlayer)
            Minecraft.getInstance().setScreenAndShow(new TypeWriterGui(user, typeWriterEntity));
    }

    public static void setSignedPaperScreen(ItemStack stack) {
        Minecraft.getInstance().setScreenAndShow(new SignedPaperGui(new SignedPaperGui.WrittenPaperContents(stack)));
    }

    /**
     * 26.2: runtime item-color registration is gone (ItemColors class removed;
     * tinting is data-driven via model JSON + ItemTintSource). The dyeable armor
     * renderers (DyeableCandlelightChestplateRenderer/LeggingsRenderer) apply the
     * packed color manually during equip render, so this is intentionally a no-op.
     */
    public static void registerColorArmor(Item item, int defaultColor) {
    }

    static int getColor(ItemStack itemStack, int defaultColor) {
        if (itemStack.has(DataComponents.CUSTOM_DATA)) {
            CompoundTag displayTag = itemStack.get(DataComponents.CUSTOM_DATA).copyTag().getCompoundOrEmpty("display");
            if (displayTag.contains("color")) return 0xFF000000 | displayTag.getIntOr("color", 0);
        }
        Vector3i rgb = new Vector3i((defaultColor >> 16) & 255, (defaultColor >> 8) & 255, defaultColor & 255);
        return (255 << 24) | (rgb.x() << 16) | (rgb.y() << 8) | rgb.z();
    }
}