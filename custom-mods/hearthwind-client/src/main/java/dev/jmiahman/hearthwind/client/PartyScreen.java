package dev.jmiahman.hearthwind.client;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Party Addon-inspired party screen on the shared Aged panel (200x215).
 * Left column lists our synced party members (player head + name + distance
 * + health bar); right column shows party info and the leave/disband/PvP
 * actions wired to the existing {@code /party} commands. Invitations are not
 * part of our party subsystem, so PartyAddon's invitation column is omitted
 * (deviation noted in docs/PLAYER_CHANGES.md).
 */
@Environment(EnvType.CLIENT)
public class PartyScreen extends AgedPanelScreen {

    private static final int CARD_BORDER = 0xFF2F2F2F;
    private static final int CARD_FACE = 0xFF6E6E6E;
    private static final int CARD_FACE_HOVER = 0xFF7A7A7A;
    private static final int TRACK = 0xFF373737;
    private static final int HEALTH = 0xFF5FBF4F;

    public PartyScreen() {
        super(Component.translatable("screen.hearthwind.party"));
    }

    @Override
    protected TabStrip.Tab activeTab() {
        return TabStrip.Tab.PARTY;
    }

    @Override
    protected void drawContent(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        String playerName = mc.player != null ? mc.player.getName().getString() : "Player";
        Component title = Component.translatable("screen.hearthwind.party.title", playerName);
        graphics.text(font, title, this.x + 100 - font.width(title) / 2, this.y + 7, INK, false);

        graphics.text(font, "Player List", this.x + 5, this.y + 15, INK, false);
        graphics.text(font, "Info", this.x + 195 - font.width("Info"), this.y + 15, INK, false);

        if (!ClientPartyData.inParty()) {
            drawNotInParty(graphics, font, mouseX, mouseY);
            return;
        }

        // --- member list (left column) ---
        List<ClientPartyData.MemberEntry> members = ClientPartyData.members();
        for (int i = 0; i < members.size() && i < 8; i++) {
            ClientPartyData.MemberEntry member = members.get(i);
            int ry = this.y + 42 + i * 19;
            graphics.fill(this.x + 5, ry, this.x + 95, ry + 17, CARD_BORDER);
            graphics.fill(this.x + 6, ry + 1, this.x + 94, ry + 16, CARD_FACE);
            graphics.item(new ItemStack(Items.PLAYER_HEAD), this.x + 6, ry + 1);

            String name = (member.isLeader() ? "* " : "") + member.name();
            if (font.width(name) > 52) {
                name = font.plainSubstrByWidth(name, 50) + "..";
            }
            graphics.text(font, name, this.x + 24, ry + 1, 0xFFFFFFFF, false);

            if (member.distance() >= 0) {
                String dist = member.distance() + "m";
                graphics.text(font, dist, this.x + 94 - font.width(dist), ry + 1, 0xFFB0B0B0, false);
            }

            float ratio = member.maxHealth() > 0
                    ? Math.max(0f, Math.min(1f, member.health() / member.maxHealth()))
                    : 0f;
            graphics.fill(this.x + 24, ry + 12, this.x + 91, ry + 14, TRACK);
            int fill = Math.round(67 * ratio);
            if (fill > 0) {
                graphics.fill(this.x + 24, ry + 12, this.x + 24 + fill, ry + 14, HEALTH);
            }
        }

        // --- info section (right column) ---
        String partyName = "* " + ClientPartyData.partyName();
        graphics.text(font, trim(font, partyName, 80), this.x + 109, this.y + 30, 0xFFFFFFFF, false);
        graphics.text(font, "Members: " + members.size(), this.x + 109, this.y + 42, INK, false);
        graphics.text(font, ClientPartyData.isLeader() ? "Role: Leader" : "Role: Member",
                this.x + 109, this.y + 54, INK, false);
        graphics.text(font, ClientPartyData.pvpEnabled() ? "PvP: ON" : "PvP: OFF",
                this.x + 109, this.y + 66,
                ClientPartyData.pvpEnabled() ? 0xFFC62828 : 0xFF2E7D32, false);

        graphics.text(font, "Actions", this.x + 195 - font.width("Actions"), this.y + 95, INK, false);
        int actionY = this.y + 110;
        if (ClientPartyData.isLeader()) {
            boolean pvp = ClientPartyData.pvpEnabled();
            boolean pvpHover = isOver(109, 110, 86, 18, mouseX, mouseY);
            drawButton(graphics, font, this.x + 109, actionY, 86, pvp ? "PvP: ON" : "PvP: OFF",
                    pvp ? 0xFFC62828 : 0xFF2E7D32, pvpHover);
            boolean disbandHover = isOver(109, 132, 86, 18, mouseX, mouseY);
            drawButton(graphics, font, this.x + 109, actionY + 22, 86, "Disband",
                    0xFFD32F2F, disbandHover);
        } else {
            boolean leaveHover = isOver(109, 110, 86, 18, mouseX, mouseY);
            drawButton(graphics, font, this.x + 109, actionY, 86, "Leave Party",
                    0xFFE65100, leaveHover);
        }
    }

