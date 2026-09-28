package com.teamremastered.tlc.mixin;

import com.teamremastered.tlc.config.ConfigHandler;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cancels vanilla stronghold generation when the config asks for it.  Aged
 * 3.1.2 shipped the castle mod with strongholds off, so this reproduces that
 * world-generation difference.
 */
@Mixin(ChunkGenerator.class)
public class DisableVanillaStrongholdsMixin {
    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void disableVanillaStrongholds(StructureSet.StructureSelectionEntry structureSetEntry,
                                           StructureManager structureManager,
                                           RegistryAccess registryAccess,
                                           RandomState randomState,
                                           StructureTemplateManager structureTemplateManager,
                                           long seed,
                                           ChunkAccess chunkAccess,
                                           ChunkPos chunkPos,
                                           SectionPos sectionPos,
                                           ResourceKey<Level> level,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (ConfigHandler.isDisableVanillaStronghold()
                && structureSetEntry.structure().value().type() == StructureType.STRONGHOLD) {
            cir.setReturnValue(false);
        }
    }
}
