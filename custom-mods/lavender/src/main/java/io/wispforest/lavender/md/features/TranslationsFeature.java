package io.wispforest.lavender.md.features;

import io.wispforest.lavendermd.MarkdownFeature;
import io.wispforest.lavendermd.compiler.MarkdownCompiler;

public class TranslationsFeature implements MarkdownFeature {
    @Override
    public String name() {
        return "translations";
    }

    @Override
    public boolean supportsCompiler(MarkdownCompiler<?> compiler) {
        return true;
    }

    @Override
    public void registerTokens(TokenRegistrar registrar) {
        // v1 26.2: no translation tokens yet
    }

    @Override
    public void registerNodes(NodeRegistrar registrar) {
        // v1 26.2: no translation nodes yet
    }
}
