package ht.treechop.common.config.resource;

import ht.treechop.TreeChop;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class ResourceIdentifier {
   private static final Pattern PATTERN = Pattern.compile("^\\s*([#@])?([a-z0-9_\\-.]*(?=:))?:?([a-z0-9_\\-./]*)?$");
   private static final Pattern QUALIFIERS_PATTERN = Pattern.compile("^\\?(.*)$");
   private static final String DEFAULT_NAMESPACE = "minecraft";
   private final String nameSpace;
   private final String localSpace;
   private final List<IdentifierQualifier> qualifiers;
   private final String string;

   public ResourceIdentifier(String nameSpace, String localSpace, List<IdentifierQualifier> qualifiers, String string) {
      this.nameSpace = nameSpace;
      this.localSpace = localSpace;
      this.qualifiers = qualifiers;
      this.string = string;
   }

   public static ResourceIdentifier from(String string) {
      Matcher matcher = PATTERN.matcher(string);
      if (matcher.find()) {
         String searchSpace = Optional.ofNullable(matcher.group(1)).orElse("");
         String namespace = Optional.ofNullable(matcher.group(2)).orElse("");
         String localSpace = Optional.ofNullable(matcher.group(3)).orElse("");
         List<IdentifierQualifier> qualifiers = List.of();
         if (searchSpace.equals("#")) {
            return new ResourceTagIdentifier(either(namespace, "minecraft"), localSpace, qualifiers, string);
         } else if (searchSpace.equals("@")) {
            return (ResourceIdentifier)(namespace.equals("")
               ? new ResourceNamespaceIdentifier(localSpace, qualifiers, string)
               : new MalformedResourceIdentifier(string, "unqualified identifier does not match \"@mod\""));
         } else {
            return new SingleResourceIdentifier(either(namespace, "minecraft"), localSpace, qualifiers, string);
         }
      } else {
         try {
            Pattern pattern = Pattern.compile("^" + string.strip() + "$");
            return new ResourcePatternIdentifier(pattern, List.of(), string);
         } catch (PatternSyntaxException var6) {
            return new MalformedResourceIdentifier(
               string, "unqualified identifier does not match \"@mod\", \"#mod:tag\", or \"mod:id\", and is not a valid regular expression"
            );
         }
      }
   }

   private static List<IdentifierQualifier> parseQualifiers(String qualifiersString) {
      Matcher matcher = QUALIFIERS_PATTERN.matcher(qualifiersString);
      return matcher.find()
         ? Arrays.stream(matcher.group(1).split(",")).map(ResourceIdentifier::parseQualifier).collect(Collectors.toList())
         : Collections.emptyList();
   }

   private static IdentifierQualifier parseQualifier(String string) {
      String[] parts = string.split("=", 2);
      return parts.length <= 1 ? new IdentifierQualifier(string, null) : new IdentifierQualifier(parts[0], parts[1]);
   }

   private static String either(String string, String fallbackIfEmpty) {
      return string.equals("") ? fallbackIfEmpty : string;
   }

   private static void parsingError(String idString, String message) {
      TreeChop.LOGGER
         .warn(
            "Configuration issue: failed to parse \"{}\": {} (to silence this warning, find and delete \"{}\" in treechop-common.toml)",
            idString,
            message,
            idString
         );
   }

   public String getNamespace() {
      return this.nameSpace;
   }

   public String getLocalSpace() {
      return this.localSpace;
   }

   public List<IdentifierQualifier> getQualifiers() {
      return this.qualifiers;
   }

   public String getString() {
      return this.string;
   }

   public String getResourceLocation() {
      return String.format("%s:%s", this.getNamespace(), this.getLocalSpace());
   }

   public abstract <R extends net.minecraft.core.DefaultedRegistry<T>, T> Stream<T> resolve(R var1);

   public void parsingError(String message) {
      parsingError(this.getString(), message);
   }

   public Optional<String> getQualifier(String key) {
      return this.qualifiers.stream().filter(qualifier -> qualifier.getKey().equals(key)).map(IdentifierQualifier::getValue).findFirst();
   }

   public boolean hasQualifier(String key) {
      return this.qualifiers.stream().anyMatch(qualifier -> qualifier.getKey().equals(key));
   }
}
