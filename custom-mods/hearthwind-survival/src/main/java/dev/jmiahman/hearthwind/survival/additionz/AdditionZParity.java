package dev.jmiahman.hearthwind.survival.additionz;

import dev.jmiahman.hearthwind.survival.HearthwindSurvivalConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The slice of AdditionZ we reproduce, with every decision that does not need a
 * mixin written as a plain static so a gametest can pin it. The mixins in
 * {@code mixin/} are thin: they call in here and do nothing else.
 *
 * <p>Everything comes from the reference pack's {@code additionz.json5}. The
 * reference has twelve keys; three of them are no-ops on 26.2 (its underwater
 * elytra check is already in vanilla, its buried-treasure listener is never
 * registered, its air-amount arithmetic adds zero) and two are dead config
 * (a passive-age calculation the reference computes and never reads). Those
 * five are deliberately not reproduced - see docs/AGED_HYDRATION.md.
 */
public final class AdditionZParity {

    /** The NBT key the reference stores its rain counter under. */
    public static final String RAIN_BURN_TIME = "RainBurnTime";

    private AdditionZParity() {
    }

    public static HearthwindSurvivalConfig.AdditionZ config() {
        return HearthwindSurvivalConfig.get().additionZ;
    }

    /**
     * One once-a-second rain sample on a lit campfire.
     *
     * <p>The reference's two conditions have to be read as a pair, which is not
     * obvious from either alone: the counter is incremented and then kept only
     * while it is <em>at or below</em> the limit, and the sample extinguishes
     * only when a roll comes up zero. So the first {@code limit} samples do
     * nothing but count, the next one has a 1-in-{@code limit} chance of putting
     * the fire out, and every sample after that rolls again. With Aged's 60
     * that is never sooner than the 61st second and about 121 s on average.
     *
     * <p>A limit of zero disables the whole rule, which the reference checks
     * before it touches the counter at all.
     *
     * @param rainBurnTime the counter as loaded from NBT
     * @param roll         the {@code random.nextInt(limit)} result
     * @return the new counter value; {@code -1} means "put the fire out"
     */
    public static int rainSample(int rainBurnTime, int limit, int roll) {
        if (limit <= 0) {
            return rainBurnTime;
        }
        int next = rainBurnTime + 1;
        if (next > limit) {
            return roll == 0 ? -1 : next;
        }
        return next;
    }

    /**
     * Whether this tick is one of the reference's once-a-second samples: it
     * checks {@code level.getGameTime() % 20 != 0} and returns, so it lands on
     * every tick whose time is a multiple of 20.
     */
    public static boolean isRainSampleTick(long gameTime) {
        return gameTime % 20L == 0L;
    }

    /**
     * The remaining gate on the rain counter. The reference bails out when the
     * limit is zero, when it is not raining, or when the campfire cannot see the
     * sky - a fire under a roof never goes out. 26.2 spells that last one
     * {@code Level.canSeeSky}.
     */
    public static boolean countsRain(Level level, BlockPos pos, int limit) {
        return limit != 0 && level.isRaining() && level.canSeeSky(pos);
    }

    /**
     * Whether a village has already produced its allowance of iron golems.
     * The reference counts them in an NBT tag on the villager; 26.2 has no cap
     * of its own ({@code Villager.golemSpawnConditionsMet} only checks whether
     * anyone slept), so the golems actually standing around the caller are what
     * we count.
     */
    /** Half-width of the box searched for golems, in blocks. */
    public static final double GOLEM_AREA = 8.0;

    public static boolean golemCapReached(Level level, BlockPos pos, int cap) {
        return cap > 0 && golemCount(level, pos) >= cap;
    }

    /**
     * How many iron golems stand in the area a villager could summon one into.
     * A village, not a single block: the reference counts them across the whole
     * area, so the query box is inflated.
     */
    public static int golemCount(Level level, BlockPos pos) {
        List<Entity> golems = level.getEntitiesOfClass(Entity.class,
                new net.minecraft.world.phys.AABB(pos).inflate(GOLEM_AREA),
                entity -> entity.getType() == EntityTypes.IRON_GOLEM);
        return golems.size();
    }

    /**
     * The spawner bookkeeping. {@code totalSpawns} is how many mobs this spawner
     * has produced since it last reset, {@code ticksLeft} counts down from the
     * configured deactivation window, and reaching zero forgets the count. The
     * reference decrements the timer every tick and resets the count when it
     * hits zero.
     *
     * @return the new total, or {@code -1} if the spawner should deactivate
     */
    public static int spawnerTick(int totalSpawns, int ticksLeft, int deactivationTicks, int cap) {
        if (deactivationTicks > 0 && ticksLeft <= 0) {
            return 0;
        }
        return cap > 0 && totalSpawns >= cap ? -1 : totalSpawns;
    }

    /** Whether a spawner that has just produced {@code total} mobs should stop. */
    public static boolean spawnerExhausted(int total, int cap) {
        return cap > 0 && total >= cap;
    }

    /**
     * How many mobs a spawner produced this tick. 26.2 loops over its spawn
     * count inside {@code serverTick}, so the number of entities that actually
     * appeared is measured rather than assumed.
     */
    public static int countNewlySpawned(int before, int after) {
        return Math.max(0, after - before);
    }
}
