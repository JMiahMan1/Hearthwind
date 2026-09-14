package net.dungeonz.block.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.systems.RenderSystem;

import net.dungeonz.DungeonzMain;
import net.dungeonz.init.DimensionInit;
import net.dungeonz.network.DungeonClientPacket;
import net.dungeonz.util.InventoryHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.levelz.access.LevelManagerAccess;
import net.levelz.level.LevelManager;
import net.minecraft.class_1657;
import net.minecraft.class_1661;
import net.minecraft.class_1703;
import net.minecraft.class_1712;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4185;
import net.minecraft.class_465;
import net.minecraft.class_8666;
import net.partyaddon.access.GroupManagerAccess;
import net.partyaddon.group.GroupManager;

@Environment(EnvType.CLIENT)
public class DungeonPortalScreen extends class_465<DungeonPortalScreenHandler> implements class_1712 {

    private static final class_2960 ICONS = class_2960.method_60654("dungeonz:textures/gui/dungeon_icons.png");
    private static final class_2561 JOIN = class_2561.method_43471("dungeon.task.join");
    private static final class_2561 LEAVE = class_2561.method_43471("dungeon.task.leave");
    private static final class_1799 INFO_ITEMSTACK = new class_1799(class_1802.field_8573);

    private final class_2960 texture;
    public DungeonDifficultyButton difficultyButton;
    private DungeonButton dungeonButton;
    private DungeonSliderButton privateButton;
    private final class_1657 playerEntity;

    public DungeonPortalScreen(DungeonPortalScreenHandler handler, class_1661 inventory, class_2561 title) {
        super(handler, inventory, title);
        this.playerEntity = inventory.field_7546;
        texture = handler.getBackgroundId() != null ? handler.getBackgroundId() : class_2960.method_60654("dungeonz:textures/gui/dungeon_portal.png");
        this.field_2792 = 256;
        this.field_2779 = 222;
    }

    @Override
    protected void method_25426() {
        super.method_25426();
        this.field_2776 = (this.field_22789 / 2 - this.field_2792 / 2);
        this.field_2800 = (this.field_22790 / 2 - this.field_2779 / 2);

        this.field_2797.method_7596(this);

        final boolean playerIsInDungeonWorld = playerEntity.method_37908().method_27983() == DimensionInit.DUNGEON_WORLD;
        class_2561 buttonText = playerIsInDungeonWorld ? LEAVE : JOIN;

        this.dungeonButton = this.method_37063(new DungeonButton(this.field_2776 + this.field_2792 / 2 - 26, this.field_2800 + this.field_2779 - 28, buttonText, (button) -> {
            if (button.field_22763) {
                DungeonClientPacket.writeC2SDungeonTeleportPacket(this.field_22787, this.field_2797.getPos(), this.playerEntity.method_5667());
                this.field_2797.setWaitingGroupSize(this.field_2797.getWaitingGroupSize() + 1);
                button.field_22763 = false;
            }
        }));
        this.difficultyButton = this.method_37063(new DungeonDifficultyButton(this.field_2776 + 144, this.field_2800 + 36, class_2561.method_30163(""), (button) -> {
            if (button.field_22763) {
                DungeonClientPacket.writeC2SChangeDifficultyPacket(this.field_22787, this.field_2797.getPos());
            }
        }));
        this.privateButton = this.method_37063(new DungeonSliderButton(this.field_2776 + 144, this.field_2800 + 63, (button) -> {
            if (button.field_22763) {
                ((DungeonSliderButton) button).cycleEnabled();
                DungeonClientPacket.writeC2SChangePrivateGroupPacket(field_22787, this.field_2797.getPos(), ((DungeonSliderButton) button).isEnabled());
            }
        }));

        this.privateButton.enabled = this.field_2797.getDungeonPortalEntity().getPrivateGroup();
        if (playerIsInDungeonWorld) {
            this.dungeonButton.field_22763 = true;
            this.difficultyButton.field_22763 = false;
            this.privateButton.field_22763 = false;
        } else {
            if (!this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().isEmpty()) {
                this.difficultyButton.field_22763 = false;
                this.privateButton.field_22763 = false;
            } else {
                this.difficultyButton.field_22763 = true;
                this.privateButton.field_22763 = true;
            }
            if ((this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().size() + this.field_2797.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().size()) < this.field_2797
                    .getDungeonPortalEntity().getMaxGroupSize() && InventoryHelper.hasRequiredItemStacks(this.playerEntity.method_31548(), this.field_2797.getRequiredItemStacks().get(this.field_2797.getDungeonPortalEntity().getDifficulty()))
                    && !this.field_2797.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().contains(this.playerEntity.method_5667())) {
                this.dungeonButton.field_22763 = true;
            } else {
                this.dungeonButton.field_22763 = false;
            }
            if (this.dungeonButton.field_22763 && DungeonzMain.isLevelZLoaded) {
                LevelManager levelManager = ((LevelManagerAccess) this.playerEntity).getLevelManager();
                if (levelManager.getOverallLevel() < this.field_2797.getRequiredLevel()) {
                    this.dungeonButton.field_22763 = false;
                }
            }
            if (this.dungeonButton.field_22763 && this.privateButton.enabled && !this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().isEmpty()) {
                if (DungeonzMain.isPartyAddonLoaded) {
                    GroupManager groupManager = ((GroupManagerAccess) this.playerEntity).getGroupManager();
                    if (groupManager.getGroupPlayerIdList().isEmpty() || !groupManager.getGroupPlayerIdList().contains(this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().get(0))) {
                        this.dungeonButton.field_22763 = false;
                    }
                } else {
                    this.dungeonButton.field_22763 = false;
                }
            }
        }
        if (this.field_2797.getDifficulties().contains(this.field_2797.getDungeonPortalEntity().getDifficulty())) {
            this.difficultyButton.setText(class_2561.method_43471("dungeonz.difficulty." + this.field_2797.getDungeonPortalEntity().getDifficulty()));
        } else {
            this.difficultyButton.setText(class_2561.method_43471("dungeonz.difficulty." + this.field_2797.getDifficulties().get(0)));
        }
        if (this.field_2797.getDungeonPortalEntity().isOnCooldown((int) this.field_22787.field_1687.method_8510())) {
            this.dungeonButton.field_22763 = false;
        }
    }

