package folk.sisby.antique_atlas;

public enum TileElevation {
   VALLEY("valley"),
   LOW("low"),
   MID("mid"),
   HIGH("high"),
   PEAK("peak");

   public final String name;

   private TileElevation(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   @Override
   public String toString() {
      return this.getName();
   }

   public static TileElevation fromBlocksAboveSea(int elevation) {
      if (elevation < 10) {
         return VALLEY;
      } else if (elevation < 20) {
         return LOW;
      } else if (elevation < 35) {
         return MID;
      } else {
         return elevation < 50 ? HIGH : PEAK;
      }
   }
}
