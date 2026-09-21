package net.satisfy.meadow.core.registry;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.satisfy.meadow.client.model.FurBootsModel;
import net.satisfy.meadow.client.model.FurChestplateModel;
import net.satisfy.meadow.client.model.FurHelmetModel;
import net.satisfy.meadow.client.model.FurLeggingsModel;
import net.satisfy.meadow.core.item.FurBootsItem;
import net.satisfy.meadow.core.item.FurChestItem;
import net.satisfy.meadow.core.item.FurHelmetItem;
import net.satisfy.meadow.core.item.FurLegsItem;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ArmorRegistry {
    private static final Map<Item, FurHelmetModel> helmetModels = new HashMap<>();
    private static final Map<Item, FurChestplateModel> chestplateModels = new HashMap<>();
    private static final Map<Item, FurLeggingsModel> leggingsModels = new HashMap<>();
    private static final Map<Item, FurBootsModel> bootsModels = new HashMap<>();

    public static FurHelmetModel getHatModel(Item item, ModelPart baseHead) {
        EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
        FurHelmetModel model = helmetModels.computeIfAbsent(item, key -> new FurHelmetModel(modelSet.bakeLayer(FurHelmetModel.LAYER_LOCATION)));

        model.copyHead(baseHead);

        return model;
    }

    public static FurChestplateModel getChestplateModel(Item item, ModelPart body, ModelPart leftArm, ModelPart rightArm, ModelPart leftLeg, ModelPart rightLeg) {
        FurChestplateModel model = chestplateModels.computeIfAbsent(item, key -> new FurChestplateModel(Minecraft.getInstance().getEntityModels().bakeLayer(FurChestplateModel.LAYER_LOCATION)));

        model.copyBody(body, leftArm, rightArm, leftLeg, rightLeg);

        return model;
    }

    public static FurLeggingsModel getLeggingsModel(Item item, ModelPart rightLeg, ModelPart leftLeg) {
        FurLeggingsModel model = leggingsModels.computeIfAbsent(item, key -> new FurLeggingsModel(Minecraft.getInstance().getEntityModels().bakeLayer(FurLeggingsModel.LAYER_LOCATION)));

        model.copyLegs(rightLeg, leftLeg);

        return model;
    }

    public static FurBootsModel getBootsModel(Item item, ModelPart rightLeg, ModelPart leftLeg) {
        FurBootsModel model = bootsModels.computeIfAbsent(item, key -> new FurBootsModel(Minecraft.getInstance().getEntityModels().bakeLayer(FurBootsModel.LAYER_LOCATION)));

        model.copyLegs(rightLeg, leftLeg);

        return model;
    }

    public static void appendToolTip(@NotNull java.util.function.Consumer<Component> tooltip) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack helmet = player.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);

        boolean hasFullSet = helmet.getItem() instanceof FurHelmetItem &&
                chestplate.getItem() instanceof FurChestItem &&
                leggings.getItem() instanceof FurLegsItem &&
                boots.getItem() instanceof FurBootsItem;

        tooltip.accept(Component.nullToEmpty(""));
        tooltip.accept(Component.nullToEmpty(ChatFormatting.DARK_GREEN + I18n.get("tooltip.meadow.armor.fur_armor0")));
        tooltip.accept(Component.nullToEmpty((helmet.getItem() instanceof FurHelmetItem ? ChatFormatting.GREEN.toString() : ChatFormatting.GRAY.toString()) + "- [" + Component.translatable(ObjectRegistry.FUR_HELMET.get().getDescriptionId()).getString() + "]"));
        tooltip.accept(Component.nullToEmpty((chestplate.getItem() instanceof FurChestItem ? ChatFormatting.GREEN.toString() : ChatFormatting.GRAY.toString()) + "- [" + Component.translatable(ObjectRegistry.FUR_CHESTPLATE.get().getDescriptionId()).getString() + "]"));
        tooltip.accept(Component.nullToEmpty((leggings.getItem() instanceof FurLegsItem ? ChatFormatting.GREEN.toString() : ChatFormatting.GRAY.toString()) + "- [" + Component.translatable(ObjectRegistry.FUR_LEGGINGS.get().getDescriptionId()).getString() + "]"));
        tooltip.accept(Component.nullToEmpty((boots.getItem() instanceof FurBootsItem ? ChatFormatting.GREEN.toString() : ChatFormatting.GRAY.toString()) + "- [" + Component.translatable(ObjectRegistry.FUR_BOOTS.get().getDescriptionId()).getString() + "]"));
        tooltip.accept(Component.nullToEmpty(""));

        ChatFormatting color = hasFullSet ? ChatFormatting.GREEN : ChatFormatting.GRAY;
        tooltip.accept(Component.nullToEmpty(color + I18n.get("tooltip.meadow.armor.fur_armor1")));
        tooltip.accept(Component.nullToEmpty(color + I18n.get("tooltip.meadow.armor.fur_armor2")));
    }
}
