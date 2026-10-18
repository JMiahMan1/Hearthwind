package folk.sisby.antique_atlas.gui;

import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.antique_atlas.gui.core.Component;
import folk.sisby.antique_atlas.gui.core.ScrollBoxComponent;
import folk.sisby.antique_atlas.gui.core.ToggleButtonRadioGroup;
import folk.sisby.antique_atlas.reloader.MarkerTextures;
import folk.sisby.antique_atlas.util.ColorUtil;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.CharacterEvent;

public class MarkerModal extends Component {
   protected WorldSummary summary;
   protected RegistryAccess manager;
   protected Landmark baseLandmark = null;
   protected MarkerTexture selectedTexture = MarkerTexture.DEFAULT;
   protected DyeColor selectedColor = DyeColor.WHITE;
   public static final int BUTTON_WIDTH = 80;
   public static final int BUTTON_SPACING = 8;
   public static final int TYPE_SPACING = 1;
   protected Button btnDone;
   protected Button btnCancel;
   protected EditBox textField;
   protected ScrollBoxComponent textureScrollBox;
   protected ToggleButtonRadioGroup<TexturePreviewButton<MarkerTexture>> textureRadioGroup;
   protected ScrollBoxComponent colorScrollBox;
   protected ToggleButtonRadioGroup<TexturePreviewButton<net.minecraft.world.item.DyeColor>> colorRadioGroup;
   protected Map<MarkerTexture, TexturePreviewButton<MarkerTexture>> textureButtons = new LinkedHashMap<>();
   protected Map<net.minecraft.world.item.DyeColor, TexturePreviewButton<net.minecraft.world.item.DyeColor>> colorButtons = new LinkedHashMap<>();
   protected final List<MarkerModal.IMarkerTypeSelectListener> markerListeners = new ArrayList<>();

   void setMarkerData(WorldSummary summary, RegistryAccess manager, Landmark baseLandmark) {
      this.summary = summary;
      this.manager = manager;
      this.baseLandmark = baseLandmark;
      this.selectedColor = Arrays.stream(DyeColor.values())
         .filter(d -> baseLandmark.contains(LandmarkComponentTypes.COLOR) && d.getTextureDiffuseColor() == (Integer)baseLandmark.get(LandmarkComponentTypes.COLOR))
         .findAny()
         .orElse(DyeColor.WHITE);
      this.selectedTexture = MarkerTextures.getInstance().fromLandmark(baseLandmark);
      if (!this.selectedTexture.keyId().getPath().startsWith("custom/")) {
         this.selectedTexture = this.textureButtons.keySet().stream().findFirst().orElse(MarkerTexture.DEFAULT);
      }

      if (this.colorRadioGroup != null) {
         this.updateSelected();
      }
   }

   void addMarkerListener(MarkerModal.IMarkerTypeSelectListener listener) {
      this.markerListeners.add(listener);
   }

   protected void updateSelected() {
      this.colorRadioGroup.setSelectedButton(this.colorButtons.get(this.selectedColor));
      this.textureRadioGroup.setSelectedButton(this.textureButtons.get(this.selectedTexture));
   }

