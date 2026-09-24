package net.adventurez.mixin.client;

public final class IronGolemRenderStateHelper {
    private static final ThreadLocal<Boolean> BLACKSTONED = new ThreadLocal<>();

    private IronGolemRenderStateHelper() {
    }

    public static void set(boolean blackstoned) {
        BLACKSTONED.set(blackstoned);
    }

    public static boolean isBlackstoned() {
        return Boolean.TRUE.equals(BLACKSTONED.get());
    }
}
