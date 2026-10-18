/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  folk.sisby.surveyor.PlayerSummary
 *  folk.sisby.surveyor.client.SurveyorClient
 *  folk.sisby.surveyor.landmark.Landmark
 *  folk.sisby.surveyor.landmark.WorldLandmarks
 *  folk.sisby.surveyor.landmark.component.LandmarkComponentTypes
 *  folk.sisby.surveyor.util.RegionPos
 *  net.minecraft.ChatFormatting
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.resources.sounds.SimpleSoundInstance
 *  net.minecraft.client.resources.sounds.SoundInstance
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.FormattedText
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.Identifier
 *  net.minecraft.server.level.ColumnPos
 *  net.minecraft.server.packs.metadata.MetadataSectionSerializer
 *  net.minecraft.server.packs.resources.Resource
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.DyeColor
 *  net.minecraft.world.level.ChunkPos
 *  net.minecraft.world.level.Level
 *  org.apache.commons.lang3.StringUtils
 *  org.apache.commons.lang3.text.WordUtils
 *  org.joml.Vector2d
 */
package folk.sisby.antique_atlas.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import folk.sisby.antique_atlas.AntiqueAtlas;
import folk.sisby.antique_atlas.AntiqueAtlasKeybindings;
import folk.sisby.antique_atlas.MarkerTexture;
import folk.sisby.antique_atlas.TileTexture;
import folk.sisby.antique_atlas.WorldAtlasData;
import folk.sisby.antique_atlas.gui.AtlasOverlay;
import folk.sisby.antique_atlas.gui.AtlasRenderer;
import folk.sisby.antique_atlas.gui.BlinkingMarkerComponent;
import folk.sisby.antique_atlas.gui.BookmarkButton;
import folk.sisby.antique_atlas.gui.MarkerBookmarkButton;
import folk.sisby.antique_atlas.gui.MarkerModal;
import folk.sisby.antique_atlas.gui.TextBookmarkButton;
import folk.sisby.antique_atlas.gui.core.ButtonComponent;
import folk.sisby.antique_atlas.gui.core.Component;
import folk.sisby.antique_atlas.gui.core.CursorComponent;
import folk.sisby.antique_atlas.gui.core.ScreenState;
import folk.sisby.antique_atlas.gui.core.ScrollBoxComponent;
import folk.sisby.antique_atlas.util.CodecUtil;
import folk.sisby.surveyor.PlayerSummary;
import folk.sisby.surveyor.client.SurveyorClient;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.util.RegionPos;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.joml.Vector2d;