   @Override
   public void init() {
      this.removeAllChildren();
      super.init();
      this.addRenderableWidget(
         this.btnDone = Button.builder(
               net.minecraft.network.chat.Component.translatable("gui.done"),
               button -> {
                  MutableComponent label = net.minecraft.network.chat.Component.literal(this.textField.getValue());
                  WorldLandmarks landmarks = this.summary.landmarks();
                  if (landmarks != null) {
                     landmarks.remove(this.baseLandmark.owner(), this.baseLandmark.id());
                     landmarks.put(
                        WorldAtlasData.copyLandmarkWith(
                           this.baseLandmark,
                           this.selectedTexture
                              .keyId()
                              .withSuffix(
                                 "/"
                                    + this.selectedColor.getName()
                                    + "/"
                                    + ((BlockPos)this.baseLandmark.get(LandmarkComponentTypes.POS)).getX()
                                    + "/"
                                    + ((BlockPos)this.baseLandmark.get(LandmarkComponentTypes.POS)).getZ()
                              ),
                           copy -> {
                              Item item = this.manager.lookupOrThrow(Registries.ITEM).getValue(this.selectedTexture.item());
                              if (item != null && !item.getDefaultInstance().isEmpty()) {
                                 copy.set(LandmarkComponentTypes.STACK, item.getDefaultInstance().copy());
                              }

                              copy.set(LandmarkComponentTypes.COLOR, this.selectedColor.getTextureDiffuseColor());
                              copy.set(LandmarkComponentTypes.NAME, label);
                           }
                        )
                     );
                  }

                  ((AtlasScreen)this.getParent()).updateBookmarkerList();
                  LocalPlayer player = Minecraft.getInstance().player;
                  if (player != null) {
                     Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_WORK_CARTOGRAPHER, 1.0F));
                  }

                  this.closeChild();
               }
            )
            .bounds(this.width / 2 - 80 - 4, this.height / 2 + 70, 80, 20)
            .build()
      );
      this.addRenderableWidget(
         this.btnCancel = Button.builder(net.minecraft.network.chat.Component.translatable("gui.cancel"), button -> this.closeChild())
            .bounds(this.width / 2 + 4, this.height / 2 + 70, 80, 20)
            .build()
      );
      int var10004 = (this.width - 200) / 2;
      this.textField = new EditBox(
         Minecraft.getInstance().font,
         var10004,
         this.height / 2 - 65,
         200,
         20,
         net.minecraft.network.chat.Component.translatable("gui.antique_atlas.marker.label")
      );
      this.textField.setEditable(true);
      this.textField.setCanLoseFocus(true);
      this.textField.setFocused(true);
      this.textField.setHint(net.minecraft.network.chat.Component.translatable("gui.antique_atlas.marker.label"));
      this.textField
         .setValue(
            ((net.minecraft.network.chat.Component)this.baseLandmark
                  .getOrDefault(LandmarkComponentTypes.NAME, net.minecraft.network.chat.Component.empty()))
               .getString()
         );
      this.textureScrollBox = new ScrollBoxComponent(false, 35);
      this.addChild(this.textureScrollBox);
      int typeCount = (int)MarkerTextures.getInstance().asMap().values().stream().filter(t -> t.keyId().getPath().startsWith("custom/")).count();
      int typesOnScreen = Math.min(typeCount, 7);
      int typeScrollWidth = typesOnScreen * 35 - 1;
      this.textureScrollBox.getViewport().setSize(typeScrollWidth, 35);
      this.textureScrollBox.setGuiCoords((this.width - typeScrollWidth) / 2, this.height / 2 - 35);
      this.textureRadioGroup = new ToggleButtonRadioGroup<>();
      this.textureRadioGroup.addListener(button -> {
         this.selectedTexture = button.getValue();

         for (MarkerModal.IMarkerTypeSelectListener listener : this.markerListeners) {
            listener.onSelectMarkerType(button.getValue());
         }
      });
      int contentX = 0;

      for (MarkerTexture texture : MarkerTextures.getInstance().asMap().values()) {
         if (texture.keyId().getPath().startsWith("custom/")) {
            if (this.selectedTexture == MarkerTexture.DEFAULT) {
               this.selectedTexture = texture;
            }

            TexturePreviewButton<MarkerTexture> markerGui = new MarkerPreviewButton(texture, ColorUtil.componentsFromRgb(this.selectedColor.getFireworkColor()));
            this.textureButtons.put(texture, markerGui);
            this.textureRadioGroup.addButton(markerGui);
            this.textureScrollBox.getViewport().addContent(markerGui).setRelativeX(contentX);
            contentX += 35;
         }
      }

      this.colorScrollBox = new ScrollBoxComponent(false, 35);
      this.addChild(this.colorScrollBox);
      int colorsOnScreen = Math.min(DyeColor.values().length, 7);
      int colorScrollWidth = colorsOnScreen * 35 - 1;
      this.colorScrollBox.getViewport().setSize(colorScrollWidth, 35);
      this.colorScrollBox.setGuiCoords((this.width - colorScrollWidth) / 2, this.height / 2 + 10);
      this.colorRadioGroup = new ToggleButtonRadioGroup<>();
      this.colorRadioGroup.addListener(button -> {
         this.selectedColor = (DyeColor)button.getValue();

         for (TexturePreviewButton<MarkerTexture> preview : this.textureRadioGroup) {
            preview.reTint(ColorUtil.componentsFromRgb(this.selectedColor.getFireworkColor()));
         }
      });
      int colorContentX = 0;

      for (DyeColor color : DyeColor.values()) {
         TexturePreviewButton<net.minecraft.world.item.DyeColor> colorGui = new TexturePreviewButton<>(
            color, BookmarkButton.TEXTURE_LEFT, 24, 18, 18, ColorUtil.componentsFromRgb(color.getFireworkColor())
         );
         this.colorButtons.put(color, colorGui);
         this.colorRadioGroup.addButton(colorGui);
         this.colorScrollBox.getViewport().addContent(colorGui).setRelativeX(colorContentX);
         colorContentX += 35;
      }

      this.updateSelected();
   }

   @Override
   public void closeChild() {
      super.closeChild();
      if (this.textureScrollBox != null) {
         this.textureScrollBox.closeChild();
      }

      if (this.colorScrollBox != null) {
         this.colorScrollBox.closeChild();
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
      return super.mouseClicked(event, bl) || this.textField.mouseClicked(event, bl);
   }

   @Override
   public boolean keyPressed(KeyEvent event) {
      return super.keyPressed(event) || this.textField.keyPressed(event);
   }

   @Override
   public boolean charTyped(CharacterEvent event) {
      return super.charTyped(event) || this.textField.charTyped(event);
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
      folk.sisby.antique_atlas.util.AtlasCanvas canvas = new folk.sisby.antique_atlas.util.GuiAtlasCanvas(context);
      this.extractBackground(context, mouseX, mouseY, partialTick);
      this.drawCentered(context, net.minecraft.network.chat.Component.translatable("gui.antique_atlas.marker.label"), this.height / 2 - 80, 14540253, true);
      this.btnCancel.extractRenderState(context, mouseX, mouseY, partialTick);
      this.btnDone.extractRenderState(context, mouseX, mouseY, partialTick);
      this.textField.extractRenderState(context, mouseX, mouseY, partialTick);
      context.fillGradient(
         this.textureScrollBox.getGuiX() + 1,
         this.textureScrollBox.getGuiY() + 1,
         this.textureScrollBox.getGuiX() + this.textureScrollBox.getWidth(),
         this.textureScrollBox.getGuiY() + this.textureScrollBox.getHeight(),
         -2012213232,
         -1727000560
      );
      context.fillGradient(
         this.colorScrollBox.getGuiX() + 1,
         this.colorScrollBox.getGuiY() + 1,
         this.colorScrollBox.getGuiX() + this.colorScrollBox.getWidth(),
         this.colorScrollBox.getGuiY() + this.colorScrollBox.getHeight(),
         -2012213232,
         -1727000560
      );
      super.extractRenderState(context, mouseX, mouseY, partialTick);
   }

   public interface IMarkerTypeSelectListener {
      void onSelectMarkerType(MarkerTexture var1);
   }
}
