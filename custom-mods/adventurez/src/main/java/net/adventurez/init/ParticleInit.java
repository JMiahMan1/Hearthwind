package net.adventurez.init;

import net.adventurez.AdventureMain;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SmokeParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.core.Registry;

public class ParticleInit {
    public static final SimpleParticleType AMETHYST_SHARD_PARTICLE = FabricParticleTypes.simple();
    public static final SimpleParticleType VOID_CLOUD_PARTICLE = FabricParticleTypes.simple();
    public static final SimpleParticleType SPRINT_PARTICLE = FabricParticleTypes.simple();
    public static final SimpleParticleType FART_PARTICLE = FabricParticleTypes.simple();

    public static void init() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, AdventureMain.identifierOf("amethyst_shard_particle"), AMETHYST_SHARD_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, AdventureMain.identifierOf("void_cloud_particle"), VOID_CLOUD_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, AdventureMain.identifierOf("sprint_particle"), SPRINT_PARTICLE);
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, AdventureMain.identifierOf("fart_particle"), FART_PARTICLE);
    }

    @Environment(EnvType.CLIENT)
    public static class FartParticle extends SmokeParticle {
        protected FartParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
            super(level, x, y, z, velocityX, velocityY, velocityZ, 1.0F, sprites);
            this.gravity = 0.5F;
            this.rCol = 0.0F;
            this.gCol = 0.0F;
            this.bCol = 0.0F;
        }

        @Override
        public SingleQuadParticle.Layer getLayer() {
            return SingleQuadParticle.Layer.TRANSLUCENT;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final FabricSpriteSet sprites;

            public Provider(FabricSpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
                return new FartParticle(level, x, y, z, velocityX, velocityY, velocityZ, this.sprites);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ShardParticle extends SingleQuadParticle {
        private final SpriteSet sprites;

        protected ShardParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
            super(level, x, y, z, velocityX, velocityY, velocityZ, sprites.first());
            this.sprites = sprites;
            this.friction = 0.96F;
            this.hasPhysics = false;
            this.quadSize *= 0.75F;
            this.setSpriteFromAge(sprites);
        }

        @Override
        protected int getLightCoords(float partialTick) {
            return LightCoordsUtil.lightCoordsWithEmission(super.getLightCoords(partialTick), 2);
        }

        @Override
        public void tick() {
            super.tick();
            this.setSpriteFromAge(this.sprites);
        }

        @Override
        public SingleQuadParticle.Layer getLayer() {
            return SingleQuadParticle.Layer.TRANSLUCENT;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final FabricSpriteSet sprites;

            public Provider(FabricSpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
                ShardParticle particle = new ShardParticle(level, x, y, z, 0.5D - random.nextDouble(), velocityY, 0.5D - random.nextDouble(), this.sprites);
                int color = random.nextInt(4);
                if (color == 0) {
                    particle.setColor(1.0F, 0.796F, 0.9F);
                } else if (color == 1) {
                    particle.setColor(0.392F, 0.278F, 0.619F);
                } else if (color == 2) {
                    particle.setColor(0.65F, 0.47F, 0.945F);
                } else {
                    particle.setColor(0.784F, 0.564F, 0.941F);
                }
                particle.yd *= 0.2D;
                if (velocityX == 0.0D && velocityZ == 0.0D) {
                    particle.xd *= 0.1D;
                    particle.zd *= 0.1D;
                }
                particle.setLifetime((int) (8.0D / (random.nextDouble() * 0.8D + 0.2D)));
                return particle;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class VoidCloudParticle extends SingleQuadParticle {
        private final double startX;
        private final double startY;
        private final double startZ;

        private VoidCloudParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
            super(level, x, y, z, sprites.first());
            this.startX = x;
            this.startY = y;
            this.startZ = z;
            this.xd = velocityX;
            this.yd = velocityY;
            this.zd = velocityZ;
            this.hasPhysics = false;
            this.quadSize = 0.5F * (this.random.nextFloat() * 0.05F + 0.4F);
            this.rCol = this.random.nextFloat() * 0.2F;
            this.gCol = this.random.nextFloat() * 0.2F;
            this.bCol = this.random.nextFloat() * 0.2F;
            this.lifetime = (int) (this.random.nextFloat() * 2.0F) + 10;
            this.setSpriteFromAge(sprites);
        }

        @Override
        public void move(double x, double y, double z) {
            this.setBoundingBox(this.getBoundingBox().move(x, y, z));
            this.setPos((this.getBoundingBox().minX + this.getBoundingBox().maxX) * 0.5D, this.getBoundingBox().minY, (this.getBoundingBox().minZ + this.getBoundingBox().maxZ) * 0.5D);
        }

        @Override
        public float getQuadSize(float partialTick) {
            float f = 1.0F - (this.age + partialTick) / this.lifetime;
            f *= f;
            return this.quadSize * (1.0F - f);
        }

        @Override
        protected int getLightCoords(float partialTick) {
            float f = this.age / (float) this.lifetime;
            f *= f;
            f *= f;
            return LightCoordsUtil.lightCoordsWithEmission(super.getLightCoords(partialTick), (int) (f * 15.0F));
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
            float f = this.age / (float) this.lifetime;
            float g = f;
            f = -f + f * f * 2.0F;
            f = 1.0F - f;
            this.x = this.startX + this.xd * f;
            this.y = this.startY + this.yd * f + 1.0F - g;
            this.z = this.startZ + this.zd * f;
        }

        @Override
        public SingleQuadParticle.Layer getLayer() {
            return SingleQuadParticle.Layer.OPAQUE;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final FabricSpriteSet sprites;

            public Provider(FabricSpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
                return new VoidCloudParticle(level, x, y, z, velocityX, velocityY, velocityZ, this.sprites);
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class SprintParticle extends SingleQuadParticle {
        private final SpriteSet sprites;

        protected SprintParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet sprites) {
            super(level, x, y, z, velocityX, velocityY, velocityZ, sprites.first());
            this.sprites = sprites;
            this.friction = 1.0F;
            this.hasPhysics = false;
            this.setSpriteFromAge(sprites);
        }

        @Override
        public void tick() {
            super.tick();
            this.setSpriteFromAge(this.sprites);
        }

        @Override
        public SingleQuadParticle.Layer getLayer() {
            return SingleQuadParticle.Layer.TRANSLUCENT;
        }

        public static class Provider implements ParticleProvider<SimpleParticleType> {
            private final FabricSpriteSet sprites;

            public Provider(FabricSpriteSet sprites) {
                this.sprites = sprites;
            }

            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
                SprintParticle particle = new SprintParticle(level, x, y, z, velocityX, velocityY, velocityZ, this.sprites);
                particle.xd = -velocityX * 1.5D;
                particle.zd = -velocityZ * 1.5D;
                particle.setLifetime(8);
                return particle;
            }
        }
    }
}
