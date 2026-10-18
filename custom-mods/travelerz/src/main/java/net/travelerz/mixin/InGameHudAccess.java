package net.travelerz.mixin;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Hud.class})
public interface InGameHudAccess {
   @Accessor("title")
   @Nullable
   Component getTitle();
}
