package ht.treechop.common.config.resource;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;

public class SingleResourceIdentifier extends ResourceIdentifier {
   public SingleResourceIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
      super(nameSpace, localSpace, qualifiers, string);
   }

   @Override
   public <R extends net.minecraft.core.DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
      String resourceString = this.getNamespace() + ":" + this.getLocalSpace();
      Identifier key = Identifier.tryParse(resourceString);
      if (key != null) {
         if (registry.containsKey(key)) {
            T resource = (T)registry.get(key);
            Identifier defaultKey = registry.getDefaultKey();
            if (!registry.getKey(resource).equals(defaultKey) || key.equals(defaultKey)) {
               return Stream.of(resource);
            }
         }
      } else {
         this.parsingError(String.format("\"%s\" is not a valid resource location", this.getResourceLocation()));
      }

      return Stream.empty();
   }
}
