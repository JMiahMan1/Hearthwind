package net.adventurez.item;

import java.util.List;
import java.util.function.Consumer;

import net.adventurez.init.ConfigInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.Level;

public class Handbook extends Item {

    private static final List<Component> PAGES = List.of(
            Component.literal("You may ask yourself, what does NRTTM mean... It does mean: Not related to the mod :D This are just some sentences I put together because I like to do it^^ First of all, thank you for playing the AdventureZ mod, I hope it will give your world a little bit more of a challenge and"),
            Component.literal("you like it :) But what I really want to tell you is: You are appreciated and loved as the person you are! I know this sounds a little cheesy but thats how it is: You are loved! My name is Micha, in some games I took the name Globox (because I like the character of Rayman ;)),"),
            Component.literal("I'm a normal person just like you (I know everybody is special of course but you get the point) and I hope you believe deep inside yourself that you are a loved person. Hopefully by people you know closely, if not, there will be people who will love you just as you are."),
            Component.literal("Why am I writing this? Good question :D Because from time to time I feel like people believe in this specific aspect a lie, that they are not loved or something even worse. Also I think, except of human needs, love is the most important thing in life you need."),
            Component.literal("It is just on my heart to say to you: You are loved! I mean, I do not know you and we might be not on a same wavelength in our personality and we would perhaps even not hang out as friends if we know each other but that doesn't change the truth about you"),
            Component.literal("(which I believe and hopefully you too). You might be young or maybe a little older, at one point in your life you should figure out why and for what reason you live here in this world (basically the meaning of your life),"),
            Component.literal("hopefully you already found it but let me tell you something: It is a great process everybody has to go through :D (I don't know how I got to the meaning of life right now... mh...). However I might edit this text a few times^^."),
            Component.literal("I wish you the best, if you got questions about the mods or anything else, hit me up on Discord (Globox_Z #1024) or anywhere else"));

    private static final WrittenBookContent WRITTEN_BOOK = new WrittenBookContent(Filterable.passThrough("NRTTM"), "Globox_Z", 1,
            PAGES.stream().map(Filterable::passThrough).toList(), true);

    public Handbook(Item.Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (!stack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, WRITTEN_BOOK);
        }
        if (!level.isClientSide() && user instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundOpenBookPacket(hand));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (ConfigInit.CONFIG.allow_extra_tooltips) {
            tooltip.accept(Component.translatable("item.adventurez.moreinfo.tooltip"));
        }
        tooltip.accept(Component.translatable("book.byAuthor", "Globox_Z").withStyle(ChatFormatting.GRAY));
    }
}
