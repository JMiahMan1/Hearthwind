package io.wispforest.lavender;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.wispforest.lavender.book.Book;
import io.wispforest.lavender.book.BookLoader;
import io.wispforest.lavender.book.LavenderBookItem;
import io.wispforest.lavender.client.StructureOverlayRenderer;
import io.wispforest.lavender.structure.LavenderStructures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public class LavenderCommands {

    private static final SimpleCommandExceptionType NO_SUCH_BOOK = new SimpleCommandExceptionType(Component.literal("No such book is loaded"));

    private static final SuggestionProvider<FabricClientCommandSource> LOADED_BOOKS = (context, builder) -> {
        return net.minecraft.commands.SharedSuggestionProvider.suggestResource(BookLoader.loadedBooks().stream().map(Book::id), builder);
    };

    @Environment(EnvType.CLIENT)
    public static class Client {

        private static final SimpleCommandExceptionType NO_SUCH_STRUCTURE = new SimpleCommandExceptionType(Component.literal("No such structure is loaded"));
        private static final SuggestionProvider<FabricClientCommandSource> STRUCTURE_INFO = (context, builder) ->
                net.minecraft.commands.SharedSuggestionProvider.suggest(LavenderStructures.loadedStructures().stream().map(Identifier::toString), builder);

        private static int executeGetLavenderBook(CommandContext<FabricClientCommandSource> context, boolean forceDynamicBook) throws CommandSyntaxException {
            var book = BookLoader.get(context.getArgument("book_id", Identifier.class));
            if (book == null) {
                throw NO_SUCH_BOOK.create();
            }

            var stack = forceDynamicBook
                    ? LavenderBookItem.createDynamic(book)
                    : LavenderBookItem.itemOf(book);

            var command = "/give @s " + BuiltInRegistries.ITEM.getKey(stack.getItem());

            var ops = net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, context.getSource().getLevel().registryAccess());
            var components = stack.getComponentsPatch().entrySet().stream().flatMap(entry -> {
                var componentType = entry.getKey();
                var typeKey = BuiltInRegistries.DATA_COMPONENT_TYPE.getResourceKey(componentType);
                if (typeKey.isEmpty()) return Stream.empty();
                var typeId = typeKey.get().identifier().toString();

                var componentOptional = entry.getValue();
                if (componentOptional.isPresent()) {
                    var componentValue = componentOptional.get();
                    return ((com.mojang.serialization.Codec<Object>) (com.mojang.serialization.Codec<?>) componentType.codec()).encodeStart(ops, componentValue).result().stream().map(value -> typeId + "=" + value);
                } else {
                    return Stream.of("!" + typeId);
                }
            }).collect(Collectors.joining(String.valueOf(',')));

            if (!components.isEmpty()) {
                command += "[" + components + "]";
            }

            if (stack.getCount() > 1) {
                command += " " + stack.getCount();
            }

            var jAvAsE = command;
            context.getSource().getClient().execute(() -> context.getSource().getClient().setScreenAndShow(new ChatScreen(jAvAsE, false)));

            return 0;
        }

        public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext access) {
            dispatcher.register(literal("get-lavender-book").requires(source -> source.permissions().hasPermission(net.minecraft.server.permissions.Permissions.COMMANDS_GAMEMASTER))
                    .then(argument("book_id", IdentifierArgument.id()).suggests(LOADED_BOOKS)
                            .executes(context -> executeGetLavenderBook(context, false))
                            .then(argument("force_dynamic_book", BoolArgumentType.bool())
                                    .executes(context -> executeGetLavenderBook(context, BoolArgumentType.getBool(context, "force_dynamic_book"))))));


            dispatcher.register(literal("structure-overlay")
                    .then(literal("clear-all").executes(context -> {
                        StructureOverlayRenderer.clearOverlays();
                        return 0;
                    }))

                    .then(literal("add")
                            .then(argument("structure", IdentifierArgument.id()).suggests(STRUCTURE_INFO).executes(context -> {
                                var structureId = context.getArgument("structure", Identifier.class);
                                if (LavenderStructures.get(structureId) == null) throw NO_SUCH_STRUCTURE.create();

                                StructureOverlayRenderer.addPendingOverlay(structureId);
                                return 0;
                            }))));
        }
    }

}
