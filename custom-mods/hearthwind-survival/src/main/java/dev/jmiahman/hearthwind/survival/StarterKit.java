package dev.jmiahman.hearthwind.survival;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
import net.minecraft.world.item.component.WrittenBookContent;

/**
 * Starter kit & Guidebook system for Hearthwind (Aged 3.1.2 Parity).
 * Grants players a Survival Guidebook, a Glass Bottle, and a Campfire on first join.
 */
public final class StarterKit {
    public static final String STARTER_TAG = "hearthwind:starter_kit_granted";

    private StarterKit() {}

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (!player.entityTags().contains(STARTER_TAG)) {
                grantStarterKit(player);
                player.addTag(STARTER_TAG);
            }
        });

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

    public static void grantStarterKit(ServerPlayer player) {
        giveOrDrop(player, createGuidebook());
        giveOrDrop(player, new ItemStack(Items.GLASS_BOTTLE));
        giveOrDrop(player, new ItemStack(Items.CAMPFIRE));
        player.sendSystemMessage(Component.literal("§6§l[Hearthwind]§r §eWelcome! You have been granted the Hearthwind Guide Book, a Glass Bottle, and a Campfire."));
    }

    private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) return;
        boolean added = player.getInventory().add(stack);
        if (!added || !stack.isEmpty()) {
            player.drop(stack, false);
        }
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
                "• Survival Guidebook\n" +
                "• Glass Bottle\n" +
                "• Campfire"
        )));

        // Page 2: Thirst & Hydration
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lTHIRST & HYDRATION§r\n\n" +
                "10 blue droplets appear directly above your hunger bar.\n\n" +
                "§1Drinking:§r\n" +
                "• Use your §9Glass Bottle§0 or craft a §9Leather Flask§0.\n" +
                "• Crouch + right-click water with an empty hand for a quick sip.\n" +
                "• Many foods & drinks (apples, melons, stews, milk, teas) also restore thirst."
        )));

        // Page 3: Body Temperature
        pages.add(Filterable.passThrough(Component.literal(
                "§0§lBODY TEMPERATURE§r\n\n" +
                "A body icon and thermometer beside your hotbar track core temperature (from -2400 to +2400).\n\n" +
                "§9Freezing (-1800):§r Cold biomes, night, rain, altitude; iced armor and ice packs.\n\n" +
                "§6Overheating (+1800):§r Deserts, nether, lava, magma; -30% attack damage.\n\n" +
                "§2Warmth:§r Lit campfires, lava and furnaces heat you within 3 blocks in sight; leather and wolf-fur armor give +3 a piece.\n\n" +
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
                "1. Gather loose §8Surface Rocks§0 and §7Flint§0 by hand on riverbanks and terrain.\n" +
                "2. Breaking rocks earns your first Mining XP.\n" +
                "3. You start with §22 Skill Points§0 — spend them right away in your Skills menu!"
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
                "With other players online, a killing blow downs you for 60 seconds instead.\n\n" +
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
