package com.github.legoatoom.connectiblechains.util;

import com.github.legoatoom.connectiblechains.util.Pair;

/**
 * 26.2 shim. Upstream uses Yarn's {@code net.minecraft.util.Pair} (Mojmap:
 * {@code net.minecraft.util.Tuple}, which 26.2 removed without a replacement),
 * carrying values as left/right. This keeps upstream's constructor and accessor
 * shape so the ported call sites read exactly as the original source does.
 */
public record Pair<A, B>(A left, B right) {
    public A getLeft() {
        return this.left;
    }

    public B getRight() {
        return this.right;
    }
}
