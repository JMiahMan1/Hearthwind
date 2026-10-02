package dev.jmiahman.hearthwind.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * Gives fabric's client-gametest dedicated server a realistic boot window.
 *
 * <p>{@code DedicatedServerImplUtil#start} waits
 * {@code serverFuture.get(10L, TimeUnit.SECONDS)} - a literal {@code 10},
 * hardcoded, not a system property and not a server property (its sibling
 * {@code setupServer(Properties)} only writes those into
 * {@code server.properties}). Ten seconds is enough for a vanilla test world
 * and impossible for this pack: 150+ mods have to be class-loaded and the
 * world generated first. Measured on this host: the dedicated server logs
 * {@code Starting minecraft server} roughly a second after the harness has
 * already given up, and reaches {@code Done (...)} several seconds later.
 *
 * <p>That is not a mod bug and not a flake - it is an unmeetable deadline, so
 * {@code PackServerConnectGameTests} failed on every local run while CI runners
 * (which finish the whole client job in minutes) passed. Raising the literal is
 * the only way to actually exercise the pack-server registry negotiation
 * locally.
 *
 * <p>The target is named as a STRING and annotated {@code @Pseudo} because
 * {@code fabric-client-gametest-api-v1} is only on the classpath of our TEST
 * pack - it is not a compile dependency of this module and it never ships to a
 * player. A class literal would not compile, and without {@code @Pseudo} the
 * mixin would fail to apply on a real install and crash the game.
 *
 * <p>The value is a ceiling, not a sleep: {@code CompletableFuture#get} returns
 * the moment the server is up, so a fast host still finishes immediately.
 */
@Pseudo
@Mixin(targets = "net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil", remap = false)
public class DedicatedServerBootWindowMixin {

    /** Seconds to allow the test dedicated server to reach its first tick. */
    private static final long BOOT_WINDOW_SECONDS = 420L;

    @ModifyConstant(method = "start", constant = @Constant(longValue = 10L), require = 1)
    private static long hearthwind$bootWindowSeconds(long original) {
        return BOOT_WINDOW_SECONDS;
    }
}
