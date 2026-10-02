package dev.jmiahman.hearthwind.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import dev.jmiahman.hearthwind.client.ThirstTooltipComponent;
import dev.jmiahman.hearthwind.survival.hydration.ThirstPreview;

/**
 * Hands our {@link ThirstPreview} to {@link ThirstTooltipComponent}.
 *
 * <p>Needed because vanilla's {@code ClientTooltipComponent.create} is a
 * hardcoded switch over two known types and its default arm is
 * {@code throw new IllegalArgumentException("Unknown TooltipComponent")} - a
 * custom component without this hook crashes the tooltip render rather than
 * drawing. Both call sites are in {@code setTooltipForNextFrame}.
 *
 * <p>This mixin is declared as an <b>interface</b> on purpose. The target is an
 * interface and the method being injected is a {@code static} interface
 * method, so mixin refuses a plain class mixin with
 * {@code InvalidMixinException: @Mixin target type mismatch: ... is an
 * interface}. Declaring the mixin as an interface makes mixin merge it as an
 * interface subtype, which is what a static interface method needs.
 */
@Mixin(ClientTooltipComponent.class)
@Environment(EnvType.CLIENT)
public interface ClientTooltipComponentCreateMixin {
    // The target has TWO static create methods - create(FormattedCharSequence)
    // and create(TooltipComponent) - and a bare name binds the first one, so
    // the descriptor has to be spelled out or the inject fails at APPLY with
    // "Invalid descriptor ... Expected (FormattedCharSequence, CIR)V".
    @Inject(method = "create(Lnet/minecraft/world/inventory/tooltip/TooltipComponent;)"
            + "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipComponent;",
            at = @At("HEAD"), cancellable = true)
    private static void hearthwind$thirstPreview(TooltipComponent component,
            CallbackInfoReturnable<ClientTooltipComponent> cir) {
        if (component instanceof ThirstPreview preview) {
            cir.setReturnValue(new ThirstTooltipComponent(preview));
        }
    }
}
