package net.smitherz;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.smitherz.init.CommandInit;
import net.smitherz.init.ConfigInit;
import net.smitherz.init.EventInit;
import net.smitherz.init.ItemInit;
import net.smitherz.init.LoaderInit;
import net.smitherz.init.ScreenInit;
import net.smitherz.init.SoundInit;
import net.smitherz.init.TagInit;
import net.smitherz.network.SmitherServerPacket;

public class SmitherzMain implements ModInitializer {
   public static final Map<String, Integer> upgradeSlotMap = new HashMap<>();
   public static final Map<net.minecraft.world.entity.EntityType<?>, HashMap<Integer, Map<net.minecraft.world.item.Item, Float>>> gemDropMap = new HashMap<>();
   public static final Map<net.minecraft.world.entity.EntityType<?>, HashMap<Float, Map<net.minecraft.world.item.Item, Float>>> gemRpgDropMap = new HashMap<>();
   public static final boolean isTieredLoaded = FabricLoader.getInstance().isModLoaded("tiered");

   public void onInitialize() {
      ConfigInit.init();
      ItemInit.init();
      LoaderInit.init();
      TagInit.init();
      ScreenInit.init();
      EventInit.init();
      SmitherServerPacket.init();
      CommandInit.init();
      SoundInit.init();
   }

   public static Identifier identifierOf(String name) {
      return Identifier.fromNamespaceAndPath("smitherz", name);
   }
}
