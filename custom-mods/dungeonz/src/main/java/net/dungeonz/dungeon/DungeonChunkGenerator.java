package net.dungeonz.dungeon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.class_1311;
import net.minecraft.class_1959;
import net.minecraft.class_1972;
import net.minecraft.class_1992;
import net.minecraft.class_2338;
import net.minecraft.class_2680;
import net.minecraft.class_2791;
import net.minecraft.class_2794;
import net.minecraft.class_2893.class_2894;
import net.minecraft.class_2902.class_2903;
import net.minecraft.class_3233;
import net.minecraft.class_3485;
import net.minecraft.class_4543;
import net.minecraft.class_4966;
import net.minecraft.class_5138;
import net.minecraft.class_5281;
import net.minecraft.class_5455;
import net.minecraft.class_5483;
import net.minecraft.class_5539;
import net.minecraft.class_6012;
import net.minecraft.class_6748;
import net.minecraft.class_6880;
import net.minecraft.class_6903;
import net.minecraft.class_7138;
import net.minecraft.class_7869;

public class DungeonChunkGenerator extends class_2794 {

    public static final MapCodec<DungeonChunkGenerator> CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(class_6903.method_46637(class_1972.field_9451)).apply(instance, instance.stable(DungeonChunkGenerator::new)));

    public DungeonChunkGenerator(class_6880.class_6883<class_1959> biomeEntry) {
        super(new class_1992(biomeEntry));
    }

    @Override
    protected MapCodec<? extends class_2794> method_28506() {
        return CODEC;
    }

    @Override
    public void method_12107(class_3233 region) {
    }

    @Override
    public void method_12102(class_5281 world, class_2791 chunk, class_5138 structureAccessor) {
    }

    @Override
    public void method_16129(class_5455 registryManager, class_7869 placementCalculator, class_5138 structureAccessor, class_2791 chunk,
            class_3485 structureTemplateManager) {
    }

    @Override
    public void method_16130(class_5281 world, class_5138 accessor, class_2791 chunk) {
    }

    @Override
    public class_6012<class_5483.class_1964> method_12113(class_6880<class_1959> biome, class_5138 accessor, class_1311 group, class_2338 pos) {
        return class_6012.method_34988(new ArrayList<class_5483.class_1964>());
    }

    @Override
    public int method_12104() {
        return 256;
    }

    @Override
    public int method_16398() {
        return 0;
    }

    @Override
    public int method_33730() {
        return 0;
    }

    @Override
    public void method_12108(class_3233 chunkRegion, long l, class_7138 noiseConfig, class_4543 biomeAccess, class_5138 structureAccessor, class_2791 chunk, class_2894 carver) {
    }

    @Override
    public void method_12110(class_3233 chunkRegion, class_5138 structureAccessor, class_7138 noiseConfig, class_2791 chunk) {
    }

    @Override
    public CompletableFuture<class_2791> method_12088(class_6748 blender, class_7138 noiseConfig, class_5138 structureAccessor, class_2791 chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int method_16397(int x, int z, class_2903 type, class_5539 heightLimitView, class_7138 noiseConfig) {
        return 0;
    }

    @Override
    public class_4966 method_26261(int x, int z, class_5539 var3, class_7138 var4) {
        return new class_4966(z, new class_2680[0]);
    }

    @Override
    public void method_40450(List<String> list, class_7138 noiseConfig, class_2338 blockPos) {
    }

}