package com.voxelgame.world;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class MeshBuilder {
   private static final int MAX_VERTICES = 100000;
   private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(300000);
   private FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(200000);
   private FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(300000);
   private int vertices = 0;
   private float u, v, r, g, b;
   private boolean hasColor = false;
   private boolean hasTexture = false;

   public void flush() {
      vertexBuffer.flip();
      texCoordBuffer.flip();
      colorBuffer.flip();
      
      GL11.glVertexPointer(3, 0, vertexBuffer);
      if (hasTexture) GL11.glTexCoordPointer(2, 0, texCoordBuffer);
      if (hasColor) GL11.glColorPointer(3, 0, colorBuffer);
      
      GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
      if (hasTexture) GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
      if (hasColor) GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
      
      GL11.glDrawArrays(GL11.GL_QUADS, 0, vertices);
      
      GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
      if (hasTexture) GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
      if (hasColor) GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
      
      clear();
   }

   private void clear() {
      vertices = 0;
      vertexBuffer.clear();
      texCoordBuffer.clear();
      colorBuffer.clear();
   }

   public void init() {
      clear();
      hasColor = false;
      hasTexture = false;
   }

   public void tex(float u, float v) {
      hasTexture = true;
      this.u = u;
      this.v = v;
   }

   public void color(float r, float g, float b) {
      hasColor = true;
      this.r = r;
      this.g = g;
      this.b = b;
   }

   public void vertex(float x, float y, float z) {
      vertexBuffer.put(vertices * 3, x).put(vertices * 3 + 1, y).put(vertices * 3 + 2, z);
      if (hasTexture) {
         texCoordBuffer.put(vertices * 2, u).put(vertices * 2 + 1, v);
      }
      if (hasColor) {
         colorBuffer.put(vertices * 3, r).put(vertices * 3 + 1, g).put(vertices * 3 + 2, b);
      }
      
      if (++vertices == MAX_VERTICES) {
         flush();
      }
   }
}
