package net.adventurez.block.entity;

import java.util.Optional;

import net.adventurez.init.BlockInit;
import net.adventurez.init.ItemInit;
import net.adventurez.init.SoundInit;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestLidController;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class ShadowChestEntity extends RandomizableContainerBlockEntity implements LidBlockEntity {

    private final ChestLidController lidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        @Override
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundInit.OPEN_SHADOW_CHEST_EVENT, SoundSource.BLOCKS, 0.5F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void onClose(Level level, BlockPos pos, BlockState state) {
            level.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, SoundInit.CLOSE_SHADOW_CHEST_EVENT, SoundSource.BLOCKS, 0.5F,
                    level.getRandom().nextFloat() * 0.1F + 0.9F);
        }

        @Override
        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
            level.blockEvent(pos, BlockInit.SHADOW_CHEST, 1, newCount);
        }

        @Override
        public boolean isOwnContainer(Player player) {
            return player.containerMenu instanceof ChestMenu chestMenu && chestMenu.getContainer() == ShadowChestEntity.this;
        }
    };
    private NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);

    public ShadowChestEntity(BlockPos pos, BlockState state) {
        super(BlockInit.SHADOW_CHEST_ENTITY, pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, ShadowChestEntity blockEntity) {
        blockEntity.lidController.tickLid();
        if (blockEntity.openersCounter.getOpenerCount() > 0) {
            for (int i = 0; i < 3; ++i) {
                int j = level.getRandom().nextInt(2) * 2 - 1;
                int k = level.getRandom().nextInt(2) * 2 - 1;
                double d = (double) pos.getX() + 0.5D + 0.25D * (double) j;
                double e = (double) ((float) pos.getY() + level.getRandom().nextFloat());
                double f = (double) pos.getZ() + 0.5D + 0.25D * (double) k;
                double g = (double) level.getRandom().nextFloat() * (float) j * 0.1D;
                double h = (double) level.getRandom().nextFloat() * 0.4D;
                double l = (double) level.getRandom().nextFloat() * (float) k * 0.1D;
                level.addParticle(ParticleTypes.END_ROD, d, e, f, g, h, l);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, this.inventory);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(input)) {
            ContainerHelper.loadAllItems(input, this.inventory);
        }
    }

    @Override
    public boolean triggerEvent(int type, int data) {
        if (type == 1) {
            this.lidController.shouldBeOpen(data > 0);
            return true;
        } else {
            return super.triggerEvent(type, data);
        }
    }

    @Override
    public void startOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!this.isRemoved() && user instanceof Player player && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState(), player.getContainerInteractionRange());
        }
    }

    @Override
    public void stopOpen(net.minecraft.world.entity.ContainerUser user) {
        if (!this.isRemoved() && user instanceof Player player && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
        if (this.isEmpty()) {
            this.getLevel().destroyBlock(this.getBlockPos(), true);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.getLevel().getBlockEntity(this.getBlockPos()) != this) {
            return false;
        }
        return !(player.distanceToSqr((double) this.getBlockPos().getX() + 0.5D, (double) this.getBlockPos().getY() + 0.5D, (double) this.getBlockPos().getZ() + 0.5D) > 64.0D);
    }

    public void onScheduledTick() {
        if (!this.isRemoved()) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public float getOpenNess(float tickDelta) {
        return this.lidController.getOpenness(tickDelta);
    }

    @Override
    protected net.minecraft.network.chat.Component getDefaultName() {
        return net.minecraft.network.chat.Component.translatable("adventurez.container.shadowchest");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return ChestMenu.threeRows(containerId, playerInventory, this);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public void setRandomLoot() {
        if (this.getLevel().isClientSide()) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel) this.getLevel();
        LootTable table = serverLevel.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.END_CITY_TREASURE);
        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.getBlockPos()))
                .create(LootContextParamSets.CHEST);
        ObjectArrayList<ItemStack> tableList = table.getRandomItems(lootParams);

        for (int i = 0; i < 27; i++) {
            if (i == 20 && this.getLevel().getRandom().nextFloat() < 0.2F && FabricLoader.getInstance().isModLoaded("medievalweapons")) {
                this.inventory.set(i, new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .get(net.minecraft.resources.Identifier.fromNamespaceAndPath("medievalweapons", "thalleous_sword"))
                        .orElseThrow()));
                continue;
            }
            if (i == 13)
                this.inventory.set(i, new ItemStack(ItemInit.SOURCE_STONE));
            else if (i == 1)
                this.inventory.set(i, new ItemStack(Items.DRAGON_EGG));
            else {
                switch (this.getLevel().getRandom().nextInt(10)) {
                    case 0:
                        this.inventory.set(i, new ItemStack(Items.ENDER_PEARL, this.getLevel().getRandom().nextInt(5) + 1));
                        break;
                    case 4:
                        this.inventory.set(i, new ItemStack(Items.GOLDEN_APPLE, 1));
                        break;
                    case 5:
                        this.inventory.set(i, new ItemStack(Items.EXPERIENCE_BOTTLE, this.getLevel().getRandom().nextInt(12) + 1));
                        break;
                    case 6:
                        ItemStack stack = new ItemStack(Items.ENCHANTED_BOOK);
                        RegistryAccess registryAccess = this.getLevel().registryAccess();
                        Optional<Holder.Reference<Enchantment>> optional = registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
                                .getRandom(this.getLevel().getRandom());
                        if (optional.isPresent()) {
                            stack.enchant(optional.get(), optional.get().value().getMaxLevel() + 1);
                        }
                        stack.set(ItemInit.VOID_DROP, true);
                        this.inventory.set(i, stack);
                        break;
                    case 7:
                        if (!tableList.isEmpty()) {
                            this.inventory.set(i, tableList.get(this.getLevel().getRandom().nextInt(tableList.size())));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

}
