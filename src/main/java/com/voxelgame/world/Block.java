package com.voxelgame.world;

public class Block {
   public static Block rock = new Block(0);
   public static Block grass = new Block(1);
   private int tex = 0;

   private Block(int tex) {
      this.tex = tex;
   }

   public void render(MeshBuilder t, World world, int layer, int x, int y, int z) {
      float u0 = (float)this.tex / 16.0F;
      float u1 = u0 + 0.0624375F;
      float v0 = 0.0F;
      float v1 = v0 + 0.0624375F;
      float c1 = 1.0F;
      float c2 = 0.8F;
      float c3 = 0.6F;
      float x0 = (float)x + 0.0F;
      float x1 = (float)x + 1.0F;
      float y0 = (float)y + 0.0F;
      float y1 = (float)y + 1.0F;
      float z0 = (float)z + 0.0F;
      float z1 = (float)z + 1.0F;
      
      // 底面 (Y-)
      if (!world.isSolidBlock(x, y - 1, z)) {
         float br = world.getBrightness(x, y - 1, z) * c1;
         if (br == c1 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v1);
            t.vertex(x0, y0, z1);
            t.tex(u0, v0);
            t.vertex(x0, y0, z0);
            t.tex(u1, v0);
            t.vertex(x1, y0, z0);
            t.tex(u1, v1);
            t.vertex(x1, y0, z1);
         }
      }

      // 顶面 (Y+)
      if (!world.isSolidBlock(x, y + 1, z)) {
         float br = world.getBrightness(x, y, z) * c1;
         if (br == c1 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v1);
            t.vertex(x1, y1, z1);
            t.tex(u1, v0);
            t.vertex(x1, y1, z0);
            t.tex(u0, v0);
            t.vertex(x0, y1, z0);
            t.tex(u0, v1);
            t.vertex(x0, y1, z1);
         }
      }

      // 底面 (Z-)
      if (!world.isSolidBlock(x, y, z - 1)) {
         float br = world.getBrightness(x, y, z - 1) * c2;
         if (br == c2 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v0);
            t.vertex(x0, y1, z0);
            t.tex(u0, v0);
            t.vertex(x1, y1, z0);
            t.tex(u0, v1);
            t.vertex(x1, y0, z0);
            t.tex(u1, v1);
            t.vertex(x0, y0, z0);
         }
      }

      // 顶面 (Z+)
      if (!world.isSolidBlock(x, y, z + 1)) {
         float br = world.getBrightness(x, y, z + 1) * c2;
         if (br == c2 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v0);
            t.vertex(x0, y1, z1);
            t.tex(u0, v1);
            t.vertex(x0, y0, z1);
            t.tex(u1, v1);
            t.vertex(x1, y0, z1);
            t.tex(u1, v0);
            t.vertex(x1, y1, z1);
         }
      }

      // 底面 (X-)
      if (!world.isSolidBlock(x - 1, y, z)) {
         float br = world.getBrightness(x - 1, y, z) * c3;
         if (br == c3 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u1, v0);
            t.vertex(x0, y1, z1);
            t.tex(u0, v0);
            t.vertex(x0, y1, z0);
            t.tex(u0, v1);
            t.vertex(x0, y0, z0);
            t.tex(u1, v1);
            t.vertex(x0, y0, z1);
         }
      }

      // 顶面 (X+)
      if (!world.isSolidBlock(x + 1, y, z)) {
         float br = world.getBrightness(x + 1, y, z) * c3;
         if (br == c3 ^ layer == 1) {
            t.color(br, br, br);
            t.tex(u0, v1);
            t.vertex(x1, y0, z1);
            t.tex(u1, v1);
            t.vertex(x1, y0, z0);
            t.tex(u1, v0);
            t.vertex(x1, y1, z0);
            t.tex(u0, v0);
            t.vertex(x1, y1, z1);
         }
      }
   }

   public void renderFace(MeshBuilder t, int x, int y, int z, int face) {
      float x0 = (float)x + 0.0F;
      float x1 = (float)x + 1.0F;
      float y0 = (float)y + 0.0F;
      float y1 = (float)y + 1.0F;
      float z0 = (float)z + 0.0F;
      float z1 = (float)z + 1.0F;
      
      // 底面 (Y-)
      if (face == 0) {
         t.vertex(x0, y0, z1);
         t.vertex(x0, y0, z0);
         t.vertex(x1, y0, z0);
         t.vertex(x1, y0, z1);
      }

      // 顶面 (Y+)
      if (face == 1) {
         t.vertex(x1, y1, z1);
         t.vertex(x1, y1, z0);
         t.vertex(x0, y1, z0);
         t.vertex(x0, y1, z1);
      }

      // 底面 (Z-)
      if (face == 2) {
         t.vertex(x0, y1, z0);
         t.vertex(x1, y1, z0);
         t.vertex(x1, y0, z0);
         t.vertex(x0, y0, z0);
      }

      // 顶面 (Z+)
      if (face == 3) {
         t.vertex(x0, y1, z1);
         t.vertex(x0, y0, z1);
         t.vertex(x1, y0, z1);
         t.vertex(x1, y1, z1);
      }

      // 底面 (X-)
      if (face == 4) {
         t.vertex(x0, y1, z1);
         t.vertex(x0, y1, z0);
         t.vertex(x0, y0, z0);
         t.vertex(x0, y0, z1);
      }

      // 顶面 (X+)
      if (face == 5) {
         t.vertex(x1, y0, z1);
         t.vertex(x1, y0, z0);
         t.vertex(x1, y1, z0);
         t.vertex(x1, y1, z1);
      }
   }
}
