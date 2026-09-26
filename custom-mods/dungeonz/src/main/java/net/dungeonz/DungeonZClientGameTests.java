package net.dungeonz;

import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.List;
import net.dungeonz.init.ItemInit;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngle;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.dungeonz.block.DungeonGateBlock;
import net.dungeonz.dungeon.Dungeon;
import net.dungeonz.block.entity.DungeonGateEntity;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.block.entity.DungeonSpawnerEntity;
import net.dungeonz.block.render.DungeonGateRenderer;
import net.dungeonz.block.render.DungeonPortalRenderer;
import net.dungeonz.block.render.DungeonSpawnerRenderer;
import net.dungeonz.block.screen.DungeonGateOpScreen;
import net.dungeonz.block.screen.DungeonPortalOpScreen;
import net.dungeonz.block.screen.DungeonPortalScreen;
import net.dungeonz.block.screen.DungeonPortalScreenHandler;
import net.dungeonz.init.BlockInit;
import net.dungeonz.item.screen.DungeonCompassScreen;
import net.dungeonz.network.DungeonServerPacket;
import net.dungeonz.network.packet.DungeonPortalPacket;
import net.dungeonz.network.packet.DungeonAdmissionPacket;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.EndPortalRenderState;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class DungeonZClientGameTests implements FabricClientGameTest {
    private static final int TIMEOUT = 20 * 300;
    private static final BlockPos GATE = new BlockPos(0, 200, 0);
    private static final BlockPos SPAWNER = new BlockPos(4, 200, 0);
    private static final BlockPos PORTAL = new BlockPos(8, 200, 0);

    @Override
    public void runTest(ClientGameTestContext context) {
        String capture = System.getProperty("dungeonz.capture", "");
        if (List.of("all-surfaces", "compass", "portal-op", "gate-op", "gate-locked", "gate-unlocked", "spawner", "portal-block").contains(capture)) {
            captureSurfaces(context, capture);
            return;
        }
        if ("portal-background".equals(capture)) {
            capturePortalBackground(context);
            return;
        }
        try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(settings -> settings.setAllowCommands(true)).create()) {
            world.getConnection().waitForChunksRender(TIMEOUT);
            context.setScreen(() -> null);
            world.getServer().runCommand("gamemode creative @p");
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            world.getServer().runOnServer(server -> {
                var level = server.getPlayerList().getPlayers().get(0).level();
                for (int x = -1; x <= 0; x++) {
                    for (int z = -1; z <= 0; z++) {
                        level.getChunk(x, z);
                    }
                }
            });
            world.getServer().runCommand("fill -4 199 -6 12 205 4 air");
            world.getServer().runCommand("fill -4 199 -6 12 199 4 stone_bricks");
            world.getServer().runCommand("tp @p 0.5 200 -4.5 0 12");
            context.waitFor(mc -> mc.player.distanceToSqr(0.5, 200, -4.5) < 1, TIMEOUT);
            world.getConnection().waitForChunksRender(TIMEOUT);
            world.getServer().runOnServer(server -> {
                var player = server.getPlayerList().getPlayers().get(0);
                require(player.canUseGameMasterBlocks(), "fixture requires creative operator permissions");
                var level = player.level();
                level.setBlock(GATE, BlockInit.DUNGEON_GATE.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(SPAWNER, BlockInit.DUNGEON_SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(PORTAL, BlockInit.DUNGEON_PORTAL.defaultBlockState(), Block.UPDATE_ALL);
                var spawner = (DungeonSpawnerEntity) level.getBlockEntity(SPAWNER);
                spawner.setEntityId(EntityTypes.ZOMBIE, level.getRandom());
                spawner.setChanged();
                level.sendBlockUpdated(SPAWNER, spawner.getBlockState(), spawner.getBlockState(), Block.UPDATE_CLIENTS);
                var portal = (DungeonPortalEntity) level.getBlockEntity(PORTAL);
                portal.setDungeonType("dark_dungeon");
                portal.setDifficulty("normal");
                portal.setMaxGroupSize(4);
                portal.setMinGroupSize(1);
                portal.setChanged();
            });
            context.waitFor(mc -> mc.level.getBlockEntity(GATE) instanceof DungeonGateEntity
                    && mc.level.getBlockEntity(SPAWNER) instanceof DungeonSpawnerEntity
                    && mc.level.getBlockEntity(PORTAL) instanceof DungeonPortalEntity, TIMEOUT);
            checkResources(context);
            setGameMode(context, world, GameType.SURVIVAL);
            testCompass(context, world);
            setGameMode(context, world, GameType.CREATIVE);
            testGateEditor(context, world);
            testPortalEditor(context, world);
            setGameMode(context, world, GameType.SURVIVAL);
            testPortal(context, world);
            setGameMode(context, world, GameType.CREATIVE);
            testRenderers(context, world);
            context.setScreen(() -> null);
        }
    }

    private static void capturePortalBackground(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(settings -> settings.setAllowCommands(true)).create()) {
            world.getConnection().waitForChunksRender(TIMEOUT);
            context.setScreen(() -> null);
            context.runOnClient(mc -> {
                mc.options.guiScale().set(2);
                mc.options.fov().set(70);
                mc.resizeGui();
            });
            for (String command : List.of("gamemode creative @p", "time set noon", "weather clear",
                    "fill -16 199 -16 16 210 16 air", "fill -16 199 -16 16 199 16 stone_bricks",
                    "fill -16 200 8 16 210 8 white_concrete", "tp @p 0.5 200 -4.5 0 12")) {
                world.getServer().runCommand(command);
            }
            context.waitFor(mc -> mc.player.distanceToSqr(0.5, 200, -4.5) < 0.1, TIMEOUT);
            world.getConnection().waitForChunksRender(TIMEOUT);
            context.waitTicks(100);
            snapshot(context, "scene");
            world.getServer().runOnServer(server -> server.getPlayerList().getPlayers().get(0).level()
                    .setBlock(PORTAL, BlockInit.DUNGEON_PORTAL.defaultBlockState(), Block.UPDATE_ALL));
            context.waitFor(mc -> mc.level.getBlockEntity(PORTAL) instanceof DungeonPortalEntity, TIMEOUT);
            openPortal(context, world, List.of(), List.of(), 0, false, false);
            context.waitTicks(100);
            snapshot(context, "portal");
            context.setScreen(() -> null);
        }
    }

    private static void captureSurfaces(ClientGameTestContext context, String mode) {
        try (TestSingleplayerContext world = context.worldBuilder().adjustSettings(settings -> settings.setAllowCommands(true)).create()) {
            world.getConnection().waitForChunksRender(TIMEOUT);
            context.setScreen(() -> null);
            context.runOnClient(mc -> {
                mc.options.guiScale().set(2);
                mc.options.fov().set(70);
                mc.options.bobView().set(false);
                mc.options.gamma().set(0.5);
                mc.options.menuBackgroundBlurriness().set(0);
                mc.resizeGui();
            });
            for (String command : List.of("gamemode creative @p", "time set noon", "weather clear",
                    "fill -16 199 -16 16 210 16 air", "fill -16 199 -16 16 199 16 stone_bricks",
                    "fill -16 200 8 16 210 8 white_concrete", "tp @p 0.5 200 -3.5 0 18")) {
                world.getServer().runCommand(command);
            }
            context.waitFor(mc -> mc.player.distanceToSqr(0.5, 200, -3.5) < 0.1, TIMEOUT);
            world.getConnection().waitForChunksRender(TIMEOUT);
            context.waitTicks(100);
            snapshot(context, "scene");
            for (String surface : List.of("compass", "portal-op", "gate-op", "gate-locked", "gate-unlocked", "spawner", "portal-block")) {
                if (!mode.equals("all-surfaces") && !mode.equals(surface)) {
                    continue;
                }
                context.setScreen(() -> null);
                context.runOnClient(mc -> {
                    mc.level.setBlock(GATE, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    if (surface.startsWith("gate")) {
                        mc.level.setBlock(GATE, BlockInit.DUNGEON_GATE.defaultBlockState()
                                .setValue(DungeonGateBlock.ENABLED, !surface.equals("gate-unlocked")), Block.UPDATE_ALL);
                    } else if (surface.startsWith("portal")) {
                        mc.level.setBlock(GATE, BlockInit.DUNGEON_PORTAL.defaultBlockState(), Block.UPDATE_ALL);
                        var portal = (DungeonPortalEntity) mc.level.getBlockEntity(GATE);
                        portal.setDungeonType("dark_dungeon");
                        portal.setDifficulty("normal");
                    } else if (surface.equals("spawner")) {
                        mc.level.setBlock(GATE, BlockInit.DUNGEON_SPAWNER.defaultBlockState(), Block.UPDATE_ALL);
                        ((DungeonSpawnerEntity) mc.level.getBlockEntity(GATE)).setEntityId(EntityTypes.ZOMBIE, mc.level.getRandom());
                    }
                });
                context.waitTicks(60);
                switch (surface) {
                    case "compass" -> context.setScreen(() -> new DungeonCompassScreen("dark_dungeon", List.of("dark_dungeon", "temple_dungeon")));
                    case "portal-op" -> context.setScreen(() -> new DungeonPortalOpScreen(GATE));
                    case "gate-op" -> context.setScreen(() -> new DungeonGateOpScreen(GATE));
                    default -> { }
                }
                context.waitTicks(100);
                snapshot(context, surface);
            }
            context.setScreen(() -> null);
        }
    }

    private static void setGameMode(ClientGameTestContext context, TestSingleplayerContext world, GameType mode) {
        world.getServer().runCommand("gamemode " + mode.getName() + " @p");
        world.getServer().waitFor(server -> server.getPlayerList().getPlayers().get(0).gameMode.getGameModeForPlayer() == mode, TIMEOUT);
        context.waitFor(mc -> mc.gameMode.getPlayerMode() == mode && mc.player.isCreative() == (mode == GameType.CREATIVE), TIMEOUT);
    }

    private static void testCompass(ClientGameTestContext context, TestSingleplayerContext world) {
        context.runOnClient(mc -> {
            try (var reader = mc.getResourceManager().getResource(Identifier.parse("dungeonz:items/dungeon_compass.json")).orElseThrow().openAsReader()) {
                var model = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("model");
                var unbound = model.getAsJsonObject("on_false");
                require(unbound.get("target").getAsString().equals("none"), "unbound needle must spin rather than point at spawn");
                require(model.getAsJsonObject("on_true").get("target").getAsString().equals("lodestone"), "bound needle must use dungeon tracker");
                var angle = CompassAngle.MAP_CODEC.codec().parse(JsonOps.INSTANCE, unbound).getOrThrow();
                var compass = new ItemStack(ItemInit.DUNGEON_COMPASS);
                require(CompassAngleState.CompassTarget.NONE.get(mc.level, compass, mc.player) == null, "unbound target must be null");
                require(angle.get(compass, mc.level, mc.player, 0) != angle.get(compass, mc.level, mc.player, 1),
                        "null target must use vanilla seed-offset spinning rotation");
                compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(mc.level.dimension(), GATE)), true));
                require(CompassAngleState.CompassTarget.LODESTONE.get(mc.level, compass, mc.player).equals(GlobalPos.of(mc.level.dimension(), GATE)),
                        "bound target must resolve synced coordinates");
            } catch (IOException exception) {
                throw new AssertionError(exception);
            }
        });
        for (int count : List.of(0, 2, 3)) {
            setInventory(context, world, new ItemStack(Items.AMETHYST_SHARD, count));
            world.getServer().runOnServer(server -> DungeonServerPacket.writeS2COpenCompassScreenPacket(
                    server.getPlayerList().getPlayers().get(0), "dark_dungeon"));
            context.waitForScreen(DungeonCompassScreen.class);
            context.waitTicks(5);
            var loaded = world.getServer().computeOnServer(server -> DungeonzMain.DUNGEONS.stream()
                    .map(Dungeon::getDungeonTypeId).toList());
            require(loaded.containsAll(List.of("dark_dungeon", "temple_dungeon")),
                    "two shipped dungeons must load: " + loaded);
            context.runOnClient(mc -> {
                var screen = mc.gui.screen();
                var buttons = widgets(screen, Button.class);
                require(buttons.size() == 8, "compass must retain seven rows and calibrate button");
                require(buttons.stream().filter(button -> button.visible).count() == loaded.size() + 1,
                        "one visible row per loaded dungeon plus calibrate, loaded=" + loaded);
                require(!buttons.get(7).active, "calibrate must start disabled");
                require(buttons.subList(0, 7).stream().filter(button -> button.visible && !button.active).count() == 1,
                        "current dungeon row must be disabled");
                var other = buttons.subList(0, 7).stream().filter(button -> button.visible && button.active).findFirst().orElseThrow();
                click(screen, other);
                require(buttons.get(7).active == (count == 3), "calibration must require three amethyst shards: " + count);
                require(other.getWidth() == 89 && other.getHeight() == 20, "compass row geometry changed");
            });
            // Regression: a pointer over an empty (hidden) row must not be
            // indexed as a dungeon id while the tooltip renders.
            context.runOnClient(mc -> {
                var screen = mc.gui.screen();
                var buttons = widgets(screen, Button.class);
                for (int i = 0; i < 7; i++) {
                    Button row = buttons.get(i);
                    if (!row.visible) {
                        screen.mouseMoved(row.getX() + row.getWidth() / 2.0, row.getY() + row.getHeight() / 2.0);
                        break;
                    }
                }
            });
            context.waitTicks(3);
            context.runOnClient(mc -> require(mc.gui.screen() instanceof DungeonCompassScreen,
                    "compass must survive a pointer over an empty row"));
            if (count == 3) {
                snapshot(context, "dungeonz_compass");
            }
            context.setScreen(() -> null);
        }
        setInventory(context, world, ItemStack.EMPTY);
    }

    private static void setInventory(ClientGameTestContext context, TestSingleplayerContext world, ItemStack stack) {
        world.getServer().runOnServer(server -> {
            var player = server.getPlayerList().getPlayers().get(0);
            player.getInventory().clearContent();
            player.getInventory().setItem(9, stack.copy());
            player.inventoryMenu.broadcastChanges();
        });
        context.waitFor(mc -> ItemStack.matches(mc.player.getInventory().getItem(9), stack)
                && mc.player.getInventory().countItem(Items.AMETHYST_SHARD) == (stack.is(Items.AMETHYST_SHARD) ? stack.getCount() : 0)
                && mc.player.getInventory().countItem(Items.DIAMOND) == (stack.is(Items.DIAMOND) ? stack.getCount() : 0), TIMEOUT);
    }

    private static void testGateEditor(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runOnServer(server -> {
            var player = server.getPlayerList().getPlayers().get(0);
            DungeonServerPacket.writeS2COpenOpScreenPacket(player, null, (DungeonGateEntity) player.level().getBlockEntity(GATE));
        });
        context.waitForScreen(DungeonGateOpScreen.class);
        context.runOnClient(mc -> {
            var screen = mc.gui.screen();
            var fields = widgets(screen, EditBox.class);
            require(fields.size() == 3, "gate editor must expose block, particle and unlock item");
            require(fields.get(0).getValue().equals("minecraft:chiseled_stone_bricks"), "gate disguise default");
            var done = widgets(screen, Button.class).get(0);
            for (String invalid : List.of("", "minecraft:", "Bad ID", "minecraft:air", "dungeonz:missing_block")) {
                fields.get(0).setValue(invalid);
                require(!done.active, "invalid gate disguise must disable Done: " + invalid);
            }
            fields.get(0).setValue("minecraft:gold_block");
            fields.get(1).setValue("minecraft:flame");
            fields.get(2).setValue("minecraft:diamond");
            require(done.active, "valid gate disguise must enable Done");
            screen.resize(screen.width, screen.height);
            require(widgets(screen, EditBox.class).stream().map(EditBox::getValue).toList()
                    .equals(List.of("minecraft:gold_block", "minecraft:flame", "minecraft:diamond")), "gate edits must survive resize");
        });
        snapshot(context, "dungeonz_gate_op");
        context.clickScreenButton("gui.done");
        context.waitFor(mc -> mc.gui.screen() == null, TIMEOUT);
        world.getServer().waitFor(server -> {
            var gate = (DungeonGateEntity) server.getPlayerList().getPlayers().get(0).level().getBlockEntity(GATE);
            return gate.getDisguiseBlockState().is(Blocks.GOLD_BLOCK) && gate.getUnlockItem() == Items.DIAMOND;
        }, TIMEOUT);
        context.waitFor(mc -> ((DungeonGateEntity) mc.level.getBlockEntity(GATE)).getDisguiseBlockState().is(Blocks.GOLD_BLOCK), TIMEOUT);
    }

    private static void testPortalEditor(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runOnServer(server -> {
            var player = server.getPlayerList().getPlayers().get(0);
            DungeonServerPacket.writeS2COpenOpScreenPacket(player, (DungeonPortalEntity) player.level().getBlockEntity(PORTAL), null);
        });
        context.waitForScreen(DungeonPortalOpScreen.class);
        context.runOnClient(mc -> {
            var screen = mc.gui.screen();
            var fields = widgets(screen, EditBox.class);
            require(fields.stream().map(EditBox::getValue).toList().equals(List.of("dark_dungeon", "normal")), "portal editor must load server values");
            var done = widgets(screen, Button.class).get(0);
            fields.get(0).setValue("");
            require(!done.active, "empty dungeon type must disable Done");
            fields.get(0).setValue("temple_dungeon");
            fields.get(1).setValue("");
            require(!done.active, "empty difficulty must disable Done");
            fields.get(1).setValue("hard");
            require(done.active, "complete portal settings must enable Done");
            screen.resize(screen.width, screen.height);
            require(widgets(screen, EditBox.class).stream().map(EditBox::getValue).toList().equals(List.of("temple_dungeon", "hard")),
                    "portal edits must survive resize");
        });
        snapshot(context, "dungeonz_portal_op");
        context.clickScreenButton("gui.done");
        context.waitFor(mc -> mc.gui.screen() == null, TIMEOUT);
        world.getServer().waitFor(server -> {
            var portal = (DungeonPortalEntity) server.getPlayerList().getPlayers().get(0).level().getBlockEntity(PORTAL);
            return portal.getDungeonType().equals("temple_dungeon") && portal.getDifficulty().equals("hard");
        }, TIMEOUT);
    }

    private static void testPortal(ClientGameTestContext context, TestSingleplayerContext world) {
        openPortal(context, world, List.of(), List.of(), 0, false, false);
        assertPortal(context, false, true, true);
        snapshot(context, "dungeonz_portal_missing_items");
        openPortal(context, world, List.of(), List.of(), 0, false, true);
        assertPortal(context, true, true, true);
        snapshot(context, "dungeonz_portal_ready");
        context.runOnClient(mc -> click(mc.gui.screen(), widgets(mc.gui.screen(), DungeonPortalScreen.DungeonDifficultyButton.class).get(0)));
        world.getServer().waitFor(server -> ((DungeonPortalEntity) server.getPlayerList().getPlayers().get(0).level().getBlockEntity(PORTAL))
                .getDifficulty().equals("extreme"), TIMEOUT);
        context.waitFor(mc -> mc.player.containerMenu instanceof DungeonPortalScreenHandler handler
                && handler.getDungeonPortalEntity().getDifficulty().equals("extreme"), TIMEOUT);
        context.runOnClient(mc -> {
            var slider = widgets(mc.gui.screen(), DungeonPortalScreen.DungeonSliderButton.class).get(0);
            click(mc.gui.screen(), slider);
            require(slider.isEnabled(), "private slider must toggle on click");
        });
        world.getServer().waitFor(server -> ((DungeonPortalEntity) server.getPlayerList().getPlayers().get(0).level().getBlockEntity(PORTAL)).getPrivateGroup(), TIMEOUT);
        UUID playerId = context.computeOnClient(mc -> mc.player.getUUID());
        openPortal(context, world, List.of(), List.of(playerId), 0, false, true);
        assertPortal(context, false, true, true);
        openPortal(context, world, List.of(playerId, new UUID(0, 1), new UUID(0, 2), new UUID(0, 3)), List.of(), 0, false, true);
        assertPortal(context, false, false, false);
        snapshot(context, "dungeonz_portal_full");
        openPortal(context, world, List.of(new UUID(0, 1)), List.of(), 0, true, true);
        assertPortal(context, false, false, false);
        int cooldown = context.computeOnClient(mc -> (int) mc.level.getGameTime() + 12000);
        openPortal(context, world, List.of(), List.of(), cooldown, false, true);
        assertPortal(context, false, true, true);
        snapshot(context, "dungeonz_portal_cooldown");
        context.setScreen(() -> null);
        context.runOnClient(mc -> mc.player.containerMenu = mc.player.inventoryMenu);
    }

    private static void openPortal(ClientGameTestContext context, TestSingleplayerContext world, List<UUID> players, List<UUID> dead, int cooldown, boolean privateGroup, boolean paid) {
        context.setScreen(() -> null);
        context.runOnClient(mc -> mc.player.containerMenu = mc.player.inventoryMenu);
        setInventory(context, world, paid ? new ItemStack(Items.DIAMOND, 2) : ItemStack.EMPTY);
        context.setScreen(() -> {
            var mc = Minecraft.getInstance();
            var required = Map.of("easy", List.of(new ItemStack(Items.DIAMOND, 2)), "normal", List.of(new ItemStack(Items.DIAMOND, 2)),
                    "hard", List.of(new ItemStack(Items.DIAMOND, 2)), "extreme", List.of(new ItemStack(Items.DIAMOND, 2)));
            var packet = new DungeonPortalPacket("temple_dungeon", PORTAL, players, dead, List.of("easy", "normal", "hard", "extreme"),
                    Map.of("hard", List.of(new ItemStack(Items.EMERALD), new ItemStack(Items.DIAMOND_SWORD))), required,
                    4, 1, 0, 0, cooldown, "hard", false, false, false, false, false, privateGroup, Optional.empty(),
                    DungeonAdmissionPacket.empty());
            var handler = new DungeonPortalScreenHandler(0, mc.player.getInventory(), packet);
            mc.player.containerMenu = handler;
            return new DungeonPortalScreen(handler, mc.player.getInventory(), Component.translatable("dungeon.temple_dungeon"));
        });
        context.waitForScreen(DungeonPortalScreen.class);
    }

    private static void assertPortal(ClientGameTestContext context, boolean join, boolean difficulty, boolean privacy) {
        context.runOnClient(mc -> {
            var screen = mc.gui.screen();
            require(widgets(screen, Button.class).size() == 3, "portal must retain Join, difficulty and private controls");
            var joinButton = widgets(screen, DungeonPortalScreen.DungeonButton.class).get(0);
            require(joinButton.active == join, "portal Join eligibility mismatch");
            require(joinButton.getMessage().getString().equals(Component.translatable("dungeon.task.join").getString()), "overworld action must be Join");
            require(widgets(screen, DungeonPortalScreen.DungeonDifficultyButton.class).get(0).active == difficulty, "difficulty lock mismatch");
            require(widgets(screen, DungeonPortalScreen.DungeonSliderButton.class).get(0).active == privacy, "privacy lock mismatch");
        });
    }

    private static void testRenderers(ClientGameTestContext context, TestSingleplayerContext world) {
        context.runOnClient(mc -> {
            var dispatcher = mc.getBlockEntityRenderDispatcher();
            var gate = (DungeonGateEntity) mc.level.getBlockEntity(GATE);
            BlockEntityRenderer<DungeonGateEntity, DungeonGateRenderer.GateRenderState> gateRenderer = dispatcher.getRenderer(gate);
            require(gateRenderer instanceof DungeonGateRenderer, "custom gate renderer must be registered");
            var gateState = gateRenderer.createRenderState();
            gateRenderer.extractRenderState(gate, gateState, 0, mc.player.getEyePosition(), null);
            require(gateState.enabled && !gateState.model.isEmpty(), "enabled disguised gate must extract a model");
            try {
                var variants = new java.util.HashSet<List<?>>();
                var disguise = Blocks.STONE.defaultBlockState();
                for (int x = 0; x < 32; x++) {
                    var pos = GATE.offset(x, 0, 0);
                    var sample = new DungeonGateEntity(pos, BlockInit.DUNGEON_GATE.defaultBlockState());
                    sample.setLevel(mc.level);
                    sample.setBlockId(Identifier.parse("minecraft:stone"));
                    gateRenderer.extractRenderState(sample, gateState, 0, mc.player.getEyePosition(), null);
                    var expected = new BlockModelRenderState();
                    mc.getModelManager().getBlockModelSet().get(disguise).update(expected, disguise, BlockDisplayContext.create(), disguise.getSeed(pos));
                    var actualParts = modelGeometry(gateState.model);
                    require(!actualParts.isEmpty(), "gate disguise must extract geometry");
                    require(actualParts.equals(modelGeometry(expected)), "gate disguise must use its position seed");
                    variants.add(actualParts);
                }
                require(variants.size() > 1, "stone disguises must not all use fixed seed 42");
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
            var spawner = (DungeonSpawnerEntity) mc.level.getBlockEntity(SPAWNER);
            require(spawner.getLogic().getRenderedEntity(mc.level) != null
                    && spawner.getLogic().getRenderedEntity(mc.level).getType() == EntityTypes.ZOMBIE, "spawner display entity must sync as zombie");
            BlockEntityRenderer<DungeonSpawnerEntity, SpawnerRenderState> spawnerRenderer = dispatcher.getRenderer(spawner);
            require(spawnerRenderer instanceof DungeonSpawnerRenderer, "custom spawner renderer must be registered");
            var spawnerState = spawnerRenderer.createRenderState();
            spawnerRenderer.extractRenderState(spawner, spawnerState, 0, mc.player.getEyePosition(), null);
            require(spawnerState.displayEntity != null && spawnerState.scale > 0 && spawnerState.scale <= 0.53125F,
                    "spawner must extract scaled display entity");
            var portal = (DungeonPortalEntity) mc.level.getBlockEntity(PORTAL);
            BlockEntityRenderer<DungeonPortalEntity, EndPortalRenderState> portalRenderer = dispatcher.getRenderer(portal);
            require(portalRenderer instanceof DungeonPortalRenderer, "custom portal renderer must be registered");
            var portalState = portalRenderer.createRenderState();
            portalRenderer.extractRenderState(portal, portalState, 0, mc.player.getEyePosition(), null);
            require(portalState.facesToShow.contains(Direction.NORTH) && portalState.facesToShow.contains(Direction.UP), "isolated portal must render front and top faces");
        });
        blockSnapshot(context, world, GATE, "dungeonz_gate_locked");
        world.getServer().runOnServer(server -> {
            var level = server.getPlayerList().getPlayers().get(0).level();
            level.setBlock(GATE, level.getBlockState(GATE).setValue(DungeonGateBlock.ENABLED, false), Block.UPDATE_ALL);
        });
        context.waitFor(mc -> !mc.level.getBlockState(GATE).getValue(DungeonGateBlock.ENABLED), TIMEOUT);
        context.runOnClient(mc -> {
            var gate = (DungeonGateEntity) mc.level.getBlockEntity(GATE);
            BlockEntityRenderer<DungeonGateEntity, DungeonGateRenderer.GateRenderState> renderer = mc.getBlockEntityRenderDispatcher().getRenderer(gate);
            var state = renderer.createRenderState();
            renderer.extractRenderState(gate, state, 0, mc.player.getEyePosition(), null);
            require(!state.enabled && state.model.isEmpty(), "unlocked gate must not extract a visible model");
            require(gate.getDisguiseBlockState().is(Blocks.GOLD_BLOCK), "unlock must retain the configured disguise");
        });
        blockSnapshot(context, world, GATE, "dungeonz_gate_unlocked");
        blockSnapshot(context, world, SPAWNER, "dungeonz_spawner");
        blockSnapshot(context, world, PORTAL, "dungeonz_portal_block");
    }

    private static List<?> modelGeometry(BlockModelRenderState state) throws ReflectiveOperationException {
        var partsField = BlockModelRenderState.class.getDeclaredField("modelParts");
        partsField.setAccessible(true);
        var parts = (List<?>) partsField.get(state);
        if (parts != null && !parts.isEmpty()) {
            return List.copyOf(parts);
        }
        var meshType = Class.forName("net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView");
        var quadType = Class.forName("net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView");
        var meshField = java.util.Arrays.stream(BlockModelRenderState.class.getDeclaredFields())
                .filter(field -> meshType.isAssignableFrom(field.getType())).findFirst().orElseThrow();
        meshField.setAccessible(true);
        var mesh = meshField.get(state);
        require(mesh != null, "gate disguise must contain vanilla parts or a Fabric mesh");
        var geometry = new java.util.ArrayList<Object>();
        var coordinates = List.of(quadType.getMethod("x", int.class), quadType.getMethod("y", int.class),
                quadType.getMethod("z", int.class), quadType.getMethod("u", int.class), quadType.getMethod("v", int.class));
        meshType.getMethod("forEach", java.util.function.Consumer.class).invoke(mesh, (java.util.function.Consumer<Object>) quad -> {
            try {
                for (int vertex = 0; vertex < 4; vertex++) {
                    for (var coordinate : coordinates) {
                        geometry.add(coordinate.invoke(quad, vertex));
                    }
                }
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(exception);
            }
        });
        return List.copyOf(geometry);
    }

    private static void blockSnapshot(ClientGameTestContext context, TestSingleplayerContext world, BlockPos pos, String name) {
        world.getServer().runCommand("tp @p " + (pos.getX() + 0.5) + " 200 -3.5 0 18");
        context.waitFor(mc -> mc.player.distanceToSqr(pos.getX() + 0.5, 200, -3.5) < 0.1, TIMEOUT);
        world.getConnection().waitForChunksRender(TIMEOUT);
        context.getInput().lookAt(pos);
        snapshot(context, name);
    }

    private static void checkResources(ClientGameTestContext context) {
        context.runOnClient(mc -> {
            for (String path : List.of("textures/gui/dungeon_compass.png", "textures/gui/dungeon_portal.png", "textures/gui/dungeon_icons.png",
                    "blockstates/dungeon_gate.json", "blockstates/dungeon_spawner.json", "blockstates/dungeon_portal.json")) {
                require(mc.getResourceManager().getResource(Identifier.fromNamespaceAndPath("dungeonz", path)).isPresent(), "missing DungeonZ resource: " + path);
            }
            for (String key : List.of("dungeon.dark_dungeon", "dungeon.temple_dungeon", "compass.compass_screen.calibrate", "dungeon.task.join")) {
                require(!Component.translatable(key).getString().equals(key), "missing DungeonZ translation: " + key);
            }
        });
    }

    private static void snapshot(ClientGameTestContext context, String name) {
        boolean hudHidden = context.computeOnClient(mc -> mc.gui.hud.isHidden());
        try {
            context.runOnClient(mc -> {
                if (!mc.gui.hud.isHidden()) {
                    mc.gui.hud.toggle();
                }
                mc.gui.hud.getChat().clearMessages(true);
                mc.gui.toastManager().clear();
            });
            context.waitTicks(10);
            context.takeScreenshot(name);
        } finally {
            context.runOnClient(mc -> {
                if (mc.gui.hud.isHidden() != hudHidden) {
                    mc.gui.hud.toggle();
                }
            });
        }
    }

    private static <T> List<T> widgets(Screen screen, Class<T> type) {
        return screen.children().stream().filter(type::isInstance).map(type::cast).toList();
    }

    private static void click(Screen screen, Button button) {
        require(button.active && button.visible, "cannot click inactive or hidden widget");
        var event = new MouseButtonEvent(button.getX() + button.getWidth() / 2.0, button.getY() + button.getHeight() / 2.0, new MouseButtonInfo(0, 0));
        require(screen.mouseClicked(event, false), "screen must handle widget click");
        screen.mouseReleased(event);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
