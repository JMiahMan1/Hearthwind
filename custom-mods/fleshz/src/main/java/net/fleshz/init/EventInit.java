package net.fleshz.init;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fleshz.network.packet.RackPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public class EventInit {

    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            List<Identifier> rackItems = new ArrayList<>();
            List<Identifier> rackResultItems = new ArrayList<>();

            for (int i = 0; i < RecipeInit.RACK_ITEM_LIST.size(); i++) {
                rackItems.add(BuiltInRegistries.ITEM.getKey(RecipeInit.RACK_ITEM_LIST.get(i)));
            }
            for (int i = 0; i < RecipeInit.RACK_RESULT_ITEM_LIST.size(); i++) {
                rackResultItems.add(BuiltInRegistries.ITEM.getKey(RecipeInit.RACK_RESULT_ITEM_LIST.get(i)));
            }
            ServerPlayNetworking.send(handler.player, new RackPacket(rackItems, rackResultItems, RecipeInit.RACK_RESULT_TIME_LIST));
        });
    }
}
