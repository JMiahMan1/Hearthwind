package net.satisfy.farm_and_charm.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public class SoupBubbleParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    protected SoupBubbleParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
        super(level, x, y, z, sprites.first());
        this.sprites = sprites;
        this.lifetime = 4;
        this.gravity = 0.008F;
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.yd -= this.gravity;
        this.move(this.xd, this.yd, this.zd);
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, net.minecraft.util.RandomSource random) {
            return new SoupBubbleParticle(level, x, y, z, velocityX, velocityY, velocityZ, this.sprites);
        }
    }
}