public class AtlasScreen
extends Component
implements AtlasRenderer {
    public final int bookWidth;
    public final int bookHeight;
    public final int mapWidth;
    public final int mapHeight;
    public static double mapOffsetX;
    public static double mapOffsetY;
    public static int tilePixels;
    public static int tileChunks;
    private ResourceKey<Level> dim;
    public int mapScale;
    public Player player;
    public WorldAtlasData worldAtlasData;
    public int prevDimScale = 0;
    public final BookmarkButton addMarkerBookmark;
    public final BookmarkButton deleteMarkerBookmark;
    public final BookmarkButton markerVisibilityBookmark;
    public final TextBookmarkButton resetScaleBookmark;
    public final BookmarkButton playerBookmark;
    public final ScrollBoxComponent markerScrollBox = new ScrollBoxComponent(true, 20);
    public final ScrollBoxComponent dimensionScrollBox = new ScrollBoxComponent(false, 26);
    public final MarkerModal markerModal = new MarkerModal();
    public final BlinkingMarkerComponent markerCursor = new BlinkingMarkerComponent();
    public final CursorComponent eraser = new CursorComponent();
    public final List<BookmarkButton> markerBookmarks = new CopyOnWriteArrayList<BookmarkButton>();
    public final List<BookmarkButton> dimBookmarks = new CopyOnWriteArrayList<BookmarkButton>();
    public final ScreenState<AtlasScreen> state = new ScreenState((oldState, newState) -> AntiqueAtlas.lastState.switchTo((ScreenState.State<AtlasScreen>)newState, this));
    public Landmark hoveredLandmark = null;
    public PlayerSummary hoveredFriend = null;
    public ButtonComponent selectedButton = null;
    public Integer targetOffsetX;
    public Integer targetOffsetY;
    public boolean isMouseOverMap = false;
    public boolean isDragging = false;
    public final boolean fullscreen;
    protected int sideButtonY = 14;
    public static final MetadataSectionType<DimensionTextureMeta> METADATA;

    public AtlasScreen() {
        this.fullscreen = AntiqueAtlas.CONFIG.fullscreen;
        if (this.fullscreen) {
            this.bookWidth = (int)((double)Minecraft.getInstance().getWindow().getGuiScaledWidth() * 0.9 - 40.0);
            this.bookHeight = (int)((double)Minecraft.getInstance().getWindow().getGuiScaledHeight() * 0.9 - 10.0);
        } else {
            this.bookWidth = 310;
            this.bookHeight = 218;
        }
        this.setSize(this.bookWidth, this.bookHeight);
        this.mapWidth = this.bookWidth - 34;
        this.mapHeight = this.bookHeight - 22;
        this.mapScale = this.calculateMapScale();
        this.playerBookmark = new BookmarkButton((net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.translatable((String)"gui.antique_atlas.followPlayer"), AntiqueAtlas.id("textures/gui/player.png"), DyeColor.GRAY.getTextureDiffuseColor(), null, 7, 8, false, false);
        this.addChild(this.playerBookmark).offsetGuiCoords(this.bookWidth - 10, this.bookHeight - 11 - 18 - 10);
        this.playerBookmark.addListener(b -> {
            this.selectedButton = this.playerBookmark;
            this.clearTargetBookmarks(this.playerBookmark);
            this.playerBookmark.setSelected(true);
        });
        this.addMarkerBookmark = new BookmarkButton(TEXT_ADD_MARKER, ICON_ADD_MARKER, DyeColor.RED.getTextureDiffuseColor(), null, 16, 16, false, false);
        this.addChild(this.addMarkerBookmark);
        this.offsetSideButton(this.addMarkerBookmark);
        this.addMarkerBookmark.addListener(button -> {
            if (this.state.is(PLACING_MARKER)) {
                this.selectedButton = null;
                this.state.switchTo(NORMAL, this);
            } else {
                this.selectedButton = button;
                this.state.switchTo(PLACING_MARKER, this);
                if (Minecraft.getInstance().hasShiftDown()) {
                    int oldScale;
                    double dimX = this.player.getBlockX();
                    double dimZ = this.player.getBlockZ();
                    Map<ResourceKey<Level>, Integer> scales = AntiqueAtlas.CONFIG.dimensions.getScales(Minecraft.getInstance().getConnection());
                    int newScale = scales.getOrDefault(this.dim(), 0);
                    if (newScale * (oldScale = scales.getOrDefault(this.player.level().dimension(), 0).intValue()) == 0) {
                        return;
                    }
                    double mult = (double)newScale / (double)oldScale;
                    dimX = mult * dimX;
                    dimZ = mult * dimZ;
                    this.markerModal.setMarkerData(SurveyorClient.tryGetSummary(this.dim), this.player.level().registryAccess(), Landmark.create((UUID)SurveyorClient.getClientUuid(), (Identifier)AntiqueAtlas.id("newmarker"), b -> b.add(LandmarkComponentTypes.POS, this.player.blockPosition())));
                    this.addChild(this.markerModal);
                    this.markerCursor.setTexture(this.markerModal.selectedTexture.id(), this.markerModal.selectedTexture.textureWidth(), this.markerModal.selectedTexture.textureHeight());
                    this.addChildBehind(this.markerModal, this.markerCursor).setGuiCoords((int)this.worldXToScreenX(dimX - 16.0), (int)this.worldZToScreenY(dimZ - 16.0));
                    KeyMapping.releaseAll();
                    this.selectedButton = null;
                    this.state.switchTo(NORMAL, this);
                }
            }
        });
        this.deleteMarkerBookmark = new BookmarkButton((net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.translatable((String)"gui.antique_atlas.delMarker"), ICON_DELETE_MARKER, DyeColor.YELLOW.getTextureDiffuseColor(), null, 16, 16, false, false);
        this.addChild(this.deleteMarkerBookmark);
        this.offsetSideButton(this.deleteMarkerBookmark);
        this.deleteMarkerBookmark.addListener(button -> {
            if (this.state.is(DELETING_MARKER)) {
                this.selectedButton = null;
                this.state.switchTo(NORMAL, this);
            } else {
                this.selectedButton = button;
                this.state.switchTo(DELETING_MARKER, this);
            }
        });
        this.markerVisibilityBookmark = new BookmarkButton((net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.translatable((String)"gui.antique_atlas.hideMarkers"), ICON_HIDE_MARKERS, DyeColor.GREEN.getTextureDiffuseColor(), null, 16, 16, false, false);
        this.addChild(this.markerVisibilityBookmark);
        this.offsetSideButton(this.markerVisibilityBookmark);
        this.markerVisibilityBookmark.addListener(button -> {
            this.selectedButton = null;
            if (this.state.is(HIDING_MARKERS)) {
                this.state.switchTo(NORMAL, this);
            } else {
                this.selectedButton = null;
                this.state.switchTo(HIDING_MARKERS, this);
            }
        });
        this.resetScaleBookmark = new TextBookmarkButton((net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.translatable((String)"gui.antique_atlas.resetScale"), net.minecraft.network.chat.Component.nullToEmpty((String)"1c"));
        this.addChild(this.resetScaleBookmark);
        this.offsetSideButton(this.resetScaleBookmark);
        this.resetScaleBookmark.addListener(button -> {
            this.resetZoom();
            this.resetScaleBookmark.setSelected(false);
        });
        this.addChild(this.markerScrollBox).setRelativeCoords(-14, 19);
        int markersOnScreen = (this.mapHeight - 20) / 18;
        this.markerScrollBox.getViewport().setSize(24, markersOnScreen * 20 - 2);
        this.addChild(this.dimensionScrollBox).setRelativeCoords(25, this.mapHeight + 11 + 3);
        int dimsOnScreen = (this.mapWidth - 20) / 18;
        this.dimensionScrollBox.getViewport().setSize(dimsOnScreen * 20 - 2, 24);
        this.markerModal.addMarkerListener(this.markerCursor);
        this.eraser.setTexture(ERASER, 12, 14, 2, 11);
        this.state.switchTo(AntiqueAtlas.lastState.is(HIDING_MARKERS) ? HIDING_MARKERS : NORMAL, this);
        for (Identifier id : overlays.keySet()) {
            ((AtlasOverlay)overlays.get(id)).onScreenInit(this);
        }
    }

    public int calculateMapScale() {
        return switch (AntiqueAtlas.CONFIG.mapScale) {
            case -2 -> Math.max(1, (int)Math.floor(this.guiScale() / 2.0));
            case -1 -> Math.max(1, (int)Math.ceil(this.guiScale() / 2.0));
            case 0 -> (int)this.guiScale();
            default -> AntiqueAtlas.CONFIG.mapScale;
        };
    }

    public void prepareToOpen() {
        Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.BOOK_PAGE_TURN, (float)1.0f));
        this.player = Minecraft.getInstance().player;
        this.dim = Minecraft.getInstance().level.dimension();
        this.updateAtlasData();
        if (!AntiqueAtlas.CONFIG.keepOffset) {
            this.playerBookmark.setSelected(true);
            this.setMapPosition(this.player.getBlockX(), this.player.getBlockZ());
        }
        if (!AntiqueAtlas.CONFIG.keepZoom) {
            this.resetZoom();
        }
    }

    @Override
    public void init() {
        super.init();
        this.setGuiCoords((this.width - this.bookWidth) / 2, (this.height - this.bookHeight) / 2);
        this.updateScaleBookmark();
        this.updateBookmarkerList();
    }

    public void updateBookmarkerList() {
        this.dimensionScrollBox.getViewport().removeAllContent();
        this.dimensionScrollBox.setScrollPos(0);
        this.dimBookmarks.clear();
        for (Object dimensionObj : this.dim == null ? new ArrayList() : AntiqueAtlas.CONFIG.dimensions.getOrder(Minecraft.getInstance().getConnection())) {
            ResourceKey<Level> dimension = (ResourceKey<Level>)dimensionObj;
            net.minecraft.network.chat.Component name;
            Integer backgroundTint;
            Identifier iconId = dimension.identifier().withPath(arg_0 -> "textures/atlas/dimension/%s.png".formatted(arg_0));
            Resource icon = Minecraft.getInstance().getResourceManager().getResource(iconId).orElse(null);
            if (icon == null) {
                iconId = ICON_UNKNOWN;
            }
            try {
                DimensionTextureMeta meta = (DimensionTextureMeta)icon.metadata().getSection(METADATA).orElseThrow();
                backgroundTint = meta.color();
                name = net.minecraft.network.chat.Component.translatable((String)meta.name());
            }
            catch (IOException | NullPointerException | NoSuchElementException e) {
                name = net.minecraft.network.chat.Component.nullToEmpty((String)WordUtils.capitalizeFully((String)dimension.identifier().getPath().replaceAll("[/_-]", " ")));
                backgroundTint = DyeColor.byId((int)(dimension.identifier().toString().hashCode() & 0xF)).getTextureDiffuseColor();
            }
            BookmarkButton bookmark = new BookmarkButton(name, iconId, backgroundTint, null, 16, 16, false, true);
            bookmark.setSelected(dimension.equals(this.dim));
            bookmark.addListener(arg_0 -> this.lambda$updateBookmarkerList$8((ResourceKey)dimension, arg_0));
            this.dimBookmarks.add(bookmark);
        }
        int[] contentX = new int[]{0};
        for (BookmarkButton bookmark : this.dimBookmarks) {
            this.dimensionScrollBox.getViewport().addContent(bookmark).setRelativeX(contentX[0]);
            contentX[0] = contentX[0] + 20;
        }
        this.markerScrollBox.getViewport().removeAllContent();
        this.markerScrollBox.setScrollPos(0);
        this.markerBookmarks.clear();
        if (this.worldAtlasData == null) {
            return;
        }
        this.worldAtlasData.getEditableLandmarks().forEach((landmark, texture) -> {
            MarkerBookmarkButton bookmark = new MarkerBookmarkButton((net.minecraft.network.chat.Component)landmark.getOrDefault(LandmarkComponentTypes.NAME, net.minecraft.network.chat.Component.literal((String)landmark.id().getPath())), (MarkerTexture)texture, (Integer)landmark.getOrDefault(LandmarkComponentTypes.COLOR, (Object)0xFFFFFF), true, false);
            bookmark.addListener(button -> {
                if (this.state.is(NORMAL)) {
                    this.clearTargetBookmarks(bookmark);
                    this.setTargetPosition(new ColumnPos(((BlockPos)landmark.getOrDefault(LandmarkComponentTypes.POS, BlockPos.ZERO)).getX(), ((BlockPos)landmark.getOrDefault(LandmarkComponentTypes.POS, BlockPos.ZERO)).getZ()));
                } else if (this.state.is(DELETING_MARKER)) {
                    if (!this.worldAtlasData.deleteLandmark(this.dim, (Landmark)landmark)) {
                        return;
                    }
                    this.updateBookmarkerList();
                    Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, (float)1.0f, (float)0.5f));
                    if (!Minecraft.getInstance().hasShiftDown()) {
                        this.state.switchTo(NORMAL, this);
                    }
                }
            });
            this.markerBookmarks.add(bookmark);
        });
        int[] contentY = new int[]{0};
        for (BookmarkButton bookmark : this.markerBookmarks) {
            this.markerScrollBox.getViewport().addContent(bookmark).setRelativeY(contentY[0]);
            contentY[0] = contentY[0] + 20;
        }
    }

    public void clearTargetBookmarks(BookmarkButton except) {
        if (this.playerBookmark != except) {
            this.playerBookmark.setSelected(false);
        }
        for (BookmarkButton bookmark : this.markerBookmarks) {
            if (bookmark == except) continue;
            bookmark.setSelected(false);
        }
    }

    public void updateMouse(double mouseX, double mouseY) {
        double relativeMouseX = mouseX - (double)this.getGuiX();
        double relativeMouseY = mouseY - (double)this.getGuiY();
        this.isMouseOverMap = relativeMouseX >= 17.0 && relativeMouseX <= (double)(17 + this.mapWidth) && relativeMouseY >= 11.0 && relativeMouseY <= (double)(11 + this.mapHeight);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(mouseX, mouseY);
        this.updateMouse(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();
        int mouseState = event.button();
        this.updateMouse(mouseX, mouseY);
        if (this.markerModal.getParent() != null) {
            return this.markerModal.mouseClicked(event, bl);
        }
        if (super.mouseClicked(event, bl)) {
            return true;
        }
        if (this.state.is(NORMAL) && this.hoveredLandmark != null && this.hoveredLandmark.contains(LandmarkComponentTypes.POS) && !this.hoveredLandmark.owner().equals(WorldLandmarks.GLOBAL) && SurveyorClient.canModify((UUID)this.hoveredLandmark.owner()) && mouseState == 1) {
            this.markerModal.setMarkerData(SurveyorClient.tryGetSummary(this.dim), this.player.level().registryAccess(), this.hoveredLandmark);
            this.addChild(this.markerModal);
            this.markerCursor.setTexture(this.markerModal.selectedTexture.id(), 32, 32);
            this.addChildBehind(this.markerModal, this.markerCursor).setGuiCoords((int)mouseX - 16, (int)mouseY - 16);
            KeyMapping.releaseAll();
            this.state.switchTo(NORMAL, this);
            return true;
        }
        if (!this.state.is(NORMAL) && !this.state.is(HIDING_MARKERS)) {
            if (this.state.is(PLACING_MARKER) && this.isMouseOverMap && mouseState == 0) {
                this.markerModal.setMarkerData(SurveyorClient.tryGetSummary(this.dim), this.player.level().registryAccess(), Landmark.create((UUID)SurveyorClient.getClientUuid(), (Identifier)AntiqueAtlas.id("newmarker"), b -> b.add(LandmarkComponentTypes.POS, new BlockPos(this.screenXToWorldX(mouseX), 0, this.screenYToWorldZ(mouseY)))));
                this.addChild(this.markerModal);
                this.markerCursor.setTexture(this.markerModal.selectedTexture.id(), 32, 32);
                this.addChildBehind(this.markerModal, this.markerCursor).setGuiCoords((int)mouseX - 16, (int)mouseY - 16);
                KeyMapping.releaseAll();
                this.state.switchTo(NORMAL, this);
                return true;
            }
            if (this.state.is(DELETING_MARKER) && this.hoveredLandmark != null && this.isMouseOverMap && mouseState == 0 && this.worldAtlasData.deleteLandmark(this.dim, this.hoveredLandmark)) {
                this.updateBookmarkerList();
                Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, (float)1.0f, (float)0.5f));
            }
            if (!Minecraft.getInstance().hasShiftDown() || !this.state.is(DELETING_MARKER)) {
                this.state.switchTo(NORMAL, this);
            }
        } else if (this.isMouseOverMap && this.selectedButton == null) {
            this.isDragging = true;
            return true;
        }
        return false;
    }

    private void changeDim(ResourceKey<Level> newDim) {
        Map<ResourceKey<Level>, Integer> scales = AntiqueAtlas.CONFIG.dimensions.getScales(Minecraft.getInstance().getConnection());
        int newScale = scales.getOrDefault(newDim, 0);
        int oldScale = scales.getOrDefault(this.dim, 0);
        int newPrevDimScale = 0;
        if (oldScale == 0 && this.prevDimScale != 0) {
            oldScale = this.prevDimScale;
        } else if (newScale == 0) {
            newPrevDimScale = oldScale;
        }
        this.dim = newDim;
        if (newScale * oldScale > 0) {
            double mult = (double)newScale / (double)oldScale;
            mapOffsetX = mult * mapOffsetX;
            mapOffsetY = mult * mapOffsetY;
            if (newScale < oldScale) {
                while (this.zoomIn(false, 16 << AntiqueAtlas.CONFIG.maxTilePixels) && !((double)newScale / (double)(oldScale /= 2) >= 1.0)) {
                }
            } else if (oldScale < newScale) {
                while (this.zoomOut(false, 1 << AntiqueAtlas.CONFIG.maxTileChunks) && !((double)newScale / (double)(oldScale *= 2) <= 1.0)) {
                }
            }
        }
        if (newPrevDimScale != 0) {
            this.prevDimScale = newPrevDimScale;
        }
        this.minecraft.getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.BOOK_PAGE_TURN, (float)1.1f));
        this.updateAtlasData();
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int keyCode = event.key();
        int scanCode = event.scancode();
        int modifiers = event.modifiers();
        if (AntiqueAtlasKeybindings.ATLAS_KEYMAPPING.matches(event) && this.markerModal.getParent() == null) {
            this.onClose();
            return true;
        }
        switch (keyCode) {
            case 265: {
                this.navigateMap(0, 24);
                break;
            }
            case 264: {
                this.navigateMap(0, -24);
                break;
            }
            case 263: {
                this.navigateMap(24, 0);
                break;
            }
            case 262: {
                this.navigateMap(-24, 0);
                break;
            }
            case 61: 
            case 334: {
                this.zoomIn(true, 16 << AntiqueAtlas.CONFIG.maxTilePixels);
                break;
            }
            case 45: 
            case 333: {
                this.zoomOut(true, 1 << AntiqueAtlas.CONFIG.maxTileChunks);
                break;
            }
            case 258: {
                List<ResourceKey<Level>> regKeys = AntiqueAtlas.CONFIG.dimensions.getOrder(this.minecraft.getConnection());
                if (!regKeys.contains(this.dim)) break;
                this.changeDim(regKeys.get((regKeys.size() + regKeys.indexOf(this.dim) + 1) % regKeys.size()));
                break;
            }
            case 256: {
                this.onClose();
                break;
            }
            default: {
                return super.keyPressed(event);
            }
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double dx, double dy) {
        this.updateMouse(mouseX, mouseY);
        if (super.mouseScrolled(mouseX, mouseY, dx, dy)) {
            return true;
        }
        if (this.markerModal.getParent() == null && dy != 0.0) {
            int direction;
            int n = direction = dy > 0.0 ? 1 : -1;
            if ((dy > 0.0 ? this.zoomIn(true, 16 << AntiqueAtlas.CONFIG.maxTilePixels) : this.zoomOut(true, 1 << AntiqueAtlas.CONFIG.maxTileChunks)) && (this.isMouseOverMap || this.isDragging)) {
                double xOffset = ((double)(this.getGuiX() + 17) + (double)this.mapWidth / 2.0 - mouseX) * (double)direction;
                double yOffset = ((double)(this.getGuiY() + 11) + (double)this.mapHeight / 2.0 - mouseY) * (double)direction;
                if (Math.abs(xOffset) > 5.0 || Math.abs(yOffset) > 5.0) {
                    mapOffsetX += xOffset / (direction < 0 ? 2.0 : 1.0);
                    mapOffsetY += yOffset / (direction < 0 ? 2.0 : 1.0);
                    this.clearTargetBookmarks(null);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int mouseState = event.button();
        boolean result = false;
        if (mouseState != -1) {
            result = this.selectedButton != null || this.isDragging;
            this.selectedButton = null;
            this.isDragging = false;
        }
        return super.mouseReleased(event) || result;
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double deltaX, double deltaY) {
        double mouseX = event.x();
        double mouseY = event.y();
        int lastMouseButton = event.button();
        boolean result = false;
        if (this.isDragging) {
            this.prevDimScale = 0;
            this.clearTargetBookmarks(null);
            mapOffsetX += deltaX;
            mapOffsetY += deltaY;
            result = true;
        }
        return super.mouseDragged(event, deltaX, deltaY) || result;
    }

    @Override
    public void tick() {
        int oldScale;
        super.tick();
        if (this.player == null) {
            return;
        }
        double dimX = this.player.getBlockX();
        double dimZ = this.player.getBlockZ();
        Map<ResourceKey<Level>, Integer> scales = AntiqueAtlas.CONFIG.dimensions.getScales(Minecraft.getInstance().getConnection());
        int newScale = scales.getOrDefault(this.dim(), 0);
        if (newScale * (oldScale = scales.getOrDefault(this.player.level().dimension(), 0).intValue()) > 0) {
            double mult = (double)newScale / (double)oldScale;
            dimX = mult * dimX;
            dimZ = mult * dimZ;
            if (this.playerBookmark.isSelected() && (mapOffsetX != -dimX * this.getPixelsPerBlock() || mapOffsetY != -dimZ * this.getPixelsPerBlock())) {
                this.setTargetPosition(new ColumnPos((int)dimX, (int)dimZ));
            }
        }
        if (this.targetOffsetX != null) {
            if (Math.abs(this.getTargetPositionX() - mapOffsetX) > 24.0) {
                this.softNavigateMap(this.getTargetPositionX() > mapOffsetX ? 24 : -24, 0);
            } else {
                mapOffsetX = this.getTargetPositionX();
                this.targetOffsetX = null;
            }
        }
        if (this.targetOffsetY != null) {
            if (Math.abs(this.getTargetPositionY() - mapOffsetY) > 24.0) {
                this.softNavigateMap(0, this.getTargetPositionY() > mapOffsetY ? 24 : -24);
            } else {
                mapOffsetY = this.getTargetPositionY();
                this.targetOffsetY = null;
            }
        }
    }

    public void updateAtlasData() {
        this.worldAtlasData = WorldAtlasData.getOrCreate(this.dim);
        this.updateBookmarkerList();
        this.updateScaleBookmark();
    }

    public void navigateMap(int dx, int dy) {
        mapOffsetX += (double)dx;
        mapOffsetY += (double)dy;
        this.clearTargetBookmarks(null);
    }

    public void softNavigateMap(int dx, int dy) {
        mapOffsetX += (double)dx;
        mapOffsetY += (double)dy;
    }

    public void setMapPosition(int x, int z) {
        mapOffsetX = (int)((double)(-x) * this.getPixelsPerBlock());
        mapOffsetY = (int)((double)(-z) * this.getPixelsPerBlock());
    }

    public void setTargetPosition(ColumnPos pos) {
        this.targetOffsetX = pos.x();
        this.targetOffsetY = pos.z();
    }

    public double getTargetPositionX() {
        return (double)(-this.targetOffsetX.intValue()) * this.getPixelsPerBlock();
    }

    public double getTargetPositionY() {
        return (double)(-this.targetOffsetY.intValue()) * this.getPixelsPerBlock();
    }

    public void updateScaleBookmark() {
        int tileSizeBlocks = tileChunks * 16 * 16 / tilePixels;
        int defaultTileSizeBlocks = 16;
        int rulerSizeBlocks = (int)((float)tileSizeBlocks / this.getEffectiveScale());
        this.resetScaleBookmark.setLabel((net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.literal((String)(rulerSizeBlocks == 16 | rulerSizeBlocks >= 32 ? "%dc".formatted(rulerSizeBlocks / 16) : "%db".formatted(rulerSizeBlocks))).withStyle(tileSizeBlocks < defaultTileSizeBlocks ? ChatFormatting.DARK_RED : (tileSizeBlocks == defaultTileSizeBlocks ? ChatFormatting.BLACK : ChatFormatting.DARK_BLUE)));
    }

    public boolean zoomIn(boolean playSound, int maxTilePixels) {
        this.prevDimScale = 0;
        if (tileChunks == 1) {
            if (tilePixels >= maxTilePixels) {
                return false;
            }
            tilePixels <<= 1;
            if (playSound) {
                Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.SPYGLASS_USE, (float)1.0f));
            }
        } else {
            tileChunks >>= 1;
            if (playSound) {
                Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.BOOK_PAGE_TURN, (float)1.0f));
            }
        }
        mapOffsetX *= 2.0;
        mapOffsetY *= 2.0;
        this.updateScaleBookmark();
        return true;
    }

    public boolean zoomOut(boolean playSound, int maxTileChunks) {
        this.prevDimScale = 0;
        if (tilePixels == 16) {
            if (tileChunks >= maxTileChunks) {
                return false;
            }
            tileChunks <<= 1;
            if (playSound) {
                Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.BOOK_PAGE_TURN, (float)1.0f));
            }
        } else {
            tilePixels >>= 1;
            if (playSound) {
                Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI((SoundEvent)SoundEvents.SPYGLASS_USE, (float)1.0f));
            }
        }
        mapOffsetX /= 2.0;
        mapOffsetY /= 2.0;
        this.updateScaleBookmark();
        return true;
    }

    public void resetZoom() {
        block3: {
            block2: {
                if (!this.zoomIn(true, 8)) break block2;
                while (this.zoomIn(false, 8)) {
                }
                break block3;
            }
            if (!this.zoomOut(true, 1)) break block3;
            while (this.zoomOut(false, 1)) {
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float partialTick) {
      folk.sisby.antique_atlas.util.AtlasCanvas canvas = new folk.sisby.antique_atlas.util.GuiAtlasCanvas(context);
        int left_width;
        int trueMouseX = mouseX;
        int trueMouseY = mouseY;
        if (this.markerModal.getParent() != null) {
            mouseX = -100;
            mouseY = -100;
        }
        this.extractBackground(context, mouseX, mouseY, partialTick);
        this.mapScale = this.calculateMapScale();
        if (this.fullscreen) {
            left_width = this.bookWidth / 2 - 15;
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FULLSCREEN, this.getGuiX(), this.getGuiY(), left_width, this.bookHeight);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FULLSCREEN_M, this.getGuiX() + left_width, this.getGuiY(), 29, this.bookHeight);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FULLSCREEN_R, this.getGuiX() + left_width + 29, this.getGuiY(), left_width + 1, this.bookHeight);
        } else {
            context.blit(RenderPipelines.GUI_TEXTURED, BOOK, this.getGuiX(), this.getGuiY(), 0.0f, 0.0f, this.bookWidth, this.bookHeight, this.bookWidth, this.bookHeight);
        }
        if (this.worldAtlasData == null) {
            return;
        }
        context.enableScissor(this.getGuiX() + 17, this.getGuiY() + 11, this.getGuiX() + 17 + this.mapWidth, this.getGuiY() + 11 + this.mapHeight);
        this.renderTiles(canvas, 0xF000F0);
        if (this.fullscreen) {
            left_width = this.bookWidth / 2 - 15;
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FRAME_FULLSCREEN, this.getGuiX(), this.getGuiY(), left_width, this.bookHeight);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FRAME_FULLSCREEN_M, this.getGuiX() + left_width, this.getGuiY(), 29, this.bookHeight);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FRAME_FULLSCREEN_R, this.getGuiX() + left_width + 29, this.getGuiY(), left_width + 1, this.bookHeight);
        } else {
            context.blit(RenderPipelines.GUI_TEXTURED, BOOK_FRAME, this.getGuiX(), this.getGuiY(), 0.0f, 0.0f, this.bookWidth, this.bookHeight, this.bookWidth, this.bookHeight);
        }
        context.pose().pushMatrix();
        context.pose().translate((float)this.getGuiX(), (float)this.getGuiY());
        float markerScale = this.getEffectiveScale() * ((float)tilePixels / 16.0f);
        Map<UUID, PlayerSummary> friends = AntiqueAtlas.getOrderedFriends();
        for (Identifier id : overlays.keySet()) {
            ((AtlasOverlay)overlays.get(id)).onScreenRender(new AtlasOverlay.AtlasScreenRenderContext(this, context, mouseX, mouseY, markerScale, friends));
        }
        this.hoveredLandmark = null;
        this.hoveredFriend = null;
        if (!this.state.is(HIDING_MARKERS)) {
            if (this.isMouseOverMap) {
                double bestDistance = Double.MAX_VALUE;
                for (Map.Entry<Landmark, MarkerTexture> entry : this.worldAtlasData.getAllMarkers(tileChunks).entrySet()) {
                    Landmark landmark2 = entry.getKey();
                    MarkerTexture texture2 = entry.getValue();
                    BlockPos pos = (BlockPos)landmark2.get(LandmarkComponentTypes.POS);
                    if (pos == null) {
                        Set<ChunkPos> chunks = RegionPos.regionsToChunks((Map)((Map)landmark2.getOrDefault(LandmarkComponentTypes.CHUNKS, new HashMap())));
                        for (ChunkPos chunk : chunks) {
                            double screenX = this.worldXToScreenX(chunk.getMinBlockX());
                            double screenEndX = this.worldXToScreenX(chunk.getMinBlockX() + 16);
                            double screenY = this.worldZToScreenY(chunk.getMinBlockZ());
                            double screenEndY = this.worldZToScreenY(chunk.getMinBlockZ() + 16);
                            boolean isInside = (double)mouseX >= screenX && (double)mouseX < screenEndX && (double)mouseY >= screenY && (double)mouseY < screenEndY;
                            if (!isInside || !(10.0 < bestDistance)) continue;
                            this.hoveredLandmark = landmark2;
                            bestDistance = 10.0;
                        }
                        continue;
                    }
                    double markerX = this.worldXToScreenX(pos.getX());
                    double markerY = this.worldZToScreenY(pos.getZ());
                    Vector2d markerCenter = texture2.getCenter(tileChunks);
                    double squaredDistance = Vector2d.distanceSquared((double)(markerX + (double)markerScale * markerCenter.x), (double)(markerY + (double)markerScale * markerCenter.y), (double)mouseX, (double)mouseY);
                    if (!(squaredDistance > 0.0) || !(squaredDistance < bestDistance) || !(squaredDistance < texture2.getSquaredSize(tileChunks) * (double)markerScale * (double)markerScale / 4.0)) continue;
                    bestDistance = squaredDistance;
                    this.hoveredLandmark = landmark2;
                }
                for (Map.Entry<UUID, PlayerSummary> entry : friends.entrySet()) {
                    double markerY;
                    double markerX;
                    double squaredDistance;
                    UUID uuid2 = entry.getKey();
                    PlayerSummary friend2 = entry.getValue();
                    boolean self = uuid2.equals(SurveyorClient.getClientUuid());
                    boolean inDim = friend2.dimension().equals(this.dim);
                    if (!self && !inDim) continue;
                    double dimX = friend2.pos().x();
                    double dimZ = friend2.pos().z();
                    if (!this.dim.equals((Object)friend2.dimension())) {
                        int oldScale;
                        Map<ResourceKey<Level>, Integer> scales = AntiqueAtlas.CONFIG.dimensions.getScales(Minecraft.getInstance().getConnection());
                        int newScale = scales.getOrDefault(this.dim(), 0);
                        if (newScale * (oldScale = scales.getOrDefault(friend2.dimension(), 0).intValue()) == 0) continue;
                        double mult = (double)newScale / (double)oldScale;
                        dimX = mult * dimX;
                        dimZ = mult * dimZ;
                    }
                    if (!((squaredDistance = Vector2d.distanceSquared((double)(markerX = this.worldXToScreenX(dimX)), (double)(markerY = this.worldZToScreenY(dimZ)), (double)mouseX, (double)mouseY)) > 0.0) || !(squaredDistance < bestDistance) || !(squaredDistance < 21.0)) continue;
                    bestDistance = squaredDistance;
                    this.hoveredFriend = friend2;
                    this.hoveredLandmark = null;
                }
            }
            this.worldAtlasData.getAllMarkers(tileChunks).forEach((landmark, texture) -> {
                boolean hovering = this.hoveredLandmark == landmark && this.markerModal.getParent() == null;
                boolean editable = !landmark.owner().equals(WorldLandmarks.GLOBAL) && SurveyorClient.canModify((UUID)landmark.owner());
                BiFunction<Double, Double, Float> alpha = (x, y) -> Float.valueOf(this.state.is(PLACING_MARKER) || this.state.is(DELETING_MARKER) && !editable || hovering && x <= 17.0 || x >= (double)(this.mapWidth + 17) || y <= 11.0 || y >= (double)(this.mapHeight + 11) ? 0.5f : 1.0f);
                this.renderMarker(canvas, (Landmark)landmark, (MarkerTexture)texture, 0.0f, 0xF000F0, alpha, editable, hovering, markerScale);
            });
        }
        context.pose().popMatrix();
        context.disableScissor();
        if (this.fullscreen) {
            int left_width2 = this.bookWidth / 2 - 15;
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FRAME_NARROW_FULLSCREEN, this.getGuiX(), this.getGuiY(), left_width2, this.bookHeight);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FRAME_NARROW_FULLSCREEN_M, this.getGuiX() + left_width2, this.getGuiY(), 29, this.bookHeight);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, BOOK_FRAME_NARROW_FULLSCREEN_R, this.getGuiX() + left_width2 + 29, this.getGuiY(), left_width2 + 1, this.bookHeight);
        } else {
            context.blit(RenderPipelines.GUI_TEXTURED, BOOK_FRAME_NARROW, this.getGuiX(), this.getGuiY(), 0.0f, 0.0f, this.bookWidth, this.bookHeight, this.bookWidth, this.bookHeight);
        }
        this.markerScrollBox.getViewport().setClipped(this.state.is(HIDING_MARKERS));
        context.pose().pushMatrix();
        context.pose().translate((float)this.getGuiX(), (float)this.getGuiY());
        friends.forEach((uuid, friend) -> {
            boolean hovering;
            boolean self = uuid.equals(SurveyorClient.getClientUuid());
            boolean inDim = friend.dimension().equals(this.dim);
            if (!self && !inDim) {
                return;
            }
            boolean bl = hovering = this.hoveredFriend == friend && this.markerModal.getParent() == null;
            if (!(!this.state.is(HIDING_MARKERS) || this.playerBookmark.isSelected() && self)) {
                return;
            }
            this.renderPlayer(canvas, 0.0f, 0xF000F0, (PlayerSummary)friend, this.getEffectiveScale(), this.state.is(PLACING_MARKER) ? 0.5f : 1.0f, hovering, self);
        });
        context.pose().popMatrix();
        if (this.state.is(PLACING_MARKER)) {
            context.blit(RenderPipelines.GUI_TEXTURED, this.markerModal.selectedTexture.id(), mouseX + this.markerModal.selectedTexture.offsetX(), mouseY + this.markerModal.selectedTexture.offsetY(), 0.0f, 0.0f, this.markerModal.selectedTexture.textureWidth(), this.markerModal.selectedTexture.textureHeight(), this.markerModal.selectedTexture.textureWidth(), this.markerModal.selectedTexture.textureHeight());
        }
        this.addMarkerBookmark.setTitle(Minecraft.getInstance().hasShiftDown() ? TEXT_ADD_MARKER_HERE : TEXT_ADD_MARKER);
        if (this.worldAtlasData.isLoading()) {
            context.text(this.font, (net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.literal((String)"...").withStyle(ChatFormatting.GRAY), this.getGuiX() + 17 + this.mapWidth - 10, this.getGuiY() + 11 + this.mapHeight - 10, -1, true);
        }
        if (Minecraft.getInstance().hasAltDown() && !this.isDragging && this.isMouseOverMap && this.markerModal.getParent() == null) {
            int x = this.screenXToWorldX((int)this.getMouseX());
            int z = this.screenYToWorldZ((int)this.getMouseY());
            ChunkPos pos = ChunkPos.containing(new BlockPos(x, 0, z));
            context.text(this.font, (net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.literal((String)"%d,%d (%d,%d)".formatted(pos.x(), pos.z(), x, z)), this.getGuiX(), this.getGuiY() - 12, -1, true);
            if (this.hoveredLandmark != null) {
                MarkerTexture markerTexture = this.worldAtlasData.getMarkerTexture(this.hoveredLandmark);
                context.text(this.font, (net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.literal((String)this.hoveredLandmark.id().toString()), this.getGuiX() + this.bookWidth - this.font.width((FormattedText)net.minecraft.network.chat.Component.literal((String)this.hoveredLandmark.id().toString())), this.getGuiY() - 12, -1, true);
                if (markerTexture != null) {
                    context.text(this.font, (net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.literal((String)markerTexture.displayId()), this.getGuiX() + this.bookWidth - this.font.width((FormattedText)net.minecraft.network.chat.Component.literal((String)markerTexture.displayId())), this.getGuiY() + this.bookHeight, -1, true);
                }
            } else {
                TileTexture tileTexture = this.worldAtlasData.getTile(pos);
                Identifier providerId = this.worldAtlasData.getProvider(pos);
                String predicate = this.worldAtlasData.getTilePredicate(pos);
                if (tileTexture != null) {
                    if (predicate != null) {
                        context.text(this.font, (net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.literal((String)predicate), this.getGuiX() + this.bookWidth - this.font.width((FormattedText)net.minecraft.network.chat.Component.literal((String)predicate)), this.getGuiY() - 12, -1, true);
                    }
                    if (providerId != null) {
                        context.text(this.font, (net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.literal((String)providerId.toString()), this.getGuiX(), this.getGuiY() + this.bookHeight + 14, -1, true);
                    }
                    context.text(this.font, (net.minecraft.network.chat.Component)net.minecraft.network.chat.Component.literal((String)tileTexture.displayId()), this.getGuiX() + this.bookWidth - this.font.width((FormattedText)net.minecraft.network.chat.Component.literal((String)tileTexture.displayId())), this.getGuiY() + this.bookHeight, -1, true);
                }
            }
        }
        if (this.markerModal.getParent() != null) {
            this.markerModal.setClipped(true);
            super.extractRenderState(context, mouseX, mouseY, partialTick);
            this.markerModal.setClipped(false);
            this.markerModal.extractRenderState(context, trueMouseX, trueMouseY, partialTick);
        } else {
            super.extractRenderState(context, mouseX, mouseY, partialTick);
        }
        context.pose().pushMatrix();
        context.pose().translate((float)this.getMouseX(), (float)this.getMouseY());
        if (this.hoveredLandmark != null) {
            net.minecraft.network.chat.Component name = (net.minecraft.network.chat.Component)this.hoveredLandmark.get(LandmarkComponentTypes.NAME);
            if (name != null && !name.getString().isEmpty()) {
                context.setComponentTooltipForNextFrame(this.font, Stream.concat(Stream.of(name), ((List<net.minecraft.network.chat.Component>)this.hoveredLandmark.getOrDefault(LandmarkComponentTypes.LORE, new ArrayList<>())).stream().map(t -> t.copy().withStyle(ChatFormatting.GRAY))).toList(), 0, 0);
            }
        } else if (this.hoveredFriend != null) {
            boolean self = this.hoveredFriend.username().equals(Minecraft.getInstance().player.getScoreboardName());
            boolean inDim = this.hoveredFriend.dimension().equals(this.dim);
            if (self && inDim) {
                return;
            }
            context.setTooltipForNextFrame(this.font, (net.minecraft.network.chat.Component)(self ? net.minecraft.network.chat.Component.translatable((String)"gui.antique_atlas.followPlayer") : net.minecraft.network.chat.Component.literal((String)this.hoveredFriend.username())).withStyle(this.hoveredFriend.online() ? (self ? ChatFormatting.WHITE : ChatFormatting.LIGHT_PURPLE) : ChatFormatting.GRAY), 0, 0);
        }
        context.pose().popMatrix();
    }

    @Override
    public double guiScale() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }

    @Override
    public ResourceKey<Level> dim() {
        return this.dim;
    }

    @Override
    public void onClose() {
        super.onClose();
        this.markerModal.closeChild();
        this.removeChild(this.markerCursor);
    }

    @Override
    public void onChildClosed(Component child) {
        if (((Object)((Object)child)).equals((Object)this.markerModal)) {
            this.removeChild(this.markerCursor);
        }
    }

    float getEffectiveScale() {
        return (float)((double)this.mapScale() / this.guiScale());
    }

    @Override
    public double getPixelsPerBlock() {
        return (double)this.getEffectiveScale() * (double)this.tilePixels() / ((double)this.tileChunks() * 16.0);
    }

    @Override
    public int bookX() {
        return this.getGuiX();
    }

    @Override
    public int bookY() {
        return this.getGuiY();
    }

    @Override
    public int bookHeight() {
        return this.bookHeight;
    }

    @Override
    public int mapWidth() {
        return this.mapWidth;
    }

    @Override
    public int mapHeight() {
        return this.mapHeight;
    }

    @Override
    public double mapOffsetX() {
        return mapOffsetX;
    }

    @Override
    public double mapOffsetY() {
        return mapOffsetY;
    }

    @Override
    public int mapScale() {
        return this.mapScale;
    }

    @Override
    public int tilePixels() {
        return tilePixels;
    }

    @Override
    public int tileChunks() {
        return tileChunks;
    }

    @Override
    public Player player() {
        return this.player;
    }

    @Override
    public int bookWidth() {
        return this.bookWidth;
    }

    @Override
    public WorldAtlasData worldAtlasData() {
        return this.worldAtlasData;
    }

    public void offsetSideButton(Component component) {
        component.offsetGuiCoords(this.bookWidth - 10, this.sideButtonY);
        this.sideButtonY += component.getHeight() + 1;
    }

    private /* synthetic */ void lambda$updateBookmarkerList$8(ResourceKey dimension, ButtonComponent button) {
        List<ResourceKey<Level>> regKeys = AntiqueAtlas.CONFIG.dimensions.getOrder(this.minecraft.getConnection());
        if (regKeys.contains(dimension) && !this.dim.equals((Object)dimension)) {
            this.changeDim((ResourceKey<Level>)dimension);
        }
    }

    private static /* synthetic */ String lambda$updateBookmarkerList$7(String rec$, Object xva$0) {
        return "textures/atlas/dimension/%s.png".formatted(xva$0);
    }

    static {
        tilePixels = 16;
        tileChunks = 1;
        METADATA = new MetadataSectionType<>(AntiqueAtlas.id("dimension").toString(), DimensionTextureMeta.CODEC);
    }

    public record DimensionTextureMeta(int color, String name) {
        public static final Codec<DimensionTextureMeta> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("color").xmap(s -> {
            try {
                return Integer.parseUnsignedInt(s.replace("#", ""), 16);
            }
            catch (NumberFormatException e) {
                return 0xFFFFFF;
            }
        }, i -> "#" + StringUtils.leftPad((String)Integer.toHexString(i & 0xFFFFFF), (int)6, (String)"0")).forGetter(DimensionTextureMeta::color), Codec.STRING.fieldOf("name").forGetter(DimensionTextureMeta::name)).apply(instance, DimensionTextureMeta::new));
    }
}
