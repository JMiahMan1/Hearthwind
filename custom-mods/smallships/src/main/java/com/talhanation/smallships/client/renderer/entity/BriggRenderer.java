package com.talhanation.smallships.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.talhanation.smallships.client.model.BriggModel;
import com.talhanation.smallships.client.renderer.entity.state.ShipRenderState;
import com.talhanation.smallships.world.entity.ship.BriggEntity;
import com.talhanation.smallships.world.entity.ship.Ship;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;

public class BriggRenderer extends ShipRenderer<BriggEntity> {
    public BriggRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected BriggModel createBoatModel(EntityRendererProvider.Context context, Ship.Type type) {
        return new BriggModel(context.bakeLayer(BriggModel.LAYER_LOCATION));
    }

    @Override
    public void submit(ShipRenderState state, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        poseStack.translate(0.0D, 2.7D, 0.0D);
        super.submit(state, poseStack, nodeCollector, cameraRenderState);
        poseStack.popPose();
    }
}
