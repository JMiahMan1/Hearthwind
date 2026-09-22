package io.github.apace100.pockets;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;

public class SoundUtil {

	private static void play(Entity entity, SoundEvent sound) {
		entity.playSound(sound, 0.8f, 0.8f + entity.getRandom().nextFloat() * 0.4f);
	}

	public static void playRemoveOneSound(Entity entity) {
		play(entity, SoundEvents.BUNDLE_REMOVE_ONE);
	}

	public static void playInsertSound(Entity entity) {
		play(entity, SoundEvents.BUNDLE_INSERT);
	}

	public static void playDropContentsSound(Entity entity) {
		play(entity, SoundEvents.BUNDLE_DROP_CONTENTS);
	}
}
