package net.dungeonz.block.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonClientPacket;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.WidgetSprites;

@Environment(EnvType.CLIENT)
public class DungeonPortalScreen extends AbstractContainerScreen<DungeonPortalScreenHandler> implements ContainerListener {

    private static final Identifier ICONS = Identifier.parse("dungeonz:textures/gui/dungeon_icons.png");
    private static final Component JOIN = Component.translatable("dungeon.task.join");
    private static final Component LEAVE = Component.translatable("dungeon.task.leave");
    private static final ItemStack INFO_ITEMSTACK = new ItemStack(Items.CREEPER_BANNER_PATTERN);

    private final Identifier texture;
    public DungeonDifficultyButton difficultyButton;
    private DungeonButton dungeonButton;
    private DungeonSliderButton privateButton;
    private final Player playerEntity;
    private boolean joinRequested;

    public DungeonPortalScreen(DungeonPortalScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, 256, 222);
        this.playerEntity = inventory.player;
        texture = handler.getBackgroundId() != null ? handler.getBackgroundId() : Identifier.parse("dungeonz:textures/gui/dungeon_portal.png");
    }

    @Override
    protected void init() {
        super.init();

        this.menu.addSlotListener(this);

        final boolean playerIsInDungeonWorld = playerEntity.level().dimension() == DimensionInit.DUNGEON_WORLD;
        Component buttonText = playerIsInDungeonWorld ? LEAVE : JOIN;

        this.dungeonButton = this.addRenderableWidget(new DungeonButton(this.leftPos + this.imageWidth / 2 - 26, this.topPos + this.imageHeight - 28, buttonText, (button) -> {
            if (button.active) {
                DungeonClientPacket.writeC2SDungeonTeleportPacket(this.minecraft, this.menu.getPos(), this.playerEntity.getUUID());
                this.joinRequested = true;
                this.menu.setWaitingGroupSize(this.menu.getWaitingGroupSize() + 1);
                button.active = false;
            }
        }));
        this.difficultyButton = this.addRenderableWidget(new DungeonDifficultyButton(this.leftPos + 144, this.topPos + 36, Component.nullToEmpty(""), (button) -> {
            if (button.active) {
                DungeonClientPacket.writeC2SChangeDifficultyPacket(this.minecraft, this.menu.getPos());
            }
        }));
        this.privateButton = this.addRenderableWidget(new DungeonSliderButton(this.leftPos + 144, this.topPos + 63, (button) -> {
            if (button.active) {
                ((DungeonSliderButton) button).cycleEnabled();
                DungeonClientPacket.writeC2SChangePrivateGroupPacket(minecraft, this.menu.getPos(), ((DungeonSliderButton) button).isEnabled());
            }
        }));

        this.privateButton.enabled = this.menu.getDungeonPortalEntity().getPrivateGroup();
        if (playerIsInDungeonWorld) {
            this.dungeonButton.active = true;
            this.difficultyButton.active = false;
            this.privateButton.active = false;
        } else {
            if (!this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().isEmpty()) {
                this.difficultyButton.active = false;
                this.privateButton.active = false;
            } else {
                this.difficultyButton.active = true;
                this.privateButton.active = true;
            }
            if ((this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().size() + this.menu.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().size()) < this.menu
                    .getDungeonPortalEntity().getMaxGroupSize() && InventoryHelper.hasRequiredItemStacks(this.playerEntity.getInventory(), this.menu.getRequiredItemStacks().get(this.menu.getDungeonPortalEntity().getDifficulty()))
                    && !this.menu.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().contains(this.playerEntity.getUUID())) {
                this.dungeonButton.active = true;
            } else {
                this.dungeonButton.active = false;
            }
            if (!this.menu.getAdmission().meetsRequiredLevel(this.menu.getRequiredLevel())) {
                this.dungeonButton.active = false;
            }
            if (this.dungeonButton.active && this.privateButton.enabled && !this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().isEmpty()) {
                if (!this.menu.getAdmission().admits(this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().get(0))) {
                    this.dungeonButton.active = false;
                }
            }
        }
        if (this.menu.getDifficulties().contains(this.menu.getDungeonPortalEntity().getDifficulty())) {
            this.difficultyButton.setText(Component.translatable("dungeonz.difficulty." + this.menu.getDungeonPortalEntity().getDifficulty()));
        } else {
            this.difficultyButton.setText(Component.translatable("dungeonz.difficulty." + this.menu.getDifficulties().get(0)));
        }
        if (this.menu.getDungeonPortalEntity().isOnCooldown((int) this.minecraft.level.getGameTime())) {
            this.dungeonButton.active = false;
        }
    }

    private Component getPlayerName(UUID playerId, int length, int substringLength) {
        if (this.minecraft.getConnection().getPlayerInfo(playerId) != null) {
            String playerName = this.minecraft.getConnection().getPlayerInfo(playerId).getProfile().name();
            if (this.minecraft.font.width(playerName) > length && substringLength != 0) {
                playerName = playerName.substring(0, substringLength) + "..";
            }
            return Component.nullToEmpty(playerName);
        }
        return Component.translatable("text.dungeonz.empty_name");
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.joinRequested) {
            return;
        }
        if (this.playerEntity.level().dimension() == DimensionInit.DUNGEON_WORLD) {
            this.dungeonButton.active = true;
            return;
        }
        var portal = this.menu.getDungeonPortalEntity();
        var occupants = portal.getDungeonPlayerUuids();
        this.dungeonButton.active = occupants.size() + portal.getDeadDungeonPlayerUUIDs().size() < portal.getMaxGroupSize()
                && !portal.isOnCooldown((int) this.minecraft.level.getGameTime())
                && !portal.getDeadDungeonPlayerUUIDs().contains(this.playerEntity.getUUID())
                && InventoryHelper.hasRequiredItemStacks(this.playerEntity.getInventory(), this.menu.getRequiredItemStacks().get(portal.getDifficulty()))
                && this.menu.getAdmission().meetsRequiredLevel(this.menu.getRequiredLevel())
                && (!this.privateButton.enabled || occupants.isEmpty() || this.menu.getAdmission().admits(occupants.get(0)));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        // Title
        graphics.text(this.font, this.title, this.leftPos + this.imageWidth / 2 - this.font.width(this.title) / 2, this.topPos + 8, 0xFF404040, false);

        // Dungeon player list
        int k = this.topPos + 37;
        graphics.text(this.font,
                Component.translatable("text.dungeonz.player_list",
                        this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().size() + this.menu.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().size(),
                        this.menu.getDungeonPortalEntity().getMaxGroupSize()),
                this.leftPos + 8, this.topPos + 24, 0xFF3F3F3F, false);
        for (int i = 0; i < this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().size() && i < 13; i++) {
            String playerName = getPlayerName(this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().get(i), 102, 15).getString();
            if (i == 12) {
                playerName = "...";
                if (this.isHovering(13, k, 16, 7, mouseX, mouseY)) {
                    List<Component> otherPlayerNames = new ArrayList<Component>();
                    for (int u = 12; u < this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().size(); u++) {
                        otherPlayerNames.add(getPlayerName(this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().get(u), 102, 15));
                    }
                    graphics.setComponentTooltipForNextFrame(this.font, otherPlayerNames, mouseX, mouseY);
                }
            }
            graphics.text(this.font, playerName, this.leftPos + 13, k, 0xFFFFFFFF, false);
            k += 13;
        }
        // Required items
        graphics.text(this.font, Component.translatable("text.dungeonz.required"), this.leftPos + 139, this.topPos + 81, 0xFF3F3F3F, false);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.leftPos + 142 + this.font.width(Component.translatable("text.dungeonz.required")), this.topPos + 78,
                52 + (InventoryHelper.hasRequiredItemStacks(this.playerEntity.getInventory(), this.menu.getRequiredItemStacks().get(this.menu.getDungeonPortalEntity().getDifficulty())) ? 0 : 14), 0, 14, 14, 256, 256);

        if (!this.menu.getRequiredItemStacks().get(this.menu.getDungeonPortalEntity().getDifficulty()).isEmpty()) {
            int l = 0;

            for (ItemStack stack : this.menu.getRequiredItemStacks().get(this.menu.getDungeonPortalEntity().getDifficulty())) {
                graphics.item(stack, this.leftPos + 144 + l, this.topPos + 93);
                graphics.itemDecorations(this.font, stack, this.leftPos + 144 + l, this.topPos + 93);
                if (this.isHovering(144 + l, 93, 16, 16, mouseX, mouseY)) {
                    graphics.setTooltipForNextFrame(this.font, stack.getHoverName(), mouseX, mouseY);
                }
                l += 18;
            }
        } else {
            graphics.text(this.font, Component.translatable("text.dungeonz.nothing_required"), this.leftPos + 144, this.topPos + 93, 0xFF3F3F3F, false);
        }

        // Possible loot
        graphics.text(this.font, Component.translatable("text.dungeonz.possible"), this.leftPos + 139, this.topPos + 115, 0xFF3F3F3F, false);
        if (this.menu.getPossibleLootDifficultyItemStackMap().size() > 0 && this.menu.getPossibleLootDifficultyItemStackMap().containsKey(this.menu.getDungeonPortalEntity().getDifficulty())
                && this.menu.getPossibleLootDifficultyItemStackMap().get(this.menu.getDungeonPortalEntity().getDifficulty()).size() > 0) {
            int l = 0;
            int o = 0;
            for (int i = 0; i < this.menu.getPossibleLootDifficultyItemStackMap().get(this.menu.getDungeonPortalEntity().getDifficulty()).size() && i < 10; i++) {
                graphics.item(this.menu.getPossibleLootDifficultyItemStackMap().get(this.menu.getDungeonPortalEntity().getDifficulty()).get(i), this.leftPos + 144 + l, this.topPos + o + 127);
                graphics.itemDecorations(this.font, this.menu.getPossibleLootDifficultyItemStackMap().get(this.menu.getDungeonPortalEntity().getDifficulty()).get(i), this.leftPos + 144 + l,
                        this.topPos + o + 127);

                if (this.isHovering(144 + l, o + 127, 16, 16, mouseX, mouseY)) {
                    graphics.setTooltipForNextFrame(this.font, this.menu.getPossibleLootDifficultyItemStackMap().get(this.menu.getDungeonPortalEntity().getDifficulty()).get(i).getHoverName(), mouseX,
                            mouseY);
                }
                l += 18;
                if (i == 4) {
                    l = 0;
                    o = 18;
                }
            }
        }
        graphics.text(this.font, Component.translatable("dungeonz.difficulty"), this.leftPos + 139, this.topPos + 24, 0xFF3F3F3F, false);
        graphics.text(this.font, Component.translatable("text.dungeonz.private"), this.leftPos + 169, this.topPos + 65, 0xFF3F3F3F, false);
        // Min group size
        if (this.menu.getDungeonPortalEntity().getDungeonPlayerCount() <= 0 && this.menu.getDungeonPortalEntity().getMinGroupSize() > 1) {
            graphics.text(this.font, Component.translatable("text.dungeonz.waiting_player_list", this.menu.getWaitingGroupSize(), this.menu.getDungeonPortalEntity().getMinGroupSize()),
                    this.leftPos + 139, this.topPos + 167, 0xFF3F3F3F, false);
        }
        // LevelZ
        if (this.menu.getAdmission().levelsEnabled()) {
            graphics.text(this.font, Component.translatable("text.dungeonz.required_level", this.menu.getRequiredLevel()), this.leftPos + 139, this.topPos + 180, 0xFF3F3F3F, false);
        }
        // Information
        if (this.isHovering(230, 6, 20, 18, mouseX, mouseY)) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.leftPos + 230, this.topPos + 6, 20, 84, 20, 18, 256, 256);

            List<Component> dungeonInfo = new ArrayList<>();
            dungeonInfo.add(Component.translatable("dungeonz.dungeon.info"));
            for (int i = 1; i < 10; i++) {

                String dungeonInfoTooltip = "dungeon." + this.menu.getDungeonPortalEntity().getDungeonType() + ".description" + "." + i;
                Component dungeonInfoText = Component.translatable(dungeonInfoTooltip);

                if (dungeonInfoText.getString().equals(dungeonInfoTooltip)) {
                    break;
                }
                dungeonInfo.add(dungeonInfoText);
            }

            dungeonInfo.add(Component.translatable("dungeonz.dungeon.info.respawn" + (this.menu.isAllowRespawn() ? "" : ".disabled")));
            dungeonInfo.add(Component.translatable("dungeonz.dungeon.info.keep_inventory" + (this.menu.isKeepInventory() ? "" : ".disabled")));
            dungeonInfo.add(Component.translatable("dungeonz.dungeon.info.positive_effects" + (this.menu.isAllowPositiveEffects() ? "" : ".disabled")));
            dungeonInfo.add(Component.translatable("dungeonz.dungeon.info.ender_pearl" + (this.menu.isAllowEnderPearl() ? "" : ".disabled")));
            dungeonInfo.add(Component.translatable("dungeonz.dungeon.info.elytra" + (this.menu.isAllowElytra() ? "" : ".disabled")));


            graphics.setComponentTooltipForNextFrame(this.font, dungeonInfo, mouseX, mouseY);
        } else {
            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.leftPos + 230, this.topPos + 6, 0, 84, 20, 18, 256, 256);
        }
        graphics.item(INFO_ITEMSTACK, this.leftPos + 232, this.topPos + 7);
    }

    @Override
    public void slotChanged(AbstractContainerMenu var1, int var2, ItemStack var3) {
    }

    @Override
    public void dataChanged(AbstractContainerMenu var1, int var2, int var3) {
    }

    public class DungeonButton extends Button {

        public DungeonButton(int x, int y, Component text, Button.OnPress onPress) {
            super(x, y, 52, 20, text, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            int j = 20;
            if (!this.active) {
                j = 0;
            } else if (this.isHovered()) {
                j = 40;
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.getX(), this.getY(), 0, j, this.width, this.height, 256, 256);

            int o = (this.active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(this.alpha * 255.0f) << 24;
            graphics.centeredText(font, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, o);

            if (!this.active && this.isHovered()) {
                Component text = null;
                if (DungeonPortalScreen.this.menu.getDungeonPortalEntity().isOnCooldown((int) DungeonPortalScreen.this.minecraft.level.getGameTime())) {
                    int cooldown = (DungeonPortalScreen.this.menu.getDungeonPortalEntity().getCooldownTime() - (int) DungeonPortalScreen.this.minecraft.level.getGameTime()) / 20;
                    int seconds = cooldown % 60;
                    int minutes = cooldown / 60 % 60;
                    int hours = cooldown / 60 / 60;
                    text = Component.translatable("text.dungeonz.dungeon_cooldown_time", hours, minutes, seconds);
                } else if ((DungeonPortalScreen.this.menu.getDungeonPortalEntity().getDungeonPlayerUuids().size()
                        + DungeonPortalScreen.this.menu.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().size()) >= DungeonPortalScreen.this.menu.getDungeonPortalEntity()
                        .getMaxGroupSize()) {
                    text = Component.translatable("text.dungeonz.dungeon_full");
                } else if (minecraft.player != null && !DungeonPortalScreen.this.menu.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().isEmpty()
                        && DungeonPortalScreen.this.menu.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().contains(minecraft.player.getUUID())) {
                    text = Component.translatable("text.dungeonz.dead_player");
                } else if (!InventoryHelper.hasRequiredItemStacks(minecraft.player.getInventory(), DungeonPortalScreen.this.menu.getRequiredItemStacks().get(DungeonPortalScreen.this.menu.getDungeonPortalEntity().getDifficulty()))) {
                    text = Component.translatable("text.dungeonz.missing");
                } else if (!DungeonPortalScreen.this.menu.getAdmission().meetsRequiredLevel(DungeonPortalScreen.this.menu.getRequiredLevel())) {
                    text = Component.translatable("text.dungeonz.required_level", DungeonPortalScreen.this.menu.getRequiredLevel());
                }
                if (text != null) {
                    graphics.setTooltipForNextFrame(font, text, mouseX, mouseY);
                }
            }
        }

    }

    public class DungeonDifficultyButton extends Button {
        private Component text;
        private static final WidgetSprites TEXTURES = new WidgetSprites(Identifier.withDefaultNamespace("widget/button"), Identifier.withDefaultNamespace("widget/button_disabled"),
                Identifier.withDefaultNamespace("widget/button_highlighted"));

        public DungeonDifficultyButton(int x, int y, Component text, Button.OnPress onPress) {
            super(x, y, 60, 20, text, onPress, DEFAULT_NARRATION);
            this.text = text;
        }

        public void setText(Component text) {
            this.text = text;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            Minecraft minecraftClient = Minecraft.getInstance();
            Font textRenderer = minecraftClient.font;

            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
            int j = (this.active ? 0xFFFFFF : 0xA0A0A0) | Mth.ceil(this.alpha * 255.0f) << 24;
            graphics.centeredText(textRenderer, this.text, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, j);
        }

    }

    public class DungeonSliderButton extends Button {
        private boolean enabled = false;

        public DungeonSliderButton(int x, int y, Button.OnPress onPress) {
            super(x, y, 20, 12, Component.nullToEmpty(""), onPress, DEFAULT_NARRATION);
        }

        public void cycleEnabled() {
            this.enabled = !this.enabled;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            int i = 60;
            if (this.enabled) {
                i = 72;
            }
            int j = 0;
            if (!this.active) {
                j = 40;
            } else if (this.isHovered()) {
                j = 20;
            }
            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.getX(), this.getY(), j, i, this.width, this.height, 256, 256);
        }

    }

}