    private void drawNotInParty(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        graphics.fill(this.x + 5, this.y + 42, this.x + 95, this.y + 60, CARD_BORDER);
        graphics.fill(this.x + 6, this.y + 43, this.x + 94, this.y + 59, CARD_FACE);
        graphics.text(font, "No party yet", this.x + 12, this.y + 48, 0xFFE0E0E0, false);

        boolean hover = isOver(5, 68, 90, 20, mouseX, mouseY);
        drawButton(graphics, font, this.x + 5, this.y + 68, 90, "Create Party",
                0xFF388E3C, hover);

        graphics.text(font, "Party up to share XP and", this.x + 109, this.y + 30, INK, false);
        graphics.text(font, "see your allies' health", this.x + 109, this.y + 42, INK, false);
        graphics.text(font, "and distance on screen.", this.x + 109, this.y + 54, INK, false);
    }

    private void drawButton(GuiGraphicsExtractor graphics, Font font, int bx, int by, int bw,
            String label, int face, boolean hover) {
        graphics.fill(bx, by, bx + bw, by + 18, 0xFF1E1E1E);
        graphics.fill(bx + 1, by + 1, bx + bw - 1, by + 17, hover ? brighten(face) : face);
        graphics.text(font, label, bx + bw / 2 - font.width(label) / 2, by + 5, 0xFFFFFFFF, false);
    }

    private static int brighten(int argb) {
        int r = Math.min(255, ((argb >> 16) & 0xFF) + 24);
        int g = Math.min(255, ((argb >> 8) & 0xFF) + 24);
        int b = Math.min(255, (argb & 0xFF) + 24);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static String trim(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        return font.plainSubstrByWidth(text, maxWidth - font.width("..")) + "..";
    }

    @Override
    protected boolean onContentClick(MouseButtonEvent event) {
        if (event.button() != 0) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.player.connection == null) {
            return false;
        }
        if (!ClientPartyData.inParty()) {
            if (isOver(5, 68, 90, 20, event.x(), event.y())) {
                this.click();
                mc.player.connection.sendCommand("party create");
                return true;
            }
            return false;
        }
        if (ClientPartyData.isLeader()) {
            if (isOver(109, 110, 86, 18, event.x(), event.y())) {
                this.click();
                mc.player.connection.sendCommand("party pvp " + !ClientPartyData.pvpEnabled());
                return true;
            }
            if (isOver(109, 132, 86, 18, event.x(), event.y())) {
                this.click();
                mc.player.connection.sendCommand("party disband");
                return true;
            }
        } else if (isOver(109, 110, 86, 18, event.x(), event.y())) {
            this.click();
            mc.player.connection.sendCommand("party leave");
            return true;
        }
        return false;
    }
}
