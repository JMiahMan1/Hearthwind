package ht.treechop.common.config.resource;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;

public class ResourceNamespaceIdentifier extends ResourceIdentifier {
   public ResourceNamespaceIdentifier(String namespace, List<IdentifierQualifier> qualifiers, String string) {
      super(namespace, "", qualifiers, string);
   }

   @Override
   public <R extends net.minecraft.core.DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
      return registry.stream().filter(resource -> {
         Identifier loc = registry.getKey(resource);
         return loc != registry.getDefaultKey() && loc.getNamespace().equals(this.getNamespace());
      });
   }
}
