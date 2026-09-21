package net.satisfy.brewery.core.event.brew_event;

import net.minecraft.resources.Identifier;
import net.satisfy.brewery.Brewery;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("unused")

public class BrewEvents {
    
    public static final Map<Identifier, Supplier<BrewEvent>> BREW_EVENTS = new HashMap<>();

    public static final Identifier KETTLE_EVENT = registerBrewEvent("kettle", KettleEvent::new);
    public static final Identifier OVEN_EVENT = registerBrewEvent("oven", OvenEvent::new);
    public static final Identifier WHISTLE_EVENT = registerBrewEvent("whistle", WhistleEvent::new);
    public static final Identifier TIMER_EVENT = registerBrewEvent("timer", TimerEvent::new);

    public static void loadClass() {
    }

    public static Identifier registerBrewEvent(String id, Supplier<BrewEvent> brewEventSupplier) {
        Identifier resourceLocation = Brewery.identifier(id);
        BREW_EVENTS.put(resourceLocation, brewEventSupplier);
        return resourceLocation;
    }

    public static List<Identifier> toLocations(Collection<BrewEvent> events) {
        return events.stream().map(BrewEvents::getId).collect(Collectors.toList());
    }

    public static List<BrewEvent> toEvents(List<Supplier<BrewEvent>> events) {
        return events.stream().map(Supplier::get).collect(Collectors.toList());
    }

    @Nullable
    public static Identifier getId(BrewEvent event) {
        if (event == null) {
            return null;
        }
        for (Identifier location : BREW_EVENTS.keySet()) {
            BrewEvent brewEvent = BREW_EVENTS.get(location).get();
            if (event.getClass() == brewEvent.getClass()) {
                return location;
            }
        }
        return null;
    }


    @Nullable
    public static Supplier<BrewEvent> byId(Identifier id) {
        return BREW_EVENTS.get(id);
    }
}
