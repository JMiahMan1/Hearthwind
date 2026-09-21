package net.satisfy.candlelight.client.gui;

import com.google.common.collect.Lists;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.satisfy.candlelight.Candlelight;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;

@SuppressWarnings("unused")
public abstract class NoteGui extends Screen {
    public static final Identifier NOTE_TEXTURE = Candlelight.identifier("textures/gui/note_paper_gui.png");
    private static final Component EDIT_TITLE_TEXT = Component.literal("Enter Note Title");
    private static final Component FINALIZE_WARNING_TEXT = Component.translatable("book.finalizeWarning");
    private static final FormattedCharSequence BLACK_CURSOR_TEXT;
    private static final FormattedCharSequence GRAY_CURSOR_TEXT;

    static {
        BLACK_CURSOR_TEXT = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.BLACK));
        GRAY_CURSOR_TEXT = FormattedCharSequence.forward("_", Style.EMPTY.withColor(ChatFormatting.GRAY));
    }

    protected final Player player;
    protected final ItemStack itemStack;
    private final List<String> text = Lists.newArrayList();
    private final Component signedByText;
    protected boolean dirty;
    private boolean signing;
    private int frameTick;
    private String title = "";
    private final TextFieldHelper noteTitleSelectionManager = new TextFieldHelper(() -> this.title, (title) -> this.title = title, this::getClipboard, this::setClipboard, (string) -> string.length() < 16);
    private long lastClickTime;
    private int lastClickIndex = -1;
    private Button doneButton;
    private Button signButton;
    private Button finalizeButton;
    private Button cancelButton;

    public NoteGui(Player player, ItemStack itemStack) {
        super(GameNarrator.NO_TITLE);
        this.player = player;
        this.itemStack = itemStack;
        CustomData data = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag nbtCompound = data.copyTag();
        List<String> string = this.text;
        Objects.requireNonNull(string);
        this.loadPages(nbtCompound, string::add);

        if (this.text.isEmpty()) {
            this.text.add("");
        }

        this.signedByText = Component.translatable("book.byAuthor", player.getGameProfile().name())
                .withStyle(style -> style.withItalic(true).withColor(0xF2E1B3));
    }

    static int getLineFromOffset(int[] lineStarts, int position) {
        int i = Arrays.binarySearch(lineStarts, position);
        return i < 0 ? -(i + 2) : i;
    }

    static int findLineFromPos(int[] is, int i) {
        int j = Arrays.binarySearch(is, i);
        return j < 0 ? -(j + 2) : j;
    }

    @Override
    protected void init() {
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), (button) -> {
            this.signing = true;
            this.updateButtons();
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            assert this.minecraft != null;
            this.minecraft.setScreenAndShow(null);
            this.finalizeNote(false);
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        this.finalizeButton = this.addRenderableWidget(Button.builder(Component.translatable("book.finalizeButton"), (button) -> {
            if (this.signing) {
                this.finalizeNote(true);
                assert this.minecraft != null;
                this.minecraft.setScreenAndShow(null);
            }

        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            if (this.signing) {
                this.signing = false;
            }

            this.updateButtons();
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
        this.updateButtons();
    }

    public void loadPages(CompoundTag compoundTag, Consumer<String> consumer) {
        IntFunction<String> intFunction;
        ListTag listTag = compoundTag.getListOrEmpty("text");
        if (Minecraft.getInstance().isTextFilteringEnabled() && compoundTag.contains("filtered_pages")) {
            CompoundTag compoundTag2 = compoundTag.getCompoundOrEmpty("filtered_pages");
            intFunction = i -> {
                String string = String.valueOf(i);
                return compoundTag2.contains(string) ? compoundTag2.getStringOr(string, "") : listTag.getStringOr(i, "");
            };
        } else {
            intFunction = i -> listTag.getStringOr(i, "");
        }
        for (int i2 = 0; i2 < listTag.size(); ++i2) {
            consumer.accept(intFunction.apply(i2));
        }
    }

    protected void removeEmptyPages() {
        ListIterator<String> listIterator = this.text.listIterator(this.text.size());

        while (listIterator.hasPrevious() && listIterator.previous().isEmpty()) {
            listIterator.remove();
        }
    }

    abstract protected void finalizeNote(boolean signNote);

    protected void writeNbtData(boolean signNote) {
        ListTag nbtList = new ListTag();
        for (String page : this.text) {
            nbtList.add(StringTag.valueOf(page));
        }

        CompoundTag tag = new CompoundTag();
        tag.put("text", nbtList);

        if (signNote) {
            tag.putString("author", this.player.getGameProfile().name());
            tag.putString("title", this.title.trim());
        }

        this.itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static boolean makeSureTagIsValid(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains("text")) return false;
        ListTag pages = tag.getListOrEmpty("text");
        for (int i = 0; i < pages.size(); i++) {
            if (!(pages.get(i) instanceof StringTag)) return false;
            if (pages.getStringOr(i, "").length() > Short.MAX_VALUE) return false;
        }
        return true;
    }

    private String getClipboard() {
        return this.minecraft != null ? TextFieldHelper.getClipboardContents(this.minecraft) : "";
    }

    private void setClipboard(String clipboard) {
        if (this.minecraft != null) {
            TextFieldHelper.setClipboardContents(this.minecraft, clipboard);
        }
    }

    public void tick() {
        super.tick();
        ++this.frameTick;
    }

    private void updateButtons() {
        this.doneButton.visible = !this.signing;
        this.signButton.visible = !this.signing;
        this.cancelButton.visible = this.signing;
        this.finalizeButton.visible = this.signing;
        this.finalizeButton.active = !this.title.trim().isEmpty();
    }
}
