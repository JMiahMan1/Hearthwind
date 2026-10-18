package ht.treechop.client.gui.screen;

import ht.treechop.client.Client;
import ht.treechop.client.gui.element.ButtonGui;
import ht.treechop.client.gui.element.EmptyGui;
import ht.treechop.client.gui.element.LabeledGui;
import ht.treechop.client.gui.element.NestedGui;
import ht.treechop.client.gui.element.RowsGui;
import ht.treechop.client.gui.element.ToggleGui;
import ht.treechop.client.gui.util.GUIUtil;
import ht.treechop.client.gui.util.Sprite;
import ht.treechop.client.gui.widget.StickyWidget;
import ht.treechop.client.gui.widget.ToggleWidget;
import ht.treechop.common.config.ConfigHandler;
import ht.treechop.common.settings.Setting;
import ht.treechop.common.settings.SettingsField;
import ht.treechop.common.settings.SneakBehavior;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClientSettingsScreen extends Screen {
   private static final int ROW_SEPARATION = 1;
   private static final boolean IS_PAUSE_SCREEN = true;
   private static final int SPACE_ABOVE_AND_BELOW_LIST = 10;
   private static final int MIN_HEIGHT = 104;
   private static final int TEXT_HEIGHT = 9;
   private final Screen parent;
   protected RowsGui optionsList;
   private Button doneButton;
   private int optionsPage = 0;
   private boolean needToRebuild = false;

   public ClientSettingsScreen(Screen parent) {
      super(Component.translatable("treechop.gui.settings.title", new Object[]{"HT's TreeChop"}));
      this.parent = parent;
   }

   public ClientSettingsScreen() {
      this(null);
   }

   protected void init() {
      super.init();
      this.rebuild();
   }

   public void onClose() {
      this.minecraft.gui.setScreen(this.parent);
   }

   private void rebuild() {
      List<NestedGui> optionRows = (List<NestedGui>)(this.optionsPage == 0 ? this.makePageOne() : this.makePageTwo());
      this.addBufferRows(optionRows);
      optionRows.add(
         new ButtonGui(
            this.optionsPage == 0 ? Sprite.PAGE_ONE : Sprite.PAGE_TWO,
            this.optionsPage == 0 ? Sprite.HIGHLIGHTED_PAGE_ONE : Sprite.HIGHLIGHTED_PAGE_TWO,
            () -> {
               this.needToRebuild = true;
               this.optionsPage = this.optionsPage == 0 ? 1 : 0;
            }
         )
      );
      this.optionsList = (RowsGui)this.addRenderableWidget(new RowsGui(1, optionRows));
      this.placeOptionsList();
      int doneButtonWidth = 200;
      this.doneButton = (Button)this.addRenderableWidget(
         new Builder(Component.translatable("gui.done"), button -> this.onClose())
            .bounds((this.width - 200) / 2, this.getDoneButtonTop(), 200, 20)
            .build()
      );
      int titleTop = this.optionsList.getY() - 10 - 8;
      this.addRenderableWidget(new StringWidget(0, titleTop, this.width, 9, this.title, this.font));
   }

   private void placeOptionsList() {
      int top = 32;
      int bottom = this.height - 32;
      int middle = (top + bottom) / 2;
      int listTop = middle - this.optionsList.getHeight() / 2;
      int listBottom = middle + this.optionsList.getHeight() / 2;
      this.optionsList.setBox(0, listTop, this.width, listBottom - listTop);
   }

   private void addBufferRows(List<NestedGui> rows) {
      int missingHeight = 104 - RowsGui.getHeightForRows(rows, 1) - 2;
      if (missingHeight > 0) {
         rows.add(0, new EmptyGui(0, Math.floorDiv(missingHeight, 2)));
         rows.add(new EmptyGui(0, (int)Math.ceil(missingHeight / 2.0F)));
      }
   }

   private List<NestedGui> makePageOne() {
      LinkedList<NestedGui> optionRows = new LinkedList<>();
      optionRows.add(
         new LabeledGui(
            this.font,
            Component.translatable("treechop.gui.settings.label.chopping"),
            this.makeToggleSettingRow(SettingsField.CHOPPING, "treechop.gui.settings.tooltip.chopping")
         )
      );
      optionRows.add(
         new LabeledGui(
            this.font,
            Component.translatable("treechop.gui.settings.label.sneaking_inverts_chopping"),
            new ToggleGui(
               () -> Client.getChopSettings().setSneakBehavior(this.getNextSneakBehavior()),
               () -> ToggleWidget.State.of(
                  Client.getChopSettings().getSneakBehavior() == SneakBehavior.INVERT_CHOPPING,
                  this.isSettingPermitted(SettingsField.SNEAK_BEHAVIOR, this.getNextSneakBehavior())
               ),
               (Supplier<net.minecraft.network.chat.Component>)(this::getSneakCycleTooltip)
            )
         )
      );
      optionRows.add(
         new LabeledGui(
            this.font,
            Component.translatable("treechop.gui.settings.label.only_chop_trees_with_leaves"),
            this.makeToggleSettingRow(SettingsField.TREES_MUST_HAVE_LEAVES, "treechop.gui.settings.tooltip.only_chop_trees_with_leaves")
         )
      );
      if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
         optionRows.add(
            new LabeledGui(
               this.font,
               Component.translatable("treechop.gui.settings.label.chop_in_creative_mode"),
               this.makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE, "treechop.gui.settings.tooltip.chop_in_creative_mode")
            )
         );
      }

      return optionRows;
   }

   private LinkedList<NestedGui> makePageTwo() {
      LinkedList<NestedGui> optionRows = new LinkedList<>();
      optionRows.add(
         new LabeledGui(
            this.font,
            Component.translatable("treechop.gui.settings.label.chop_in_creative_mode"),
            this.makeToggleSettingRow(SettingsField.CHOP_IN_CREATIVE_MODE, "treechop.gui.settings.tooltip.chop_in_creative_mode")
         )
      );
      optionRows.add(
         new LabeledGui(
            this.font,
            Component.translatable("treechop.gui.settings.label.chopping_indicator"),
            new ToggleGui(
               () -> Client.setChoppingIndicatorVisibility(!Client.isChoppingIndicatorEnabled()),
               () -> ToggleWidget.State.of(Client.isChoppingIndicatorEnabled(), true),
               (Supplier<net.minecraft.network.chat.Component>)(() -> Component.translatable(
                  "treechop.gui.settings.tooltip.chopping_indicator" + (Client.isChoppingIndicatorEnabled() ? ".on" : ".off")
               ))
            )
         )
      );
      optionRows.add(
         new LabeledGui(
            this.font,
            Component.translatable("treechop.gui.settings.label.feedback_messages"),
            new ToggleGui(
               () -> ConfigHandler.CLIENT.showFeedbackMessages.set(!(Boolean)ConfigHandler.CLIENT.showFeedbackMessages.get()),
               () -> ToggleWidget.State.of((Boolean)ConfigHandler.CLIENT.showFeedbackMessages.get(), true),
               (Supplier<net.minecraft.network.chat.Component>)(() -> Component.translatable(
                  "treechop.gui.settings.tooltip.feedback_messages" + (ConfigHandler.CLIENT.showFeedbackMessages.get() ? ".on" : ".off")
               ))
            )
         )
      );
      optionRows.add(
         new LabeledGui(
            this.font,
            Component.translatable("treechop.gui.settings.label.tooltips"),
            new ToggleGui(
               () -> ConfigHandler.CLIENT.showTooltips.set(!(Boolean)ConfigHandler.CLIENT.showTooltips.get()),
               () -> ToggleWidget.State.of((Boolean)ConfigHandler.CLIENT.showTooltips.get(), true),
               (Supplier<net.minecraft.network.chat.Component>)(() -> Component.translatable("treechop.gui.settings.tooltip.tooltips"))
            )
         )
      );
      return optionRows;
   }

   private Component getSneakCycleTooltip() {
      switch (Client.getChopSettings().getSneakBehavior()) {
         case INVERT_CHOPPING:
            SettingsField field = SettingsField.CHOPPING;
            String enablesOrDisablesLangKey = Client.getChopSettings().get(field, Boolean.class)
               ? "treechop.gui.settings.tooltip.sneaking_enables_x"
               : "treechop.gui.settings.tooltip.sneaking_disables_x";
            return Component.translatable(enablesOrDisablesLangKey, new Object[]{field.getFancyName()});
         default:
            return Component.translatable("treechop.gui.settings.tooltip.sneaking_does_nothing");
      }
   }

   private SneakBehavior getNextSneakBehavior() {
      return Client.getChopSettings().getSneakBehavior() == SneakBehavior.NONE ? SneakBehavior.INVERT_CHOPPING : SneakBehavior.NONE;
   }

   private ToggleGui makeToggleSettingRow(SettingsField field, String tooltipLangKey) {
      return new ToggleGui(
         () -> Client.getChopSettings().set(field, !Client.getChopSettings().get(field, Boolean.class)),
         () -> ToggleWidget.State.of(
            Client.getChopSettings().get(field, Boolean.class),
            Client.getServerPermissions().isPermitted(new Setting(field, !Client.getChopSettings().get(field, Boolean.class)))
         ),
         (Supplier<net.minecraft.network.chat.Component>)(() -> Component.translatable(
            tooltipLangKey + (Client.getChopSettings().get(field, Boolean.class) ? ".on" : ".off")
         ))
      );
   }

   private boolean isSettingPermitted(SettingsField field, Object value) {
      return Client.getServerPermissions().isPermitted(new Setting(field, value));
   }

   private StickyWidget.State makeStickyWidgetState(SettingsField field, Object value) {
      return StickyWidget.State.of(Client.getChopSettings().get(field) == value, Client.getServerPermissions().isPermitted(new Setting(field, value)));
   }

   public void extractRenderState(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTicks) {
      if (this.needToRebuild) {
         this.clearWidgets();
         this.rebuild();
         this.needToRebuild = false;
      }

      this.doneButton.setY(this.getDoneButtonTop());
      super.extractRenderState(gui, mouseX, mouseY, partialTicks);
      if ((Boolean)ConfigHandler.CLIENT.showTooltips.get()) {
         GUIUtil.renderTooltip(gui);
      }
   }

   public boolean isPauseScreen() {
      return true;
   }

   protected int getDoneButtonTop() {
      return this.optionsList.getBottom() + 10;
   }
}
