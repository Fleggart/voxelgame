package com.voxelgame.world;

import com.voxelgame.physics.BoundingBox;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class Frustum {
   public float[][] m_Frustum = new float[6][4];
   public static final int RIGHT = 0;
   public static final int LEFT = 1;
   public static final int BOTTOM = 2;
   public static final int TOP = 3;
   public static final int BACK = 4;
   public static final int FRONT = 5;
   
   private static Frustum frustum = new Frustum();
   private FloatBuffer _proj = BufferUtils.createFloatBuffer(16);
   private FloatBuffer _modl = BufferUtils.createFloatBuffer(16);
   private FloatBuffer _clip = BufferUtils.createFloatBuffer(16);
   float[] proj = new float[16];
   float[] modl = new float[16];
   float[] clip = new float[16];

   private Frustum() {}

   public static Frustum getFrustum() {
      frustum.calculateFrustum();
      return frustum;
   }

   private void normalizePlane(int side) {
      float magnitude = (float)Math.sqrt(
         m_Frustum[side][0] * m_Frustum[side][0] + 
         m_Frustum[side][1] * m_Frustum[side][1] + 
         m_Frustum[side][2] * m_Frustum[side][2]
      );
      m_Frustum[side][0] /= magnitude;
      m_Frustum[side][1] /= magnitude;
      m_Frustum[side][2] /= magnitude;
      m_Frustum[side][3] /= magnitude;
   }

   private void calculateFrustum() {
      this._proj.clear();
      this._modl.clear();
      this._clip.clear();
      GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, this._proj);
      GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, this._modl);
      this._proj.flip().limit(16);
      this._proj.get(this.proj);
      this._modl.flip().limit(16);
      this._modl.get(this.modl);
      
      // 计算裁剪矩阵
      for (int i = 0; i < 4; i++) {
         for (int j = 0; j < 4; j++) {
            int idx = i * 4 + j;
            clip[idx] = 0;
            for (int k = 0; k < 4; k++) {
               clip[idx] += modl[i * 4 + k] * proj[k * 4 + j];
            }
         }
      }
      
      // 提取平面
      m_Frustum[RIGHT] = new float[]{clip[3] - clip[0], clip[7] - clip[4], clip[11] - clip[8], clip[15] - clip[12]};
      m_Frustum[LEFT] = new float[]{clip[3] + clip[0], clip[7] + clip[4], clip[11] + clip[8], clip[15] + clip[12]};
      m_Frustum[BOTTOM] = new float[]{clip[3] + clip[1], clip[7] + clip[5], clip[11] + clip[9], clip[15] + clip[13]};
      m_Frustum[TOP] = new float[]{clip[3] - clip[1], clip[7] - clip[5], clip[11] - clip[9], clip[15] - clip[13]};
      m_Frustum[BACK] = new float[]{clip[3] - clip[2], clip[7] - clip[6], clip[11] - clip[10], clip[15] - clip[14]};
      m_Frustum[FRONT] = new float[]{clip[3] + clip[2], clip[7] + clip[6], clip[11] + clip[10], clip[15] + clip[14]};
      
      for (int i = 0; i < 6; i++) normalizePlane(i);
   }

   public boolean cubeInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
      for (int i = 0; i < 6; i++) {
         if (m_Frustum[i][0] * x1 + m_Frustum[i][1] * y1 + m_Frustum[i][2] * z1 + m_Frustum[i][3] <= 0.0F &&
             m_Frustum[i][0] * x2 + m_Frustum[i][1] * y1 + m_Frustum[i][2] * z1 + m_Frustum[i][3] <= 0.0F &&
             m_Frustum[i][0] * x1 + m_Frustum[i][1] * y2 + m_Frustum[i][2] * z1 + m_Frustum[i][3] <= 0.0F &&
             m_Frustum[i][0] * x2 + m_Frustum[i][1] * y2 + m_Frustum[i][2] * z1 + m_Frustum[i][3] <= 0.0F &&
             m_Frustum[i][0] * x1 + m_Frustum[i][1] * y1 + m_Frustum[i][2] * z2 + m_Frustum[i][3] <= 0.0F &&
             m_Frustum[i][0] * x2 + m_Frustum[i][1] * y1 + m_Frustum[i][2] * z2 + m_Frustum[i][3] <= 0.0F &&
             m_Frustum[i][0] * x1 + m_Frustum[i][1] * y2 + m_Frustum[i][2] * z2 + m_Frustum[i][3] <= 0.0F &&
             m_Frustum[i][0] * x2 + m_Frustum[i][1] * y2 + m_Frustum[i][2] * z2 + m_Frustum[i][3] <= 0.0F) {
            return false;
         }
      }
      return true;
   }

   public boolean cubeInFrustum(BoundingBox aabb) {
      return cubeInFrustum(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1);
   }
}
