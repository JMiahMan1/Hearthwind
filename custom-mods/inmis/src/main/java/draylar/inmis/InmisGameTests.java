package draylar.inmis;

import draylar.inmis.config.BackpackInfo;
import draylar.inmis.config.InmisConfig;
import draylar.inmis.compat.TrinketsCompat;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.component.BackpackComponent;
import draylar.inmis.menu.BackpackMenu;
import draylar.inmis.network.InmisNetworking;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

public final class InmisGameTests {

    public InmisGameTests() {
    }

    private static BackpackItem frayed() {
        return Inmis.BACKPACKS.stream()
                .filter(item -> item.getTier().getName().equals("frayed"))
                .findFirst()
                .orElseThrow();
    }

    /**
     * 26.2 silently drops recipes that reference removed ids or use stale
     * ingredient shapes (only a "Couldn't parse data file" log line), and the
     * 1.20-era exports used the old {@code {"item": id}} shorthand.
     */
    @GameTest
    public void recipesLoad(GameTestHelper helper) {
        String[] ids = {
                "inmis:baby_backpack",
                "inmis:bejeweled_backpack",
                "inmis:blazing_backpack",
                "inmis:ender_pouch",
                "inmis:endless_backpack",
                "inmis:frayed_backpack",
                "inmis:gilded_backpack",
                "inmis:plated_backpack",
                "inmis:withered_backpack",
        };
        for (String id : ids) {
            boolean present = helper.getLevel().recipeAccess()
                    .byKey(net.minecraft.resources.ResourceKey.create(
                            net.minecraft.core.registries.Registries.RECIPE,
                            net.minecraft.resources.Identifier.parse(id)))
                    .isPresent();
            helper.assertTrue(present, "inmis recipe must load in 26.2: " + id);
        }
        helper.succeed();
    }

    @GameTest
    public void backpacksRegisterFromConfig(GameTestHelper helper) {
        helper.assertTrue(Inmis.BACKPACKS.size() == 8,
                "expected 8 backpacks from the default config, got " + Inmis.BACKPACKS.size());
        BackpackItem frayed = frayed();
        helper.assertTrue(frayed.getTier().getRowWidth() == 9 && frayed.getTier().getNumberOfRows() == 1,
                "frayed must be 9x1");
        helper.assertTrue(Inmis.BACKPACKS.stream().anyMatch(item -> item.getTier().isFireImmune()
                        && item.getTier().getName().equals("blazing")),
                "blazing must be fire immune");
        helper.succeed();
    }

    @GameTest
    public void componentRoundTrips(GameTestHelper helper) {
        ItemStack stack = new ItemStack(frayed());
        SimpleContainer container = new SimpleContainer(9);
        container.setItem(3, new ItemStack(Items.DIAMOND, 4));
        stack.set(Inmis.BACKPACK_COMPONENT, new BackpackComponent(container));

        BackpackComponent read = stack.get(Inmis.BACKPACK_COMPONENT);
        helper.assertTrue(read != null, "component must survive a set/get");
        helper.assertTrue(read.getContainer().getItem(3).is(Items.DIAMOND)
                        && read.getContainer().getItem(3).getCount() == 4,
                "stored stack must round trip");
        helper.assertTrue(!Inmis.isBackpackEmpty(stack), "backpack with contents is not empty");
        Inmis.wipeBackpack(stack);
        helper.assertTrue(Inmis.isBackpackEmpty(stack), "wipe must empty the backpack");
        helper.succeed();
    }

    @GameTest
    public void menuMatchesTierGeometry(GameTestHelper helper) {
        Inventory inventory = helper.makeMockServerPlayerInLevel().getInventory();
        BackpackMenu menu = new BackpackMenu(0, inventory, new ItemStack(frayed()));
        int backpackSlots = 9;
        helper.assertTrue(menu.slots.size() == backpackSlots + 36,
                "menu must hold the tier slots plus the player inventory, got " + menu.slots.size());
        helper.assertTrue(menu.getImageWidth() == 178 && menu.getImageHeight() == 134,
                "frayed geometry must be 178x134, got " + menu.getImageWidth() + "x" + menu.getImageHeight());
        helper.succeed();
    }

    @GameTest
    public void backpackSlotsAreLocked(GameTestHelper helper) {
        Inventory inventory = helper.makeMockServerPlayerInLevel().getInventory();
        BackpackMenu menu = new BackpackMenu(0, inventory, new ItemStack(frayed()));
        Slot slot = menu.slots.get(0);

        helper.assertTrue(slot.mayPlace(new ItemStack(Items.DIAMOND)), "plain items must be allowed");
        helper.assertTrue(!slot.mayPlace(new ItemStack(frayed())), "backpacks must not nest");
        helper.assertTrue(!slot.mayPlace(new ItemStack(Blocks.SHULKER_BOX)),
                "shulkers are disabled by the default config");
        helper.succeed();
    }

