package net.satisfy.herbalbrews.core.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.satisfy.herbalbrews.client.gui.handler.CauldronGuiHandler;
import net.satisfy.herbalbrews.core.registry.EntityTypeRegistry;
import net.satisfy.herbalbrews.core.registry.ObjectRegistry;
import net.satisfy.herbalbrews.core.world.ImplementedInventory;
import net.satisfy.herbalbrews.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class CauldronBlockEntity extends BlockEntity implements ImplementedInventory, BlockEntityTicker<CauldronBlockEntity>, MenuProvider {
    public static final int CAPACITY = 5;
    private static final int[] SLOTS_FOR_SIDE = new int[]{0, 4};
    private static final int[] SLOTS_FOR_UP = new int[]{1, 2};
    private static final int[] SLOTS_FOR_DOWN = new int[]{3};
    private NonNullList<ItemStack> inventory;
    private int brewingTime = 0;
    private int totalBrewingTime;
    private final ContainerData propertyDelegate = new ContainerData() {

        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> CauldronBlockEntity.this.brewingTime;
                case 1 -> CauldronBlockEntity.this.totalBrewingTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> CauldronBlockEntity.this.brewingTime = value;
                case 1 -> CauldronBlockEntity.this.totalBrewingTime = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    public CauldronBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.CAULDRON_BLOCK_ENTITY.get(), pos, state);
        this.inventory = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.inventory);
        this.brewingTime = input.getIntOr("BrewingTime", 0);
        this.totalBrewingTime = input.getIntOr("TotalBrewingTime", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.inventory);
        output.putInt("BrewingTime", this.brewingTime);
        output.putInt("TotalBrewingTime", this.totalBrewingTime);
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, CauldronBlockEntity blockEntity) {
        if (world.isClientSide()) return;
        boolean dirty = false;
        if (canCraft()) {
            brewingTime++;
            if (brewingTime >= totalBrewingTime) {
                brewingTime = 0;
                craft(world);
                dirty = true;
            }
        } else {
            brewingTime = 0;
        }
        if (dirty) {
            setChanged();
        }
    }

    private boolean canCraft() {
        for (int i = 0; i < 3; i++) {
            ItemStack stack = this.getItem(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof PotionItem)) {
                return false;
            }
        }
        ItemStack catalyst = this.getItem(4);
        if (catalyst.isEmpty() || catalyst.getItem() != ObjectRegistry.HERBAL_INFUSION.get()) {
            return false;
        }
        ItemStack output = this.getItem(3);
        return output.isEmpty();
    }

    private void craft(Level world) {
        List<MobEffectInstance> combinedEffects = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ItemStack potionStack = this.getItem(i);
            List<MobEffectInstance> potionEffects = StreamSupport
                    .stream(potionStack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY)
                            .getAllEffects().spliterator(), false).toList();
            combinedEffects.addAll(potionEffects);
        }

        /**
         * @author wdog5
         * @reason allow potion without effect craft
         */
        /*
        if (combinedEffects.isEmpty()) {
            return;
        }*/

        Map<Holder<MobEffect>, MobEffectInstance> uniqueEffectsMap = new HashMap<>();
        for (MobEffectInstance effectInstance : combinedEffects) {
            Holder<MobEffect> effect = effectInstance.getEffect();
            if (!uniqueEffectsMap.containsKey(effect) || effectInstance.getAmplifier() > uniqueEffectsMap.get(effect).getAmplifier()) {
                uniqueEffectsMap.put(effect, new MobEffectInstance(effect, effectInstance.getDuration(), effectInstance.getAmplifier()));
            }
        }

        ItemStack outputPotion = new ItemStack(ObjectRegistry.FLASK.get());
        PotionContents potionContents = outputPotion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);

        RandomSource random = world != null ? world.getRandom() : RandomSource.create();
        int randomTexture = random.nextInt(6) + 1;
        outputPotion.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of((float) randomTexture), List.of(), List.of(), List.of()));

        for (MobEffectInstance effectInstance : uniqueEffectsMap.values()) {
            potionContents = potionContents.withEffectAdded(effectInstance);
        }

        outputPotion.set(DataComponents.POTION_CONTENTS, potionContents);
        setItem(3, outputPotion);

        for (int i = 0; i < 3; i++) {
            this.removeItem(i, 1);
        }
        this.removeItem(4, 1);
    }

    @Override
    public int @NotNull [] getSlotsForFace(Direction side) {
        if (side.equals(Direction.UP)) {
            return SLOTS_FOR_UP;
        } else if (side.equals(Direction.DOWN)) {
            return SLOTS_FOR_DOWN;
        } else {
            return SLOTS_FOR_SIDE;
        }
    }

    @Override
    public NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        final ItemStack stackInSlot = this.inventory.get(slot);
        boolean dirty = !stack.isEmpty() && ItemStack.isSameItem(stack, stackInSlot) && ItemStack.matches(stack, stackInSlot);
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        if (slot < 3) {
            if (!dirty) {
                this.totalBrewingTime = PlatformHelper.getBrewingDuration();
                this.brewingTime = 0;
                setChanged();
            }
        }
        if (slot == 4) {
            if (!dirty) {
                setChanged();
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        assert this.level != null;
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double) this.worldPosition.getX() + 0.5,
                    (double) this.worldPosition.getY() + 0.5,
                    (double) this.worldPosition.getZ() + 0.5) <= 64.0;
        }
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new CauldronGuiHandler(syncId, inv, this, this.propertyDelegate);
    }
}
