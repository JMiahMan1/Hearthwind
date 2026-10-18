package ht.treechop.common.config.resource;

import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

public class ResourceTagIdentifier extends ResourceIdentifier {
   public ResourceTagIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
      super(nameSpace, localSpace, qualifiers, string);
   }

   @Override
   public <R extends net.minecraft.core.DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
      Identifier tagId = Identifier.tryParse(this.getNamespace() + ":" + this.getLocalSpace());
      if (tagId != null) {
         net.minecraft.tags.TagKey<T> tag = TagKey.create(registry.key(), tagId);
         return StreamSupport.stream(registry.getTagOrEmpty(tag).spliterator(), false).map(Holder::value);
      } else {
         this.parsingError(String.format("\"%s\" is not a valid resource location", this.getResourceLocation()));
         return Stream.empty();
      }
   }
}
