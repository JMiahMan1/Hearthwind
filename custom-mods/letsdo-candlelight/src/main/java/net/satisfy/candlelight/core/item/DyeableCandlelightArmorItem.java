package net.satisfy.candlelight.core.item;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.Nullable;

public class DyeableCandlelightArmorItem extends Item {
    private final ArmorType type;
    private final Identifier texture;
    private final Identifier overlayTexture;
    private final int defaultColor;
    private final Identifier normalizedTexture;
    private final Identifier normalizedOverlayTexture;

    public DyeableCandlelightArmorItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, int color, Properties properties, Identifier texture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.defaultColor = color;
        this.texture = texture;
        this.overlayTexture = null;
        this.normalizedTexture = normalize(texture);
        this.normalizedOverlayTexture = null;
    }

    public DyeableCandlelightArmorItem(Holder<ArmorMaterial> armorMaterial, ArmorType type, int color, Properties properties, Identifier texture, Identifier overlayTexture) {
        super(properties.humanoidArmor(armorMaterial.value(), type));
        this.type = type;
        this.defaultColor = color;
        this.texture = texture;
        this.overlayTexture = overlayTexture;
        this.normalizedTexture = normalize(texture);
        this.normalizedOverlayTexture = normalize(overlayTexture);
    }

    public int getColor(ItemStack stack) {
        DyedItemColor dyed = stack.get(DataComponents.DYED_COLOR);
        if (dyed != null) return dyed.rgb();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getCompoundOrEmpty("display");
        if (tag.contains("color")) return tag.getIntOr("color", 0);
        return defaultColor;
    }

    public Identifier getTexture() {
        return normalizedTexture;
    }

    @Nullable
    public Identifier getOverlayTexture() {
        return normalizedOverlayTexture;
    }

    private static Identifier normalize(Identifier loc) {
        String path = loc.getPath();
        if (!path.startsWith("textures/")) path = "textures/" + path;
        if (!path.endsWith(".png")) path = path + ".png";
        return Identifier.fromNamespaceAndPath(loc.getNamespace(), path);
    }
}