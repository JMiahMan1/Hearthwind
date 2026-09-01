package com.talhanation.smallships.client.model.sail;

import com.talhanation.smallships.SmallShipsMod;
import com.talhanation.smallships.client.renderer.entity.state.ShipRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.resources.Identifier;

import java.util.Arrays;

public abstract class SailModel extends EntityModel<ShipRenderState> {
    protected ModelPart[] sailParts;

    protected SailModel(ModelPart modelPart) {
        super(modelPart);

        ModelPart sail = modelPart.getChild("Sail");
        sailParts = new ModelPart[] { sail.getChild("Sail_0"), sail.getChild("Sail_1"), sail.getChild("Sail_2"), sail.getChild("Sail_3"), sail.getChild("Sail_4") };
    }

    public static SailModel.Color getSailColor(String stringColor) {
        return Arrays.stream(Color.values()).filter(color -> color.toString().equals(stringColor)).findAny().orElse(Color.WHITE);
    }

    @Override
    public void setupAnim(ShipRenderState state) {
        byte sailState = state.sailable.getSailState();
        Arrays.stream(sailParts).forEach(sailPart -> sailPart.visible = false);
        sailParts[sailState].visible = true;

        super.setupAnim(state);
    }

    public enum Color {
        WHITE(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/white_sail.png")),
        ORANGE(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/orange_sail.png")),
        MAGENTA(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/magenta_sail.png")),
        LIGHT_BLUE(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/light_blue_sail.png")),
        YELLOW(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/yellow_sail.png")),
        LIME(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/lime_sail.png")),
        PINK(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/pink_sail.png")),
        GRAY(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/gray_sail.png")),
        LIGHT_GRAY(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/light_gray_sail.png")),
        CYAN(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/cyan_sail.png")),
        PURPLE(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/purple_sail.png")),
        BLUE(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/blue_sail.png")),
        BROWN(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/brown_sail.png")),
        GREEN(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/green_sail.png")),
        RED(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/red_sail.png")),
        BLACK(Identifier.fromNamespaceAndPath(SmallShipsMod.MOD_ID,"textures/entity/sail/black_sail.png"));

        public final Identifier location;

        Color(Identifier location) {
            this.location = location;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
