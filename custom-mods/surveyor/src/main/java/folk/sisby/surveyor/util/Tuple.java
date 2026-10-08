package folk.sisby.surveyor.util;

/**
 * 26.2 shim for {@code net.minecraft.util.Tuple}, which 26.2 removed with no
 * replacement in the game (there is no net.minecraft Pair either). Surveyor
 * only uses it as an (A, B) carrier for a StreamCodec, so this keeps the
 * removed class's exact API - getA()/getB() - and the ported call sites read
 * unchanged.
 */
public record Tuple<A, B>(A a, B b) {
    public A getA() {
        return this.a;
    }

    public B getB() {
        return this.b;
    }
}
