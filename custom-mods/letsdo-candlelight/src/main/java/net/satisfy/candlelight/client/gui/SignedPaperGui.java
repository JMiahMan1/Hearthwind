package net.satisfy.candlelight.client.gui;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.*;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.satisfy.candlelight.Candlelight;
import net.satisfy.candlelight.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntFunction;

@Environment(EnvType.CLIENT)
public class SignedPaperGui extends Screen {
    public static final Contents EMPTY_PROVIDER = new Contents() {
        public int getPageCount() {
            return 0;
        }

        public FormattedText getPageUnchecked(int index) {
            return FormattedText.EMPTY;
        }
    };
    public static final Identifier BOOK_TEXTURE = Candlelight.identifier("textures/gui/note_paper_gui.png");
    private final Contents contents;
    private int pageIndex;
    private List<FormattedCharSequence> cachedPage;
    private int cachedPageIndex;

    public SignedPaperGui(Contents contents) {
        super(GameNarrator.NO_TITLE);
        this.cachedPage = Collections.emptyList();
        this.cachedPageIndex = -1;
        this.contents = contents;
    }

    static List<String> readPages(CompoundTag nbt) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        Objects.requireNonNull(builder);
        filterPages(nbt, builder::add);
        return builder.build();
    }

    @SuppressWarnings("all")
    public static void filterPages(CompoundTag nbt, Consumer<String> pageConsumer) {
        ListTag nbtList = nbt.getListOrEmpty("text").copy();
        IntFunction intFunction;
        if (Minecraft.getInstance().isTextFilteringEnabled() && nbt.contains("filtered_pages")) {
            CompoundTag nbtCompound = nbt.getCompoundOrEmpty("filtered_pages");
            intFunction = (page) -> {
                String string = String.valueOf(page);
                return nbtCompound.contains(string) ? nbtCompound.getStringOr(string, "") : nbtList.getStringOr(page, "");
            };
        } else {
            Objects.requireNonNull(nbtList);
            intFunction = nbtList::getString;
        }

        for (int i = 0; i < nbtList.size(); ++i) {
            pageConsumer.accept((String) intFunction.apply(i));
        }

    }

    public boolean setPage(int index) {
        int i = Mth.clamp(index, 0, this.contents.getPageCount() - 1);
        if (i != this.pageIndex) {
            this.pageIndex = i;
            this.cachedPageIndex = -1;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.extractMenuBackground(graphics);
    }

    protected boolean jumpToPage(int page) {
        return this.setPage(page);
    }

    protected void init() {
        this.addCloseButton();
    }

    protected void addCloseButton() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            assert this.minecraft != null;
            this.minecraft.setScreenAndShow(null);
        }).bounds(this.width / 2 - 100, 196, 200, 20).build());
    }

    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        return super.keyPressed(event);
    }

    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
        this.extractBackground(guiGraphics, mouseX, mouseY, delta);
        int i = (this.width - 192) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_TEXTURE, i, 2, 0.0F, 0.0F, 192, 192, 192, 192, 256, 256);
        if (this.cachedPageIndex != this.pageIndex) {
            FormattedText stringVisitable = this.contents.getPage(this.pageIndex);
            this.cachedPage = this.font.split(stringVisitable, 114);
        }

        this.cachedPageIndex = this.pageIndex;
        Objects.requireNonNull(this.font);
        int l = Math.min(128 / 9, this.cachedPage.size());

        for (int m = 0; m < l; ++m) {
            FormattedCharSequence orderedText = this.cachedPage.get(m);
            int var10003 = i + 36;
            Objects.requireNonNull(this.font);
            guiGraphics.text(this.font, orderedText, var10003, 32 + m * 9, 0, false);
        }

        Style style = this.getTextStyleAt(mouseX, mouseY);

        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean down) {
        if (down && event.button() == 0) {
            Style style = this.getTextStyleAt(event.x(), event.y());
            if (style != null && this.handleComponentClicked(style)) {
                return true;
            }
        }

        return super.mouseClicked(event, down);
    }

    public boolean handleComponentClicked(Style style) {
        assert style != null;
        ClickEvent clickEvent = style.getClickEvent();
        if (clickEvent == null) {
            return false;
        } else if (clickEvent.action() == ClickEvent.Action.CHANGE_PAGE) {
            int i = ((ClickEvent.ChangePage) clickEvent).page() - 1;
            return this.jumpToPage(i);
        } else {
            boolean bl = false; // super.handleComponentClicked removed in 26.2
            if (bl && clickEvent.action() == ClickEvent.Action.RUN_COMMAND) {
                this.closeScreen();
            }
            return bl;
        }
    }

    protected void closeScreen() {
        assert this.minecraft != null;
        this.minecraft.setScreenAndShow(null);
    }

    @Nullable
    public Style getTextStyleAt(double x, double y) {
        if (this.cachedPage.isEmpty()) return null;

        int i = Mth.floor(x - (this.width - 192) / 2.0 - 36.0);
        int j = Mth.floor(y - 32.0);
        if (i < 0 || j < 0 || i > 114) return null;

        int k = Math.min(128 / 9, this.cachedPage.size());
        if (j >= 9 * k + k) return null;

        int l = j / 9;
        if (l >= this.cachedPage.size()) return null;

        FormattedCharSequence line = this.cachedPage.get(l);
        assert this.minecraft != null;
        return null;
    }

    @Environment(EnvType.CLIENT)
    public interface Contents {
        static Contents create(ItemStack stack) {
            if (stack.is(ObjectRegistry.NOTE_PAPER_WRITEABLE.get())) {
                return new WrittenPaperContents(stack);
            } else {
                return stack.is(ObjectRegistry.NOTE_PAPER_WRITEABLE.get()) ? new WritablePaperContents(stack) : EMPTY_PROVIDER;
            }
        }

        int getPageCount();

        FormattedText getPageUnchecked(int index);

        default FormattedText getPage(int index) {
            return index >= 0 && index < this.getPageCount() ? this.getPageUnchecked(index) : FormattedText.EMPTY;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class WritablePaperContents implements Contents {
        private final List<String> pages;

        public WritablePaperContents(ItemStack stack) {
            this.pages = getPages(stack);
        }

        private static List<String> getPages(ItemStack stack) {
            CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag nbtCompound = data.copyTag();
            return readPages(nbtCompound);
        }

        public int getPageCount() {
            return this.pages.size();
        }

        public FormattedText getPageUnchecked(int index) {
            return FormattedText.of(this.pages.get(index));
        }
    }

    @Environment(EnvType.CLIENT)
    public static class WrittenPaperContents implements Contents {
        private final List<String> pages;

        public WrittenPaperContents(ItemStack stack) {
            this.pages = getPages(stack);
        }

        private static List<String> getPages(ItemStack stack) {
            CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag nbtCompound = data.copyTag();
            if (makeSureTagIsValid(nbtCompound)) {
                return readPages(nbtCompound);
            } else {
                assert Minecraft.getInstance().level != null;
                return ImmutableList.of("{\"text\":\"invalid\"}");
            }
        }

        private static boolean makeSureTagIsValid(CompoundTag nbtCompound) {
            if (nbtCompound == null) {
                return false;
            }
            if (!nbtCompound.contains("text")) {
                return false;
            }
            ListTag listTag = nbtCompound.getListOrEmpty("text");
            for (int i = 0; i < listTag.size(); ++i) {
                String string = listTag.getStringOr(i, "");
                if (string.length() <= Short.MAX_VALUE) continue;
                return false;
            }
            return true;
        }

        public int getPageCount() {
            return this.pages.size();
        }

        public FormattedText getPageUnchecked(int index) {
            String string = this.pages.get(index);

            return FormattedText.of(string);
        }
    }
}
