package net.nameplate.config;

import java.util.ArrayList;
import java.util.List;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.Config.Gui.Background;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Gui.RequiresRestart;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(
   name = "nameplate"
)
@Background("minecraft:textures/block/stone.png")
public class NameplateConfig implements ConfigData {
   @Comment("Calculates Level: Real HP / Old HP * multiplier - multiplier + 1\nIf RpgDifficulty installed: Real HP / Old HP = Health Modificator")
   public int levelMultiplier = 10;
   @Comment("0xAARRGGBB Format")
   public int nameColor = 553648127;
   public int backgroundColor = -1;
   public float backgroundOpacity = 0.4F;
   @Comment("Added onto the mob height")
   public float nameplateHeight = 0.5F;
   @Comment("Smaller float will increase size")
   public float nameplateSize = -0.025F;
   public boolean showHealth = false;
   @Comment("Setting if for example WTHIT is installed")
   public boolean showLevel = true;
   public double squaredDistance = 128.0;
   @Comment("Might increase performance when true")
   public boolean showNameplateIfObstructed = false;
   @Comment("Exclusively use rpgdifficulty level when installed")
   public boolean useRpgDifficultyLvl = true;
   @Comment("Travelers title compat")
   public boolean levelTitle = true;
   @Comment("Show a health bar over mobs")
   public boolean healthBar = false;
   @RequiresRestart
   @Comment("Example: minecraft:zombie or adventurez:brown_fungus")
   public ArrayList<String> excludedEntities = new ArrayList<>(
      List.of("minecraft:ender_dragon", "minecraft:wither", "adventurez:stone_golem", "adventurez:void_shadow", "adventurez:the_eye")
   );
}