    private class_2561 getPlayerName(UUID playerId, int length, int substringLength) {
        if (this.field_22787.method_1562().method_2871(playerId) != null) {
            String playerName = this.field_22787.method_1562().method_2871(playerId).method_2966().getName();
            if (this.field_22787.field_1772.method_1727(playerName) > length && substringLength != 0) {
                playerName = playerName.substring(0, substringLength) + "..";
            }
            return class_2561.method_30163(playerName);
        }
        return class_2561.method_43471("text.dungeonz.empty_name");
    }

    @Override
    public void method_37432() {
        super.method_37432();
    }

    @Override
    public void method_25394(class_332 context, int mouseX, int mouseY, float delta) {
        super.method_25394(context, mouseX, mouseY, delta);

        // Title
        context.method_51439(this.field_22793, this.field_22785, this.field_2776 + this.field_2792 / 2 - this.field_22793.method_27525(this.field_22785) / 2, this.field_2800 + 8, 0x404040, false);

        // Dungeon player list
        int k = this.field_2800 + 37;
        context.method_51439(this.field_22793,
                class_2561.method_43469("text.dungeonz.player_list",
                        this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().size() + this.field_2797.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().size(),
                        this.field_2797.getDungeonPortalEntity().getMaxGroupSize()),
                this.field_2776 + 8, this.field_2800 + 24, 0x3F3F3F, false);
        for (int i = 0; i < this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().size() && i < 13; i++) {
            String playerName = getPlayerName(this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().get(i), 102, 15).getString();
            if (i == 12) {
                playerName = "...";
                if (this.method_2378(13, k, 16, 7, mouseX, mouseY)) {
                    List<class_2561> otherPlayerNames = new ArrayList<class_2561>();
                    for (int u = 12; u < this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().size(); u++) {
                        otherPlayerNames.add(getPlayerName(this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().get(u), 102, 15));
                    }
                    context.method_51434(this.field_22793, otherPlayerNames, mouseX, mouseY);
                }
            }
            context.method_51433(this.field_22793, playerName, this.field_2776 + 13, k, 0xFFFFFF, false);
            k += 13;
        }
        // Required items
        context.method_51439(this.field_22793, class_2561.method_43471("text.dungeonz.required"), this.field_2776 + 139, this.field_2800 + 81, 0x3F3F3F, false);
        context.method_25302(ICONS, this.field_2776 + 142 + this.field_22793.method_27525(class_2561.method_43471("text.dungeonz.required")), this.field_2800 + 78,
                52 + (InventoryHelper.hasRequiredItemStacks(this.playerEntity.method_31548(), this.field_2797.getRequiredItemStacks().get(this.field_2797.getDungeonPortalEntity().getDifficulty())) ? 0 : 14), 0, 14, 14);

        if (!this.field_2797.getRequiredItemStacks().get(this.field_2797.getDungeonPortalEntity().getDifficulty()).isEmpty()) {
            int l = 0;

            for (class_1799 stack : this.field_2797.getRequiredItemStacks().get(this.field_2797.getDungeonPortalEntity().getDifficulty())) {
                context.method_51427(stack, this.field_2776 + 144 + l, this.field_2800 + 93);
                context.method_51431(this.field_22793, stack, this.field_2776 + 144 + l, this.field_2800 + 93);
                if (this.method_2378(144 + l, 93, 16, 16, mouseX, mouseY)) {
                    context.method_51438(this.field_22793, stack.method_7964(), mouseX, mouseY);
                }
                l += 18;
            }
        } else {
            context.method_51439(this.field_22793, class_2561.method_43471("text.dungeonz.nothing_required"), this.field_2776 + 144, this.field_2800 + 93, 0x3F3F3F, false);
        }

        // Possible loot
        context.method_51439(this.field_22793, class_2561.method_43471("text.dungeonz.possible"), this.field_2776 + 139, this.field_2800 + 115, 0x3F3F3F, false);
        if (this.field_2797.getPossibleLootDifficultyItemStackMap().size() > 0 && this.field_2797.getPossibleLootDifficultyItemStackMap().containsKey(this.field_2797.getDungeonPortalEntity().getDifficulty())
                && this.field_2797.getPossibleLootDifficultyItemStackMap().get(this.field_2797.getDungeonPortalEntity().getDifficulty()).size() > 0) {
            int l = 0;
            int o = 0;
            for (int i = 0; i < this.field_2797.getPossibleLootDifficultyItemStackMap().get(this.field_2797.getDungeonPortalEntity().getDifficulty()).size() && i < 10; i++) {
                context.method_51427(this.field_2797.getPossibleLootDifficultyItemStackMap().get(this.field_2797.getDungeonPortalEntity().getDifficulty()).get(i), this.field_2776 + 144 + l, this.field_2800 + o + 127);
                context.method_51431(this.field_22793, this.field_2797.getPossibleLootDifficultyItemStackMap().get(this.field_2797.getDungeonPortalEntity().getDifficulty()).get(i), this.field_2776 + 144 + l,
                        this.field_2800 + o + 127);

                if (this.method_2378(144 + l, o + 127, 16, 16, mouseX, mouseY)) {
                    context.method_51438(this.field_22793, this.field_2797.getPossibleLootDifficultyItemStackMap().get(this.field_2797.getDungeonPortalEntity().getDifficulty()).get(i).method_7964(), mouseX,
                            mouseY);
                }
                l += 18;
                if (i == 4) {
                    l = 0;
                    o = 18;
                }
            }
        }
        context.method_51439(this.field_22793, class_2561.method_43471("dungeonz.difficulty"), this.field_2776 + 139, this.field_2800 + 24, 0x3F3F3F, false);
        context.method_51439(this.field_22793, class_2561.method_43471("text.dungeonz.private"), this.field_2776 + 169, this.field_2800 + 65, 0x3F3F3F, false);
        // Min group size
        if (this.field_2797.getDungeonPortalEntity().getDungeonPlayerCount() <= 0 && this.field_2797.getDungeonPortalEntity().getMinGroupSize() > 1) {
            context.method_51439(this.field_22793, class_2561.method_43469("text.dungeonz.waiting_player_list", this.field_2797.getWaitingGroupSize(), this.field_2797.getDungeonPortalEntity().getMinGroupSize()),
                    this.field_2776 + 139, this.field_2800 + 167, 0x3F3F3F, false);
        }
        // LevelZ
        if (DungeonzMain.isLevelZLoaded) {
            context.method_51439(this.field_22793, class_2561.method_43469("text.dungeonz.required_level", this.field_2797.getRequiredLevel()), this.field_2776 + 139, this.field_2800 + 180, 0x3F3F3F, false);
        }
        // Information
        if (this.method_2378(230, 6, 20, 18, mouseX, mouseY)) {
            context.method_25302(ICONS, this.field_2776 + 230, this.field_2800+6, 20, 84, 20, 18);

            List<class_2561> dungeonInfo = new ArrayList<>();
            dungeonInfo.add(class_2561.method_43471("dungeonz.dungeon.info"));
            for (int i = 1; i < 10; i++) {

                String dungeonInfoTooltip = "dungeon." + this.field_2797.getDungeonPortalEntity().getDungeonType() + ".description" + "." + i;
                class_2561 dungeonInfoText = class_2561.method_43471(dungeonInfoTooltip);

                if (dungeonInfoText.getString().equals(dungeonInfoTooltip)) {
                    break;
                }
                dungeonInfo.add(dungeonInfoText);
            }

            dungeonInfo.add(class_2561.method_43471("dungeonz.dungeon.info.respawn" + (this.field_2797.isAllowRespawn() ? "" : ".disabled")));
            dungeonInfo.add(class_2561.method_43471("dungeonz.dungeon.info.keep_inventory" + (this.field_2797.isKeepInventory() ? "" : ".disabled")));
            dungeonInfo.add(class_2561.method_43471("dungeonz.dungeon.info.positive_effects" + (this.field_2797.isAllowPositiveEffects() ? "" : ".disabled")));
            dungeonInfo.add(class_2561.method_43471("dungeonz.dungeon.info.ender_pearl" + (this.field_2797.isAllowEnderPearl() ? "" : ".disabled")));
            dungeonInfo.add(class_2561.method_43471("dungeonz.dungeon.info.elytra" + (this.field_2797.isAllowElytra() ? "" : ".disabled")));


            context.method_51434(this.field_22793, dungeonInfo, mouseX, mouseY);
        } else {
            context.method_25302(ICONS, this.field_2776 + 230, this.field_2800+6, 0, 84, 20, 18);
        }
        context.method_51427(INFO_ITEMSTACK,this.field_2776 + 232, this.field_2800+7);

        this.method_2380(context, mouseX, mouseY);
    }

