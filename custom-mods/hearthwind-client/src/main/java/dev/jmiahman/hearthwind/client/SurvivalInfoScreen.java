package dev.jmiahman.hearthwind.client;

import java.util.Map;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Survival info panel (skills / jobs / party / thirst / temperature), styled after
 * the Aged Modrinth reference: light-gray panel with 7-tab strip, rich cards,
 * true item icons, dynamic progress bars, and informative tooltips.
 */
@Environment(EnvType.CLIENT)
public class SurvivalInfoScreen extends Screen {
    public enum Kind { SKILLS, JOBS, PARTY, THIRST, TEMPERATURE }

    private static final Identifier PANEL = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/panel");
    private static final Identifier ARROW = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/arrow");
    private static final Identifier ARROW_HOVER = Identifier.fromNamespaceAndPath("hearthwind", "nutrition/arrow_hover");

    // Colors (strict ARGB with high contrast against #C6C6C6 panel)
    private static final int TITLE = 0xFF2A2A2A;
    private static final int LABEL = 0xFF222222;
    private static final int HINT = 0xFF5A5A5A;
    private static final int VALUE = 0xFF444444;
    private static final int GOOD = 0xFF2E7D32;
    private static final int WARNING = 0xFFD87D12;
    private static final int BAD = 0xFFC62828;
    private static final int COLD_BLUE = 0xFF1976D2;

    // Card colors
    private static final int CARD_BORDER = 0xFFB4B4B4;
    private static final int CARD_FACE = 0xFFDEDEDE;
    private static final int CARD_FACE_HOVER = 0xFFEEEEEE;
    private static final int CARD_ACTIVE_BORDER = 0xFF2E7D32;
    private static final int CARD_ACTIVE_FACE = 0xFFE4F3E4;
    private static final int TRACK_COLOR = 0xFFB8B8B8;
    private static final int FILL_COLOR = 0xFF43A047;

    private static final String[] SKILL_IDS = {
            "farming", "mining", "smithing", "strength", "agility", "defense",
            "health", "stamina", "luck", "archery", "alchemy", "trade" };

    private static final Item[] SKILL_ITEM_ICONS = {
            Items.WHEAT, Items.IRON_PICKAXE, Items.ANVIL, Items.IRON_SWORD,
            Items.LEATHER_BOOTS, Items.IRON_CHESTPLATE, Items.GOLDEN_APPLE, Items.FEATHER,
            Items.RABBIT_FOOT, Items.BOW, Items.BREWING_STAND, Items.EMERALD };

    private static final String[] SKILL_PERKS = {
            "+Crops & Livestock", "+Mining Speed", "Forging & Metal", "+Attack Damage",
            "+Move Speed", "+Armor Defense", "+Max Health", "Digging & Endurance",
            "+Rare Drop Luck", "Bow & Crossbow", "Potions & Elixirs", "Barter & Trading" };

    private static final String[] SKILL_DESCS = {
            "Harvest crops and breed livestock.",
            "Excavate stone, ores, and subterranean minerals.",
            "Craft armor, weapons, and refine metal ingots.",
            "Slay hostile monsters in melee combat.",
            "Run, sprint, and traverse difficult terrain.",
            "Withstand enemy strikes and physical damage.",
            "Endure harsh environments and survive danger.",
            "Dig sand, gravel, and dirt with shovels.",
            "Fish rare items and discover lucky loot.",
            "Fire bows, crossbows, and tridents at range.",
            "Brew potent alchemy potions and herbal remedies.",
            "Trade with villagers and wandering merchants." };

    private static final String[] JOB_IDS = {
            "fisher", "miner", "farmer", "warrior",
            "smither", "brewer", "builder", "lumberjack" };

    private static final Item[] JOB_ITEM_ICONS = {
            Items.FISHING_ROD, Items.IRON_PICKAXE, Items.GOLDEN_HOE, Items.IRON_SWORD,
            Items.ANVIL, Items.BREWING_STAND, Items.BRICKS, Items.IRON_AXE };

    private static final String[] JOB_DESCS = {
            "Catches fish and reels in river treasures",
            "Excavates ores, coal, and underground minerals",
            "Harvests crops, tilled soil, and breeds animals",
            "Slays hostile monsters and dungeon beasts",
            "Smelts ores and crafts weapons and armor",
            "Brews potions, herbal teas, and remedies",
            "Places blocks and constructs architecture",
            "Chops logs and harvests forest timber" };

