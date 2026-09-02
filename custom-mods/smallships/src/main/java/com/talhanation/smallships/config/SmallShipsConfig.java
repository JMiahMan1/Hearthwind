package com.talhanation.smallships.config;

import com.talhanation.smallships.world.entity.ship.Ship;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SmallShipsConfig {

    public static class ConfigValue<T> {
        private final T val;
        public ConfigValue(T val) { this.val = val; }
        public T get() { return val; }
    }

    public static class Common {
        public static ConfigValue<Integer> schematicVersion = new ConfigValue<>(5);
        public static ConfigValue<Integer> shipGeneralSailCooldown = new ConfigValue<>(30);
        public static ConfigValue<Double> shipGeneralCollisionDamage = new ConfigValue<>(7.5D);
        public static ConfigValue<Boolean> shipGeneralCollisionKnockBack = new ConfigValue<>(true);
        public static ConfigValue<Boolean> shipGeneralDoItemDrop = new ConfigValue<>(true);
        public static ConfigValue<Double> shipGeneralContainerModifier = new ConfigValue<>(10.0D);
        public static ConfigValue<Double> shipGeneralCannonModifier = new ConfigValue<>(2.5D);
        public static ConfigValue<Double> shipGeneralPaddlingModifier = new ConfigValue<>(35.0D);
        public static ConfigValue<Double> shipGeneralBiomeModifier = new ConfigValue<>(20.0D);
        public static ConfigValue<List<String>> mountBlackList = new ConfigValue<>(Arrays.asList("minecraft:ender_dragon", "minecraft:wither", "minecraft:ghast", "minecraft:warden", "alexmobs:cachalot_whale"));
        public static ConfigValue<Double> shipGeneralShieldDamageReduction = new ConfigValue<>(3.0D);
        public static ConfigValue<Double> shipGeneralDespawnTimeSunken = new ConfigValue<>(15.0D);
        public static ConfigValue<Double> shipGeneralCannonDamage = new ConfigValue<>(25.0D);
        public static ConfigValue<Double> shipGeneralCannonDestruction = new ConfigValue<>(1.0D);

        // Cog
        public static ConfigValue<Double> shipAttributeCogMaxHealth = new ConfigValue<>(300.0D);
        public static ConfigValue<Double> shipAttributeCogMaxSpeed = new ConfigValue<>(30.0D);
        public static ConfigValue<Double> shipAttributeCogMaxReverseSpeed = new ConfigValue<>(0.1D);
        public static ConfigValue<Double> shipAttributeCogMaxRotationSpeed = new ConfigValue<>(4.5D);
        public static ConfigValue<Double> shipAttributeCogAcceleration = new ConfigValue<>(0.015D);
        public static ConfigValue<Double> shipAttributeCogRotationAcceleration = new ConfigValue<>(0.7D);
        public static ConfigValue<Integer> shipContainerCogContainerSize = new ConfigValue<>(108);
        public static ConfigValue<Ship.BiomeModifierType> shipModifierCogBiome = new ConfigValue<>(Ship.BiomeModifierType.COLD);

        // Brigg
        public static ConfigValue<Double> shipAttributeBriggMaxHealth = new ConfigValue<>(450.0D);
        public static ConfigValue<Double> shipAttributeBriggMaxSpeed = new ConfigValue<>(35.0D);
        public static ConfigValue<Double> shipAttributeBriggMaxReverseSpeed = new ConfigValue<>(0.1D);
        public static ConfigValue<Double> shipAttributeBriggMaxRotationSpeed = new ConfigValue<>(4.0D);
        public static ConfigValue<Double> shipAttributeBriggAcceleration = new ConfigValue<>(0.015D);
        public static ConfigValue<Double> shipAttributeBriggRotationAcceleration = new ConfigValue<>(0.55D);
        public static ConfigValue<Integer> shipContainerBriggContainerSize = new ConfigValue<>(162);
        public static ConfigValue<Ship.BiomeModifierType> shipModifierBriggBiome = new ConfigValue<>(Ship.BiomeModifierType.COLD);

        // Galley
        public static ConfigValue<Double> shipAttributeGalleyMaxHealth = new ConfigValue<>(200.0D);
        public static ConfigValue<Double> shipAttributeGalleyMaxSpeed = new ConfigValue<>(30.0D);
        public static ConfigValue<Double> shipAttributeGalleyMaxReverseSpeed = new ConfigValue<>(0.1D);
        public static ConfigValue<Double> shipAttributeGalleyMaxRotationSpeed = new ConfigValue<>(5.0D);
        public static ConfigValue<Double> shipAttributeGalleyAcceleration = new ConfigValue<>(0.015D);
        public static ConfigValue<Double> shipAttributeGalleyRotationAcceleration = new ConfigValue<>(1.00D);
        public static ConfigValue<Integer> shipContainerGalleyContainerSize = new ConfigValue<>(54);
        public static ConfigValue<Ship.BiomeModifierType> shipModifierGalleyBiome = new ConfigValue<>(Ship.BiomeModifierType.WARM);

        // Drakkar
        public static ConfigValue<Double> shipAttributeDrakkarMaxHealth = new ConfigValue<>(200.0D);
        public static ConfigValue<Double> shipAttributeDrakkarMaxSpeed = new ConfigValue<>(30.0D);
        public static ConfigValue<Double> shipAttributeDrakkarMaxReverseSpeed = new ConfigValue<>(0.1D);
        public static ConfigValue<Double> shipAttributeDrakkarMaxRotationSpeed = new ConfigValue<>(5.0D);
        public static ConfigValue<Double> shipAttributeDrakkarAcceleration = new ConfigValue<>(0.015D);
        public static ConfigValue<Double> shipAttributeDrakkarRotationAcceleration = new ConfigValue<>(1.00D);
        public static ConfigValue<Integer> shipContainerDrakkarContainerSize = new ConfigValue<>(54);
        public static ConfigValue<Ship.BiomeModifierType> shipModifierDrakkarBiome = new ConfigValue<>(Ship.BiomeModifierType.COLD);

        // Water Animals
        public static ConfigValue<Double> waterAnimalFleeRadius = new ConfigValue<>(15.0D);
        public static ConfigValue<Double> waterAnimalFleeSpeed = new ConfigValue<>(1.5D);
        public static ConfigValue<Double> waterAnimalFleeDistance = new ConfigValue<>(10.0D);

        public static ConfigValue<Boolean> smallshipsItemGroupEnable = new ConfigValue<>(true);
    }

    public static class Client {
        public static ConfigValue<Integer> schematicVersion = new ConfigValue<>(2);
        public static ConfigValue<Boolean> shipGeneralCameraZoomEnable = new ConfigValue<>(true);
        public static ConfigValue<Boolean> shipGeneralCameraAutoThirdPerson = new ConfigValue<>(true);
        public static ConfigValue<Double> shipGeneralCameraZoomMax = new ConfigValue<>(20.0D);
        public static ConfigValue<Double> shipGeneralCameraZoomMin = new ConfigValue<>(5.0D);
        public static ConfigValue<Integer> shipModSpeedUnit = new ConfigValue<>(0);
    }
}
