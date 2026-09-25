package io.wispforest.lavender.client;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.lavender.Lavender;
import io.wispforest.lavender.book.*;
import io.wispforest.lavender.md.ItemListComponent;
import io.wispforest.lavender.md.compiler.BookCompiler;
import io.wispforest.lavender.md.features.*;
import io.wispforest.lavendermd.MarkdownFeature;
import io.wispforest.lavendermd.MarkdownProcessor;
import io.wispforest.lavendermd.feature.*;
import io.wispforest.owo.Owo;
import io.wispforest.owo.ops.TextOps;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.*;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.StackLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.ui.util.CommandOpenedScreen;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.ParentUIComponent;

public class LavenderBookScreen extends BaseUIModelScreen<FlowLayout> implements CommandOpenedScreen {

    private static final Identifier DEFAULT_BOOK_TEXTURE = Lavender.id("textures/gui/brown_book.png");

    private static final Map<Identifier, Map<RecipeType<?>, RecipeFeature.RecipePreviewBuilder<?>>> RECIPE_HANDLERS = new HashMap<>();
    private static final Map<Identifier, FeatureProvider> FEATURE_PROVIDERS = new HashMap<>();

    private static final Map<Identifier, List<NavFrame.Replicator>> NAV_TRAILS = new HashMap<>();

    private final BookCompiler.ComponentSource bookComponentSource = LavenderBookScreen.this::template;

    public final Book book;
    public final boolean isOverlay;

    private final MarkdownProcessor<ParentUIComponent> processor;
    private Window window;
    private int scaleFactor;

    private ButtonComponent previousButton;
    private ButtonComponent returnButton;
    private ButtonComponent nextButton;
    private TextBoxComponent searchBox;

    private FlowLayout leftPageAnchor;
    private FlowLayout rightPageAnchor;
    private FlowLayout bookmarkPanel;

    private final Deque<NavFrame> navStack = new ArrayDeque<>();

    public LavenderBookScreen(Book book, boolean isOverlay) {
        super(FlowLayout.class, Lavender.id("book"));
        this.book = book;
        this.isOverlay = isOverlay;

        var processor = MarkdownProcessor.richText(0)
                .copyWith(() -> new BookCompiler(this.bookComponentSource))
                .copyWith(
                        new ImageFeature(), new BlockStateFeature(), new ItemStackFeature(Minecraft.getInstance().level.registryAccess()), new EntityFeature(),
                        new PageBreakFeature(), new OwoUITemplateFeature(this.bookComponentSource),
                        new RecipeFeature(this.bookComponentSource, RECIPE_HANDLERS.get(this.book.id())),
                        new StructureFeature(this.bookComponentSource), new KeybindFeature(),
                        new ItemTagFeature(), new OwoUIModelFeature(), new TranslationsFeature()
                );

        if (FEATURE_PROVIDERS.get(book.id()) != null) {
            processor = processor.copyWith(FEATURE_PROVIDERS.get(book.id()).createFeatures(this.bookComponentSource).toArray(MarkdownFeature[]::new));
        }

        this.processor = processor;
    }

    public LavenderBookScreen(Book book) {
        this(book, false);
    }

    @Override
    protected void init() {
        this.window = this.minecraft.getWindow();
        double gameScale = this.window.getGuiScale();

        this.scaleFactor = this.window.calculateScale(!this.isOverlay ? this.minecraft.options.guiScale().get() : 0, true);
        this.window.setGuiScale(this.scaleFactor);

        this.width = this.window.getGuiScaledWidth();
        this.height = this.window.getGuiScaledHeight();

        super.init();

        this.window.setGuiScale((int) Math.round(gameScale));
    }

    protected <C extends UIComponent> C template(Class<C> expectedComponentClass, String name) {
        return this.template(expectedComponentClass, name, Map.of());
    }

    protected <C extends UIComponent> C template(Class<C> expectedComponentClass, String name, Map<String, String> parameters) {
        return this.template(this.model, expectedComponentClass, name, parameters);
    }

    protected <C extends UIComponent> C template(UIModel model, Class<C> expectedComponentClass, String name, Map<String, String> parameters) {
        var params = new HashMap<String, String>();
        params.put("book-texture", this.bookTexture().toString());
        params.putAll(parameters);

        return model.expandTemplate(expectedComponentClass, name, params);
    }

    protected Identifier bookTexture() {
        return this.book.texture() != null ? this.book.texture() : DEFAULT_BOOK_TEXTURE;
    }

