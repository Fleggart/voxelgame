package com.voxelgame;

import com.voxelgame.world.World;
import com.voxelgame.physics.BoundingBox;
import java.util.List;
import org.lwjgl.input.Keyboard;

public class Player {
   private World world;
   public float xo;
   public float yo;
   public float zo;
   public float x;
   public float y;
   public float z;
   public float xd;
   public float yd;
   public float zd;
   public float yRot;
   public float xRot;
   public BoundingBox bb;
   public boolean onGround = false;

   public Player(World world) {
      this.world = world;
      this.resetPos();
   }

   private void resetPos() {
      float x = (float)Math.random() * (float)this.world.width;
      float y = (float)(this.world.depth + 10);
      float z = (float)Math.random() * (float)this.world.height;
      this.setPos(x, y, z);
   }

   private void setPos(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
      float w = 0.3F;
      float h = 0.9F;
      this.bb = new BoundingBox(x - w, y - h, z - w, x + w, y + h, z + w);
   }

   public void turn(float xo, float yo) {
      this.yRot = (float)((double)this.yRot + (double)xo * 0.15);
      this.xRot = (float)((double)this.xRot - (double)yo * 0.15);
      if (this.xRot < -90.0F) {
         this.xRot = -90.0F;
      }
      if (this.xRot > 90.0F) {
         this.xRot = 90.0F;
      }
   }

   public void tick() {
      this.xo = this.x;
      this.yo = this.y;
      this.zo = this.z;
      float xa = 0.0F;
      float ya = 0.0F;
      
      if (Keyboard.isKeyDown(19)) {
         this.resetPos();
      }
      if (Keyboard.isKeyDown(200) || Keyboard.isKeyDown(17)) {
         --ya;
      }
      if (Keyboard.isKeyDown(208) || Keyboard.isKeyDown(31)) {
         ++ya;
      }
      if (Keyboard.isKeyDown(203) || Keyboard.isKeyDown(30)) {
         --xa;
      }
      if (Keyboard.isKeyDown(205) || Keyboard.isKeyDown(32)) {
         ++xa;
      }
      if ((Keyboard.isKeyDown(57) || Keyboard.isKeyDown(219)) && this.onGround) {
         // RubyDung: 跳跃初速度 0.5F (原 VoxelGame 为 0.12F)
         this.yd = 0.5F;
      }

      // RubyDung: 地面加速度 0.1F, 空中加速度 0.02F (原为 0.02F / 0.005F)
      this.moveRelative(xa, ya, this.onGround ? 0.1F : 0.02F);
      
      // RubyDung: 重力强度 0.08 (原为 0.005)
      this.yd = (float)((double)this.yd - 0.08);
      
      this.move(this.xd, this.yd, this.zd);
      this.xd *= 0.91F;
      this.yd *= 0.98F;
      this.zd *= 0.91F;
      
      if (this.onGround) {
         // RubyDung: 地面摩擦力 0.7F (原为 0.8F)
         this.xd *= 0.7F;
         this.zd *= 0.7F;
      }
   }

   public void move(float xa, float ya, float za) {
      float xaOrg = xa;
      float yaOrg = ya;
      float zaOrg = za;
      List<BoundingBox> boxes = this.world.getCubes(this.bb.expand(xa, ya, za));

      for(int i = 0; i < boxes.size(); ++i) {
         ya = boxes.get(i).clipYCollide(this.bb, ya);
      }
      this.bb.move(0.0F, ya, 0.0F);

      for(int i = 0; i < boxes.size(); ++i) {
         xa = boxes.get(i).clipXCollide(this.bb, xa);
      }
      this.bb.move(xa, 0.0F, 0.0F);

      for(int i = 0; i < boxes.size(); ++i) {
         za = boxes.get(i).clipZCollide(this.bb, za);
      }
      this.bb.move(0.0F, 0.0F, za);
      this.onGround = yaOrg != ya && yaOrg < 0.0F;
      
      if (xaOrg != xa) {
         this.xd = 0.0F;
      }
      if (yaOrg != ya) {
         this.yd = 0.0F;
      }
      if (zaOrg != za) {
         this.zd = 0.0F;
      }

      this.x = (this.bb.x0 + this.bb.x1) / 2.0F;
      this.y = this.bb.y0 + 1.62F;
      this.z = (this.bb.z0 + this.bb.z1) / 2.0F;
   }

   public void moveRelative(float xa, float za, float speed) {
      float dist = xa * xa + za * za;
      if (!(dist < 0.01F)) {
         dist = speed / (float)Math.sqrt((double)dist);
         xa *= dist;
         za *= dist;
         float sin = (float)Math.sin((double)this.yRot * Math.PI / (double)180.0F);
         float cos = (float)Math.cos((double)this.yRot * Math.PI / (double)180.0F);
         this.xd += xa * cos - za * sin;
         this.zd += za * cos + xa * sin;
      }
   }
}