    @Override
    protected void method_2389(class_332 context, float delta, int mouseX, int mouseY) {
        context.method_25302(texture, this.field_2776, this.field_2800, 0, 0, this.field_2792, this.field_2779);
    }

    @Override
    protected void method_2388(class_332 context, int mouseX, int mouseY) {
    }

    @Override
    public boolean method_25402(double mouseX, double mouseY, int button) {
        return super.method_25402(mouseX, mouseY, button);
    }

    @Override
    public void method_7635(class_1703 var1, int var2, class_1799 var3) {
    }

    @Override
    public void method_7633(class_1703 var1, int var2, int var3) {
    }

    public class DungeonButton extends class_4185 {

        public DungeonButton(int x, int y, class_2561 text, class_4185.class_4241 onPress) {
            super(x, y, 52, 20, text, onPress, field_40754);
        }

        @Override
        public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
            int j = 20;
            if (!this.field_22763) {
                j = 0;
            } else if (this.method_49606()) {
                j = 40;
            }
            context.method_25302(ICONS, this.method_46426(), this.method_46427(), 0, j, this.field_22758, this.field_22759);

            int o = this.field_22763 ? 0xFFFFFF : 0xA0A0A0;
            context.method_27534(field_22793, this.method_25369(), this.method_46426() + this.field_22758 / 2, this.method_46427() + (this.field_22759 - 8) / 2, o | class_3532.method_15386(this.field_22765 * 255.0f) << 24);