    @Override
    protected <C extends UIComponent> @NotNull C component(Class<C> expectedClass, String id) {
        //noinspection DataFlowIssue
        return super.component(expectedClass, id);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        if (this.isOverlay) rootComponent.surface(Surface.BLANK);
        rootComponent.child(this.template(UIComponent.class, "primary-panel"));

        this.leftPageAnchor = this.component(FlowLayout.class, "left-page-anchor");
        this.rightPageAnchor = this.component(FlowLayout.class, "right-page-anchor");
        this.bookmarkPanel = this.component(FlowLayout.class, "bookmark-panel");

        (this.previousButton = this.component(ButtonComponent.class, "previous-button")).onPress(button -> this.turnPage(true));
        (this.nextButton = this.component(ButtonComponent.class, "next-button")).onPress(button -> this.turnPage(false));
        (this.returnButton = this.component(ButtonComponent.class, "back-button")).onPress(button -> {
            if (Minecraft.getInstance().hasShiftDown()) {
                while (this.navStack.size() > 1) this.navStack.pop();
                this.rebuildContent(this.book.flippingSound());
            } else {
                this.navPop();
            }
        });

        if (Owo.DEBUG && !this.isOverlay) {
            this.component(FlowLayout.class, "primary-panel").child(
                    this.template(ButtonComponent.class, "reload-button").onPress(buttonComponent -> {
                        BookLoader.reload(this.minecraft.getResourceManager());
                        BookContentLoader.reloadContents(this.minecraft.getResourceManager());

                        var newBook = BookLoader.get(this.book.id());
                        if (newBook != null) {
                            this.minecraft.setScreenAndShow(new LavenderBookScreen(newBook));
                        } else {
                            this.minecraft.setScreenAndShow(null);
                        }
                    })
            );
        }

        this.searchBox = this.component(TextBoxComponent.class, "search-box");
        searchBox.visible = searchBox.active = false;
        searchBox.onChanged().subscribe(value -> {
            var frame = this.currentNavFrame();

            this.navStack.pop();
            this.navPush(new NavFrame(frame.pageSupplier.replicator().apply(this), frame.selectedPage), true);

            this.rebuildContent(null);
        });

        var navTrail = getNavTrail(this.book);
        for (int i = navTrail.size() - 1; i >= 0; i--) {
            var frame = navTrail.get(i).createFrame(this);
            if (frame == null) continue;

            this.navPush(frame, true);
        }

        if (this.book.introEntry() != null && !LavenderClientStorage.wasBookOpened(this.book.id())) {
            this.navPush(new NavFrame(new EntryPageSupplier(this, this.book.introEntry()), 0), true);
        }

        LavenderClientStorage.markBookOpened(this.book.id());
        this.rebuildContent(!this.isOverlay ? this.book.openSound() : null);
    }

    @ApiStatus.Internal
    public void rebuildContent(@Nullable SoundEvent sound) {
        var pageSupplier = this.currentNavFrame().replicator().pageSupplier.apply(this);
        int selectedPage = this.currentNavFrame().selectedPage;

        if (pageSupplier == null) {
            this.navPop();
            return;
        }

        if (sound != null && this.minecraft.player != null) this.minecraft.player.playSound(sound, 1f, 1f);

        if (selectedPage >= pageSupplier.pageCount()) {
            selectedPage = this.currentNavFrame().selectedPage = (pageSupplier.pageCount() - 1) / 2 * 2;
        }

        if (!this.isOverlay) {
            this.returnButton.active(this.navStack.size() > 1);
            this.previousButton.active(selectedPage > 0);
            this.nextButton.active(selectedPage + 2 < pageSupplier.pageCount());
        }

        searchBox.visible = searchBox.active = pageSupplier.searchable();

        int index = 0;
        while (index < 2) {
            var anchor = index == 0 ? this.leftPageAnchor : this.rightPageAnchor;
            anchor.clearChildren();

            if (selectedPage + index < pageSupplier.pageCount()) {
                anchor.child(pageSupplier.getPageContent(selectedPage + index));
            } else {
                anchor.child(this.template(UIComponent.class, "empty-page-content"));
            }

            index++;
        }

        this.bookmarkPanel.<FlowLayout>configure(bookmarkContainer -> {
            bookmarkContainer.clearChildren();

            var bookmarks = LavenderClientStorage.getBookmarks(this.book);
            for (var bookmark : bookmarks) {
                var element = bookmark.tryResolve(this.book);
                if (element == null) continue;

                var bookmarkComponent = this.createBookmarkButton("bookmark");
                bookmarkComponent.tooltip(List.of(Component.literal(element.title()), Component.translatable("text.lavender.book.bookmark.remove_hint")));
                bookmarkComponent.childById(StackLayout.class, "bookmark-preview").child(element.iconFactory().apply(Sizing.fill()).cursorStyle(CursorStyle.HAND));
                bookmarkComponent.childById(ButtonComponent.class, "bookmark-button").<ButtonComponent>configure(bookmarkButton -> {
                    bookmarkButton.onPress($ -> {
                        if (Minecraft.getInstance().hasShiftDown()) {
                            LavenderClientStorage.removeBookmark(this.book, bookmark);
                            this.rebuildContent(null);
                        } else if (element instanceof Entry entry) {
                            this.navPush(new EntryPageSupplier(this, entry));
                        } else if (element instanceof Category category) {
                            this.navPush(new CategoryPageSupplier(this, category));
                        }
                    });
                });

                bookmarkContainer.child(bookmarkComponent);
            }

            if (this.currentNavFrame().pageSupplier instanceof PageSupplier.Bookmarkable bookmarkable) {
                var addBookmarkButton = this.createBookmarkButton("add-bookmark");
                addBookmarkButton.childById(ButtonComponent.class, "bookmark-button").<ButtonComponent>configure(bookmarkButton -> {
                    bookmarkButton.tooltip(Component.translatable("text.lavender.book.bookmark.add"));
                    bookmarkButton.onPress($ -> {
                        bookmarkable.addBookmark();
                        this.rebuildContent(null);
                        this.component(ScrollContainer.class, "bookmark-scroll").scrollTo(1);
                    });
                });

                bookmarkContainer.child(addBookmarkButton);
            }
        });
    }

