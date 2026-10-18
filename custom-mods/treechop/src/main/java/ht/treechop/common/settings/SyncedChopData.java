package ht.treechop.common.settings;

import ht.treechop.TreeChop;
import ht.treechop.common.config.ConfigHandler;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;

public class SyncedChopData {
   protected static final String CHOPPING_ENABLED_KEY = "choppingEnabled";
   protected static final String SNEAK_BEHAVIOR_KEY = "sneakBehavior";
   protected static final String TREES_MUST_HAVE_LEAVES_KEY = "treesMustHaveLeaves";
   protected static final String CHOP_IN_CREATIVE_MODE_KEY = "chopInCreativeMode";
   protected static final String IS_SYNCED_KEY = "isSynced";
   private final ChopSettings settings;
   private boolean isSynced = false;

   public SyncedChopData(ChopSettings settings) {
      this.settings = settings;
   }

   public ChopSettings getSettings() {
      return this.settings;
   }

   public boolean isSynced() {
      return this.isSynced;
   }

   public void setSynced() {
      this.isSynced = true;
   }

   public CompoundTag makeSaveData() {
      CompoundTag nbt = new CompoundTag();
      nbt.putBoolean("choppingEnabled", this.settings.getChoppingEnabled());
      nbt.putString("sneakBehavior", this.settings.getSneakBehavior().name());
      nbt.putBoolean("treesMustHaveLeaves", this.settings.getTreesMustHaveLeaves());
      nbt.putBoolean("chopInCreativeMode", this.settings.getChopInCreativeMode());
      nbt.putBoolean("isSynced", this.isSynced());
      return nbt;
   }

   public SyncedChopData readSaveData(CompoundTag tag) {
      if (tag.contains("isSynced")) {
         Optional<Boolean> choppingEnabled = this.getBoolean(tag, "choppingEnabled");
         Optional<Boolean> onlyChopTreesWithLeaves = this.getBoolean(tag, "treesMustHaveLeaves");
         Optional<Boolean> chopInCreativeMode = this.getBoolean(tag, "chopInCreativeMode");
         Optional<Boolean> isSynced = this.getBoolean(tag, "isSynced");
         SneakBehavior defaultSneakBehavior = ConfigHandler.defaultChopSettings.get().getSneakBehavior();
         String sneakBehaviorId = tag.getStringOr("sneakBehavior", "");
         if (sneakBehaviorId.isEmpty()) {
            this.settings.setSneakBehavior(defaultSneakBehavior);
         } else {
            SneakBehavior sneakBehavior;
            try {
               sneakBehavior = SneakBehavior.valueOf(sneakBehaviorId);
            } catch (IllegalArgumentException var10) {
               TreeChop.LOGGER
                  .warn(
                     String.format(
                        "NBT contains bad sneak behavior value \"%s\"; using default value \"%s\"",
                        tag.getStringOr("sneakBehavior", ""),
                        defaultSneakBehavior.name()
                     )
                  );
               sneakBehavior = defaultSneakBehavior;
            }

            this.settings.setSneakBehavior(sneakBehavior);
         }

         this.settings.setChoppingEnabled(choppingEnabled.orElse(this.settings.getChoppingEnabled()));
         this.settings.setTreesMustHaveLeaves(onlyChopTreesWithLeaves.orElse(this.settings.getTreesMustHaveLeaves()));
         this.settings.setChopInCreativeMode(chopInCreativeMode.orElse(this.settings.getChopInCreativeMode()));
         if (isSynced.orElse(false)) {
            this.setSynced();
         }
      }

      return this;
   }

   protected Optional<Boolean> getBoolean(net.minecraft.nbt.CompoundTag CompoundTag, String key) {
      return CompoundTag.getBoolean(key);
   }
}
