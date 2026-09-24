package net.fleshz.init;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fleshz.loader.RackRecipeLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;

public class RecipeInit {

    public static final List<Item> RACK_ITEM_LIST = new ArrayList<>();
    public static final List<Item> RACK_RESULT_ITEM_LIST = new ArrayList<>();
    public static final List<Integer> RACK_RESULT_TIME_LIST = new ArrayList<>();

    public static void init() {
        ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new RackRecipeLoader());
    }
}
