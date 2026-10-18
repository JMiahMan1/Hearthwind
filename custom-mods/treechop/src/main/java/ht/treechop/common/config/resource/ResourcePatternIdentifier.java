package ht.treechop.common.config.resource;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;

public class ResourcePatternIdentifier extends ResourceIdentifier {
   private final Pattern pattern;

   public ResourcePatternIdentifier(Pattern pattern, List<IdentifierQualifier> qualifiers, String string) {
      super("", "", qualifiers, string);
      this.pattern = pattern;
   }

   @Override
   public <R extends net.minecraft.core.DefaultedRegistry<T>, T> Stream<T> resolve(R registry) {
      return registry.stream().filter(resource -> {
         Identifier loc = registry.getKey(resource);
         return loc != registry.getDefaultKey() && this.pattern.matcher(loc.toString()).matches();
      });
   }
}
