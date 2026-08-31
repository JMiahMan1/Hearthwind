package dev.jmiahman.hearthwind.world.fauna;

import java.util.HashMap;
import java.util.Map;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Complete Naturalist Wildlife & Animal AI / Behavior Engine (Aged 3.1.2 Parity).
 */
public final class NaturalistFauna {
    public static final String MOD_ID = "naturalist";

    public static final Map<String, Item> ITEMS = new HashMap<>();
    public static final Map<String, Block> BLOCKS = new HashMap<>();
    public static final Map<String, EntityType<?>> ENTITIES = new HashMap<>();

    public static final FoodProperties MEAT_FOOD = new FoodProperties.Builder().nutrition(3).saturationModifier(0.3f).build();
    public static final FoodProperties COOKED_MEAT_FOOD = new FoodProperties.Builder().nutrition(8).saturationModifier(0.8f).build();

    private NaturalistFauna() {}

    public static void registerAll() {
        // 1. Snails & Critters
        registerSnail("snail", 0.4f, 0.3f);
        registerHerbivore("butterfly", 0.4f, 0.4f, 4.0, 0.25);
        registerHerbivore("firefly", 0.3f, 0.3f, 2.0, 0.25);
        registerHerbivore("caterpillar", 0.3f, 0.2f, 4.0, 0.15);

        // 2. Temperate & Forest Animals
        registerHerbivore("deer", 0.9f, 1.4f, 16.0, 0.30);
        registerHerbivore("duck", 0.4f, 0.5f, 6.0, 0.22);
        registerPredator("bear", 1.4f, 1.4f, 30.0, 0.28, 6.0);
        registerHerbivore("boar", 0.9f, 0.9f, 14.0, 0.25);

        // 3. Savanna & Safari Animals
        registerHerbivore("zebra", 1.4f, 1.6f, 20.0, 0.28);
        registerHerbivore("giraffe", 1.6f, 3.8f, 32.0, 0.26);
        registerHerbivore("elephant", 2.4f, 2.8f, 50.0, 0.22);
        registerHerbivore("rhino", 1.8f, 1.6f, 40.0, 0.25);
        registerPredator("lion", 1.2f, 1.1f, 28.0, 0.32, 7.0);
        registerPredator("vulture", 0.8f, 0.8f, 12.0, 0.28, 3.0);
        registerPredator("rattlesnake", 0.6f, 0.3f, 10.0, 0.22, 3.0);

        // 4. Jungle & Swamp Animals
        registerPredator("alligator", 1.4f, 0.7f, 35.0, 0.24, 7.0);
        registerHerbivore("lizard", 0.5f, 0.3f, 8.0, 0.26);
        registerHerbivore("tortoise", 0.8f, 0.5f, 20.0, 0.15);
        registerPredator("snake", 0.6f, 0.3f, 10.0, 0.24, 3.0);
        registerPredator("coral_snake", 0.5f, 0.3f, 8.0, 0.24, 4.0);
        registerHerbivore("hippo", 1.8f, 1.4f, 45.0, 0.24);

        // 5. Water & River Creatures
        registerFish("bass", 0.5f, 0.4f, 6.0, 0.25);
        registerFish("catfish", 0.6f, 0.4f, 8.0, 0.22);

        // 6. Food, Meat & Item Drops
        registerFood("venison", MEAT_FOOD);
        registerFood("cooked_venison", COOKED_MEAT_FOOD);
        registerFood("duck", MEAT_FOOD);
        registerFood("cooked_duck", COOKED_MEAT_FOOD);
        registerFood("boar_chop", MEAT_FOOD);
        registerFood("cooked_boar_chop", COOKED_MEAT_FOOD);
        registerFood("alligator_tail", MEAT_FOOD);
        registerFood("cooked_alligator_tail", COOKED_MEAT_FOOD);
        registerFood("catfish", MEAT_FOOD);
        registerFood("cooked_catfish", COOKED_MEAT_FOOD);
        registerFood("bass", MEAT_FOOD);
        registerFood("cooked_bass", COOKED_MEAT_FOOD);
        registerFood("snail", MEAT_FOOD);
        registerFood("cooked_snail", COOKED_MEAT_FOOD);
        registerFood("hippo_meat", MEAT_FOOD);
        registerFood("cooked_hippo_meat", COOKED_MEAT_FOOD);
        registerFood("lizard_tail", MEAT_FOOD);
        registerFood("cooked_lizard_tail", COOKED_MEAT_FOOD);

        // Utility, Drops & Shell Items
        registerItem("snail_bucket");
        registerItem("snail_shell");
        registerItem("snail_mucus");
        registerItem("bear_fur");
        registerItem("deer_antler");
        registerItem("lion_mane");
        registerItem("elephant_tusk");
        registerItem("rhino_horn");
        registerItem("rattle");
        registerItem("vulture_feather");
        registerItem("caterpillar");
        registerItem("glow_goop");
        registerItem("duck_feather");
        registerItem("bug_net");

        // Blocks
        registerBlock("snail_shell_block", SoundType.STONE, 1.5f);
        registerBlock("snail_shell_bricks", SoundType.STONE, 2.0f);
        registerBlock("chiseled_snail_shell_bricks", SoundType.STONE, 2.0f);

        registerBiomeSpawns();
        registerAIBehaviors();
    }

