package ht.treechop.common.properties;

import ht.treechop.common.util.FaceShape;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public enum ChoppedLogShape implements net.minecraft.util.StringRepresentable {
   PILLAR_Y("pillar", 60),
   CORNER_NWEU("corner_nweu", 54),
   CORNER_NWED("corner_nwed", 53),
   CORNER_NSWU("corner_nswu", 30),
   CORNER_NSWD("corner_nswd", 29),
   CORNER_NWUD("corner_nwud", 23),
   CORNER_SWEU("corner_sweu", 58),
   CORNER_SWED("corner_swed", 57),
   PILLAR_Z("pillar_z", 51),
   CORNER_SWUD("corner_swud", 27),
   CORNER_NSEU("corner_nseu", 46),
   CORNER_NSED("corner_nsed", 45),
   CORNER_NEUD("corner_neud", 39),
   PILLAR_X("pillar_x", 15),
   CORNER_SEUD("corner_seud", 43),
   END_N("end_n", 52),
   END_W("end_w", 28),
   CORNER_NWU("corner_nwu", 22),
   CORNER_NWD("corner_nwd", 21),
   END_S("end_s", 56),
   CORNER_WEU("corner_weu", 50),
   CORNER_WED("corner_wed", 49),
   CORNER_SWU("corner_swu", 26),
   CORNER_SWD("corner_swd", 25),
   CORNER_WUD("corner_wud", 19),
   END_E("end_e", 44),
   CORNER_NEU("corner_neu", 38),
   CORNER_NED("corner_ned", 37),
   END_U("end_u", 14),
   END_D("end_d", 13),
   CORNER_NUD("corner_nud", 7),
   CORNER_SEU("corner_seu", 42),
   CORNER_SED("corner_sed", 41),
   CORNER_EUD("corner_eud", 35),
   CORNER_SUD("corner_sud", 11),
   CORNER_NW("corner_nw", 20),
   SLAB_YZ("slab_yz", 48),
   CORNER_SW("corner_sw", 24),
   CORNER_WU("corner_wu", 18),
   CORNER_WD("corner_wd", 17),
   CORNER_NE("corner_ne", 36),
   SLAB_XY("slab_xy", 12),
   CORNER_NU("corner_nu", 6),
   CORNER_ND("corner_nd", 5),
   CORNER_SE("corner_se", 40),
   CORNER_EU("corner_eu", 34),
   CORNER_ED("corner_ed", 33),
   CORNER_SU("corner_su", 10),
   CORNER_SD("corner_sd", 9),
   SLAB_XZ("slab_xz", 3),
   SIDE_W("side_w", 16),
   SIDE_N("side_n", 4),
   SIDE_E("side_e", 32),
   SIDE_S("side_s", 8),
   SIDE_U("side_u", 2),
   SIDE_D("side_d", 1);

   private static final int MIN_RADIUS = 1;
   public static final int MAX_RADIUS = 8;
   private final String name;
   private final byte openSides;
   private final Map<Integer, net.minecraft.world.phys.AABB> radiusBoxes;
   private static final ChoppedLogShape[] openSidesMap = new ChoppedLogShape[64];
   private final VoxelShape occlusionShape;

   private ChoppedLogShape(String name, int openSides) {
      this.name = name;
      this.openSides = (byte)openSides;
      this.radiusBoxes = IntStream.range(1, 8).boxed().collect(Collectors.toMap(radius -> (Integer)radius, this::bakeBoundingBox));
      this.occlusionShape = Shapes.or(
         Shapes.empty(),
         Arrays.stream(Direction.values())
            .filter(direction -> !this.isSideOpen(direction))
            .map(direction -> Shapes.create(FaceShape.get(direction).toAABB()))
            .toArray(VoxelShape[]::new)
      );
      int x = 0;
   }

   public static ChoppedLogShape forOpenSides(byte openSides) {
      return openSidesMap[openSides];
   }

   @Override
   public String toString() {
      return this.name;
   }

   public String getSerializedName() {
      return this.name;
   }

   private AABB bakeBoundingBox(int radius) {
      boolean down = this.isSideOpen(Direction.DOWN);
      boolean up = this.isSideOpen(Direction.UP);
      boolean north = this.isSideOpen(Direction.NORTH);
      boolean south = this.isSideOpen(Direction.SOUTH);
      boolean west = this.isSideOpen(Direction.WEST);
      boolean east = this.isSideOpen(Direction.EAST);
      int chops = 8 - radius;
      float xCenter = 8 + (west ? chops : 0) - (east ? chops : 0);
      float yCenter = 8 + (down ? chops : 0) - (up ? chops : 0);
      float zCenter = 8 + (north ? chops : 0) - (south ? chops : 0);
      float xRadius = !west && !east ? 8.0F : radius;
      float yRadius = !down && !up ? 8.0F : radius;
      float zRadius = !north && !south ? 8.0F : radius;
      return new AABB(xCenter - xRadius, yCenter - yRadius, zCenter - zRadius, xCenter + xRadius, yCenter + yRadius, zCenter + zRadius);
   }

   public AABB getBoundingBox(int radius) {
      AABB defaultBox = new AABB(0.0, 0.0, 0.0, 16.0, 16.0, 16.0);
      return (AABB)this.radiusBoxes.getOrDefault(Math.max(1, Math.min(radius, 8)), defaultBox);
   }

   public boolean isSideOpen(Direction side) {
      return (this.openSides >> side.ordinal() & 1) == 1;
   }

   static {
      Arrays.stream(values()).forEach(shape -> openSidesMap[shape.openSides] = shape);
      openSidesMap[0] = PILLAR_Y;
      openSidesMap[63] = PILLAR_Y;
      openSidesMap[62] = PILLAR_Y;
      openSidesMap[61] = PILLAR_Y;
      openSidesMap[55] = PILLAR_Z;
      openSidesMap[31] = PILLAR_X;
      openSidesMap[59] = PILLAR_Z;
      openSidesMap[47] = PILLAR_X;
   }
}
