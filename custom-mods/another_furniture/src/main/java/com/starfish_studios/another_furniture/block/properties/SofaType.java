package com.starfish_studios.another_furniture.block.properties;

public enum SofaType implements net.minecraft.util.StringRepresentable {
   SINGLE("single"),
   LEFT("left"),
   MIDDLE("middle"),
   RIGHT("right"),
   INNER_LEFT("inner_left"),
   INNER_RIGHT("inner_right"),
   OUTER_LEFT("outer_left"),
   OUTER_RIGHT("outer_right");

   private final String name;

   private SofaType(String type) {
      this.name = type;
   }

   @Override
   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }
}
