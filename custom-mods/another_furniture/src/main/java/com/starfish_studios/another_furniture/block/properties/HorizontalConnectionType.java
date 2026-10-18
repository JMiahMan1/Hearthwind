package com.starfish_studios.another_furniture.block.properties;

public enum HorizontalConnectionType implements net.minecraft.util.StringRepresentable {
   SINGLE("single"),
   LEFT("left"),
   MIDDLE("middle"),
   RIGHT("right");

   private final String name;

   private HorizontalConnectionType(String type) {
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