            if (!this.field_22763 && this.method_49606()) {
                class_2561 text = null;
                if (DungeonPortalScreen.this.field_2797.getDungeonPortalEntity().isOnCooldown((int) DungeonPortalScreen.this.field_22787.field_1687.method_8510())) {
                    int cooldown = (DungeonPortalScreen.this.field_2797.getDungeonPortalEntity().getCooldownTime() - (int) DungeonPortalScreen.this.field_22787.field_1687.method_8510()) / 20;
                    int seconds = cooldown % 60;
                    int minutes = cooldown / 60 % 60;
                    int hours = cooldown / 60 / 60;
                    text = class_2561.method_43469("text.dungeonz.dungeon_cooldown_time", hours, minutes, seconds);
                } else if ((DungeonPortalScreen.this.field_2797.getDungeonPortalEntity().getDungeonPlayerUuids().size()
                        + DungeonPortalScreen.this.field_2797.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().size()) >= DungeonPortalScreen.this.field_2797.getDungeonPortalEntity()
                        .getMaxGroupSize()) {
                    text = class_2561.method_43471("text.dungeonz.dungeon_full");
                } else if (field_22787.field_1724 != null && !DungeonPortalScreen.this.field_2797.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().isEmpty()
                        && DungeonPortalScreen.this.field_2797.getDungeonPortalEntity().getDeadDungeonPlayerUUIDs().contains(field_22787.field_1724.method_5667())) {
                    text = class_2561.method_43471("text.dungeonz.dead_player");
                } else if (!InventoryHelper.hasRequiredItemStacks(field_22787.field_1724.method_31548(), DungeonPortalScreen.this.field_2797.getRequiredItemStacks().get(DungeonPortalScreen.this.field_2797.getDungeonPortalEntity().getDifficulty()))) {
                    text = class_2561.method_43471("text.dungeonz.missing");
                } else if (DungeonzMain.isLevelZLoaded) {
                    LevelManager levelManager = ((LevelManagerAccess) DungeonPortalScreen.this.playerEntity).getLevelManager();
                    if (levelManager.getOverallLevel() < DungeonPortalScreen.this.field_2797.getRequiredLevel()) {
                        text = class_2561.method_43469("text.dungeonz.required_level", DungeonPortalScreen.this.field_2797.getRequiredLevel());
                    }
                }
                if (text != null) {
                    context.method_51438(field_22793, text, mouseX, mouseY);
                }
            }
        }

    }

    public class DungeonDifficultyButton extends class_4185 {
        private class_2561 text;
        private static final class_8666 TEXTURES = new class_8666(class_2960.method_60656("widget/button"), class_2960.method_60656("widget/button_disabled"),
                class_2960.method_60656("widget/button_highlighted"));

        public DungeonDifficultyButton(int x, int y, class_2561 text, class_4185.class_4241 onPress) {
            super(x, y, 60, 20, text, onPress, field_40754);
            this.text = text;
        }

        public void setText(class_2561 text) {
            this.text = text;
        }

        @Override
        public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
            class_310 minecraftClient = class_310.method_1551();
            class_327 textRenderer = minecraftClient.field_1772;

            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, this.field_22765);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            context.method_52706(TEXTURES.method_52729(this.field_22763, this.method_25367()), this.method_46426(), this.method_46427(), this.method_25368(), this.method_25364());
            int j = this.field_22763 ? 0xFFFFFF : 0xA0A0A0;
            context.method_27534(textRenderer, this.text, this.method_46426() + this.field_22758 / 2, this.method_46427() + (this.field_22759 - 8) / 2, j | class_3532.method_15386(this.field_22765 * 255.0f) << 24);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }

    }

    public class DungeonSliderButton extends class_4185 {
        private boolean enabled = false;

        public DungeonSliderButton(int x, int y, class_4185.class_4241 onPress) {
            super(x, y, 20, 12, class_2561.method_30163(""), onPress, field_40754);
        }

        public void cycleEnabled() {
            this.enabled = !this.enabled;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public void method_48579(class_332 context, int mouseX, int mouseY, float delta) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int i = 60;
            if (this.enabled) {
                i = 72;
            }
            int j = 0;
            if (!this.field_22763) {
                j = 40;
            } else if (this.method_49606()) {
                j = 20;
            }
            context.method_25302(ICONS, this.method_46426(), this.method_46427(), j, i, this.field_22758, this.field_22759);
        }

    }

}
