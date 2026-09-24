package net.adventurez.init;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class ModelInit {
    private static final SelectItemModelProperty.Type<LavaLightProperty, Float> LAVA_LIGHT_TYPE =
            SelectItemModelProperty.Type.create(MapCodec.unit(new LavaLightProperty()), Codec.FLOAT);

    public static void init() {
        SelectItemModelProperties.ID_MAPPER.put(Identifier.fromNamespaceAndPath("adventurez", "lavalight"), LAVA_LIGHT_TYPE);
    }

    private static final class LavaLightProperty implements SelectItemModelProperty<Float> {
        @Override
        public Float get(ItemStack stack, ClientLevel level, LivingEntity entity, int seed, ItemDisplayContext displayContext) {
            return stack.get(ItemInit.LAVA_LIGHT) != null && stack.get(ItemInit.LAVA_LIGHT) ? 1.0F : 0.0F;
        }

        @Override
        public Codec<Float> valueCodec() {
            return Codec.FLOAT;
        }

        @Override
        public SelectItemModelProperty.Type<LavaLightProperty, Float> type() {
            return LAVA_LIGHT_TYPE;
        }
    }
}
