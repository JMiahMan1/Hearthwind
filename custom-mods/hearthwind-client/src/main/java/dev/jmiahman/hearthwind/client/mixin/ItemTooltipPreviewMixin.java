package dev.jmiahman.hearthwind.client.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import dev.jmiahman.hearthwind.survival.hydration.ClientHydration;
import dev.jmiahman.hearthwind.survival.hydration.ThirstPreview;

/**
 * The thirst droplet preview for everything that is not a flask, replacing the
 * reference's two overlapping {@code Item} mixins.
 *
 * <p>The reference splits this across {@code ItemMixin} (a HEAD
 * {@code setReturnValue} on {@code Item#getTooltipData}, covering food, stews
 * and drinks) and {@code PotionItemMixin} (a method overwrite for potions).
 * In 26.2 both would land on the same {@code Item#getTooltipImage} hook, and
 * {@code PotionItem} does not declare it, so there is exactly one place to
 * inject. The potion branch is checked first, which is what the overwrite
 * achieved.
 *
 * <p>The flasks are deliberately absent: {@code LeatherFlaskItem} overrides
 * {@code getTooltipImage} itself, so this handler never runs for one.
 */
@Mixin(Item.class)
@Environment(EnvType.CLIENT)
public abstract class ItemTooltipPreviewMixin {
    private static final TagKey<Item> HYDRATING_STEW =
            tag("hydrating_stew");
    private static final TagKey<Item> HYDRATING_FOOD =
            tag("hydrating_food");
    private static final TagKey<Item> HYDRATING_DRINKS =
            tag("hydrating_drinks");
    private static final TagKey<Item> STRONGER_HYDRATING_STEW =
            tag("stronger_hydrating_stew");
    private static final TagKey<Item> STRONGER_HYDRATING_FOOD =
            tag("stronger_hydrating_food");
    private static final TagKey<Item> STRONGER_HYDRATING_DRINKS =
            tag("stronger_hydrating_drinks");

    private static TagKey<Item> tag(String path) {
        return TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("dehydration", path));
    }

    @Inject(method = "getTooltipImage", at = @At("HEAD"), cancellable = true)
    private void hearthwind$thirstPreview(ItemStack stack,
            CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        HearthwindSurvivalConfig cfg = HearthwindSurvivalConfig.get();
        if (!cfg.thirst.thirstPreview || stack.isEmpty()) {
            return;
        }
        int corpus = ClientHydration.quench(stack);

        if (stack.getItem() instanceof PotionItem) {
            // Reference PotionItemMixin: splash and lingering are refused
            // outright (it tests ThrowablePotionItem), and a bad potion is
            // drawn at quality 2 so the risk is visible before drinking.
            cir.setReturnValue(ThirstPreview.forPotion(stack, corpus,
                    (int) Math.round(cfg.thirst.potionThirstQuench),
                    ThirstPreview.isThrowable(stack))
                    .map(preview -> (TooltipComponent) preview));
            return;
        }

        ThirstPreview.TagQuench tags = new ThirstPreview.TagQuench(
                stack.is(HYDRATING_STEW) ? cfg.thirst.stewThirstQuench : 0,
                stack.is(HYDRATING_FOOD) ? cfg.thirst.foodThirstQuench : 0,
                stack.is(HYDRATING_DRINKS) ? cfg.thirst.drinksThirstQuench : 0,
                stack.is(STRONGER_HYDRATING_STEW) ? cfg.thirst.strongerStewThirstQuench : 0,
                stack.is(STRONGER_HYDRATING_FOOD) ? cfg.thirst.strongerFoodThirstQuench : 0,
                stack.is(STRONGER_HYDRATING_DRINKS) ? cfg.thirst.strongerDrinksThirstQuench : 0);
        cir.setReturnValue(ThirstPreview.forItem(stack, tags, corpus)
                .map(preview -> (TooltipComponent) preview));
    }
}