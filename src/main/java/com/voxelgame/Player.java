package com.voxelgame;

public class Player {
   private static final float WIDTH = 0.3f;
   private static final float HEIGHT = 0.9f;
   private static final float JUMP_FORCE = 0.12f;
   private static final float GRAVITY = 0.005f;
   private static final float MOVE_SPEED_GROUND = 0.02f;
   private static final float MOVE_SPEED_AIR = 0.005f;
   
   private final com.voxelgame.world.World world;
   
   public float x, y, z;
   public float xo, yo, zo;
   public float xd, yd, zd;
   public float yRot, xRot;
   public com.voxelgame.physics.BoundingBox bb;
   public boolean onGround = false;

   public Player(com.voxelgame.world.World world) {
      this.world = world;
      resetPos();
   }

   public void resetPos() {
      setPos((float)Math.random() * world.width, world.depth + 10, (float)Math.random() * world.height);
   }

   private void setPos(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
      bb = new com.voxelgame.physics.BoundingBox(x - WIDTH, y - HEIGHT, z - WIDTH, x + WIDTH, y + HEIGHT, z + WIDTH);
   }

   public void turn(float dx, float dy) {
      yRot += dx * 0.15f;
      xRot = Math.min(90f, Math.max(-90f, xRot - dy * 0.15f));
   }

   public void tick() {
      xo = x; yo = y; zo = z;
      
      float xa = 0, ya = 0;
      
      // 简化的输入处理
      if (org.lwjgl.input.Keyboard.isKeyDown(19)) resetPos();
      if (isKeyDown(200, 17)) ya--;
      if (isKeyDown(208, 31)) ya++;
      if (isKeyDown(203, 30)) xa--;
      if (isKeyDown(205, 32)) xa++;
      
      if ((org.lwjgl.input.Keyboard.isKeyDown(57) || org.lwjgl.input.Keyboard.isKeyDown(219)) && onGround) {
         yd = JUMP_FORCE;
      }
      
      float speed = onGround ? MOVE_SPEED_GROUND : MOVE_SPEED_AIR;
      moveRelative(xa, ya, speed);
      yd -= GRAVITY;
      move(xd, yd, zd);
      
      float friction = 0.91f;
      xd *= friction;
      yd *= 0.98f;
      zd *= friction;
      
      if (onGround) {
         xd *= 0.8f;
         zd *= 0.8f;
      }
   }
   
   private boolean isKeyDown(int key1, int key2) {
      return org.lwjgl.input.Keyboard.isKeyDown(key1) || org.lwjgl.input.Keyboard.isKeyDown(key2);
   }

   public void move(float xa, float ya, float za) {
      float xaOrg = xa, yaOrg = ya, zaOrg = za;
      var boxes = world.getCubes(bb.expand(xa, ya, za));
      
      ya = collideY(boxes, ya);
      bb.move(0, ya, 0);
      xa = collideX(boxes, xa);
      bb.move(xa, 0, 0);
      za = collideZ(boxes, za);
      bb.move(0, 0, za);
      
      onGround = yaOrg != ya && yaOrg < 0;
      
      if (xaOrg != xa) xd = 0;
      if (yaOrg != ya) yd = 0;
      if (zaOrg != za) zd = 0;
      
      x = (bb.x0 + bb.x1) / 2;
      y = bb.y0 + 1.62f;
      z = (bb.z0 + bb.z1) / 2;
   }
   
   private float collideX(java.util.List<com.voxelgame.physics.BoundingBox> boxes, float xa) {
      for (var box : boxes) xa = box.clipXCollide(bb, xa);
      return xa;
   }
   
   private float collideY(java.util.List<com.voxelgame.physics.BoundingBox> boxes, float ya) {
      for (var box : boxes) ya = box.clipYCollide(bb, ya);
      return ya;
   }
   
   private float collideZ(java.util.List<com.voxelgame.physics.BoundingBox> boxes, float za) {
      for (var box : boxes) za = box.clipZCollide(bb, za);
      return za;
   }

   public void moveRelative(float xa, float za, float speed) {
      float dist = xa * xa + za * za;
      if (dist < 0.01f) return;
      
      dist = speed / (float)Math.sqrt(dist);
      xa *= dist;
      za *= dist;
      
      float rad = yRot * (float)Math.PI / 180f;
      float sin = (float)Math.sin(rad);
      float cos = (float)Math.cos(rad);
      
      xd += xa * cos - za * sin;
      zd += za * cos + xa * sin;
   }
}
