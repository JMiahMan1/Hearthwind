package net.dungeonz.criteria;

import java.util.Optional;
import net.minecraft.class_2048;
import net.minecraft.class_3222;
import net.minecraft.class_4558;
import net.minecraft.class_5258;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class DungeonBossCriterion extends class_4558<DungeonBossCriterion.Conditions> {

    public void trigger(class_3222 player, String dungeonType, String difficulty) {
        this.method_22510(player, conditions -> conditions.matches(player, dungeonType, difficulty));
    }

    @Override
    public Codec<Conditions> method_54937() {
        return Conditions.CODEC;
    }

    public record Conditions(Optional<class_5258> player, String dungeonType, String difficulty) implements class_4558.class_8788 {

        public static final Codec<Conditions> CODEC = RecordCodecBuilder
                .create(instance -> instance
                        .group(class_2048.field_47250.optionalFieldOf("player").forGetter(Conditions::comp_2029),
                                Codec.STRING.fieldOf("dungeon_type").forGetter(Conditions::dungeonType), Codec.STRING.fieldOf("difficulty").forGetter(Conditions::difficulty))
                        .apply(instance, Conditions::new));

        public boolean matches(class_3222 player, String dungeonType, String difficulty) {
            return this.dungeonType.equals(dungeonType) && this.difficulty.equals(difficulty);
        }

    }

}
