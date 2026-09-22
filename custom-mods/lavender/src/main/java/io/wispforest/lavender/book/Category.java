package io.wispforest.lavender.book;

import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record Category(
        Identifier id,
        @Nullable Identifier parent,
        String title,
        Function<Sizing, UIComponent> iconFactory,
        boolean secret,
        int ordinal,
        String content
) implements Book.BookmarkableElement {}
