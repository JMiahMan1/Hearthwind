package net.mehvahdjukaar.smarterfarmers;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "smarterfarmers")
public class SmarterFarmersConfig implements ConfigData {
   @ConfigEntry.Gui.Tooltip
   public boolean pickupFoodOverride = true;
   @ConfigEntry.Gui.Tooltip
   public boolean eatFood = true;
   @ConfigEntry.Gui.Tooltip
   @ConfigEntry.BoundedDiscrete(min = 1, max = 1000)
   public int timeToHarvest = 40;
   @ConfigEntry.Gui.Tooltip
   public boolean debugRenderer = false;
   @ConfigEntry.Gui.Tooltip
   public boolean plantOnTillable = false;
}
