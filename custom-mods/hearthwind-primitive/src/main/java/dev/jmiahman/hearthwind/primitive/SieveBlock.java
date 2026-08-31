package dev.jmiahman.hearthwind.primitive;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Sieve: place a siftable block (dirt, gravel, sand, clay - anything with a
 * drop template in {@code data/earlystage/sieve_drops}) on the mesh, then tap
 * it with any hand four times to shake the good stuff out (earlystage
 * parity; the drop tables ship as data files).
 */
public class SieveBlock extends Block implements EntityBlock {

    public static final ResourceKey<Block> KEY =
            ResourceKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath("earlystage", "sieve"));

    public static final net.minecraft.world.level.block.state.properties.EnumProperty<net.minecraft.core.Direction> FACING =
            BlockStateProperties.HORIZONTAL_FACING;

    static final List<SieveDrop> DROPS = new ArrayList<>();

    public SieveBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(defaultBlockState().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    public record SieveDrop(Identifier blockId, List<SieveEntry> entries) {}

    public record SieveEntry(Identifier itemId, float chance, int rolls) {}

    public static int dropCount() {
        return DROPS.size();
    }

    public static List<SieveDrop> drops() {
        return DROPS;
    }

    /** The siftable item for a template: the block item registered under block_id. */
    public static net.minecraft.world.item.Item templateItem(SieveDrop drop) {
        return BuiltInRegistries.ITEM.getOptional(drop.blockId()).orElse(Items.AIR);
    }

    public static boolean isSiftable(ItemStack stack) {
        for (SieveDrop drop : DROPS) {
            if (stack.is(templateItem(drop))) {
                return true;
            }
        }
        return false;
    }

    public static void loadDrops() {
        try {
            java.nio.file.Path root = net.fabricmc.loader.api.FabricLoader.getInstance()
                    .getModContainer("hearthwind_primitive")
                    .flatMap(c -> c.findPath("data/earlystage/sieve_drops"))
                    .orElse(null);
            if (root == null) {
                HearthwindPrimitive.LOGGER.warn("sieve_drops directory not found");
                return;
            }
            List<java.nio.file.Path> files;
            try (var stream = java.nio.file.Files.walk(root)) {
                files = stream
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .sorted()
                        .toList();
            }
            for (java.nio.file.Path path : files) {
                loadSieveDrops(path);
            }
            HearthwindPrimitive.LOGGER.info("Loaded {} sieve drop entries", DROPS.size());
        } catch (Exception e) {
            HearthwindPrimitive.LOGGER.error("Failed to load sieve drops", e);
        }
    }

    private static void loadSieveDrops(java.nio.file.Path path) {
        try {
            String text = java.nio.file.Files.readString(path, StandardCharsets.UTF_8);
            com.google.gson.JsonObject root = JsonParser.parseString(text).getAsJsonObject();
            JsonArray drops = root.getAsJsonArray("drops");
            if (drops == null) return;
            for (JsonElement dropEl : drops) {
                com.google.gson.JsonObject drop = dropEl.getAsJsonObject();
                Identifier blockId = Identifier.parse(drop.get("block_id").getAsString());
                JsonArray blockDrops = drop.getAsJsonArray("block_drops");
                if (blockDrops == null) continue;
                List<SieveEntry> entries = new ArrayList<>();
                for (JsonElement entryEl : blockDrops) {
                    com.google.gson.JsonObject entry = entryEl.getAsJsonObject();
                    entries.add(new SieveEntry(
                            Identifier.parse(entry.get("item_id").getAsString()),
                            (float) entry.get("chance").getAsDouble(),
                            entry.get("rolls").getAsInt()));
                }
                boolean replace = drop.has("replace") && drop.get("replace").getAsBoolean();
                if (replace) {
                    DROPS.removeIf(existing -> existing.blockId().equals(blockId));
                }
                DROPS.add(new SieveDrop(blockId, entries));
            }
        } catch (Exception e) {
            HearthwindPrimitive.LOGGER.error("Failed to parse sieve drops: {}", e.getMessage());
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SieveBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, net.minecraft.world.level.block.Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, net.minecraft.world.level.block.Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, net.minecraft.world.entity.player.Player player,
            net.minecraft.world.InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof SieveBlockEntity sieve) {
            ItemStack blockStack = sieve.getItem(0);
            if (blockStack.isEmpty()) {
                if (sieve.canPlaceItem(0, player.getItemInHand(hand))) {
                    if (!level.isClientSide()) {
                        sieve.setItem(0, new ItemStack(player.getItemInHand(hand).getItem(), 1));
                        if (!player.hasInfiniteMaterials()) {
                            player.getItemInHand(hand).shrink(1);
                        }
                    }
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            } else {
                sieve.sieve();
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos,
            Block neighborBlock,
            net.minecraft.world.level.redstone.Orientation orientation, boolean notify) {
        if (!level.isClientSide() && !level.getBlockState(pos.above()).isAir()
                && level.getBlockEntity(pos) instanceof SieveBlockEntity sieve) {
            Containers.dropContents(level, pos, sieve);
            sieve.clearContent();
        }
        super.neighborChanged(state, level, pos, neighborBlock, orientation, notify);
    }
}