    protected ParentUIComponent createBookmarkButton(String templateName) {
        var bookmark = this.template(ParentUIComponent.class, templateName);
        bookmark.mouseEnter().subscribe(() -> bookmark.margins(Insets.none()));
        bookmark.mouseLeave().subscribe(() -> bookmark.margins(Insets.top(1)));
        return bookmark;
    }

    protected NavFrame currentNavFrame() {
        return this.navStack.peek();
    }

    private void turnPage(boolean left) {
        var frame = this.currentNavFrame();

        int previousPage = frame.selectedPage;
        frame.selectedPage = Math.max(0, Math.min(frame.selectedPage + (left ? -2 : 2), frame.pageSupplier.pageCount() - 1)) / 2 * 2;

        if (frame.selectedPage != previousPage) {
            this.rebuildContent(this.book.flippingSound());
        }
    }

    public void navPush(PageSupplier supplier) {
        this.navPush(new NavFrame(supplier, 0));
    }

    public void navPush(NavFrame frame) {
        this.navPush(frame, false);
    }

    public void navPush(NavFrame frame, boolean suppressUpdate) {
        var topFrame = this.navStack.peek();
        if (topFrame != null && frame.pageSupplier.canMerge(topFrame.pageSupplier)) {
            topFrame.selectedPage = frame.selectedPage;
        } else {
            if (frame.selectedPage >= frame.pageSupplier.pageCount() - 1) {
                frame.selectedPage = frame.pageSupplier.pageCount() / 2 * 2;
            }

            this.navStack.push(frame);
        }

        if (!suppressUpdate) this.rebuildContent(this.book.flippingSound());
    }

    public void navPop() {
        if (this.navStack.size() <= 1) return;

        this.navStack.pop();
        this.rebuildContent(this.book.flippingSound());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        // v1 26.2: custom projection/scale hacks removed (setProjectionMatrix now takes GpuBufferSlice)
        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    protected void drawComponentTooltip(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float tickDelta) {
        // v1 26.2: custom projection/scale hacks removed
        super.drawComponentTooltip(drawContext, mouseX, mouseY, tickDelta);
    }
    @Override
    public boolean charTyped(net.minecraft.client.input.CharacterEvent event) {
        if (super.charTyped(event)) return true;

        int chr = event.codepoint();
        int modifiers = event.codepoint(); // modifiers not on CharacterEvent; only handle plain 'e' for editor
        if (chr == 'e') {
            // alt-e editor shortcut disabled without modifiers on CharacterEvent
        }

        this.searchBox.charTyped(event);
        return true;
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        if (super.keyPressed(event)) return true;

        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            this.navPop();
        } else if (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
            this.turnPage(true);
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT || keyCode == GLFW.GLFW_KEY_PAGE_UP) {
            this.turnPage(false);
        } else {
            return false;
        }

        return true;
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubleClick) {
        var scaled = new net.minecraft.client.input.MouseButtonEvent(
            event.x() * this.window.getGuiScale() / this.scaleFactor,
            event.y() * this.window.getGuiScale() / this.scaleFactor,
            event.buttonInfo()
        );

        if (this.uiAdapter.mouseClicked(scaled, doubleClick) || super.mouseClicked(scaled, doubleClick)) return true;

        int button = event.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.navPop();
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_4) {
            this.turnPage(true);
        } else if (button == GLFW.GLFW_MOUSE_BUTTON_5) {
            this.turnPage(false);
        } else {
            return false;
        }

