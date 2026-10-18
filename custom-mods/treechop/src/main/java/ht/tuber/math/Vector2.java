package ht.tuber.math;

import java.util.Objects;

public class Vector2 {
   public final double x;
   public final double y;

   public Vector2(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public double x() {
      return this.x;
   }

   public double y() {
      return this.y;
   }

   public Vector2 withX(double x) {
      return new Vector2(x, this.y);
   }

   public Vector2 withX(Number x) {
      return this.withX(x.doubleValue());
   }

   public Vector2 withY(double y) {
      return new Vector2(this.x, y);
   }

   public Vector2 withY(Number y) {
      return this.withY(y.doubleValue());
   }

   public Vector2 scale(double factor) {
      return new Vector2(this.x * factor, this.y * factor);
   }

   public Vector2 scale(Number factor) {
      return this.scale(factor.doubleValue());
   }

   public Vector2 add(Vector2 vector2) {
      return new Vector2(this.x + vector2.x, this.y + vector2.y);
   }

   public Vector2 add(double amount) {
      return new Vector2(this.x + amount, this.y + amount);
   }

   public Vector2 add(Number amount) {
      return this.add(amount.doubleValue());
   }

   public Vector2 clamp(Vector2 min, Vector2 max) {
      return new Vector2(this.clamp(this.x, min.x, max.x), this.clamp(this.y, min.y, max.y));
   }

   private double clamp(double value, double min, double max) {
      return Double.max(Double.min(value, max), min);
   }

   public Vector2 subtract(Vector2 other) {
      return new Vector2(this.x - other.x, this.y - other.y);
   }

   public double length() {
      return Math.sqrt(this.length2());
   }

   private double length2() {
      return this.x * this.x + this.y * this.y;
   }

   public double dot(double x, double y) {
      return this.x * x + this.y * y;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y);
   }

   @Override
   public boolean equals(Object other) {
      return !(other instanceof Vector2 vec) ? false : this.x == vec.x && this.y == vec.y;
   }
}
