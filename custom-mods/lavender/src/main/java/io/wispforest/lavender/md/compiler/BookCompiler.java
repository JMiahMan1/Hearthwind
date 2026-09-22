package io.wispforest.lavender.md.compiler;

import com.google.common.primitives.Ints;
import io.wispforest.lavender.Lavender;
import io.wispforest.lavender.client.LavenderBookScreen;
import io.wispforest.lavendermd.compiler.OwoUICompiler;
import io.wispforest.lavendermd.feature.OwoUITemplateFeature;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIModelLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.UIComponent;

public class BookCompiler extends OwoUICompiler {

    private static final Style UNICODE_FONT_STYLE = Style.EMPTY.withFont(new net.minecraft.network.chat.FontDescription.Resource(net.minecraft.resources.Identifier.withDefaultNamespace("default")));

    private final FlowLayout resultContainer = UIContainers.verticalFlow(Sizing.content(), Sizing.content());
    private final ComponentSource bookComponentSource;

    private boolean addImageBackground = false;

    public BookCompiler(ComponentSource bookComponentSource) {
        this.push(UIContainers.verticalFlow(Sizing.content(), Sizing.content()));
        this.bookComponentSource = bookComponentSource;
    }

    @Override
    protected LabelComponent makeLabel(MutableComponent text) {
        return new BookLabelComponent(text.withStyle(style -> UNICODE_FONT_STYLE.applyTo(style))).color(Color.BLACK).lineHeight(7);
    }

    @Override
    public void visitImage(Identifier image, String description, boolean fit) {
        this.addImageBackground = fit;
        super.visitImage(image, description, fit);
    }

    @Override
    public void visitHorizontalRule() {
        this.append(this.bookComponentSource.builtinTemplate(UIComponent.class, "horizontal-rule"));
    }

    public void visitPageBreak() {
        this.resultContainer.child(components.peek());
        this.pop();
        this.push(UIContainers.verticalFlow(Sizing.content(), Sizing.content()));
    }

    @Override
    protected void append(UIComponent component) {
        if (this.addImageBackground) {
            this.addImageBackground = false;
            if (component instanceof StackLayout stack) {
                stack.children().get(0).margins(Insets.of(3));
                stack.child(0, this.bookComponentSource.builtinTemplate(TextureComponent.class, "fit-image-background"));
            }
        }

        super.append(component);
    }

    @Override
    public ParentUIComponent compile() {
        this.pop();
        return super.compile();
    }

    @Override
    public String name() {
        return "lavender_builtin_book";
    }

    public static class BookLabelComponent extends LabelComponent {

        private @Nullable LavenderBookScreen owner;

        protected BookLabelComponent(Component text) {
            super(text);
            this.margins(Insets.horizontal(1));
            this.textClickHandler(style -> {
                if (style == null || this.owner == null) return false;

                var clickEvent = style.getClickEvent();
                if (clickEvent instanceof ClickEvent.OpenUrl openUrl && openUrl.uri().toString().startsWith("^")) {
                    var linkTarget = this.resolveLinkTarget(openUrl.uri().toString());
                    if (linkTarget != null && linkTarget.supplier != null) {
                        this.owner.navPush(linkTarget.supplier.get());
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            });
        }

        public void setOwner(@NotNull LavenderBookScreen screen) {
            this.owner = screen;
        }

        protected @Nullable LinkTarget resolveLinkTarget(String link) {
            if (this.owner == null) return null;

            var rawLinkText = link.substring(1);
            int targetPage;

            int pageSeparatorIndex = rawLinkText.indexOf('#');
            if (pageSeparatorIndex > 0) {
                var parsed = Ints.tryParse(rawLinkText.substring(pageSeparatorIndex + 1));
                if (parsed == null) return null;

                targetPage = Math.max(0, (parsed - 1) / 2 * 2);
                rawLinkText = rawLinkText.substring(0, pageSeparatorIndex);
            } else {
                targetPage = 0; // effectively final my ass
            }

            var entryId = Identifier.tryParse(rawLinkText);
            if (entryId == null) return null;

            var entry = this.owner.book.entryById(entryId);
            if (entry != null) {
                return new LinkTarget(
                        Component.literal(entry.title()),
                        entry.canPlayerView(Minecraft.getInstance().player)
                                ? () -> new LavenderBookScreen.NavFrame(new LavenderBookScreen.EntryPageSupplier(this.owner, entry), targetPage)
                                : null
                );
            }

            var category = this.owner.book.categoryById(entryId);
            if (category != null) {
                return new LinkTarget(
                        Component.literal(category.title()),
                        this.owner.book.shouldDisplayCategory(category, Minecraft.getInstance().player)
                                ? () -> new LavenderBookScreen.NavFrame(new LavenderBookScreen.CategoryPageSupplier(this.owner, category), targetPage)
                                : null
                );
            }

            return null;
        }

        @Override
        protected Style styleAt(int mouseX, int mouseY) {
            var style = super.styleAt(mouseX, mouseY);
            if (style == null) return null;

            var event = style.getHoverEvent();
            if (this.owner != null && event != null && event instanceof HoverEvent.ShowText showText && showText.value().getString().startsWith("^")) {
                var rawLink = showText.value().getString();
                var linkTarget = this.resolveLinkTarget(rawLink);

                style = style.withHoverEvent(new HoverEvent.ShowText(linkTarget != null
                        ? linkTarget.supplier != null ? linkTarget.title : Component.translatable("text.lavender.locked_internal_link")
                        : Component.translatable("text.lavender.invalid_internal_link", rawLink)
                ));
            }

            return style;
        }

        protected record LinkTarget(Component title, @Nullable Supplier<LavenderBookScreen.NavFrame> supplier) {}
    }

    @FunctionalInterface
    public interface ComponentSource extends OwoUITemplateFeature.TemplateProvider {
        <C extends UIComponent> C template(UIModel model, Class<C> expectedComponentClass, String name, Map<String, String> params);

        @Override
        default <C extends UIComponent> C template(Identifier model, Class<C> expectedClass, String templateName, Map<String, String> templateParams) {
            return this.template(UIModelLoader.get(model), expectedClass, templateName, templateParams);
        }

        default <C extends UIComponent> C builtinTemplate(Class<C> expectedComponentClass, String name, Map<String, String> params) {
            return this.template(UIModelLoader.get(Lavender.id("book_components")), expectedComponentClass, name, params);
        }

        default <C extends UIComponent> C builtinTemplate(Class<C> expectedComponentClass, String name) {
            return this.builtinTemplate(expectedComponentClass, name, Map.of());
        }

        default <C extends UIComponent> C template(UIModel model, Class<C> expectedComponentClass, String name) {
            return this.template(model, expectedComponentClass, name, Map.of());
        }
    }
}