        return true;
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double deltaX, double deltaY) {
        var scaled = new net.minecraft.client.input.MouseButtonEvent(
            event.x() * this.window.getGuiScale() / this.scaleFactor,
            event.y() * this.window.getGuiScale() / this.scaleFactor,
            event.buttonInfo()
        );
        return this.uiAdapter.mouseDragged(scaled, deltaX, deltaY) || super.mouseDragged(scaled, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        var scaled = new net.minecraft.client.input.MouseButtonEvent(
            event.x() * this.window.getGuiScale() / this.scaleFactor,
            event.y() * this.window.getGuiScale() / this.scaleFactor,
            event.buttonInfo()
        );
        return super.mouseReleased(scaled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        mouseX = mouseX * this.window.getGuiScale() / this.scaleFactor;
        mouseY = mouseY * this.window.getGuiScale() / this.scaleFactor;

        if (this.uiAdapter.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount) || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) return true;
        this.turnPage(verticalAmount < 0);

        return true;
    }

    @Override
    public void removed() {
        super.removed();

        var trail = new ArrayList<NavFrame.Replicator>();
        for (var frame : this.navStack) {
            trail.add(frame.replicator());
        }

        NAV_TRAILS.put(this.book.id(), trail);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public OwoUIAdapter<?> adapter() {
        return this.uiAdapter;
    }

    protected static List<NavFrame.Replicator> getNavTrail(Book book) {
        return NAV_TRAILS.computeIfAbsent(book.id(), $ -> Util.make(
                new ArrayList<>(),
                trail -> trail.add(0, new NavFrame.Replicator(LandingPageSupplier::new, 0))
        ));
    }

    /**
     * Push {@code entry} onto the navigation stack of {@code book}, making it the active
     * entry the next time the player opens this book
     */
    public static void pushEntry(Book book, Entry entry) {
        getNavTrail(book).add(0, new NavFrame.Replicator(screen -> new EntryPageSupplier(screen, entry), 0));
    }

    /**
     * Register {@code builder} as the preview builder for recipes of {@code recipeType}. This is necessary
     * to support custom recipe types and only applies to entries defined in the book {@code bookId}.
     * <p>
     * If you have multiple books that all display some of your custom recipes, you need to register the
     * same builder for all of them individually
     */
    public static <R extends Recipe<?>> void registerRecipePreviewBuilder(Identifier bookId, RecipeType<R> recipeType, RecipeFeature.RecipePreviewBuilder<R> builder) {
        RECIPE_HANDLERS.computeIfAbsent(bookId, $ -> new HashMap<>()).put(recipeType, builder);
    }

    /**
     * Register {@code provider} as the additional feature provider for the book
     * {@code bookId}.
     * <p>
     * Use this only if you need to build entirely custom markdown components
     * that cannot be otherwise implemented through owo-ui templates using the
     * {@code <|template:here|param=value|>} template instantiation syntax
     */
    public static void registerFeatureFactory(Identifier bookId, FeatureProvider provider) {
        FEATURE_PROVIDERS.put(bookId, provider);
    }

    public static abstract class PageSupplier {

        protected final LavenderBookScreen context;
        protected final List<UIComponent> pages = new ArrayList<>();

        protected PageSupplier(LavenderBookScreen context) {
            this.context = context;
        }

        public int pageCount() {
            return this.pages.size();
        }

        public UIComponent getPageContent(int pageIndex) {
            return this.pages.get(pageIndex);
        }

        public boolean searchable() {
            return false;
        }

        abstract boolean canMerge(PageSupplier other);

        abstract Function<LavenderBookScreen, @Nullable PageSupplier> replicator();

        // --- prebuilt utility ---

        protected FlowLayout pageWithHeader(@NotNull Component title) {
            return UIContainers.verticalFlow(Sizing.fill(100), Sizing.fill(100)).child(this.pageTitleHeader(title));
        }

        protected UIComponent pageTitleHeader(net.minecraft.network.chat.Component text) {
            var component = this.context.template(ParentUIComponent.class, "page-title-header");
            component.childById(LabelComponent.class, "title-label").text(text);
            return component;
        }

        protected ParentUIComponent parseMarkdown(String markdown) {
            var component = this.context.processor.process(markdown);
            component.forEachDescendant(descendant -> {
                if (descendant instanceof BookCompiler.BookLabelComponent label) {
                    label.setOwner(this.context);
                }

                if (descendant instanceof ItemComponent item) {
                    var entry = this.context.book.entryByAssociatedItem(item.stack());
                    if (entry == null || (this instanceof EntryPageSupplier entrySupplier && entry == entrySupplier.entry)) {
                        return;
                    }

                    if (!entry.canPlayerView(this.context.minecraft.player)) {
                        return;
                    }

                    var newTooltip = new ArrayList<ClientTooltipComponent>();
                    newTooltip.add(ClientTooltipComponent.create(Component.empty().getVisualOrderText()));
                    newTooltip.add(ClientTooltipComponent.create(Component.translatable("text.lavender.book.click_to_open").getVisualOrderText()));
                    newTooltip.add(ClientTooltipComponent.create(TextOps.withFormatting(entry.title(), ChatFormatting.GRAY).getVisualOrderText()));

                    if (item instanceof ItemListComponent ingredient) {
                        ingredient.extraTooltipSection(newTooltip);
                    } else {
                        newTooltip.addAll(0, item.tooltip());
                        item.tooltip(newTooltip);
                    }

                    item.mouseDown().subscribe((mouseEvent, doubleClick) -> {
                                int button = mouseEvent.button();
                        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;
                        this.context.navPush(new EntryPageSupplier(this.context, entry));

                        UISounds.playInteractionSound();
                        return true;
                    });
                }
            });

            return component;
        }

        protected List<FlowLayout> buildEntryIndex(Collection<Entry> entries, boolean respectOrdinals, int... pageSizes) {
            var indexSections = new ArrayList<FlowLayout>();
            var currentSectionHeight = new MutableInt(0);
            indexSections.add(UIContainers.verticalFlow(Sizing.fill(100), Sizing.content()));

            var searchText = this.context.searchBox.getValue().strip();
            var filter = searchText.isEmpty() ? new String[0] : searchText.split(" ");
            for (int i = 0; i < filter.length; i++) {
                filter[i] = filter[i].strip().toLowerCase(Locale.ROOT);
            }

            entries.stream()
                    .sorted((o1, o2) -> AlphanumComparator.compare(o1.title(), o2.title()))
                    .sorted(respectOrdinals ? Comparator.comparingInt(Entry::ordinal) : (o1, o2) -> 0)
                    .sorted(Comparator.comparing(entry -> !entry.canPlayerView(this.context.minecraft.player)))
                    .forEach(entry -> {
                        boolean entryVisible = entry.canPlayerView(this.context.minecraft.player);
                        if (entry.secret() && !entryVisible) {
                            return;
                        }

                        if (filter.length > 0) {
                            if (!entryVisible) return;

                            var entryTitle = entry.title().toLowerCase(Locale.ROOT);

                            var entryMatches = Arrays.stream(filter).allMatch(entryTitle::contains)
                                    || entry.additionalSearchTerms().stream().anyMatch(term -> Arrays.stream(filter).allMatch(term::contains));
                            if (!entryMatches) return;
                        }

                        FlowLayout indexItem;
                        boolean hasUnreadNotification;
                        if (entryVisible) {
                            hasUnreadNotification = this.context.book.shouldDisplayUnreadNotification(entry);

                            indexItem = this.context.template(FlowLayout.class, "index-item");
                            indexItem.childById(StackLayout.class, "icon-anchor").child(entry.iconFactory().apply(Sizing.fill()));

                            var label = indexItem.childById(LabelComponent.class, "index-label");

                            label.text(Component.literal(entry.title()).withStyle($ -> $.withFont(new net.minecraft.network.chat.FontDescription.Resource(net.minecraft.resources.Identifier.withDefaultNamespace("default"))).withItalic(false && hasUnreadNotification)));
                            label.mouseDown().subscribe((mouseEvent, doubleClick) -> {
                                int button = mouseEvent.button();
                                if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                                this.context.navPush(new EntryPageSupplier(this.context, entry));
                                UISounds.playInteractionSound();
                                return true;
                            });

                            var animation = label.color().animate(150, Easing.SINE, Color.ofFormatting(ChatFormatting.GOLD));
                            label.mouseEnter().subscribe(animation::forwards);
                            label.mouseLeave().subscribe(animation::backwards);

                            if (hasUnreadNotification) {
                                indexItem.child(new UnreadNotificationComponent(this.context.bookTexture(), false));
                            }
                        } else {
                            hasUnreadNotification = false;

                            indexItem = this.context.template(FlowLayout.class, "locked-index-item");
                            indexItem.childById(LabelComponent.class, "index-label").text(Component.translatable("text.lavender.entry.locked"));
                        }

                        int sectionIndex = indexSections.size() - 1;
                        int entryHeight = entry.canPlayerView(this.context.minecraft.player)
                                ? Math.max(8, this.lineCount(entry.title(), hasUnreadNotification) * 7) + 2
                                : 10;

                        if (currentSectionHeight.intValue() + entryHeight >= (sectionIndex < pageSizes.length ? pageSizes[sectionIndex] : 150)) {
                            indexSections.add(UIContainers.verticalFlow(Sizing.fill(100), Sizing.content()));
                            currentSectionHeight.setValue(0);
                        }

                        Iterables.getLast(indexSections).child(indexItem);
                        currentSectionHeight.add(entryHeight);
                    });

            return indexSections;
        }

        protected FlowLayout buildCategoryIndex(Stream<Category> categories) {
            var categoryContainer = UIContainers.ltrTextFlow(Sizing.fill(100), Sizing.content()).gap(4);
            categories
                    .sorted(Comparator.comparingInt(Category::ordinal))
                    .sorted(Comparator.comparing($ -> !this.context.book.shouldDisplayCategory($, this.context.minecraft.player)))
                    .forEach(category_ -> {
                        if (this.context.book.shouldDisplayCategory(category_, this.context.minecraft.player)) {
                            var icon = category_.iconFactory().apply(Sizing.fixed(16)).configure(categoryButton -> {
                                categoryButton
                                        .tooltip(Component.literal(category_.title()))
                                        .margins(Insets.of(4))
                                        .cursorStyle(CursorStyle.HAND);

                                categoryButton.mouseDown().subscribe((mouseEvent, doubleClick) -> {
                                int button = mouseEvent.button();
                                    if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                                    this.context.navPush(new CategoryPageSupplier(this.context, category_));
                                    UISounds.playInteractionSound();
                                    return true;
                                });
                            });

                            if (this.context.book.shouldDisplayUnreadNotification(category_, this.context.minecraft.player)) {
                                categoryContainer.child(UIContainers.stack(Sizing.content(), Sizing.content())
                                        .child(icon)
                                        .child(new UnreadNotificationComponent(this.context.bookTexture(), true)
                                                .positioning(Positioning.relative(100, 100))
                                                .margins(Insets.of(0, 1, 0, 1)))
                                );
                            } else {
                                categoryContainer.child(icon);
                            }
                        } else if (!category_.secret()) {
                            categoryContainer.child(this.context.template(UIComponent.class, "locked-category-button"));
                        }
                    });

            return categoryContainer;
        }

        protected int lineCount(String entryTitle, boolean hasNotification) {
            return this.context.minecraft.font.split(net.minecraft.network.chat.Component.literal(entryTitle), hasNotification ? 90 : 98).size();
        }

        public interface Bookmarkable {
            void addBookmark();
        }
    }

    public static class LandingPageSupplier extends PageSupplier {

        public LandingPageSupplier(LavenderBookScreen context) {
            super(context);

            var book = this.context.book;
            var landingPageEntry = book.landingPage();

            if (landingPageEntry != null) {
                var landingPage = UIContainers.verticalFlow(Sizing.fill(100), Sizing.fill(100));
                landingPage.child(this.context.template(UIComponent.class, "landing-page-header", Map.of("page-title", landingPageEntry.title())));
                landingPage.child(this.parseMarkdown(landingPageEntry.content()));

                if (book.displayCompletion()) {
                    var completionBar = this.context.template(
                            FlowLayout.class,
                            "completion-bar",
                            Map.of("progress", String.valueOf((int) (100 * (book.countVisibleEntries(this.context.minecraft.player) / (float) book.entries().size()))))
                    );

                    completionBar.childById(LabelComponent.class, "completion-label")
                            .text(Component.translatable("text.lavender.book.unlock_progress", book.countVisibleEntries(this.context.minecraft.player), book.entries().size()));

                    landingPage.child(completionBar);
                }

                this.pages.add(landingPage);
            } else {
                this.pages.add(this.context.template(UIComponent.class, "empty-page-content"));
            }

            var indexPage = UIContainers.verticalFlow(Sizing.fill(100), Sizing.content());
            this.pages.add(indexPage);

            var rootCategories = book.categories().stream().filter(category -> category.parent() == null).toList();
            if (!book.categories().isEmpty()) {
                var categories = this.pageWithHeader(Component.translatable("text.lavender.categories"));
                categories.verticalSizing(Sizing.content());

                var categoryContainer = this.buildCategoryIndex(rootCategories.stream());
                categories.child(categoryContainer);

                categoryContainer.child(UIComponents.item(LavenderBookItem.itemOf(this.context.book)).<ItemComponent>configure(categoryButton -> {
                    categoryButton
                            .tooltip(Component.translatable("text.lavender.index_category"))
                            .margins(Insets.of(4))
                            .cursorStyle(CursorStyle.HAND);

                    categoryButton.mouseDown().subscribe((mouseEvent, doubleClick) -> {
                                int button = mouseEvent.button();
                        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) return false;

                        this.context.navPush(new IndexPageSupplier(this.context));
                        UISounds.playInteractionSound();
                        return true;
                    });
                }));

                indexPage.child(categories);
            }

            indexPage.child(book.categories().isEmpty()
                    ? this.pageTitleHeader(Component.translatable("text.lavender.index"))
                    : this.context.bookComponentSource.builtinTemplate(UIComponent.class, "horizontal-rule").margins(Insets.vertical(6))
            );

            int entriesOnCategoryPage = !book.categories().isEmpty()
                    ? 150 - 36 - (int) Math.ceil((double) (rootCategories.size() + 1) / (4)) * 24
                    : 150;

            var orphanedEntries = this.buildEntryIndex(book.orphanedEntries(), true, entriesOnCategoryPage);
            indexPage.child(orphanedEntries.remove(0));
            this.pages.addAll(orphanedEntries);
        }

        @Override
        public boolean searchable() {
            return true;
        }

        @Override
        public boolean canMerge(PageSupplier other) {
            return other instanceof LandingPageSupplier;
        }

        @Override
        public Function<LavenderBookScreen, @Nullable PageSupplier> replicator() {
            return LandingPageSupplier::new;
        }
    }

    public static class IndexPageSupplier extends PageSupplier {

        public IndexPageSupplier(LavenderBookScreen context) {
            super(context);

            var entries = this.buildEntryIndex(this.context.book.entries(), false, 125);
            this.pages.add(this.pageWithHeader(Component.translatable("text.lavender.index_category.title")).child(entries.remove(0)));
            this.pages.addAll(entries);
        }

        @Override
        public boolean searchable() {
            return true;
        }

        @Override
        boolean canMerge(PageSupplier other) {
            return other instanceof IndexPageSupplier;
        }

        @Override
        Function<LavenderBookScreen, @Nullable PageSupplier> replicator() {
            return IndexPageSupplier::new;
        }
    }

    public static class EditorPageSupplier extends PageSupplier {

        private static String editorTextCache = "";

        protected EditorPageSupplier(LavenderBookScreen context) {
            super(context);
            var editorComponent = UIComponents.textArea(Sizing.fill(100), Sizing.fill(100)).<TextAreaComponent>configure(editor -> {
                editor.onChanged().subscribe(value -> {
                    editorTextCache = value;

                    if (this.pages.size() > 1) {
                        this.pages.subList(1, this.pages.size()).clear();
                    }

                    var compiled = this.context.processor.process(editorTextCache);
                    boolean firstPage = true;

                    while (!compiled.children().isEmpty()) {
                        var component = compiled.children().get(0);
                        compiled.removeChild(component);

                        if (firstPage) {
                            firstPage = false;
                            this.pages.add(this.pageWithHeader(Component.literal("Title Here")).child(component));
                        } else {
                            this.pages.add(component);
                        }
                    }

                    this.context.rebuildContent(null);
                });
            });

            this.pages.add(editorComponent);
            editorComponent.text(editorTextCache);
        }

        @Override
        public UIComponent getPageContent(int pageIndex) {
            return pageIndex % 2 == 0
                    ? super.getPageContent(0)
                    : super.getPageContent(pageIndex / 2 + 1);
        }

        @Override
        public int pageCount() {
            return Math.max(1, (super.pageCount() - 1) * 2);
        }

        @Override
        boolean canMerge(PageSupplier other) {
            return other instanceof EditorPageSupplier;
        }

        @Override
        Function<LavenderBookScreen, @Nullable PageSupplier> replicator() {
            return EditorPageSupplier::new;
        }
    }

    public static class CategoryPageSupplier extends PageSupplier implements PageSupplier.Bookmarkable {

        private final Category category;

        public CategoryPageSupplier(LavenderBookScreen context, Category category) {
            super(context);
            this.category = category;

            // --- landing page ---

            var parsedLandingPage = this.parseMarkdown(category.content());

            var landingPageContent = parsedLandingPage.children().get(0);
            parsedLandingPage.removeChild(landingPageContent);

            var landingPage = this.pageWithHeader(Component.literal(category.title())).child(landingPageContent);
            this.pages.add(landingPage);

            // --- category & entry index ---

            var categoryContainer = this.buildCategoryIndex(this.context.book.categories().stream().filter(category_ -> Objects.equals(category_.parent(), category.id())));
            int spaceOnCategoryPage = !categoryContainer.children().isEmpty()
                    ? 125 - 15 - (int) Math.ceil((double) (categoryContainer.children().size()) / (4)) * 24
                    : 125;

            var entries = this.context.book.entriesByCategory(this.category);
            if (entries != null) {
                var indexPages = this.buildEntryIndex(entries, true, spaceOnCategoryPage);
                for (int i = 0; i < indexPages.size(); i++) {
                    var page = i == 0
                            ? this.pageWithHeader(Component.translatable("text.lavender.index")).child(indexPages.get(0))
                            : indexPages.get(i);

                    if (i == 0 && !categoryContainer.children().isEmpty()) {
                        page.child(1, categoryContainer).child(2, this.context.bookComponentSource.builtinTemplate(UIComponent.class, "horizontal-rule").margins(Insets.vertical(6)));
                    }

                    this.pages.add(page);
                }

                if (this.context.book.displayCompletion()) {
                    var completionBar = this.context.template(
                            FlowLayout.class,
                            "completion-bar",
                            Map.of("progress", String.valueOf((int) (100 * (this.countVisibleEntries(entries, this.context.minecraft.player) / (float) entries.size()))))
                    );

                    completionBar.childById(LabelComponent.class, "completion-label")
                            .text(Component.translatable("text.lavender.book.unlock_progress", this.countVisibleEntries(entries, this.context.minecraft.player), entries.size()));

                    landingPage.child(completionBar);
                }
            } else {
                this.pages.add(this.pageWithHeader(Component.translatable("text.lavender.index")).child(categoryContainer).child(this.context.bookComponentSource.builtinTemplate(UIComponent.class, "horizontal-rule").margins(Insets.vertical(6))));
            }
        }

        protected int countVisibleEntries(Collection<Entry> entries, LocalPlayer player) {
            int visible = 0;
            for (var entry : entries) {
                if (!entry.canPlayerView(player)) continue;
                visible++;
            }

            return visible;
        }

        @Override
        public boolean searchable() {
            return true;
        }

        @Override
        public boolean canMerge(PageSupplier other) {
            return other instanceof CategoryPageSupplier supplier && supplier.category.id().equals(this.category.id());
        }

        @Override
        public Function<LavenderBookScreen, @Nullable PageSupplier> replicator() {
            var categoryId = this.category.id();
            return context -> {
                var category = context.book.categoryById(categoryId);
                return category != null && context.book.shouldDisplayCategory(category, context.minecraft.player) ? new CategoryPageSupplier(context, category) : null;
            };
        }

        @Override
        public void addBookmark() {
            LavenderClientStorage.addBookmark(this.context.book, this.category);
        }
    }

    public static class EntryPageSupplier extends PageSupplier implements PageSupplier.Bookmarkable {

        private final Entry entry;

        public EntryPageSupplier(LavenderBookScreen context, Entry entry) {
            super(context);
            this.entry = entry;

            var pages = this.parseMarkdown(entry.content());
            boolean firstPage = true;

            while (!pages.children().isEmpty()) {
                var component = pages.children().get(0);
                pages.removeChild(component);

                if (firstPage) {
                    firstPage = false;
                    this.pages.add(this.pageWithHeader(Component.literal(entry.title())).child(component));
                } else {
                    this.pages.add(component);
                }
            }

            LavenderClientStorage.markEntryViewed(this.context.book, entry);
        }

        @Override
        public boolean canMerge(PageSupplier other) {
            return other instanceof EntryPageSupplier supplier && supplier.entry.id().equals(this.entry.id());
        }

        @Override
        public Function<LavenderBookScreen, @Nullable PageSupplier> replicator() {
            var entryId = this.entry.id();
            return context -> {
                var entry = context.book.entryById(entryId);
                return entry != null && entry.canPlayerView(context.minecraft.player) ? new EntryPageSupplier(context, entry) : null;
            };
        }

        @Override
        public void addBookmark() {
            LavenderClientStorage.addBookmark(this.context.book, this.entry);
        }
    }

    public static class NavFrame {
        public final PageSupplier pageSupplier;
        public int selectedPage;

        public NavFrame(PageSupplier pageSupplier, int selectedPage) {
            this.pageSupplier = pageSupplier;
            this.selectedPage = selectedPage;
        }

        public Replicator replicator() {
            return new Replicator(this.pageSupplier.replicator(), this.selectedPage);
        }

        public record Replicator(Function<LavenderBookScreen, @Nullable PageSupplier> pageSupplier, int selectedPage) {
            public @Nullable NavFrame createFrame(LavenderBookScreen screen) {
                var supplier = this.pageSupplier.apply(screen);
                if (supplier == null) return null;

                return new NavFrame(supplier, this.selectedPage);
            }
        }
    }

    @FunctionalInterface
    public interface FeatureProvider {
        /**
         * Create a new set of additional features to insert into the Markdown
         * processor used for compiling entries of your book. {@code bookComponentSource}
         * should be used to create components from templates as it has a set of Lavender-specific
         * template parameters pre-filled
         */
        List<MarkdownFeature> createFeatures(BookCompiler.ComponentSource componentSource);
    }

    static {
        UIParsing.registerFactory(Lavender.id("structure"), StructureComponent::parse);
        UIParsing.registerFactory(Lavender.id("unread-notification"), UnreadNotificationComponent::parse);
    }
}