    @GameTest
    public void openingPathOpensMenu(GameTestHelper helper) {
        // Opening a menu sends a packet, and the vanilla mock player has no
        // connection; attach a minimal one like the survival tests do.
        var player = helper.makeMockServerPlayerInLevel();
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(
                helper.getLevel().getServer(), connection, player,
                net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(), false));

        player.getInventory().setItem(0, new ItemStack(frayed()));
        InmisNetworking.openFirstBackpack(player);
        helper.assertTrue(player.containerMenu instanceof BackpackMenu,
                "carrying a backpack must open its menu when the keybind packet fires, got "
                        + player.containerMenu.getClass().getSimpleName());
        helper.succeed();
    }

    @GameTest
    public void upgradingKeepsContents(GameTestHelper helper) {
        ItemStack oldBackpack = new ItemStack(frayed());
        SimpleContainer container = new SimpleContainer(9);
        container.setItem(0, new ItemStack(Items.EMERALD, 2));
        oldBackpack.set(Inmis.BACKPACK_COMPONENT, new BackpackComponent(container));

        // Emulate the crafting-table result the shaped-recipe mixin patches.
        ItemStack result = new ItemStack(Inmis.BACKPACKS.stream()
                .filter(item -> item.getTier().getName().equals("plated")).findFirst().orElseThrow());
        BackpackComponent moved = oldBackpack.get(Inmis.BACKPACK_COMPONENT);
        SimpleContainer bigger = new SimpleContainer(
                moved.getContainer().getContainerSize() * 2);
        for (int i = 0; i < moved.getContainer().getContainerSize(); i++) {
            bigger.setItem(i, moved.getContainer().getItem(i).copy());
        }
        result.set(Inmis.BACKPACK_COMPONENT, new BackpackComponent(bigger));

        BackpackComponent read = result.get(Inmis.BACKPACK_COMPONENT);
        helper.assertTrue(read != null && read.getContainer().getItem(0).is(Items.EMERALD),
                "upgraded backpack must keep its contents");
        helper.succeed();
    }

    /**
     * The shipped config/inmis.json is Aged's file verbatim, comments and
     * all: it spells the key {@code isFireImmune} (not {@code fireImmune})
     * and carries {@code //} comment lines that strict Gson would reject.
     * Both are easy to break silently, so parse the real bytes here.
     */
    @GameTest
    public void shippedConfigParsesLikeAged(GameTestHelper helper) {
        String json = """
                {
                // Whether Shulker Boxes should be blacklisted
                "requireEmptyForUnequip": false,
                "backpacks": [
                  { "name": "baby", "rowWidth": 3, "numberOfRows": 1, "isFireImmune": false, "openSound": "minecraft:item.armor.equip_leather" },
                  { "name": "blazing", "rowWidth": 9, "numberOfRows": 6, "isFireImmune": true, "openSound": "minecraft:item.armor.equip_leather" }
                ]
                }
                """;
        InmisConfig parsed = InmisConfig.parse(json);
        helper.assertTrue(parsed != null, "a config with comment lines must still parse");
        helper.assertTrue(!parsed.requireEmptyForUnequip,
                "Aged ships requireEmptyForUnequip false");
        BackpackInfo blazing = parsed.backpacks.stream()
                .filter(info -> info.getName().equals("blazing")).findFirst().orElseThrow();
        helper.assertTrue(blazing.isFireImmune(),
                "isFireImmune must bind: the only fireproof tier would lose it otherwise");
        helper.succeed();
    }

    @GameTest
    public void trinketsCompatIsOptIn(GameTestHelper helper) {
        // The addon features are read-side only and must degrade quietly when
        // Trinkets is absent, so the lookup is allowed to return null.
        helper.assertTrue(TrinketsCompat.findBackpack(null) == null,
                "a null attachment must not blow up");
        if (!TrinketsCompat.isEnabled()) {
            helper.assertTrue(TrinketsCompat.findEquippedBackpack(
                            helper.makeMockServerPlayerInLevel()) == null,
                    "disabled compatibility must find nothing");
        }
        helper.succeed();
    }

    /** The addon's 3D backpack models ship with the module, MIT-attributed. */
    @GameTest
    public void backpackModelAssetsShip(GameTestHelper helper) {
        for (String id : Inmis.BACKPACKS.stream().map(item -> item.getTier().getName()).toList()) {
            String texture = "assets/inmis/textures/entity/" + id + "_backpack.png";
            helper.assertTrue(getClass().getClassLoader().getResource(texture) != null,
                    "missing 3D backpack texture: " + texture);
        }
        helper.assertTrue(getClass().getClassLoader().getResource("LICENSE_inmisaddon") != null,
                "the addon model textures are MIT and must ship their licence");
        helper.succeed();
    }
}
