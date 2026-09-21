package io.github.mortuusars.exposure;

import io.github.mortuusars.exposure.fabric.RegisterImpl;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.triggers.CriterionTrigger;
import net.minecraft.advancements.predicates.entity.EntitySubPredicate;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Register {
    public static <T extends Block> Supplier<T> block(String id, Function<ResourceKey<Block>, T> factory) {
        return RegisterImpl.block(id, factory);
    }

    public static <T extends BlockEntityType<E>, E extends BlockEntity> Supplier<T> blockEntityType(String id, Supplier<T> sup) {
        return RegisterImpl.blockEntityType(id, sup);
    }

    public static <T extends BlockEntity> BlockEntityType<T> newBlockEntityType(BlockEntitySupplier<T> blockEntitySupplier, Block... validBlocks) {
        return RegisterImpl.newBlockEntityType(blockEntitySupplier, validBlocks);
    }

    @FunctionalInterface
    public interface BlockEntitySupplier<T extends BlockEntity> {

        @NotNull T create(BlockPos pos, BlockState state);
    }

    public static <T extends Item> Supplier<T> item(String id, Function<Item.Properties, T> factory) {
        return RegisterImpl.item(id, factory);
    }

    public static <T extends CreativeModeTab> Supplier<T> creativeTab(String id, Supplier<T> supplier) {
        return RegisterImpl.creativeTab(id, supplier);
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory,
                                                                        MobCategory category, float width, float height,
                                                                        int clientTrackingRange, boolean velocityUpdates, int updateInterval) {
        return RegisterImpl.entityType(id, factory, category, width, height, clientTrackingRange, velocityUpdates, updateInterval);
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id, EntityType.EntityFactory<T> factory, MobCategory category,
                                                                        boolean receiveVelocityUpdates, Consumer<EntityType.Builder<T>> typeBuilder) {
        return RegisterImpl.entityType(id, factory, category, receiveVelocityUpdates, typeBuilder);
    }

    public static <T extends SoundEvent> Supplier<T> soundEvent(String id, Supplier<T> supplier) {
        return RegisterImpl.soundEvent(id, supplier);
    }

    @SuppressWarnings("unchecked")
    public static <T extends MenuType<E>, E extends AbstractContainerMenu> Supplier<T> menuType(String id, MenuTypeSupplier<E> supplier) {
        return (Supplier<T>) (Supplier<?>) RegisterImpl.menuType(id, supplier);
    }

    @FunctionalInterface
    public interface MenuTypeSupplier<T extends AbstractContainerMenu> {
        @NotNull T create(int windowId, Inventory playerInv, RegistryFriendlyByteBuf extraData);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Recipe<I>, I extends RecipeInput> Supplier<RecipeType<T>> recipeType(String name, Supplier<RecipeType<T>> supplier) {
        Supplier<RecipeType<?>> adapted = supplier::get;
        return (Supplier<RecipeType<T>>) (Supplier<?>) RegisterImpl.recipeType(name, adapted);
    }

    public static Supplier<RecipeSerializer<?>> recipeSerializer(String name, Supplier<RecipeSerializer<?>> supplier) {
        return RegisterImpl.recipeSerializer(name, supplier);
    }

    public static <T extends CriterionTrigger<?>> Supplier<T> criterionTrigger(String name, Supplier<T> supplier) {
        return RegisterImpl.criterionTrigger(name, supplier);
    }

    public static <T extends DataComponentPredicate.Type<?>> Supplier<T> itemSubPredicate(String name, Supplier<T> supplier) {
        return RegisterImpl.itemSubPredicate(name, supplier);
    }

    public static <T extends EntitySubPredicate> Supplier<MapCodec<T>> entitySubPredicate(String name, Supplier<MapCodec<T>> supplier) {
        return RegisterImpl.entitySubPredicate(name, supplier);
    }

    public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>>
    Supplier<ArgumentTypeInfo<A, T>> commandArgumentType(String id, Class<A> infoClass, I argumentTypeInfo) {
        return RegisterImpl.commandArgumentType(id, infoClass, argumentTypeInfo);
    }

    public static <T extends FeatureConfiguration> Supplier<Feature<?>> worldGenFeature(String name, Supplier<Feature<T>> featureSupplier) {
        return RegisterImpl.worldGenFeature(name, featureSupplier);
    }

    public static <T> DataComponentType<T> dataComponentType(String name, Consumer<DataComponentType.Builder<T>> builderConsumer) {
        return RegisterImpl.dataComponentType(name, builderConsumer);
    }

    public static <T extends ParticleType<? extends ParticleOptions>> Supplier<T> particleType(String name, Supplier<T> supplier) {
        return RegisterImpl.particleType(name, supplier);
    }
}
