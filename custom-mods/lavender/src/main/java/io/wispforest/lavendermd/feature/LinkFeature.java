package io.wispforest.lavendermd.feature;

import io.wispforest.lavendermd.Lexer;
import io.wispforest.lavendermd.MarkdownFeature;
import io.wispforest.lavendermd.Parser;
import io.wispforest.lavendermd.compiler.MarkdownCompiler;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;

public class LinkFeature implements MarkdownFeature {

    /**
     * Click action id for {@code [text](^book:entry)} internal links. 26.2
     * {@link ClickEvent.OpenUrl} validates its URI, and the {@code ^} prefix
     * is not a legal URI scheme, so internal links travel as a custom click
     * event and {@code BookCompiler} resolves them against the open book.
     */
    public static final net.minecraft.resources.Identifier INTERNAL_LINK_ID =
            net.minecraft.resources.Identifier.fromNamespaceAndPath("lavender", "entry_link");

    @Override
    public String name() {
        return "links";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        registrar.registerToken(Lexer.Token.lexFromChar(OpenLinkToken::new), '[');
        registrar.registerToken((nibbler, tokens) -> {
            nibbler.skip();
            if (!nibbler.tryConsume('(')) return false;

            var link = nibbler.consumeUntil(')');
            if (link == null) return false;

            tokens.add(new CloseLinkToken(link));
            return true;
        }, ']');
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        registrar.registerNode((parser, left, tokens) -> {
            int pointer = tokens.pointer();
            var content = parser.parseUntil(tokens, CloseLinkToken.class);

            if (tokens.peek() instanceof CloseLinkToken right) {
                tokens.nibble();
                var clickEvent = right.link.startsWith("^")
                        ? new ClickEvent.Custom(INTERNAL_LINK_ID, java.util.Optional.of(net.minecraft.nbt.StringTag.valueOf(right.link)))
                        : new ClickEvent.OpenUrl(java.net.URI.create(right.link));
                return new Parser.FormattingNode(style -> style.withClickEvent(
                        clickEvent
                ).withHoverEvent(
                        new HoverEvent.ShowText(Component.literal(right.link))
                ).withColor(ChatFormatting.BLUE)).addChild(content);
            } else {
                tokens.setPointer(pointer);
                return new Parser.TextNode(left.content());
            }
        }, (token, tokens) -> token instanceof OpenLinkToken link ? link : null);
    }

    // --- tokens ---

    private static final class OpenLinkToken extends Lexer.Token {
        public OpenLinkToken() {
            super("[");
        }
    }

    private static final class CloseLinkToken extends Lexer.Token {

        public final @NotNull String link;

        public CloseLinkToken(@NotNull String link) {
            super("](" + link + ")");
            this.link = link;
        }
    }
}