    private static void registerSnail(String name, float width, float height) {
        ResourceKey<EntityType<?>> eKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
        EntityType<NaturalistEntities.SnailCritter> type = EntityType.Builder.<NaturalistEntities.SnailCritter>of(
                NaturalistEntities.SnailCritter::new, MobCategory.CREATURE)
                .sized(width, height)
                .build(eKey);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, eKey, type);
        FabricDefaultAttributeRegistry.register(type, NaturalistEntities.createHerbivoreAttributes(4.0, 0.12));
        ENTITIES.put(name, type);
        registerItem(name + "_spawn_egg");
    }

    private static void registerHerbivore(String name, float width, float height, double health, double speed) {
        ResourceKey<EntityType<?>> eKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
        EntityType<NaturalistEntities.HerbivoreAnimal> type = EntityType.Builder.<NaturalistEntities.HerbivoreAnimal>of(
                (t, l) -> new NaturalistEntities.HerbivoreAnimal(t, l, name), MobCategory.CREATURE)
                .sized(width, height)
                .build(eKey);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, eKey, type);
        FabricDefaultAttributeRegistry.register(type, NaturalistEntities.createHerbivoreAttributes(health, speed));
        ENTITIES.put(name, type);
        registerItem(name + "_spawn_egg");
    }

    private static void registerPredator(String name, float width, float height, double health, double speed, double attackDmg) {
        ResourceKey<EntityType<?>> eKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
        EntityType<NaturalistEntities.PredatorAnimal> type = EntityType.Builder.<NaturalistEntities.PredatorAnimal>of(
                (t, l) -> new NaturalistEntities.PredatorAnimal(t, l, name), MobCategory.CREATURE)
                .sized(width, height)
                .build(eKey);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, eKey, type);
        FabricDefaultAttributeRegistry.register(type, NaturalistEntities.createPredatorAttributes(health, speed, attackDmg));
        ENTITIES.put(name, type);
        registerItem(name + "_spawn_egg");
    }

    private static void registerFish(String name, float width, float height, double health, double speed) {
        ResourceKey<EntityType<?>> eKey = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name));
        EntityType<NaturalistEntities.AquaticFish> type = EntityType.Builder.<NaturalistEntities.AquaticFish>of(
                (t, l) -> new NaturalistEntities.AquaticFish(t, l, name), MobCategory.WATER_CREATURE)
                .sized(width, height)
                .build(eKey);
        Registry.register(BuiltInRegistries.ENTITY_TYPE, eKey, type);
        FabricDefaultAttributeRegistry.register(type, NaturalistEntities.createFishAttributes(health));
        ENTITIES.put(name, type);
        registerItem(name + "_spawn_egg");

        net.minecraft.world.entity.SpawnPlacements.register(type,
                net.minecraft.world.entity.SpawnPlacementTypes.IN_WATER,
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                NaturalistFauna::checkFishSpawnRules);
    }

    public static boolean checkFishSpawnRules(EntityType<? extends net.minecraft.world.entity.animal.fish.AbstractFish> entityType,
            net.minecraft.world.level.LevelAccessor levelAccessor,
            net.minecraft.world.entity.EntitySpawnReason spawnType,
            BlockPos blockPos,
            net.minecraft.util.RandomSource randomSource) {
        if (!levelAccessor.getFluidState(blockPos).is(net.minecraft.tags.FluidTags.WATER)
                || !levelAccessor.getBlockState(blockPos).is(net.minecraft.world.level.block.Blocks.WATER)) {
            return false;
        }
        var aboveState = levelAccessor.getBlockState(blockPos.above());
        return aboveState.is(net.minecraft.world.level.block.Blocks.WATER)
                || aboveState.is(net.minecraft.world.level.block.Blocks.ICE)
                || aboveState.is(net.minecraft.world.level.block.Blocks.FROSTED_ICE)
                || aboveState.is(net.minecraft.world.level.block.Blocks.PACKED_ICE)
                || aboveState.is(net.minecraft.world.level.block.Blocks.BLUE_ICE)
                || aboveState.isAir();
    }

    private static void registerBiomeSpawns() {
        // Temperate & Taiga: deer, duck, butterfly, firefly, bear, boar
        for (String mob : new String[] {"deer", "duck", "butterfly", "firefly", "bear", "boar"}) {
            EntityType<?> type = ENTITIES.get(mob);
            if (type != null) {
                BiomeModifications.addSpawn(
                        BiomeSelectors.tag(BiomeTags.IS_FOREST).or(BiomeSelectors.tag(BiomeTags.IS_TAIGA)),
                        MobCategory.CREATURE, type, 4, 1, 3);
            }
        }

        // Savannas & Badlands: zebra, giraffe, elephant, rhino, lion, vulture, rattlesnake
        for (String mob : new String[] {"zebra", "giraffe", "elephant", "rhino", "lion", "vulture", "rattlesnake"}) {
            EntityType<?> type = ENTITIES.get(mob);
            if (type != null) {
                BiomeModifications.addSpawn(
                        BiomeSelectors.tag(BiomeTags.IS_SAVANNA).or(BiomeSelectors.tag(BiomeTags.IS_BADLANDS)),
                        MobCategory.CREATURE, type, 3, 1, 2);
            }
        }

        // Jungles & Swamps: alligator, lizard, snake, coral_snake, snail, tortoise, hippo
        for (String mob : new String[] {"alligator", "lizard", "snake", "coral_snake", "snail", "tortoise"}) {
            EntityType<?> type = ENTITIES.get(mob);
            if (type != null) {
                BiomeModifications.addSpawn(
                        BiomeSelectors.tag(BiomeTags.IS_JUNGLE),
                        MobCategory.CREATURE, type, 4, 1, 2);
            }
        }

        // Swamps (hippo needs water adjacency — spawn in jungle+swamp overlap)
        EntityType<?> hippoType = ENTITIES.get("hippo");
        if (hippoType != null) {
            BiomeModifications.addSpawn(
                    BiomeSelectors.tag(BiomeTags.IS_JUNGLE)
                            .or(ctx -> ctx.getBiomeKey().identifier().getPath().contains("swamp")),
                    MobCategory.CREATURE, hippoType, 2, 1, 2);
        }

        // Rivers & Oceans: bass, catfish — WATER_CREATURE in all water bodies (including frozen rivers/oceans)
        for (String mob : new String[] {"bass", "catfish"}) {
            EntityType<?> type = ENTITIES.get(mob);
            if (type != null) {
                BiomeModifications.addSpawn(
                        ctx -> {
                            var key = ctx.getBiomeKey().identifier();
                            String path = key.getPath();
                            return ctx.hasTag(BiomeTags.IS_RIVER) || ctx.hasTag(BiomeTags.IS_OCEAN)
                                    || path.contains("river") || path.contains("ocean");
                        },
                        MobCategory.WATER_CREATURE, type, 5, 2, 5);
            }
        }
    }

    private static void registerAIBehaviors() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);
            BlockPos pos = hitResult.getBlockPos();

            if (held.getItem() == ITEMS.get("bug_net") || (held.isEmpty() && player.isShiftKeyDown())) {
                var state = level.getBlockState(pos);
                if (state.is(BlockTags.LEAVES) || state.is(BlockTags.CROPS) || state.is(BlockTags.FLOWERS)) {
                    if (!level.isClientSide() && level.getRandom().nextFloat() < 0.15f) {
                        String[] critterNames = {"caterpillar", "snail_mucus", "snail_shell", "glow_goop"};
                        String chosen = critterNames[level.getRandom().nextInt(critterNames.length)];
                        Item dropItem = ITEMS.get(chosen);
                        if (dropItem != null) {
                            ItemEntity drop = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, new ItemStack(dropItem));
                            level.addFreshEntity(drop);
                            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.2f);
                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });

        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            ItemStack held = player.getItemInHand(hand);

            if (entity instanceof NaturalistEntities.SnailCritter && held.getItem() == Items.WATER_BUCKET) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
                Item snailBucket = ITEMS.get("snail_bucket");
                if (snailBucket != null) {
                    player.getInventory().add(new ItemStack(snailBucket));
                    level.playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundSource.PLAYERS, 1.0f, 1.0f);
                    entity.discard();
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS;
        });
    }

    private static void registerItem(String name) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static void registerFood(String name, FoodProperties foodProps) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Item item = new Item(new Item.Properties().setId(key).food(foodProps));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        ITEMS.put(name, item);
    }

    private static void registerBlock(String name, SoundType sound, float hardness) {
        ResourceKey<Block> bKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
        Block block = new Block(BlockBehaviour.Properties.of().setId(bKey).sound(sound).strength(hardness));
        Registry.register(BuiltInRegistries.BLOCK, bKey, block);
        BLOCKS.put(name, block);

        ResourceKey<Item> iKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(iKey));
        Registry.register(BuiltInRegistries.ITEM, iKey, item);
        ITEMS.put(name, item);
    }
}
