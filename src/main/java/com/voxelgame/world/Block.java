package com.voxelgame.world;

public class Block {
   public static final Block[] BLOCKS = new Block[256];
   public static final Block ROCK = new Block(0);
   public static final Block GRASS = new Block(1);
   
   private final int tex;
   private static final float[][] BRIGHTNESS = {
      {1.0f, 1.0f, 1.0f},  // Y轴亮度
      {0.8f, 0.8f, 0.8f},  // Z轴亮度
      {0.6f, 0.6f, 0.6f}   // X轴亮度
   };

   private Block(int tex) {
      this.tex = tex;
      BLOCKS[tex] = this;
   }
   
   public void render(MeshBuilder t, World world, int layer, int x, int y, int z) {
      float u0 = tex / 16.0f;
      float u1 = u0 + 0.0624375f;
      
      // 统一处理6个面
      renderFace(t, world, layer, x, y, z, 0, u0, u1, 0); // Y-
      renderFace(t, world, layer, x, y, z, 1, u0, u1, 0); // Y+
      renderFace(t, world, layer, x, y, z, 2, u0, u1, 1); // Z-
      renderFace(t, world, layer, x, y, z, 3, u0, u1, 1); // Z+
      renderFace(t, world, layer, x, y, z, 4, u0, u1, 2); // X-
      renderFace(t, world, layer, x, y, z, 5, u0, u1, 2); // X+
   }
   
   private void renderFace(MeshBuilder t, World world, int layer, int x, int y, int z, 
                           int face, float u0, float u1, int brightnessIndex) {
      int[] dx = {0, 0, 0, 0, -1, 1};
      int[] dy = {-1, 1, 0, 0, 0, 0};
      int[] dz = {0, 0, -1, 1, 0, 0};
      
      int nx = x + dx[face];
      int ny = y + dy[face];
      int nz = z + dz[face];
      
      if (!world.isSolidBlock(nx, ny, nz)) {
         float br = world.getBrightness(nx, ny, nz) * BRIGHTNESS[brightnessIndex][0];
         if ((br == BRIGHTNESS[brightnessIndex][0]) ^ (layer == 1)) {
            t.color(br, br, br);
            float[][] vertices = getFaceVertices(x, y, z, face);
            for (float[] v : vertices) {
               t.tex(v[3], v[4]);
               t.vertex(v[0], v[1], v[2]);
            }
         }
      }
   }
   
   private float[][] getFaceVertices(int x, int y, int z, int face) {
      float x0 = x, x1 = x + 1;
      float y0 = y, y1 = y + 1;
      float z0 = z, z1 = z + 1;
      
      // 预定义所有面的顶点和纹理坐标
      float[][][] faces = {
         {{x0, y0, z1, 0, 1}, {x0, y0, z0, 0, 0}, {x1, y0, z0, 1, 0}, {x1, y0, z1, 1, 1}}, // Y-
         {{x1, y1, z1, 1, 1}, {x1, y1, z0, 1, 0}, {x0, y1, z0, 0, 0}, {x0, y1, z1, 0, 1}}, // Y+
         {{x0, y1, z0, 1, 0}, {x1, y1, z0, 0, 0}, {x1, y0, z0, 0, 1}, {x0, y0, z0, 1, 1}}, // Z-
         {{x0, y1, z1, 0, 0}, {x0, y0, z1, 0, 1}, {x1, y0, z1, 1, 1}, {x1, y1, z1, 1, 0}}, // Z+
         {{x0, y1, z1, 1, 0}, {x0, y1, z0, 0, 0}, {x0, y0, z0, 0, 1}, {x0, y0, z1, 1, 1}}, // X-
         {{x1, y0, z1, 0, 1}, {x1, y0, z0, 1, 1}, {x1, y1, z0, 1, 0}, {x1, y1, z1, 0, 0}}  // X+
      };
      return faces[face];
   }
}
