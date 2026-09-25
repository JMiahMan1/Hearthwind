package net.satisfy.nethervinery.core;

import net.satisfy.nethervinery.core.event.CommonEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;
import net.satisfy.nethervinery.core.registry.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetherVinery {
    public static final String MODID = "nethervinery";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static void init() {
        NetherObjectRegistry.init();
        // 26.x validates a block entity against its type's allowed blocks.
        // These plain BarrelBlocks reuse vanilla's BARREL entity, so placing
        // one crashed with "Invalid block entity minecraft:barrel".
        FabricBlockEntityType barrel = (FabricBlockEntityType) BlockEntityTypes.BARREL;
        barrel.addValidBlock(NetherObjectRegistry.CRIMSON_BARREL.get());
        barrel.addValidBlock(NetherObjectRegistry.WARPED_BARREL.get());
        // Nether lattices are vinery LatticeBlocks: allow them on vinery's
        // lattice entity type. Fabric does not order entrypoints by
        // dependency, so vinery may register after us; listen() fires when
        // the type registers (or at once if it already has).
        net.satisfy.vinery.core.registry.EntityTypeRegistry.LATTICE.listen(type -> {
            FabricBlockEntityType lattice = (FabricBlockEntityType) type;
            lattice.addValidBlock(NetherObjectRegistry.CRIMSON_LATTICE.get());
            lattice.addValidBlock(NetherObjectRegistry.WARPED_LATTICE.get());
        });
        NetherEntityTypeRegistry.init();
        NetherTabRegistry.init();
        NetherGrapeTypes.addGrapeAttributes();
        NetherEffects.init();
        CommonEvents.init();
    }
}
