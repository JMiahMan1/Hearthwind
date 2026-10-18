package net.voidz.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.Config.Gui.Background;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(
   name = "voidz"
)
@Background("minecraft:textures/block/stone.png")
public class VoidConfig implements ConfigData {
   public boolean allow_boss_respawn = false;
   @Comment("In ticks: 20 = 1sec")
   public int boss_respawn_time = 72000;
}
