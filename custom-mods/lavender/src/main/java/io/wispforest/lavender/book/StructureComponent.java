package io.wispforest.lavender.book;

import io.wispforest.lavender.client.StructureOverlayRenderer;
import io.wispforest.lavender.structure.LavenderStructures;
import io.wispforest.lavender.structure.StructureTemplate;
import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Easing;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.parsing.UIModelParsingException;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import com.mojang.math.Axis;
import org.lwjgl.glfw.GLFW;
import org.w3c.dom.Element;

import java.util.List;

public class StructureComponent extends BaseUIComponent {

    private final StructureTemplate structure;
    private final int displayAngle;

    private float rotation = -45;
    private long lastInteractionTime = 0L;

    private boolean placeable = true;
    private int visibleLayer = -1;

    public StructureComponent(StructureTemplate structure, int displayAngle) {
        this.structure = structure;
        this.displayAngle = displayAngle;
        this.cursorStyle(CursorStyle.HAND);
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);

        var diff = System.currentTimeMillis() - this.lastInteractionTime;
        if (diff < 5000L) return;

        this.rotation += delta * Easing.SINE.apply(Math.min(1f, (diff - 5000) / 1500f));
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        // v1 26.2: world-space structure mesh preview disabled (no renderBlockAsEntity / buffer builders).
        var client = Minecraft.getInstance();
        context.drawText(net.minecraft.network.chat.Component.literal(this.structure.id.toString()), this.x + 4, this.y + 4, 1f, 0xFF666666);

        if (this.placeable) {
            if (StructureOverlayRenderer.isShowingOverlay(this.structure.id)) {
                this.tooltip(Component.translatable("text.lavender.structure_component.hide_hint"));
            } else {
                this.tooltip(Component.translatable("text.lavender.structure_component.place_hint"));
            }
        }
    }

    @Override
    public boolean onMouseDown(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        var result = super.onMouseDown(event, doubleClick);
        if (!this.placeable || event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT || !Minecraft.getInstance().hasShiftDown()) return result;

        if (StructureOverlayRenderer.isShowingOverlay(this.structure.id)) {
            StructureOverlayRenderer.removeAllOverlays(this.structure.id);
        } else {
            StructureOverlayRenderer.addPendingOverlay(this.structure.id);
            StructureOverlayRenderer.restrictVisibleLayer(this.structure.id, this.visibleLayer);

            Minecraft.getInstance().setScreenAndShow(null);
        }

        return true;
    }

    @Override
    public boolean onMouseDrag(net.minecraft.client.input.MouseButtonEvent event, double deltaX, double deltaY) {
        var result = super.onMouseDrag(event, deltaX, deltaY);
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) return result;

        this.rotation += (float) deltaX;
        this.lastInteractionTime = System.currentTimeMillis();

        return true;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return source == FocusSource.MOUSE_CLICK;
    }

    public StructureComponent visibleLayer(int visibleLayer) {
        StructureOverlayRenderer.restrictVisibleLayer(this.structure.id, visibleLayer);

        this.visibleLayer = visibleLayer;
        return this;
    }

    public StructureComponent placeable(boolean placeable) {
        if (!placeable) {
            this.tooltip((List<ClientTooltipComponent>) null);
        }

        this.cursorStyle(placeable ? CursorStyle.HAND : CursorStyle.POINTER);

        this.placeable = placeable;
        return this;
    }

    public boolean placeable() {
        return this.placeable;
    }

    public static StructureComponent parse(Element element) {
        UIParsing.expectAttributes(element, "structure-id");

        var structureId = Identifier.tryParse(element.getAttribute("structure-id"));
        if (structureId == null) {
            throw new UIModelParsingException("Invalid structure id '" + element.getAttribute("structure-id") + "'");
        }

        var structure = LavenderStructures.get(structureId);
        if (structure == null) throw new UIModelParsingException("Unknown structure '" + structureId + "'");

        int displayAngle = 35;
        if (element.hasAttribute("display-angle")) {
            displayAngle = UIParsing.parseSignedInt(element.getAttributeNode("display-angle"));
        }

        return new StructureComponent(structure, displayAngle);
    }
}
