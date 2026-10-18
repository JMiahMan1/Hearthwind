package net.nameplate.access;

import net.minecraft.network.chat.Component;

public interface NameplateRenderState {
   boolean nameplateIsMob();

   void nameplateIsMob(boolean mob);

   int nameplateLevel();

   void nameplateLevel(int level);

   boolean nameplateShowLabel();

   void nameplateShowLabel(boolean show);

   float nameplateHealth();

   void nameplateHealth(float health);

   float nameplateMaxHealth();

   void nameplateMaxHealth(float maxHealth);

   boolean nameplateVehicle();

   void nameplateVehicle(boolean vehicle);

   boolean nameplateCanSee();

   void nameplateCanSee(boolean canSee);

   Component nameplateName();

   void nameplateName(Component name);
}
