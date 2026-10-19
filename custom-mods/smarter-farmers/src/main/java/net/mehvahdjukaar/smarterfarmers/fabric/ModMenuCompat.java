package net.mehvahdjukaar.smarterfarmers.fabric;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.mehvahdjukaar.smarterfarmers.SmarterFarmersConfig;

public class ModMenuCompat implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> AutoConfigClient.getConfigScreen(SmarterFarmersConfig.class, parent).get();
   }
}