    private final Kind kind;
    private int x;
    private int y;

    public SurvivalInfoScreen(Kind kind) {
        super(Component.translatable("screen.hearthwind." + kind.name().toLowerCase()));
        this.kind = kind;
    }

    private TabStrip.Tab tab() {
        return switch (this.kind) {
            case JOBS -> TabStrip.Tab.JOBS;
            default -> TabStrip.Tab.SKILLS;
        };
    }

    @Override
    protected void init() {
        super.init();
        this.x = (this.width - 176) / 2;
        this.y = (this.height - 166) / 2;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        Font font = Minecraft.getInstance().font;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PANEL, this.x, this.y, 176, 166);
        TabStrip.draw(graphics, font, this.x, this.y, tab(), mouseX, mouseY);
        String title = this.title.getString();
        graphics.text(font, title, this.x + 88 - font.width(title) / 2, this.y + 6, TITLE, false);

        switch (this.kind) {
            case SKILLS -> drawSkills(graphics, font, mouseX, mouseY);
            case JOBS -> drawJobs(graphics, font, mouseX, mouseY);
            case PARTY -> drawParty(graphics, font, mouseX, mouseY);
            case THIRST -> drawThirst(graphics, font);
            case TEMPERATURE -> drawTemperature(graphics, font);
        }

