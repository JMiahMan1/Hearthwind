package net.travelerz.config;

import java.util.ArrayList;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.Config.Gui.Background;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(
   name = "travelerz"
)
@Background("minecraft:textures/block/stone.png")
public class TravelerzConfig implements ConfigData {
   public float titleSize = 1.0F;
   public int titleX = 0;
   public int titleY = 0;
   @Comment("Entry example: minecraft:plains")
   public ArrayList<String> excludedBiomes = new ArrayList<>();
}
