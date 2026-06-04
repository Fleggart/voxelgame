package com.voxelgame.world;

import com.voxelgame.world.MeshBuilder;

public class Block {
   public static final Block[] BLOCKS = new Block[256];
   public static final Block ROCK = new Block(0);
   public static final Block GRASS = new Block(1);
   
   private final int tex;
   private static final float[][] BRIGHTNESS = {
      {1.0f, 1.0f, 1.0f},
      {0.8f, 0.8f, 0.8f},
      {0.6f, 0.6f, 0.6f}
   };
   
   // 纹理图集参数：256x256 图片，16x16 格子，每个格子 16x16 像素
   private static final float TEXTURE_SIZE = 256.0f;
   private static final float TILE_SIZE = 16.0f;
   private static final float TILE_U = TILE_SIZE / TEXTURE_SIZE;  // = 0.0625f

   private Block(int tex) {
      this.tex = tex;
      BLOCKS[tex] = this;
   }
   
   public void render(MeshBuilder t, World world, int layer, int x, int y, int z) {
      // 计算纹理坐标：每个格子的 uv 范围
      int tileX = tex % 16;
      int tileY = tex / 16;
      float u0 = tileX * TILE_U;
      float u1 = u0 + TILE_U;
      float v0 = tileY * TILE_U;
      float v1 = v0 + TILE_U;
      
      // 统一处理6个面
      renderFace(t, world, layer, x, y, z, 0, u0, u1, v0, v1, 0); // Y-
      renderFace(t, world, layer, x, y, z, 1, u0, u1, v0, v1, 0); // Y+
      renderFace(t, world, layer, x, y, z, 2, u0, u1, v0, v1, 1); // Z-
      renderFace(t, world, layer, x, y, z, 3, u0, u1, v0, v1, 1); // Z+
      renderFace(t, world, layer, x, y, z, 4, u0, u1, v0, v1, 2); // X-
      renderFace(t, world, layer, x, y, z, 5, u0, u1, v0, v1, 2); // X+
   }
   
   private void renderFace(MeshBuilder t, World world, int layer, int x, int y, int z, 
                           int face, float u0, float u1, float v0, float v1, int brightnessIndex) {
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
               // v[3] 是 0-1 的水平纹理坐标，v[4] 是 0-1 的垂直纹理坐标
               float u = u0 + v[3] * (u1 - u0);
               float vv = v0 + v[4] * (v1 - v0);
               t.tex(u, vv);
               t.vertex(v[0], v[1], v[2]);
            }
         }
      }
   }
   
   // 供 WorldRenderer.pick() 和 renderHit() 调用
   public void renderFace(MeshBuilder t, int x, int y, int z, int face) {
      // 计算纹理坐标
      int tileX = tex % 16;
      int tileY = tex / 16;
      float u0 = tileX * TILE_U;
      float u1 = u0 + TILE_U;
      float v0 = tileY * TILE_U;
      float v1 = v0 + TILE_U;
      
      float x0 = x, x1 = x + 1;
      float y0 = y, y1 = y + 1;
      float z0 = z, z1 = z + 1;
      
      t.color(1.0f, 1.0f, 1.0f);
      
      float[][][] faces = {
         {{x0, y0, z1, 0, 1}, {x0, y0, z0, 0, 0}, {x1, y0, z0, 1, 0}, {x1, y0, z1, 1, 1}},
         {{x1, y1, z1, 1, 1}, {x1, y1, z0, 1, 0}, {x0, y1, z0, 0, 0}, {x0, y1, z1, 0, 1}},
         {{x0, y1, z0, 1, 0}, {x1, y1, z0, 0, 0}, {x1, y0, z0, 0, 1}, {x0, y0, z0, 1, 1}},
         {{x0, y1, z1, 0, 0}, {x0, y0, z1, 0, 1}, {x1, y0, z1, 1, 1}, {x1, y1, z1, 1, 0}},
         {{x0, y1, z1, 1, 0}, {x0, y1, z0, 0, 0}, {x0, y0, z0, 0, 1}, {x0, y0, z1, 1, 1}},
         {{x1, y0, z1, 0, 1}, {x1, y0, z0, 1, 1}, {x1, y1, z0, 1, 0}, {x1, y1, z1, 0, 0}}
      };
      
      float[][] vertices = faces[face];
      for (float[] v : vertices) {
         float u = u0 + v[3] * (u1 - u0);
         float vv = v0 + v[4] * (v1 - v0);
         t.tex(u, vv);
         t.vertex(v[0], v[1], v[2]);
      }
   }
   
   private float[][] getFaceVertices(int x, int y, int z, int face) {
      float x0 = x, x1 = x + 1;
      float y0 = y, y1 = y + 1;
      float z0 = z, z1 = z + 1;
      
      float[][][] faces = {
         {{x0, y0, z1, 0, 1}, {x0, y0, z0, 0, 0}, {x1, y0, z0, 1, 0}, {x1, y0, z1, 1, 1}},
         {{x1, y1, z1, 1, 1}, {x1, y1, z0, 1, 0}, {x0, y1, z0, 0, 0}, {x0, y1, z1, 0, 1}},
         {{x0, y1, z0, 1, 0}, {x1, y1, z0, 0, 0}, {x1, y0, z0, 0, 1}, {x0, y0, z0, 1, 1}},
         {{x0, y1, z1, 0, 0}, {x0, y0, z1, 0, 1}, {x1, y0, z1, 1, 1}, {x1, y1, z1, 1, 0}},
         {{x0, y1, z1, 1, 0}, {x0, y1, z0, 0, 0}, {x0, y0, z0, 0, 1}, {x0, y0, z1, 1, 1}},
         {{x1, y0, z1, 0, 1}, {x1, y0, z0, 1, 1}, {x1, y1, z0, 1, 0}, {x1, y1, z1, 0, 0}}
      };
      return faces[face];
   }
}
