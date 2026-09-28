package dev.jmiahman.hearthwind.survival;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * Starter kit &amp; Guidebook system for Hearthwind (Aged 3.1.2 Parity).
 *
 * <p>Aged does not hand anything out during {@code JOIN}. The pack ships the
 * {@code welcomescreen} mod with an {@code aged_welcome_screen} datapack, so
 * joining a world opens a welcome screen and its {@code Start} button runs:
 *
 * <pre>
 * /item replace entity @s hotbar.0 with minecraft:bread 4
 * /item replace entity @s hotbar.1 with minecraft:apple 4
 * /item replace entity @s hotbar.4 with lavender:dynamic_book{BookId:'aged:aged_guide_book'}
 * /item replace entity @s hotbar.7 with minecraft:potion{Potion:'minecraft:purified_water'}
 * /item replace entity @s hotbar.8 with minecraft:campfire
 * </pre>
 *
 * <p>So the slots, the counts and the purified water bottle are all part of the
 * first ten minutes a player sees, and the {@code Start} button is what grants
 * them. We rebuild that flow: the server sends {@link WelcomeScreenPayload} on
 * the first join, the client shows {@code WelcomeScreen}, and the button's
 * {@link WelcomeStartPayload} is what calls {@link #grantStarterKit}.
 *
 * <p>Deviation: Aged's command names the potion {@code minecraft:purified_water}
 * (Dehydration 1.3.6 registers it with
 * {@code Registry.register(Registries.POTION, "purified_water", ...)}, which
 * defaults to the vanilla namespace). Our dehydration port registers the same
 * potion as {@code dehydration:purified_water}, so the granted item is the real
 * purified water bottle rather than an unresolvable potion reference.
 */
public final class StarterKit {
    public static final String STARTER_TAG = "hearthwind:starter_kit_granted";

    /** Hotbar slot of the first bread stack (Aged {@code hotbar.0}). */
    public static final int SLOT_BREAD = 0;
    /** Hotbar slot of the apples (Aged {@code hotbar.1}). */
    public static final int SLOT_APPLE = 1;
    /** Hotbar slot of the guide book (Aged {@code hotbar.4}). */
    public static final int SLOT_GUIDE = 4;
    /** Hotbar slot of the purified water bottle (Aged {@code hotbar.7}). */
    public static final int SLOT_PURIFIED_WATER = 7;
    /** Hotbar slot of the campfire (Aged {@code hotbar.8}). */
    public static final int SLOT_CAMPFIRE = 8;

    /**
     * Ticks a client gets to answer the welcome payload before the loadout is
     * granted anyway: three seconds, long enough to read the screen and press
     * Start, short enough that nobody waits for supplies.
     */
    private static final int FALLBACK_TICKS = 60;

    /** Players waiting on a {@code WelcomeStartPayload}, with ticks left. */
    private static final Map<UUID, PendingWelcome> pendingFallbacks = new HashMap<>();

    /** A player who was offered the welcome screen and has not answered yet. */
    private record PendingWelcome(ServerPlayer player, int ticksLeft) {
        PendingWelcome tick() {
            return new PendingWelcome(this.player, this.ticksLeft - 1);
        }
    }

    private StarterKit() {}

    public static void register() {
        PayloadTypeRegistry.clientboundPlay().register(WelcomeScreenPayload.TYPE, WelcomeScreenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(WelcomeStartPayload.TYPE, WelcomeStartPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(WelcomeStartPayload.TYPE,
                (payload, context) -> {
                    if (!payload.pressed()) {
                        return;
                    }
                    ServerPlayer player = context.player();
                    context.server().execute(() -> beginAdventure(player));
                });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (player.entityTags().contains(STARTER_TAG)) {
                return;
            }
            // Aged: nothing is granted until the welcome screen's Start button
            // runs its commands. The payload is sent unconditionally - a client
            // without hearthwind-client simply does not decode it - because
            // ServerPlayNetworking.canSend is always false here: fabric-api only
            // sends the client's channel registration from
            // ClientPlayNetworkAddon#onServerReady, which the client reaches
            // *after* the respawn packet this event runs behind. The fallback
            // below is what covers a client that never answers, so it is armed
            // BEFORE the send and a failed send must never skip it.
            armWelcomeFallback(player, FALLBACK_TICKS);
            try {
                ServerPlayNetworking.send(player, new WelcomeScreenPayload(true));
            } catch (RuntimeException e) {
                HearthwindSurvival.LOGGER.warn(
                        "Could not send the welcome payload to {}; the fallback will grant the loadout",
                        player.getName().getString(), e);
            }
        });

        // Nobody may be left with an empty hotbar because their client could
        // not show (or did not answer) the welcome screen: a client without
        // hearthwind-client, or a player who quit mid-screen, still gets the
        // loadout three seconds later. The tag makes this a no-op once the Start
        // button (or an earlier fallback) has already granted it. The pending
        // entry holds the player itself, not a lookup, so a client that hangs
        // up before the fallback fires is still covered.
        ServerTickEvents.END_SERVER_TICK.register(server -> tickWelcomeFallbacks());

        // Command to retrieve a replacement survival guidebook
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("guide")
                    .executes(ctx -> {
                        if (ctx.getSource().getEntity() instanceof ServerPlayer sp) {
                            giveOrDrop(sp, createGuidebook());
                            sp.sendSystemMessage(Component.literal("§a[Hearthwind] Granted Survival Guidebook."));
                            return 1;
                        }
                        return 0;
                    }));
            dispatcher.register(Commands.literal("guidebook")
                    .executes(ctx -> {
                        if (ctx.getSource().getEntity() instanceof ServerPlayer sp) {
                            giveOrDrop(sp, createGuidebook());
                            sp.sendSystemMessage(Component.literal("§a[Hearthwind] Granted Survival Guidebook."));
                            return 1;
                        }
                        return 0;
                    }));
        });
    }

    /**
     * Put a player back in the state of a first join and offer the welcome
     * screen again: the tag goes, the five Aged hotbar slots are emptied, and
     * the payload is re-sent.
     *
     * <p>The client gametest tour drives the whole welcome path (screen, Start
     * button, loadout) against a real client, and a session that already joined
     * a world earlier has the tag set, so the tour needs a deterministic first
     * join instead of whatever the previous test happened to leave behind.
     */
    public static void resetAndOfferWelcomeScreen(ServerPlayer player) {
        player.removeTag(STARTER_TAG);
        for (int slot : new int[] {SLOT_BREAD, SLOT_APPLE, SLOT_GUIDE, SLOT_PURIFIED_WATER, SLOT_CAMPFIRE}) {
            player.getInventory().setItem(slot, ItemStack.EMPTY);
        }
        pendingFallbacks.put(player.getUUID(), new PendingWelcome(player, FALLBACK_TICKS));
        try {
            ServerPlayNetworking.send(player, new WelcomeScreenPayload(true));
        } catch (RuntimeException e) {
            HearthwindSurvival.LOGGER.warn("Could not send the welcome payload; the fallback will grant the loadout", e);
        }
    }

    /**
     * Remember that this player still owes the server an answer to the welcome
     * payload, and that {@link #tickWelcomeFallbacks()} grants the loadout in
     * {@code ticks} ticks if nothing arrives.
     *
     * <p>Armed BEFORE the payload is sent, so a client whose channel send throws
     * still gets its supplies.
     */
    static void armWelcomeFallback(ServerPlayer player, int ticks) {
        pendingFallbacks.put(player.getUUID(), new PendingWelcome(player, ticks));
    }

    /**
     * Count every unanswered welcome offer down and grant the loadout to the
     * ones that ran out of ticks. One tick per server tick, so the default
     * {@link #FALLBACK_TICKS} is three seconds.
     */
    static void tickWelcomeFallbacks() {
        if (pendingFallbacks.isEmpty()) {
            return;
        }
        List<UUID> due = new ArrayList<>();
        pendingFallbacks.replaceAll((uuid, pending) -> {
            if (pending.ticksLeft() <= 1) {
                due.add(uuid);
                return pending;
            }
            return pending.tick();
        });
        for (UUID uuid : due) {
            PendingWelcome pending = pendingFallbacks.remove(uuid);
            if (pending != null) {
                beginAdventure(pending.player());
            }
        }
    }

    /** True while this player still owes the server an answer to the welcome payload. */
    public static boolean isAwaitingWelcomeStart(ServerPlayer player) {
        return pendingFallbacks.containsKey(player.getUUID());
    }

    /** Inventory first, world drop when the pack is full. */
    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        boolean added = player.getInventory().add(stack);
        if (!added || !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    /**
     * The player pressed {@code Start}: grant the loadout once and mark the tag
     * so the welcome screen never opens for them again.
     */
    public static void beginAdventure(ServerPlayer player) {
        if (player.entityTags().contains(STARTER_TAG)) {
            return;
        }
        pendingFallbacks.remove(player.getUUID());
        grantStarterKit(player);
        player.addTag(STARTER_TAG);
    }

    /**
     * Aged's five welcome-screen commands, hotbar slots and all: bread x4,
     * apples x4, the guide book, a purified water bottle and a campfire.
     * Slots are overwritten, exactly like {@code /item replace entity @s}.
     */
    public static void grantStarterKit(ServerPlayer player) {
        player.getInventory().setItem(SLOT_BREAD, new ItemStack(Items.BREAD, 4));
        player.getInventory().setItem(SLOT_APPLE, new ItemStack(Items.APPLE, 4));
        player.getInventory().setItem(SLOT_GUIDE, createGuidebook());
        player.getInventory().setItem(SLOT_PURIFIED_WATER, purifiedWaterBottle());
        player.getInventory().setItem(SLOT_CAMPFIRE, new ItemStack(Items.CAMPFIRE));
        player.sendSystemMessage(Component.literal(
                "§6§l[Hearthwind]§r §eYour supplies are in the hotbar: bread, apples, the guide book, "
                        + "a bottle of purified water and a campfire. Type §6/guide§e any time for a new book."));
    }

    private static ItemStack purifiedWaterBottle() {
        ItemStack potion = new ItemStack(Items.POTION);
        potion.set(DataComponents.POTION_CONTENTS, new PotionContents(PurifiedWater.PURIFIED_POTION));
        return potion;
    }

    public static ItemStack createGuidebook() {
        if (FabricLoader.getInstance().isModLoaded("lavender")) {
            if (BuiltInRegistries.ITEM.getOptional(GuideBook.ID).isPresent()) {
                return new ItemStack(BuiltInRegistries.ITEM.getValue(GuideBook.ID));
            }
        }
        return createVanillaGuidebook();
    }

    private static ItemStack createVanillaGuidebook() {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        List<Filterable<Component>> pages = new ArrayList<>();

        // Page 1: Welcome & Health
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lHEARTHWIND SURVIVAL§r\n" +
                "§8Aged Progression Guide§r\n\n" +
                "§4§lStarting Health:§r\n" +
                "You begin with §c3 Hearts (6.0 HP)§0.\n\n" +
                "Level up your §1Health§0 skill to unlock up to §c18 Hearts (36 HP)§0.\n\n" +
                "§2§lStarter Equipment:§r\n" +
                "• Bread x4 and Apples x4\n" +
                "• Survival Guidebook\n" +
                "• A Bottle of Purified Water\n" +
                "• Campfire"
        )));

        // Page 2: Thirst & Hydration
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lTHIRST & HYDRATION§r\n\n" +
                "10 droplets fill the row just above your hunger bar.\n\n" +
                "§1Drinking:§r\n" +
                "• Craft a §9Leather Flask§0 and right-click open water to fill it.\n" +
                "• An empty §9Glass Bottle§0 only becomes purified water at a\n" +
                "  campfire, a cauldron over fire, or a bamboo pump.\n" +
                "• Sneak and hold right-click on §9still§0 water with an empty\n" +
                "  hand for ~4s. The source is used up, and it may leave you Thirsty.\n" +
                "• Many foods & drinks (apples, melons, stews, milk, teas) also restore thirst."
        )));

        // Page 3: Body Temperature
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lBODY TEMPERATURE§r\n\n" +
                "The body icon above your hotbar is your core temperature (-2400 to +2400);\n" +
                "the thermometer beside it is the ambient reading.\n\n" +
                "§9Freezing (-1800):§r Cold biomes, night, rain, altitude; iced armor and ice packs.\n\n" +
                "§6Overheating (+1800):§r Deserts, nether, lava, magma; -30% attack damage.\n\n" +
                "§2Warmth:§r Lit campfires, lava and furnaces heat you within 3 blocks (blocks must be in sight); leather and Wolf Pelt armor give +3 a piece.\n\n" +
                "At §9-2400§0 you freeze; at §6+2400§0 you exhaust. Find shelter or shade!"
        )));

        // Page 4: Diet & 5 Nutrients
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lDIET & NUTRITION§r\n\n" +
                "Track 5 nutrients (0-300) by pressing §2'N'§0 or the button at the top right of your inventory:\n\n" +
                "• §cCarbohydrates§0\n" +
                "• §6Protein§0\n" +
                "• §eFat§0\n" +
                "• §aVitamins§0\n" +
                "• §7Minerals§0\n\n" +
                "A full store (270+) gives a bonus; an empty one (30 or less) a penalty."
        )));

        // Page 5: Spoilage & Food Safety
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lFOOD SPOILAGE§r\n\n" +
                "Meat, fish, bread, berries, stews and fresh greens slowly rot, in your pack and in chests.\n\n" +
                "§cHot biomes§0 double the spoilage rate!\n\n" +
                "§2Safe items:§0\n" +
                "Honey, sugar, cookies, cake and cheese wheels never spoil."
        )));

        // Page 6: Age 0 - Starting Out
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lAGE 0 - STRANDED§r\n\n" +
                "Mining stone is gated behind §8Mining 5§0.\n\n" +
                "§6How to start:§0\n" +
                "1. Gather loose §8Rock§0 and §7Flint§0 by hand - they spawn on the surface in forests, hills, mountains and riverbanks.\n" +
                "2. Breaking rocks earns your first Mining XP.\n" +
                "3. You start with §22 Skill Points§0 - press §aK§0 and spend them on a skill's §a+§0 button."
        )));

        // Page 7: Skills & Professions
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lSKILLS & JOBS§r\n\n" +
                "§1Skills:§0 Farming, Mining, Smithing, Strength, Agility, Defense, Health, Stamina, Luck, Archery, Alchemy, Trade.\n\n" +
                "§2Jobs:§0\n" +
                "Join a job with §6/job join <job>§0 (Miner, Farmer, Fisher, Warrior, Smither, Brewer, Builder, Lumberjack)."
        )));

        // Page 8: Downed & Revive
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lDOWNED & REVIVE§r\n\n" +
                "With other players online, a killing blow downs you for 60 seconds instead -\n" +
                "but a second killing blow while downed is fatal.\n\n" +
                "Teammates can channel for 3 seconds to revive you back to 3 hearts.\n\n" +
                "Stick together, build sturdy shelter, and master the Ages!\n\n" +
                "§8Type §6/guide§8 anytime to get a new copy of this book.§r"
        )));

        WrittenBookContent content = new WrittenBookContent(
                Filterable.passThrough("Hearthwind Survival Guide"),
                "Hearthwind",
                0,
                pages,
                true
        );

        book.set(DataComponents.WRITTEN_BOOK_CONTENT, content);
        return book;
    }
}
