package ht.tuber.math;

import java.util.Objects;

public class Vector3 {
   public final double x;
   public final double y;
   public final double z;

   public Vector3(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public double x() {
      return this.x;
   }

   public double y() {
      return this.y;
   }

   public double z() {
      return this.z;
   }

   public Vector3 withX(double x) {
      return new Vector3(x, this.y(), this.z());
   }

   public Vector3 withX(Number x) {
      return this.withX(x.doubleValue());
   }

   public Vector3 withY(double y) {
      return new Vector3(this.x(), y, this.z());
   }

   public Vector3 withY(Number y) {
      return this.withY(y.doubleValue());
   }

   public Vector3 withZ(double z) {
      return new Vector3(this.x(), this.y(), z);
   }

   public Vector3 withZ(Number z) {
      return this.withZ(z.doubleValue());
   }

   public Vector3 scale(double factor) {
      return new Vector3(this.x() * factor, this.y() * factor, this.z() * factor);
   }

   public Vector3 scale(Number factor) {
      return this.scale(factor.doubleValue());
   }

   public Vector3 add(Vector3 vector3) {
      return new Vector3(this.x() + vector3.x(), this.y() + vector3.y(), this.z() + vector3.z());
   }

   public Vector3 add(double amount) {
      return new Vector3(this.x() + amount, this.y() + amount, this.z() + amount);
   }

   public Vector3 add(Number amount) {
      return this.add(amount.doubleValue());
   }

   public Vector3 clamp(Vector3 min, Vector3 max) {
      return new Vector3(this.clamp(this.x, min.x, max.x), this.clamp(this.y, min.y, max.y), this.clamp(this.z, min.z, max.z));
   }

   private double clamp(double value, double min, double max) {
      return Double.max(Double.min(value, max), min);
   }

   public Vector3 subtract(Vector3 other) {
      return new Vector3(this.x - other.x, this.y - other.y, this.z - other.z);
   }

   public double length() {
      return Math.sqrt(this.length2());
   }

   private double length2() {
      return this.x * this.x + this.y * this.y + this.z * this.z;
   }

   public double dot(Vector3 vector) {
      return this.dot(vector.x, vector.y, vector.z);
   }

   public double dot(double x, double y, double z) {
      return this.x * x + this.y * y + this.z * z;
   }

   public Vector3 cross(Vector3 vector) {
      return this.cross(vector.x, vector.y, vector.z);
   }

   private Vector3 cross(double x, double y, double z) {
      return new Vector3(this.y * z - this.z * y, this.z * x - this.x * z, this.x * y - this.y * x);
   }

   public Vector3 normalize() {
      return this.scale(1.0 / this.length());
   }

   @Override
   public String toString() {
      return "Vector3{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }

   @Override
   public boolean equals(Object other) {
      return !(other instanceof Vector3 vec) ? false : this.x == vec.x && this.y == vec.y && this.z == vec.z;
   }
}
