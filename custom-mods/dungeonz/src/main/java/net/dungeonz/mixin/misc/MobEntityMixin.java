package net.dungeonz.mixin.misc;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.dungeonz.access.BossEntityAccess;
import net.dungeonz.block.entity.DungeonPortalEntity;
import net.dungeonz.init.BlockInit;
import net.minecraft.class_1282;
import net.minecraft.class_1299;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1937;
import net.minecraft.class_2338;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_3218;
import net.minecraft.class_5321;
import net.minecraft.class_7924;

@Mixin(class_1308.class)
public abstract class MobEntityMixin extends class_1309 implements BossEntityAccess {

    @Unique
    private boolean isDungeonBossEntity = false;
    @Unique
    private class_2338 portalPos = new class_2338(0, 0, 0);
    @Unique
    private String worldRegistryKey = "";

    public MobEntityMixin(class_1299<? extends class_1309> entityType, class_1937 world) {
        super(entityType, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbtMixin(class_2487 nbt, CallbackInfo info) {
        if (this.isDungeonBossEntity) {
            nbt.method_10556("IsDungeonBossEntity", this.isDungeonBossEntity);
            nbt.method_10582("WorldRegistryKey", this.worldRegistryKey);
            nbt.method_10569("PortalPosX", this.portalPos.method_10263());
            nbt.method_10569("PortalPosY", this.portalPos.method_10264());
            nbt.method_10569("PortalPosZ", this.portalPos.method_10260());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbtMixin(class_2487 nbt, CallbackInfo info) {
        if (nbt.method_10545("IsDungeonBossEntity")) {
            this.isDungeonBossEntity = nbt.method_10577("IsDungeonBossEntity");
            this.worldRegistryKey = nbt.method_10558("WorldRegistryKey");
            this.portalPos = new class_2338(nbt.method_10550("PortalPosX"), nbt.method_10550("PortalPosY"), nbt.method_10550("PortalPosZ"));
        }
    }

    @Override
    public void method_6078(class_1282 damageSource) {
        if (!this.method_37908().method_8608() && this.isDungeonBossEntity) {
            class_3218 nonDungeonWorld = method_37908().method_8503().method_3847(class_5321.method_29179(class_7924.field_41223, class_2960.method_60654(this.worldRegistryKey)));

            if (nonDungeonWorld != null && nonDungeonWorld.method_8321(this.portalPos) != null && nonDungeonWorld.method_8321(this.portalPos) instanceof DungeonPortalEntity) {
                ((DungeonPortalEntity) nonDungeonWorld.method_8321(this.portalPos)).finishDungeon((class_3218) this.method_37908(), this.method_24515());
            } else {
                this.method_37908().method_8501(this.method_24515(), BlockInit.DUNGEON_PORTAL.method_9564());
            }

        }
        super.method_6078(damageSource);

    }

    @Override
    public void setBoss(class_2338 portalPos, String worldRegistryKey) {
        this.isDungeonBossEntity = true;
        this.portalPos = portalPos;
        this.worldRegistryKey = worldRegistryKey;
    }

}
