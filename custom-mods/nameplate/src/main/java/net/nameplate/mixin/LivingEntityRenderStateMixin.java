package net.nameplate.mixin;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.nameplate.access.NameplateRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements NameplateRenderState {
   @Unique
   private boolean nameplateIsMob;
   @Unique
   private int nameplateLevel = 1;
   @Unique
   private boolean nameplateShowLabel = true;
   @Unique
   private float nameplateHealth = 1.0F;
   @Unique
   private float nameplateMaxHealth = 1.0F;
   @Unique
   private boolean nameplateVehicle;
   @Unique
   private boolean nameplateCanSee = true;
   @Unique
   private Component nameplateName = Component.empty();

   @Override
   public boolean nameplateIsMob() {
      return this.nameplateIsMob;
   }

   @Override
   public void nameplateIsMob(boolean mob) {
      this.nameplateIsMob = mob;
   }

   @Override
   public int nameplateLevel() {
      return this.nameplateLevel;
   }

   @Override
   public void nameplateLevel(int level) {
      this.nameplateLevel = level;
   }

   @Override
   public boolean nameplateShowLabel() {
      return this.nameplateShowLabel;
   }

   @Override
   public void nameplateShowLabel(boolean show) {
      this.nameplateShowLabel = show;
   }

   @Override
   public float nameplateHealth() {
      return this.nameplateHealth;
   }

   @Override
   public void nameplateHealth(float health) {
      this.nameplateHealth = health;
   }

   @Override
   public float nameplateMaxHealth() {
      return this.nameplateMaxHealth;
   }

   @Override
   public void nameplateMaxHealth(float maxHealth) {
      this.nameplateMaxHealth = maxHealth;
   }

   @Override
   public boolean nameplateVehicle() {
      return this.nameplateVehicle;
   }

   @Override
   public void nameplateVehicle(boolean vehicle) {
      this.nameplateVehicle = vehicle;
   }

   @Override
   public boolean nameplateCanSee() {
      return this.nameplateCanSee;
   }

   @Override
   public void nameplateCanSee(boolean canSee) {
      this.nameplateCanSee = canSee;
   }

   @Override
   public Component nameplateName() {
      return this.nameplateName;
   }

   @Override
   public void nameplateName(Component name) {
      this.nameplateName = name;
   }
}
