package draylar.inmis.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The contents of a backpack item. Serialised both to item data (persistent
 * codec) and across the network so an opened backpack shows its contents.
 */
public final class BackpackComponent {

    public static final Codec<BackpackComponent> CODEC = ItemStack.OPTIONAL_CODEC.listOf()
            .xmap(BackpackComponent::new, component -> component.container.getItems());

    public static final StreamCodec<RegistryFriendlyByteBuf, BackpackComponent> STREAM_CODEC =
            ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(BackpackComponent::new, component -> component.container.getItems());

    private final SimpleContainer container;

    public BackpackComponent(SimpleContainer container) {
        this.container = container;
    }

    public BackpackComponent(List<ItemStack> stacks) {
        this.container = new SimpleContainer(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            this.container.setItem(i, stacks.get(i));
        }
    }

    public SimpleContainer getContainer() {
        return container;
    }
}
