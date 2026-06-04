package com.voxelgame.world;

import org.lwjgl.opengl.GL11;

public class Block {
   public static final Block[] BLOCKS = new Block[256];
   public static final Block STONE = new Block(1);  // 石头纹理，tex=1
   public static final Block GRASS = new Block(0);  // 草纹理，tex=0
   
   private final int tex;
   private static final float[][] BRIGHTNESS = {
      {1.0f, 1.0f, 1.0f},
      {0.8f, 0.8f, 0.8f},
      {0.6f, 0.6f, 0.6f}
   };
   
   private static final float TEXTURE_SIZE = 256.0f;
   private static final float TILE_SIZE = 16.0f;
   private static final float TILE_U = TILE_SIZE / TEXTURE_SIZE;
   
   // 预定义所有面的顶点数据（相对坐标+UV）
   private static final float[][][] FACE_VERTICES = {
      {{0,0,1,0,1},{0,0,0,0,0},{1,0,0,1,0},{1,0,1,1,1}}, // Y- (下)
      {{1,1,1,1,1},{1,1,0,1,0},{0,1,0,0,0},{0,1,1,0,1}}, // Y+ (上)
      {{0,1,0,1,0},{1,1,0,0,0},{1,0,0,0,1},{0,0,0,1,1}}, // Z- (北)
      {{0,1,1,0,0},{0,0,1,0,1},{1,0,1,1,1},{1,1,1,1,0}}, // Z+ (南)
      {{0,1,1,1,0},{0,1,0,0,0},{0,0,0,0,1},{0,0,1,1,1}}, // X- (西)
      {{1,0,1,0,1},{1,0,0,1,1},{1,1,0,1,0},{1,1,1,0,0}}  // X+ (东)
   };

   private Block(int tex) {
      this.tex = tex;
      BLOCKS[tex] = this;
   }
   
   private float[] getTexCoords() {
      int tileX = tex % 16;
      int tileY = tex / 16;
      float u0 = tileX * TILE_U;
      float u1 = u0 + TILE_U;
      float v0 = tileY * TILE_U;
      float v1 = v0 + TILE_U;
      return new float[]{u0, u1, v0, v1};
   }
   
   public void render(MeshBuilder t, World world, int layer, int x, int y, int z) {
      float[] uv = getTexCoords();
      float u0 = uv[0], u1 = uv[1], v0 = uv[2], v1 = uv[3];
      
      for (int face = 0; face < 6; face++) {
         int brightnessIndex = face >= 4 ? 2 : (face >= 2 ? 1 : 0);
         renderFace(t, world, layer, x, y, z, face, u0, u1, v0, v1, brightnessIndex);
      }
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
               float u = u0 + v[3] * (u1 - u0);
               float vv = v0 + v[4] * (v1 - v0);
               t.tex(u, vv);
               t.vertex(v[0], v[1], v[2]);
            }
         }
      }
   }
   
   public void renderFace(MeshBuilder t, int x, int y, int z, int face) {
      float[] uv = getTexCoords();
      float u0 = uv[0], u1 = uv[1], v0 = uv[2], v1 = uv[3];
      
      t.color(1.0f, 1.0f, 1.0f);
      float[][] vertices = getFaceVertices(x, y, z, face);
      for (float[] v : vertices) {
         float u = u0 + v[3] * (u1 - u0);
         float vv = v0 + v[4] * (v1 - v0);
         t.tex(u, vv);
         t.vertex(v[0], v[1], v[2]);
      }
   }
   
   public void renderFaceImmediate(int x, int y, int z, int face) {
      float[] uv = getTexCoords();
      float u0 = uv[0], u1 = uv[1], v0 = uv[2], v1 = uv[3];
      
      float[][] vertices = getFaceVertices(x, y, z, face);
      for (float[] v : vertices) {
         float u = u0 + v[3] * (u1 - u0);
         float vv = v0 + v[4] * (v1 - v0);
         GL11.glTexCoord2f(u, vv);
         GL11.glVertex3f(v[0], v[1], v[2]);
      }
   }
   
   private float[][] getFaceVertices(int x, int y, int z, int face) {
      float[][] verts = new float[4][5];
      float[][] template = FACE_VERTICES[face];
      for (int i = 0; i < 4; i++) {
         verts[i][0] = x + template[i][0];
         verts[i][1] = y + template[i][1];
         verts[i][2] = z + template[i][2];
         verts[i][3] = template[i][3];
         verts[i][4] = template[i][4];
      }
      return verts;
   }
}
