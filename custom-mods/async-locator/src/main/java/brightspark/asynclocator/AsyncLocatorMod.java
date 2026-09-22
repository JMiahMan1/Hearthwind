package brightspark.asynclocator;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class AsyncLocatorMod implements ModInitializer {
	@Override
	public void onInitialize() {
		ALConfig.init();

		ServerLifecycleEvents.SERVER_STARTING.register(server -> AsyncLocator.setupExecutorService());
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> AsyncLocator.shutdownExecutorService());
	}
}
