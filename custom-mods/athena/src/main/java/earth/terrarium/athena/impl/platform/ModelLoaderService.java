package earth.terrarium.athena.impl.platform;

import earth.terrarium.athena.api.client.models.AthenaModelFactory;
import earth.terrarium.athena.api.client.utils.AthenaUnbakedModelLoader;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@PlatformService
public interface ModelLoaderService {

    AthenaUnbakedModelLoader register(Identifier type, AthenaModelFactory factory);

    static ModelLoaderService create() {
        // Fabric-only fork: architectury's @ExpectPlatform transformer is not
        // on the classpath, so wire the Fabric implementation directly.
        return new ModelLoaderServiceFabricImpl();
    }
}
