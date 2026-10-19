package de.keksuccino.loadmyresources.pack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources.ResourceOutput;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class LMRPackResources extends PathPackResources {
   private static final Logger LOGGER = LogManager.getLogger("loadmyresources/LMRPackResources");
   private static final boolean ON_WINDOWS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
   private final File file = PackHandler.resourcesDirectory;

   public LMRPackResources() {
      super(PackHandler.LOCATION, PackHandler.resourcesDirectory.toPath());
   }

   @Override
   public <T> T getMetadataSection(MetadataSectionType<T> type) throws IOException {
      return null;
   }

   @NotNull
   @Override
   public Set<String> getNamespaces(@NotNull PackType type) {
      Set<String> namespaces = new HashSet<>();
      File[] files = this.file.listFiles(File::isDirectory);
      if (files != null) {
         for (File f : files) {
            String path = getRelativePath(this.file, f);
            if (path.equals(path.toLowerCase(Locale.ROOT))) {
               namespaces.add(path.substring(0, path.length() - 1));
            } else {
               this.logWarning(path);
            }
         }
      }

      return namespaces;
   }

   @Nullable
   @Override
   public IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull Identifier location) {
      try {
         File f = this.getFile(getPathFromLocation(location));
         if (f != null) {
            return IoSupplier.create(f.toPath());
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return null;
   }

   @Nullable
   private File getFile(String path) {
      try {
         File resource = new File(this.file, path);
         if (resource.isFile() && validatePath(resource, path)) {
            return resource;
         }
      } catch (IOException var3) {
      }

      return null;
   }

   public static boolean validatePath(File file, String path) throws IOException {
      String canonical = file.getCanonicalPath();
      if (ON_WINDOWS) {
         canonical = canonical.replace('\\', '/');
      }

      return canonical.endsWith(path);
   }

   private static String getPathFromLocation(Identifier location) {
      return String.format("%s/%s", location.getNamespace(), location.getPath());
   }

   @Override
   public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String resourcePrefix, @NotNull ResourceOutput output) {
      this.listFiles(new File(new File(this.file, namespace), resourcePrefix), namespace, resourcePrefix + "/", output);
   }

   private void listFiles(File file, String namespace, String prefix, ResourceOutput output) {
      File[] files = file.listFiles();
      if (files != null) {
         for (File child : files) {
            if (child.isDirectory()) {
               this.listFiles(child, namespace, prefix + child.getName() + "/", output);
            } else if (!child.getName().endsWith(".mcmeta")) {
               try {
                  String path = prefix + child.getName();
                  Identifier location = Identifier.tryBuild(namespace, path);
                  if (location == null) {
                     LOGGER.warn("Invalid path in resource pack: {}:{}, ignoring", namespace, path);
                  } else {
                     output.accept(location, IoSupplier.create(child.toPath()));
                  }
               } catch (Exception var11) {
                  LOGGER.error(var11.getMessage());
               }
            }
         }
      }
   }

   protected static String getRelativePath(File parent, File child) {
      return parent.toURI().relativize(child.toURI()).getPath();
   }

   protected void logWarning(String path) {
      LOGGER.warn("ResourcePack: ignored non-lowercase namespace: {} in {}", path, this.file);
   }
}
