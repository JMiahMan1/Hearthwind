package ht.treechop.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import ht.treechop.client.gui.screen.ClientSettingsScreen;

public class ModMenu implements ModMenuApi {
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return ClientSettingsScreen::new;
   }
}
