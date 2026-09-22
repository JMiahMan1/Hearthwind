package io.wispforest.lavender.client;

import io.wispforest.lavender.Lavender;
import io.wispforest.lavender.book.Book;
import io.wispforest.owo.ui.base.BaseOwoToast;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@SuppressWarnings("UnstableApiUsage")
public class NewEntriesToast extends BaseOwoToast<StackLayout> {

    public static final Identifier TEXTURE = Lavender.id("new_entries_toast");

    public NewEntriesToast(Book.ToastSettings settings) {
        super(
            () -> UIContainers.stack(Sizing.content(), Sizing.content()).configure(component -> component
                .child(UIComponents.sprite(Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.resources.Identifier.withDefaultNamespace("gui")).getSprite(settings.backgroundSprite() != null ? settings.backgroundSprite() : TEXTURE)))
                .child(UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(UIComponents.item(settings.iconStack()).margins(Insets.of(0, 0, 8, 6)))
                    .child(UIComponents.label(Component.translatable("text.lavender.toast.new_entries", settings.bookName())))
                    .verticalAlignment(VerticalAlignment.CENTER))
                .verticalAlignment(VerticalAlignment.CENTER)),
            (baseOwoToast, time) -> time <= 5000 ? Visibility.SHOW : Visibility.HIDE
        );
    }
}
