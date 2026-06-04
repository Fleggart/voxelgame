package com.voxelgame.physics;

public class BoundingBox {
   private float epsilon = 0.0F;
   public float x0;
   public float y0;
   public float z0;
   public float x1;
   public float y1;
   public float z1;

   public BoundingBox(float x0, float y0, float z0, float x1, float y1, float z1) {
      this.x0 = x0;
      this.y0 = y0;
      this.z0 = z0;
      this.x1 = x1;
      this.y1 = y1;
      this.z1 = z1;
   }

   public BoundingBox expand(float xa, float ya, float za) {
      float _x0 = this.x0;
      float _y0 = this.y0;
      float _z0 = this.z0;
      float _x1 = this.x1;
      float _y1 = this.y1;
      float _z1 = this.z1;
      if (xa < 0.0F) _x0 += xa;
      if (xa > 0.0F) _x1 += xa;
      if (ya < 0.0F) _y0 += ya;
      if (ya > 0.0F) _y1 += ya;
      if (za < 0.0F) _z0 += za;
      if (za > 0.0F) _z1 += za;
      return new BoundingBox(_x0, _y0, _z0, _x1, _y1, _z1);
   }

   public BoundingBox grow(float xa, float ya, float za) {
      return new BoundingBox(
         this.x0 - xa, this.y0 - ya, this.z0 - za,
         this.x1 + xa, this.y1 + ya, this.z1 + za
      );
   }

   // 统一的碰撞检测方法
   private float clipCollide(BoundingBox c, float a, int axis) {
      // 检查其他两个轴是否重叠
      if (axis != 0 && (c.x1 <= this.x0 || c.x0 >= this.x1)) return a;
      if (axis != 1 && (c.y1 <= this.y0 || c.y0 >= this.y1)) return a;
      if (axis != 2 && (c.z1 <= this.z0 || c.z0 >= this.z1)) return a;
      
      if (a > 0.0F) {
         float max = (axis == 0 ? this.x0 : axis == 1 ? this.y0 : this.z0) - 
                     (axis == 0 ? c.x1 : axis == 1 ? c.y1 : c.z1) - this.epsilon;
         if (max < a) a = max;
      } else if (a < 0.0F) {
         float max = (axis == 0 ? this.x1 : axis == 1 ? this.y1 : this.z1) - 
                     (axis == 0 ? c.x0 : axis == 1 ? c.y0 : c.z0) + this.epsilon;
         if (max > a) a = max;
      }
      return a;
   }

   public float clipXCollide(BoundingBox c, float xa) {
      return clipCollide(c, xa, 0);
   }

   public float clipYCollide(BoundingBox c, float ya) {
      return clipCollide(c, ya, 1);
   }

   public float clipZCollide(BoundingBox c, float za) {
      return clipCollide(c, za, 2);
   }

   public boolean intersects(BoundingBox c) {
      return !(c.x1 <= this.x0) && !(c.x0 >= this.x1) &&
             !(c.y1 <= this.y0) && !(c.y0 >= this.y1) &&
             !(c.z1 <= this.z0) && !(c.z0 >= this.z1);
   }

   public void move(float xa, float ya, float za) {
      this.x0 += xa;
      this.y0 += ya;
      this.z0 += za;
      this.x1 += xa;
      this.y1 += ya;
      this.z1 += za;
   }
}
