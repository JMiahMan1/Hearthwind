package io.wispforest.lavender.book;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface ClientNewEntriesUnlockedCallback {

    Event<ClientNewEntriesUnlockedCallback> EVENT = EventFactory.createArrayBacked(ClientNewEntriesUnlockedCallback.class, callbacks -> (client, book, newEntryCount) -> {
        for (var callback : callbacks) {
            callback.newEntriesUnlocked(client, book, newEntryCount);
        }
    });

    void newEntriesUnlocked(Minecraft client, Book book, int newEntryCount);
}
