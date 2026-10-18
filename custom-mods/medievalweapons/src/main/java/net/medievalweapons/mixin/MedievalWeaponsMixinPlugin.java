package net.medievalweapons.mixin;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MedievalWeaponsMixinPlugin implements IMixinConfigPlugin {
   public void onLoad(String mixinPackage) {
   }

   public String getRefMapperConfig() {
      return null;
   }

   public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
      return (
               mixinClassName.contains("BigAxeItemMixin")
                  || mixinClassName.contains("DaggerItemMixin")
                  || mixinClassName.contains("FranciscaItemMixin")
                  || mixinClassName.contains("HealingStaffItemMixin")
                  || mixinClassName.contains("JavelinItemMixin")
                  || mixinClassName.contains("LanceItemMixin")
                  || mixinClassName.contains("LongSwordItemMixin")
                  || mixinClassName.contains("MaceItemMixin")
                  || mixinClassName.contains("NinjatoItemMixin")
                  || mixinClassName.contains("RapierItemMixin")
                  || mixinClassName.contains("SickleItemMixin")
                  || mixinClassName.contains("SmallAxeItemMixin")
            )
            && !FabricLoader.getInstance().isModLoaded("betternether")
         ? false
         : !mixinClassName.contains("ServerPlayNetworkHandlerMixin") && !mixinClassName.contains("GameRendererMixin")
            || FabricLoader.getInstance().isModLoaded("reach-entity-attributes");
   }

   public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
   }

   public List<String> getMixins() {
      return null;
   }

   public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }

   public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
   }
}
