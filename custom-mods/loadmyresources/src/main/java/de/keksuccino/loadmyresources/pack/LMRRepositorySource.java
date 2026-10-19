package de.keksuccino.loadmyresources.pack;

import java.util.function.Consumer;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.RepositorySource;
import org.jetbrains.annotations.NotNull;

public class LMRRepositorySource implements RepositorySource {
   public void loadPacks(@NotNull Consumer<Pack> consumer) {
      PackHandler.prepareResourcesFolder();
      Pack p = PackHandler.createPack(this.createSupplier());
      consumer.accept(p);
   }

   protected Pack.ResourcesSupplier createSupplier() {
      return new Pack.ResourcesSupplier() {
         @NotNull
         public PackResources openPrimary(@NotNull PackLocationInfo info) {
            return new LMRPackResources();
         }

         @NotNull
         public PackResources openFull(@NotNull PackLocationInfo info, @NotNull Pack.Metadata metadata) {
            return new LMRPackResources();
         }
      };
   }
}
