package net.dungeonz.criteria;

import java.util.Optional;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.advancements.triggers.SimpleCriterionTrigger;
import net.minecraft.advancements.predicates.ContextAwarePredicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class DungeonBossCriterion extends SimpleCriterionTrigger<DungeonBossCriterion.Conditions> {

    public void trigger(ServerPlayer player, String dungeonType, String difficulty) {
        this.trigger(player, conditions -> conditions.matches(player, dungeonType, difficulty));
    }

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<ContextAwarePredicate> player, String dungeonType, String difficulty) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder
                .create(instance -> instance
                        .group(EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Conditions::player),
                                Codec.STRING.fieldOf("dungeon_type").forGetter(Conditions::dungeonType), Codec.STRING.fieldOf("difficulty").forGetter(Conditions::difficulty))
                        .apply(instance, Conditions::new));

        public boolean matches(ServerPlayer player, String dungeonType, String difficulty) {
            return this.dungeonType.equals(dungeonType) && this.difficulty.equals(difficulty);
        }

    }

}
