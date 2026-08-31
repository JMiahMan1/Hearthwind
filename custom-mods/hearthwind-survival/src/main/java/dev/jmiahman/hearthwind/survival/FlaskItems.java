package dev.jmiahman.hearthwind.survival;

import java.util.function.Consumer;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;

/**
 * The five water flasks, registered under the original dehydration
 * namespace so migrated datapack files (job rewards, smithing gates)
 * resolve unchanged. Component presence on a stack means "carries
 * water"; an empty flask has no flask_data component at all.
 */
public final class FlaskItems {
    /** Drink consumable: vanilla hold-right-click flow, 1.6s, drink sounds. */
    public static final Consumable DRINK = Consumables.defaultDrink().build();

    public static final DataComponentType<FlaskData> FLASK_DATA = DataComponentType.<FlaskData>builder()
            .persistent(FlaskData.CODEC)
            .networkSynchronized(FlaskData.STREAM_CODEC)
            .build();

    public static final LeatherFlaskItem LEATHER_FLASK =
            new LeatherFlaskItem(props("leather_flask", false), 2);
    public static final LeatherFlaskItem IRON_LEATHER_FLASK =
            new LeatherFlaskItem(props("iron_leather_flask", false), 3);
    public static final LeatherFlaskItem GOLDEN_LEATHER_FLASK =
            new LeatherFlaskItem(props("golden_leather_flask", false), 4);
    public static final LeatherFlaskItem DIAMOND_LEATHER_FLASK =
            new LeatherFlaskItem(props("diamond_leather_flask", false), 5);
    public static final LeatherFlaskItem NETHERITE_LEATHER_FLASK =
            new LeatherFlaskItem(props("netherite_leather_flask", true), 6);

    private static Properties props(String path, boolean fireproof) {
        Properties props = new Properties().stacksTo(1).setId(key(path));
        return fireproof ? props.fireResistant() : props;
    }

    private static ResourceKey<Item> key(String path) {
        return ResourceKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("dehydration", path));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("dehydration", path);
    }

    public static void registerAll(Consumer<String> log) {
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id("flask_data"), FLASK_DATA);
        Registry.register(BuiltInRegistries.ITEM, id("leather_flask"), LEATHER_FLASK);
        Registry.register(BuiltInRegistries.ITEM, id("iron_leather_flask"), IRON_LEATHER_FLASK);
        Registry.register(BuiltInRegistries.ITEM, id("golden_leather_flask"), GOLDEN_LEATHER_FLASK);
        Registry.register(BuiltInRegistries.ITEM, id("diamond_leather_flask"), DIAMOND_LEATHER_FLASK);
        Registry.register(BuiltInRegistries.ITEM, id("netherite_leather_flask"), NETHERITE_LEATHER_FLASK);
        log.accept("[aged-survival] leather flasks registered");
    }

    /**
     * Called from ConsumableConsumeMixin when a flask drink finishes.
     * Applies hydration and quality-based thirst risk, decrements the fill
     * level and drops the flask_data component once the flask is empty.
     */
    public static ItemStack onFlaskConsumed(ServerPlayer player, ItemStack stack) {
        FlaskData data = stack.get(FLASK_DATA);
        if (data == null) {
            return stack;
        }
        HearthwindSurvivalConfig.Flask cfg = HearthwindSurvivalConfig.get().flask;
        HearthwindSurvivalThirst.addHydration(player, cfg.quench);
        if (!player.getAbilities().instabuild) {
            float chance;
            int amplifier;
            if (data.qualityLevel() == FlaskData.DIRTY) {
                chance = (float) cfg.dirtyThirstChance;
                amplifier = 1;
            } else if (data.qualityLevel() == FlaskData.IMPURIFIED) {
                chance = (float) cfg.impureThirstChance;
                amplifier = 0;
            } else {
                chance = 0f;
                amplifier = 0;
            }
            if (chance > 0f && player.getRandom().nextFloat() < chance) {
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        ThirstMobEffect.HOLDER, cfg.thirstDuration, amplifier));
            }
        }
        int newFill = data.fillLevel() - 1;
        if (newFill <= 0) {
            stack.remove(FLASK_DATA);
            stack.remove(net.minecraft.core.component.DataComponents.CONSUMABLE);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.GENERIC_DRINK.value(), net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 0.9f);
        }
        return newFill <= 0 ? stack : setFill(stack, newFill, data.qualityLevel());
    }

    public static ItemStack setFill(ItemStack stack, int fill, int quality) {
        stack.set(FLASK_DATA, new FlaskData(fill, quality));
        stack.set(net.minecraft.core.component.DataComponents.CONSUMABLE, DRINK);
        return stack;
    }

    private FlaskItems() {}
}
