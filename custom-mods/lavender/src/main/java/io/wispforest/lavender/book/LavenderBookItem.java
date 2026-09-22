package io.wispforest.lavender.book;

import com.google.common.base.Preconditions;
import io.wispforest.lavender.Lavender;
import io.wispforest.lavender.client.LavenderBookScreen;
import io.wispforest.owo.ops.TextOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LavenderBookItem extends Item {

    public static final DataComponentType<Identifier> BOOK_ID = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Lavender.id("book_id"),
            DataComponentType.<Identifier>builder()
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC)
                    .build()
    );

    public static final LavenderBookItem DYNAMIC_BOOK = new LavenderBookItem(null, new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Lavender.id("dynamic_book"))));

    private static final Map<Identifier, LavenderBookItem> BOOK_ITEMS = new HashMap<>();

    private final @Nullable Identifier bookId;

    private LavenderBookItem(@Nullable Identifier bookId, Item.Properties settings) {
        super(settings);
        this.bookId = bookId;
    }

    protected LavenderBookItem(Item.Properties settings, @NotNull Identifier bookId) {
        super(settings);
        this.bookId = Preconditions.checkNotNull(bookId, "Book-specific book items must have a non-null book ID");
    }

    @SuppressWarnings("DataFlowIssue")
    protected @NotNull Identifier bookId() {
        return this.bookId;
    }

    /**
     * Shorthand of {@link #registerForBook(Identifier, Identifier, net.minecraft.world.item.Item.Settings)} which
     * uses {@code bookId} as the item id
     */
    public static LavenderBookItem registerForBook(@NotNull Identifier bookId, Item.Properties settings) {
        return registerForBook(bookId, bookId, settings);
    }

    /**
     * Create, register and return a book item under {@code itemId} as the canonical
     * item for the book referred to by the given {@code bookId}
     */
    public static LavenderBookItem registerForBook(@NotNull Identifier bookId, @NotNull Identifier itemId, Item.Properties settings) {
        return registerForBook(Registry.register(BuiltInRegistries.ITEM, itemId, new LavenderBookItem(bookId, settings.setId(ResourceKey.create(Registries.ITEM, itemId)))));
    }

    /**
     * Register and return the given book item as the canonical item
     * for the book referred to by the item's bookId field
     */
    public static LavenderBookItem registerForBook(LavenderBookItem item) {
        BOOK_ITEMS.put(item.bookId(), item);
        return item;
    }

    /**
     * @return The id of the book referred to by the given item stack
     * (either through static associating of NBT in the case the dynamic book),
     * or {@code null} if neither a static association nor NBT exist
     */
    public static @Nullable Identifier bookIdOf(ItemStack bookStack) {
        if (!(bookStack.getItem() instanceof LavenderBookItem book)) return null;
        return book.bookId != null ? book.bookId : bookStack.get(BOOK_ID);
    }

    /**
     * Convenience variant of {@link #bookIdOf(ItemStack)} which attempts
     * looking up the book referred to by the id said method returns
     */
    public static @Nullable Book bookOf(ItemStack bookStack) {
        var bookId = bookIdOf(bookStack);
        if (bookId == null) return null;

        return BookLoader.get(bookId);
    }

    /**
     * @return An item stack representing the given book. If a canonical item
     * was registered, it is used - otherwise a dynamic book with NBT is created
     */
    public static ItemStack itemOf(Book book) {
        var bookItem = BOOK_ITEMS.get(book.id());
        if (bookItem != null) {
            return bookItem.getDefaultInstance();
        } else {
            return createDynamic(book);
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        if (this.bookId != null) return super.getName(stack);

        var book = bookOf(stack);
        if (book == null || book.dynamicBookName() == null) return super.getName(stack);

        return book.dynamicBookName();
    }

    /**
     * @return A dynamic book with the correct NBT to represent the given book
     */
    public static ItemStack createDynamic(Book book) {
        var stack = DYNAMIC_BOOK.getDefaultInstance();
        stack.set(BOOK_ID, book.id());
        return stack;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        var playerStack = user.getItemInHand(hand);

        var bookId = bookIdOf(playerStack);
        if (bookId == null) return InteractionResult.SUCCESS;
        if (!world.isClientSide()) return InteractionResult.SUCCESS;

        var book = BookLoader.get(bookId);
        if (book == null) {
            user.sendSystemMessage(Component.translatable("text.lavender.unknown_book", bookId).withStyle(ChatFormatting.RED));
            return InteractionResult.PASS;
        }

        openBookScreen(book);
        return InteractionResult.SUCCESS;
    }

    @Environment(EnvType.CLIENT)
    private static void openBookScreen(Book book) {
        Minecraft.getInstance().setScreenAndShow(new LavenderBookScreen(book));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, net.minecraft.world.item.component.TooltipDisplay tooltipDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag type) {
        var bookId = bookIdOf(stack);
        if (bookId == null) {
            tooltip.accept(TextOps.withFormatting("⚠ §No associated book", ChatFormatting.RED, ChatFormatting.DARK_GRAY));
        } else {
            var book = BookLoader.get(bookId);
            if (book != null) return;

            tooltip.accept(TextOps.withFormatting("⚠ §Unknown book \"" + bookId + "\"", ChatFormatting.RED, ChatFormatting.DARK_GRAY));
        }
    }
}
