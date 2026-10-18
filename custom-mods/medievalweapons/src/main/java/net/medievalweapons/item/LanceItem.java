package net.medievalweapons.item;

import net.medievalweapons.MedievalMain;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item.Properties;

public class LanceItem extends Item {
   public static final Identifier ATTACK_BONUS_MODIFIER_ID = MedievalMain.identifierOf("riding_attack_bonus");
   private static final AttributeModifier ATTACK_BONUS_MODIFIER = new AttributeModifier(ATTACK_BONUS_MODIFIER_ID, 2.0, Operation.ADD_VALUE);

   public LanceItem(ToolMaterial material, Properties settings) {
      super(settings);
   }

   public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, EquipmentSlot slot) {
      if (entity instanceof Player player) {
         if (slot == EquipmentSlot.MAINHAND && player.isPassenger() && !player.getAttributes().hasModifier(Attributes.ATTACK_DAMAGE, ATTACK_BONUS_MODIFIER_ID)) {
            AttributeInstance entityAttributeInstance = player.getAttribute(Attributes.ATTACK_DAMAGE);
            entityAttributeInstance.addTransientModifier(ATTACK_BONUS_MODIFIER);
         } else if (player.getAttributes().hasModifier(Attributes.ATTACK_DAMAGE, ATTACK_BONUS_MODIFIER_ID) && !player.isPassenger()) {
            player.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_BONUS_MODIFIER_ID);
         }
      }
   }
}
