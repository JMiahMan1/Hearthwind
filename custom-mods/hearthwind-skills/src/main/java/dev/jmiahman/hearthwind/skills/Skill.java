package dev.jmiahman.hearthwind.skills;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * The twelve levelz-parity skills. Only some map to vanilla attributes;
 * the rest gate content via datapack unlock files (mining/smithing/farming
 * tiers etc.) and accrue levels without numeric bonuses.
 */
public enum Skill {
    FARMING("farming", null),
    MINING("mining", Attributes.BLOCK_BREAK_SPEED),
    SMITHING("smithing", null),
    STRENGTH("strength", Attributes.ATTACK_DAMAGE),
    AGILITY("agility", Attributes.MOVEMENT_SPEED),
    DEFENSE("defense", Attributes.ARMOR),
    HEALTH("health", Attributes.MAX_HEALTH),
    STAMINA("stamina", null),
    LUCK("luck", Attributes.LUCK),
    ARCHERY("archery", null),
    ALCHEMY("alchemy", null),
    TRADE("trade", null);

    /** Lowercase id used in attachments, config, and lang keys. */
    public final String id;
    /** Vanilla attribute boosted per level, or null for unlock-only skills. */
    public final Holder<Attribute> attribute;

    Skill(String id, Holder<Attribute> attribute) {
        this.id = id;
        this.attribute = attribute;
    }

    public static Skill byId(String id) {
        for (Skill s : values()) {
            if (s.id.equals(id)) {
                return s;
            }
        }
        throw new IllegalArgumentException("unknown skill: " + id);
    }
}