        boolean hoverArrow = within(5, 5, 11, 10, mouseX, mouseY);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, hoverArrow ? ARROW_HOVER : ARROW, this.x + 5, this.y + 5,
                11, 10);
    }

    private void drawSkills(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        Map<String, Integer> levels = ClientSkillData.knownLevels();
        String hoveredTooltip = null;
        Minecraft mc = Minecraft.getInstance();
        int pts = mc.player != null ? mc.player.experienceLevel : 0;

        String ptsText = "Pts: " + pts;
        graphics.text(font, ptsText, this.x + 168 - font.width(ptsText), this.y + 6, pts > 0 ? 0xFF2E7D32 : HINT, false);

        for (int i = 0; i < SKILL_IDS.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = this.x + 7 + col * 82;
            int cy = this.y + 18 + row * 19;
            int cw = 80;
            int ch = 18;

            int lvl = levels.getOrDefault(SKILL_IDS[i], 0);
            boolean hover = mouseX >= cx && mouseX < cx + cw && mouseY >= cy && mouseY < cy + ch;

            int border = (lvl > 0) ? CARD_ACTIVE_BORDER : CARD_BORDER;
            int face = (lvl > 0) ? CARD_ACTIVE_FACE : hover ? CARD_FACE_HOVER : CARD_FACE;

            graphics.fill(cx, cy, cx + cw, cy + ch, border);
            graphics.fill(cx + 1, cy + 1, cx + cw - 1, cy + ch - 1, face);

            // Skill item icon (16x16)
            graphics.item(new ItemStack(SKILL_ITEM_ICONS[i]), cx + 1, cy + 1);

            String skillName = SKILL_IDS[i];
            String cap = skillName.substring(0, 1).toUpperCase() + skillName.substring(1);
            graphics.text(font, cap, cx + 18, cy + 1, LABEL, false);

            String lvlStr = "Lv." + lvl;
            int lvlColor = lvl >= 30 ? 0xFFD87D12 : lvl > 0 ? GOOD : HINT;
            graphics.text(font, lvlStr, cx + 18, cy + 9, lvlColor, false);

            // Plus button if player has experience points to spend
            boolean canLevel = pts > 0 && lvl < 30;
            if (canLevel) {
                int btnX = cx + cw - 15;
                int btnY = cy + 3;
                int btnW = 12;
                int btnH = 12;
                boolean btnHover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
                graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, btnHover ? 0xFF2E7D32 : 0xFF388E3C);
                graphics.text(font, "+", btnX + 3, btnY + 2, 0xFFFFFFFF, true);
                if (btnHover) {
                    hoveredTooltip = "+1 Level (1 Point)";
                }
            }
        }

        if (hoveredTooltip != null) {
            graphics.setTooltipForNextFrame(Component.literal(hoveredTooltip), mouseX, mouseY);
        }
    }

    private void drawJobs(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        String current = ClientJobData.hasJob() ? ClientJobData.jobId() : "";

        for (int i = 0; i < JOB_IDS.length; i++) {
            String job = JOB_IDS[i];
            int col = i % 2;
            int row = i / 2;
            int cx = this.x + 7 + col * 82;
            int cy = this.y + 18 + row * 27;
            int cw = 80;
            int ch = 25;

            boolean isCurrent = job.equalsIgnoreCase(current);
            boolean hover = mouseX >= cx && mouseX < cx + cw && mouseY >= cy && mouseY < cy + ch;

            int border = isCurrent ? CARD_ACTIVE_BORDER : CARD_BORDER;
            int face = isCurrent ? CARD_ACTIVE_FACE : hover ? CARD_FACE_HOVER : CARD_FACE;

            graphics.fill(cx, cy, cx + cw, cy + ch, border);
            graphics.fill(cx + 1, cy + 1, cx + cw - 1, cy + ch - 1, face);

            // Job item icon (16x16)
            graphics.item(new ItemStack(JOB_ITEM_ICONS[i]), cx + 2, cy + 4);

            String cap = job.substring(0, 1).toUpperCase() + job.substring(1);
            graphics.text(font, cap, cx + 20, cy + 2, LABEL, false);

            if (isCurrent) {
                int lvl = ClientJobData.level();
                String badge = "Lv." + lvl;
                graphics.text(font, badge, cx + cw - 3 - font.width(badge), cy + 2, GOOD, false);

                // XP bar
                int barW = 56;
                int barX = cx + 20;
                int barY = cy + 14;
                graphics.fill(barX, barY, barX + barW, barY + 5, TRACK_COLOR);
                int fill = Math.round(barW * ClientJobData.xpProgress());
                if (fill > 0) {
                    graphics.fill(barX, barY, barX + fill, barY + 5, FILL_COLOR);
                }
            } else {
                graphics.text(font, "Join Job", cx + 20, cy + 13, hover ? 0xFF1565C0 : HINT, false);
            }
        }

        String footer = "Click a job to join or leave";
        graphics.text(font, footer, this.x + 88 - font.width(footer) / 2, this.y + 130, HINT, false);
    }

    private void drawParty(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (!ClientPartyData.inParty()) {
            // Not in party view
            int cardX = this.x + 8;
            int cardY = this.y + 17;
            int cardW = 160;
            int cardH = 22;
            graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, CARD_BORDER);
            graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + cardH - 1, CARD_FACE);
            graphics.text(font, "No Active Party", cardX + 8, cardY + 7, LABEL, false);

            // Create party button
            int btnX = this.x + 8;
            int btnY = this.y + 43;
            int btnW = 160;
            int btnH = 20;
            boolean hover = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;
            graphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, hover ? 0xFF2E7D32 : 0xFF388E3C);
            graphics.text(font, "+ Create Party", btnX + btnW / 2 - font.width("+ Create Party") / 2, btnY + 6, 0xFFFFFFFF, true);

            // Guide rows
            int guideY = this.y + 68;
            drawGuideRow(graphics, font, Items.EXPERIENCE_BOTTLE, "Shared Experience", "Earn 50% skill XP from nearby allies", guideY);
            drawGuideRow(graphics, font, Items.SHIELD, "Friendly Fire Protection", "Prevent accidental damage to teammates", guideY + 25);
            drawGuideRow(graphics, font, Items.PLAYER_HEAD, "Live Party HUD", "Monitor ally health and distance", guideY + 50);
        } else {
            // In party view
            int cardX = this.x + 8;
            int cardY = this.y + 16;
            int cardW = 160;
            int cardH = 24;
            graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, CARD_ACTIVE_BORDER);
            graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + cardH - 1, CARD_ACTIVE_FACE);

            graphics.item(new ItemStack(Items.PLAYER_HEAD), cardX + 4, cardY + 4);
            graphics.text(font, "★ " + ClientPartyData.partyName(), cardX + 24, cardY + 4, 0xFFD87D12, true);
            String role = ClientPartyData.isLeader() ? "Party Leader" : "Party Member";
            graphics.text(font, role, cardX + 24, cardY + 14, HINT, false);

            // Action row (PvP toggle & leave/disband buttons)
            int actY = this.y + 43;
            if (ClientPartyData.isLeader()) {
                // PvP Toggle button
                int pvpX = this.x + 8;
                int pvpW = 76;
                int pvpH = 16;
                boolean pvp = ClientPartyData.pvpEnabled();
                boolean pHover = mouseX >= pvpX && mouseX < pvpX + pvpW && mouseY >= actY && mouseY < actY + pvpH;
                graphics.fill(pvpX, actY, pvpX + pvpW, actY + pvpH, pvp ? 0xFFC62828 : 0xFF2E7D32);
                String pvpText = pvp ? "PvP: ON" : "PvP: OFF";
                graphics.text(font, pvpText, pvpX + pvpW / 2 - font.width(pvpText) / 2, actY + 4, 0xFFFFFFFF, true);

                // Disband button
                int disX = this.x + 92;
                int disW = 76;
                int disH = 16;
                boolean dHover = mouseX >= disX && mouseX < disX + disW && mouseY >= actY && mouseY < actY + disH;
                graphics.fill(disX, actY, disX + disW, actY + disH, dHover ? 0xFFB71C1C : 0xFFD32F2F);
                String disText = "Disband";
                graphics.text(font, disText, disX + disW / 2 - font.width(disText) / 2, actY + 4, 0xFFFFFFFF, true);
            } else {
                // Leave button
                int lX = this.x + 8;
                int lW = 160;
                int lH = 16;
                boolean lHover = mouseX >= lX && mouseX < lX + lW && mouseY >= actY && mouseY < actY + lH;
                graphics.fill(lX, actY, lX + lW, actY + lH, lHover ? 0xFFD87D12 : 0xFFE65100);
                String lText = "Leave Party";
                graphics.text(font, lText, lX + lW / 2 - font.width(lText) / 2, actY + 4, 0xFFFFFFFF, true);
            }

            // Member list
            int memY = this.y + 63;
            for (ClientPartyData.MemberEntry m : ClientPartyData.members()) {
                int rW = 160;
                int rH = 17;
                graphics.fill(cardX, memY, cardX + rW, memY + rH, CARD_BORDER);
                graphics.fill(cardX + 1, memY + 1, cardX + rW - 1, memY + rH - 1, CARD_FACE);

                // Player icon
                graphics.item(new ItemStack(Items.PLAYER_HEAD), cardX + 2, memY + 1);

                // Name
                String nameText = (m.isLeader() ? "★ " : "") + m.name();
                graphics.text(font, nameText, cardX + 20, memY + 2, m.isLeader() ? GOOD : LABEL, false);

                // Distance
                if (m.distance() >= 0) {
                    String distText = m.distance() + "m";
                    graphics.text(font, distText, cardX + rW - 4 - font.width(distText), memY + 2, HINT, false);
                }

                // Health bar
                int bX = cardX + 20;
                int bY = memY + 11;
                int bW = 134;
                int bH = 3;
                graphics.fill(bX, bY, bX + bW, bY + bH, TRACK_COLOR);
                float hpRatio = (m.maxHealth() > 0) ? Math.max(0f, Math.min(1f, m.health() / m.maxHealth())) : 0f;
                int fill = Math.round(bW * hpRatio);
                if (fill > 0) {
                    int barColor = hpRatio > 0.5f ? GOOD : hpRatio > 0.25f ? WARNING : BAD;
                    graphics.fill(bX, bY, bX + fill, bY + bH, barColor);
                }

                memY += 19;
            }
        }
    }

    private void drawThirst(GuiGraphicsExtractor graphics, Font font) {
        float hydration = ClientThirstData.getHydration();
        String value = String.format("%.1f / 20.0", hydration);

        // Main Hydration card
        int cardX = this.x + 8;
        int cardY = this.y + 17;
        int cardW = 160;
        int cardH = 34;
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, CARD_BORDER);
        graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + cardH - 1, CARD_FACE);

        graphics.item(new ItemStack(Items.POTION), cardX + 4, cardY + 8);
        graphics.text(font, "Hydration Level", cardX + 24, cardY + 4, LABEL, false);
        graphics.text(font, value, cardX + cardW - 5 - font.width(value), cardY + 4, VALUE, false);

        // Hydration bar
        int barX = cardX + 24;
        int barY = cardY + 15;
        int barW = 130;
        graphics.fill(barX, barY, barX + barW, barY + 6, TRACK_COLOR);
        int fill = Math.round(barW * Mth.clamp(hydration / 20f, 0f, 1f));
        if (fill > 0) {
            graphics.fill(barX, barY, barX + fill, barY + 6, COLD_BLUE);
        }

        // Status badge
        String status;
        int statusColor;
        if (hydration >= 16f) {
            status = "● Well Hydrated";
            statusColor = GOOD;
        } else if (hydration >= 8f) {
            status = "● Normal";
            statusColor = VALUE;
        } else if (hydration >= 1f) {
            status = "● Thirsty";
            statusColor = WARNING;
        } else {
            status = "● Dehydrated!";
            statusColor = BAD;
        }
        graphics.text(font, status, cardX + 24, cardY + 23, statusColor, false);

        // Guide entries (3 rows)
        int guideY = this.y + 54;
        drawGuideRow(graphics, font, Items.WATER_BUCKET, "Sip Fresh Water", "Crouch + Right-Click water sources", guideY);
        drawGuideRow(graphics, font, Items.CAMPFIRE, "Campfire Purification", "Boil water bowls to kill parasites", guideY + 26);
        drawGuideRow(graphics, font, Items.LEATHER, "Leather Flasks", "Craft flasks for multi-use travel water", guideY + 52);
    }

    private void drawTemperature(GuiGraphicsExtractor graphics, Font font) {
        float temp = ClientTempData.getTemperature();
        String tempStr = String.format("%+.1f°C", temp);

        // Main Temp card
        int cardX = this.x + 8;
        int cardY = this.y + 17;
        int cardW = 160;
        int cardH = 34;
        graphics.fill(cardX, cardY, cardX + cardW, cardY + cardH, CARD_BORDER);
        graphics.fill(cardX + 1, cardY + 1, cardX + cardW - 1, cardY + cardH - 1, CARD_FACE);

        graphics.item(new ItemStack(temp >= 0 ? Items.CAMPFIRE : Items.SNOWBALL), cardX + 4, cardY + 8);
        graphics.text(font, "Body Temperature", cardX + 24, cardY + 4, LABEL, false);
        graphics.text(font, tempStr, cardX + cardW - 5 - font.width(tempStr), cardY + 4, VALUE, false);

        // Temp scale bar (-10..+10)
        int barX = cardX + 24;
        int barY = cardY + 15;
        int barW = 130;
        graphics.fill(barX, barY, barX + barW, barY + 6, TRACK_COLOR);
        float norm = Mth.clamp((temp + 10f) / 20f, 0f, 1f);
        int fill = Math.round(barW * norm);
        int barColor = temp > 3f ? WARNING : temp < -3f ? COLD_BLUE : GOOD;
        if (fill > 0) {
            graphics.fill(barX, barY, barX + fill, barY + 6, barColor);
        }
        // Center equilibrium tick (0°C)
        graphics.fill(barX + 64, barY - 1, barX + 66, barY + 7, 0xFF444444);

        // Status badge
        String status;
        int statusColor;
        if (temp >= 7f) {
            status = "● Overheating! (Seek shade / ice)";
            statusColor = BAD;
        } else if (temp >= 3f) {
            status = "● Hot (Drink cold water)";
            statusColor = WARNING;
        } else if (temp <= -7f) {
            status = "● Freezing! (Warm by campfire)";
            statusColor = BAD;
        } else if (temp <= -3f) {
            status = "● Cold (Wear warm armor)";
            statusColor = COLD_BLUE;
        } else {
            status = "● Comfortable (Stable)";
            statusColor = GOOD;
        }
        graphics.text(font, status, cardX + 24, cardY + 23, statusColor, false);

        // Guide entries (3 rows)
        int guideY = this.y + 54;
        drawGuideRow(graphics, font, Items.CAMPFIRE, "Campfire & Torches", "Campfires add +9.5°C; wool armor +1.2°C/pc", guideY);
        drawGuideRow(graphics, font, Items.SNOWBALL, "Shade & Cold Water", "Cold water drunk provides -1.5°C cooling", guideY + 26);
        drawGuideRow(graphics, font, Items.CLOCK, "Seasonal Changes", "Winter cools biomes; Summer brings heat", guideY + 52);
    }

    private void drawGuideRow(GuiGraphicsExtractor graphics, Font font, Item icon, String header, String detail, int yPos) {
        int cardX = this.x + 8;
        int cardW = 160;
        int cardH = 24;
        graphics.fill(cardX, yPos, cardX + cardW, yPos + cardH, CARD_BORDER);
        graphics.fill(cardX + 1, yPos + 1, cardX + cardW - 1, yPos + cardH - 1, CARD_FACE);

        graphics.item(new ItemStack(icon), cardX + 4, yPos + 4);
        graphics.text(font, header, cardX + 24, yPos + 3, LABEL, false);
        graphics.text(font, detail, cardX + 24, yPos + 13, HINT, false);
    }

    private boolean jobHover(int i, double mx, double my) {
        int col = i % 2;
        int row = i / 2;
        int cx = this.x + 7 + col * 82;
        int cy = this.y + 18 + row * 27;
        int cw = 80;
        int ch = 25;
        return mx >= cx && mx < cx + cw && my >= cy && my < cy + ch;
    }

    private boolean onJobButton(double mx, double my) {
        if (this.kind != Kind.JOBS) {
            return false;
        }
        String current = ClientJobData.hasJob() ? ClientJobData.jobId() : "";
        for (int i = 0; i < JOB_IDS.length; i++) {
            if (!jobHover(i, mx, my)) {
                continue;
            }
            String job = JOB_IDS[i];
            boolean isCurrent = job.equalsIgnoreCase(current);
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.connection != null) {
                mc.player.connection.sendCommand(isCurrent ? "job leave" : "job join " + job);
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }
        return false;
    }

    private boolean onPartyButton(double mx, double my) {
        if (this.kind != Kind.PARTY) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.connection == null) {
            return false;
        }

        if (!ClientPartyData.inParty()) {
            int btnX = this.x + 8;
            int btnY = this.y + 43;
            int btnW = 160;
            int btnH = 20;
            if (mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH) {
                mc.player.connection.sendCommand("party create");
                mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        } else {
            int actY = this.y + 43;
            if (ClientPartyData.isLeader()) {
                int pvpX = this.x + 8;
                int pvpW = 76;
                int pvpH = 16;
                if (mx >= pvpX && mx < pvpX + pvpW && my >= actY && my < actY + pvpH) {
                    mc.player.connection.sendCommand("party pvp " + !ClientPartyData.pvpEnabled());
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }

                int disX = this.x + 92;
                int disW = 76;
                int disH = 16;
                if (mx >= disX && mx < disX + disW && my >= actY && my < actY + disH) {
                    mc.player.connection.sendCommand("party disband");
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            } else {
                int lX = this.x + 8;
                int lW = 160;
                int lH = 16;
                if (mx >= lX && mx < lX + lW && my >= actY && my < actY + lH) {
                    mc.player.connection.sendCommand("party leave");
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return true;
                }
            }
        }
        return false;
    }

    private boolean onSkillPlusButton(double mx, double my) {
        if (this.kind != Kind.SKILLS) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        int pts = mc.player.experienceLevel;
        if (pts <= 0 && !mc.player.getAbilities().instabuild) {
            return false;
        }
        Map<String, Integer> levels = ClientSkillData.knownLevels();
        for (int i = 0; i < SKILL_IDS.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int cx = this.x + 7 + col * 82;
            int cy = this.y + 18 + row * 19;
            int btnX = cx + 80 - 15;
            int btnY = cy + 3;
            int btnW = 12;
            int btnH = 12;
            boolean clickedPlus = mx >= btnX && mx < btnX + btnW && my >= btnY && my < btnY + btnH;
            boolean clickedCard = mx >= cx && mx < cx + 80 && my >= cy && my < cy + 18;
            if (clickedPlus || clickedCard) {
                int lvl = levels.getOrDefault(SKILL_IDS[i], 0);
                if (lvl < 30) {
                    mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    ClientSkillData.onSkillUp(SKILL_IDS[i], lvl + 1);
                    net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                            new dev.jmiahman.hearthwind.survival.SkillUpPayload(SKILL_IDS[i], lvl + 1));
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        TabStrip.Tab tab = TabStrip.clicked(event.x(), event.y(), this.x, this.y);
        if (tab != null && tab != tab()) {
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            TabStrip.open(tab);
            return true;
        }
        if (onSkillPlusButton(event.x(), event.y())) {
            return true;
        }
        if (onJobButton(event.x(), event.y())) {
            return true;
        }
        if (onPartyButton(event.x(), event.y())) {
            return true;
        }
        if (within(5, 5, 11, 10, event.x(), event.y())) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (minecraft.player != null) {
                minecraft.setScreenAndShow(new InventoryScreen(minecraft.player));
            }
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (Minecraft.getInstance().options.keyInventory.matches(event)
                || NutrientsKey.openNutrients.matches(event)
                || NutrientsKey.openSkills.matches(event)
                || NutrientsKey.openJobs.matches(event)
                || NutrientsKey.openParty.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean within(int bx, int by, int bw, int bh, double px, double py) {
        px -= this.x;
        py -= this.y;
        return px >= bx && px < bx + bw && py >= by && py < by + bh;
    }
}
