package net.satisfy.nethervinery.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.satisfy.nethervinery.core.NetherVinery;
import net.satisfy.nethervinery.core.effect.*;

import java.util.function.Supplier;

public class NetherEffects {

    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(NetherVinery.MODID, Registries.MOB_EFFECT);
    private static final Registrar<MobEffect> MOB_EFFECTS_REGISTRAR = MOB_EFFECTS.getRegistrar();

    public static final RegistrySupplier<MobEffect> GRAVEDIGGER;
    public static final RegistrySupplier<MobEffect> HEARTHSTONE;
    public static final RegistrySupplier<MobEffect> IMPROVED_GRAVEDIGGER;
    public static final RegistrySupplier<MobEffect> IMPROVED_HEARTHSTONE;
    public static final RegistrySupplier<MobEffect> NETHERITE;


    private static RegistrySupplier<MobEffect> registerEffect(String name, Supplier<MobEffect> effect){
        return MOB_EFFECTS_REGISTRAR.register(Identifier.fromNamespaceAndPath(NetherVinery.MODID, name), effect);
    }

    public static void init(){
        NetherVinery.LOGGER.debug("Mob effects");
        MOB_EFFECTS.register();
    }

    static {
        GRAVEDIGGER = registerEffect("gravedigger", GravediggerEffect::new);
        HEARTHSTONE = registerEffect("hearthstone", HearthstoneEffect::new);
        IMPROVED_GRAVEDIGGER = registerEffect("improved_gravedigger", ImprovedGravediggerEffect::new);
        IMPROVED_HEARTHSTONE = registerEffect("improved_hearthstone", ImprovedHearthstoneEffect::new);
        NETHERITE = registerEffect("netherite", NetheriteEffect::new);

    }
}
