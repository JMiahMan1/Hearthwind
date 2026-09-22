package io.wispforest.lavender.client;

import io.wispforest.owo.ui.base.BaseUIComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.w3c.dom.Element;

public class UnreadNotificationComponent extends BaseUIComponent {

    private final Identifier bookTexture;

    public UnreadNotificationComponent(Identifier bookTexture, boolean plural) {
        this.bookTexture = bookTexture;
        this.tooltip(Component.translatable(plural ? "text.lavender.entry.multiple_unread" : "text.lavender.entry.unread"));
    }

    @Override
    protected int determineHorizontalContentSize(Sizing sizing) {
        return 8;
    }

    @Override
    protected int determineVerticalContentSize(Sizing sizing) {
        return 8;
    }

    @Override
    public void draw(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
        int u = (int) ((System.currentTimeMillis() / 1500d) % 2 == 0 ? 188 : 204);
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, this.bookTexture, this.x, this.y, u, 180, this.width, this.height, 16, 16, 512, 256);
    }

    public static UnreadNotificationComponent parse(Element element) {
        UIParsing.expectAttributes(element, "book-texture", "plural");
        return new UnreadNotificationComponent(
            UIParsing.parseIdentifier(element.getAttributeNode("book-texture")),
            UIParsing.parseBool(element.getAttributeNode("plural"))
        );
    }
}
