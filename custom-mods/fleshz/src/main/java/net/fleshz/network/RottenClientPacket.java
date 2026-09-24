package net.fleshz.network;

import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fleshz.init.RecipeInit;
import net.fleshz.network.packet.RackPacket;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

@Environment(EnvType.CLIENT)
public class RottenClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(RackPacket.TYPE, (payload, context) -> {
            List<Identifier> rackItems = payload.rackItems();
            List<Identifier> rackResultItems = payload.rackResultItems();
            List<Integer> rackResultTimes = payload.rackResultTimes();

            context.client().execute(() -> {
                RecipeInit.RACK_ITEM_LIST.clear();
                RecipeInit.RACK_RESULT_ITEM_LIST.clear();
                RecipeInit.RACK_RESULT_TIME_LIST.clear();

                for (Identifier id : rackItems) {
                    BuiltInRegistries.ITEM.getOptional(id)
                            .ifPresent(item -> RecipeInit.RACK_ITEM_LIST.add((Item) item));
                }
                for (Identifier id : rackResultItems) {
                    BuiltInRegistries.ITEM.getOptional(id)
                            .ifPresent(item -> RecipeInit.RACK_RESULT_ITEM_LIST.add((Item) item));
                }
                RecipeInit.RACK_RESULT_TIME_LIST.addAll(rackResultTimes);
            });
        });
    }
}
