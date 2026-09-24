package dev.jmiahman.hearthwind.client;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/**
 * Pixel-exact rebuild of NutritionZ 1.0.11's {@code NutritionScreen}
 * (the version Aged 3.1.2 ships), on the 26.2 GuiGraphicsExtractor API.
 *
 * <p>Panel 176x142 centred, title at y+7, five rows at 23 px pitch with a
 * 141x5 bar, hover tooltips on the 31 px zones at x+27 / x+137 and the
 * 11x10 back arrow at (x+5, y+5). All coordinates come from
 * {@code assets/hearthwind/textures/gui/nutritionz_icons.png}.
 */
@Environment(EnvType.CLIENT)
public class NutrientsScreen extends Screen {
    public static final Identifier ICONS =
            Identifier.fromNamespaceAndPath("hearthwind", "textures/gui/nutritionz_icons.png");

    private static final int TITLE_COLOR = 0xFF3F3F3F;
    private static final int INK = 0xFF3F3F3F;

    private final List<ItemStack> nutritionItems = new ArrayList<>();
    private int x;
    private int y;

    public NutrientsScreen() {
        super(Component.translatable("screen.nutritionz"));
        var cfg = dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig.get().diet;
        this.nutritionItems.add(stack(cfg.carbohydrateItemId));
        this.nutritionItems.add(stack(cfg.proteinItemId));
        this.nutritionItems.add(stack(cfg.fatItemId));
        this.nutritionItems.add(stack(cfg.vitaminItemId));
        this.nutritionItems.add(stack(cfg.mineralItemId));
    }

    private static ItemStack stack(String id) {
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) {
            return new ItemStack(Items.AIR);
        }
        Item item = BuiltInRegistries.ITEM.getOptional(identifier).orElse(Items.AIR);
        return new ItemStack(item);
    }

    @Override
    protected void init() {
        super.init();
        this.x = this.width / 2 - (176 / 2);
        this.y = this.height / 2 - (141 / 2);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        Font font = Minecraft.getInstance().font;

        graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.x, this.y, 0f, 0f,
                176, 142, 256, 256, 0xFFFFFFFF);
        String title = this.title.getString();
        graphics.text(font, title, this.x + 176 / 2 - font.width(title) / 2, this.y + 7,
                TITLE_COLOR, false);

        int extraY = 0;
        int extraBarY = 0;
        int max = ClientDietData.max();
        for (int i = 0; i < 5; i++) {
            graphics.item(this.nutritionItems.get(i), this.x + 7, this.y + 25 + extraY);
            graphics.text(font,
                    Component.translatable("screen.nutritionz."
                            + dev.jmiahman.hearthwind.survival.HearthwindSurvivalDiet.NUTRIENT_NAMES[i]),
                    this.x + 28, this.y + 26 + extraY, INK, false);

            graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.x + 27, this.y + 36 + extraY,
                    0f, 206f + extraBarY, 141, 5, 256, 256, 0xFFFFFFFF);
            int level = ClientDietData.get(i);
            if (level > 0) {
                int fill = 140 * level / max;
                if (fill > 0) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.x + 27, this.y + 36 + extraY,
                            0f, 211f + extraBarY, fill, 5, 256, 256, 0xFFFFFFFF);
                }
                for (int tick = 1; tick < 10; tick++) {
                    int tickX = this.x + 27 + tick * 14;
                    graphics.fill(tickX, this.y + 36 + extraY,
                            tickX + 1, this.y + 41 + extraY, 0x66000000);
                }
            }
            graphics.text(font,
                    Component.translatable("screen.nutritionz.nutritionValue", level, max),
                    this.x + 127, this.y + 26 + extraY, INK, false);

            List<Component> tooltips = new ArrayList<>();
            if (isPointWithinBounds(27, 36 + extraY, 31, 5, mouseX, mouseY)) {
                tooltips.addAll(ClientNutritionData.effectTooltip(i, false));
            } else if (isPointWithinBounds(137, 36 + extraY, 31, 5, mouseX, mouseY)) {
                tooltips.addAll(ClientNutritionData.effectTooltip(i, true));
            }
            if (!tooltips.isEmpty()) {
                graphics.setComponentTooltipForNextFrame(font, tooltips, mouseX, mouseY);
            }
            extraY += 23;
            extraBarY += 10;
        }

        boolean arrowHovered = isPointWithinBounds(5, 5, 11, 10, mouseX, mouseY);
        graphics.blit(RenderPipelines.GUI_TEXTURED, ICONS, this.x + 5, this.y + 5,
                arrowHovered ? 187f : 176f, 0f, 11, 10, 256, 256, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && isPointWithinBounds(5, 5, 11, 10, event.x(), event.y())) {
            Minecraft minecraft = Minecraft.getInstance();
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
            if (minecraft.player != null) {
                minecraft.setScreenAndShow(new InventoryScreen(minecraft.player));
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (NutrientsKey.openSkills.matches(event)) {
            TabStrip.open(TabStrip.Tab.SKILLS);
            return true;
        }
        if (NutrientsKey.openJobs.matches(event)) {
            TabStrip.open(TabStrip.Tab.JOBS);
            return true;
        }
        if (NutrientsKey.openParty.matches(event)) {
            TabStrip.open(TabStrip.Tab.PARTY);
            return true;
        }
        if (mc.options.keyInventory.matches(event)
                || NutrientsKey.openNutrients.matches(event)
                || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isPointWithinBounds(int bx, int by, int bw, int bh, double pointX, double pointY) {
        double relX = pointX - this.x;
        double relY = pointY - this.y;
        return relX >= bx - 1 && relX < bx + bw + 1 && relY >= by - 1 && relY < by + bh + 1;
    }
}
