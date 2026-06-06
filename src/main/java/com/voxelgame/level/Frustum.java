package com.voxelgame.level;

import com.voxelgame.phys.AABB;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;

public class Frustum {
    public float[][] m_Frustum = new float[6][4];
    public static final int RIGHT = 0, LEFT = 1, BOTTOM = 2, TOP = 3, BACK = 4, FRONT = 5;
    private static Frustum frustum = new Frustum();
    private FloatBuffer _proj = BufferUtils.createFloatBuffer(16);
    private FloatBuffer _modl = BufferUtils.createFloatBuffer(16);
    private float[] proj = new float[16];
    private float[] modl = new float[16];
    private float[] clip = new float[16];
    
    private Frustum() {}
    
    public static Frustum getFrustum() {
        frustum.calculateFrustum();
        return frustum;
    }
    
    private void normalizePlane(float[][] frustum, int side) {
        float magnitude = (float) Math.sqrt(
            frustum[side][0] * frustum[side][0] +
            frustum[side][1] * frustum[side][1] +
            frustum[side][2] * frustum[side][2]
        );
        frustum[side][0] /= magnitude;
        frustum[side][1] /= magnitude;
        frustum[side][2] /= magnitude;
        frustum[side][3] /= magnitude;
    }
    
    private void calculateFrustum() {
        _proj.clear();
        _modl.clear();
        glGetFloatv(GL_PROJECTION_MATRIX, _proj);
        glGetFloatv(GL_MODELVIEW_MATRIX, _modl);
        _proj.flip().limit(16);
        _modl.flip().limit(16);
        _proj.get(proj);
        _modl.get(modl);
        
        // 计算裁剪矩阵
        clip[0] = modl[0] * proj[0] + modl[1] * proj[4] + modl[2] * proj[8] + modl[3] * proj[12];
        clip[1] = modl[0] * proj[1] + modl[1] * proj[5] + modl[2] * proj[9] + modl[3] * proj[13];
        clip[2] = modl[0] * proj[2] + modl[1] * proj[6] + modl[2] * proj[10] + modl[3] * proj[14];
        clip[3] = modl[0] * proj[3] + modl[1] * proj[7] + modl[2] * proj[11] + modl[3] * proj[15];
        clip[4] = modl[4] * proj[0] + modl[5] * proj[4] + modl[6] * proj[8] + modl[7] * proj[12];
        clip[5] = modl[4] * proj[1] + modl[5] * proj[5] + modl[6] * proj[9] + modl[7] * proj[13];
        clip[6] = modl[4] * proj[2] + modl[5] * proj[6] + modl[6] * proj[10] + modl[7] * proj[14];
        clip[7] = modl[4] * proj[3] + modl[5] * proj[7] + modl[6] * proj[11] + modl[7] * proj[15];
        clip[8] = modl[8] * proj[0] + modl[9] * proj[4] + modl[10] * proj[8] + modl[11] * proj[12];
        clip[9] = modl[8] * proj[1] + modl[9] * proj[5] + modl[10] * proj[9] + modl[11] * proj[13];
        clip[10] = modl[8] * proj[2] + modl[9] * proj[6] + modl[10] * proj[10] + modl[11] * proj[14];
        clip[11] = modl[8] * proj[3] + modl[9] * proj[7] + modl[10] * proj[11] + modl[11] * proj[15];
        clip[12] = modl[12] * proj[0] + modl[13] * proj[4] + modl[14] * proj[8] + modl[15] * proj[12];
        clip[13] = modl[12] * proj[1] + modl[13] * proj[5] + modl[14] * proj[9] + modl[15] * proj[13];
        clip[14] = modl[12] * proj[2] + modl[13] * proj[6] + modl[14] * proj[10] + modl[15] * proj[14];
        clip[15] = modl[12] * proj[3] + modl[13] * proj[7] + modl[14] * proj[11] + modl[15] * proj[15];
        
        // 提取视锥平面
        m_Frustum[LEFT][0] = clip[3] - clip[0];
        m_Frustum[LEFT][1] = clip[7] - clip[4];
        m_Frustum[LEFT][2] = clip[11] - clip[8];
        m_Frustum[LEFT][3] = clip[15] - clip[12];
        normalizePlane(m_Frustum, LEFT);
        
        m_Frustum[RIGHT][0] = clip[3] + clip[0];
        m_Frustum[RIGHT][1] = clip[7] + clip[4];
        m_Frustum[RIGHT][2] = clip[11] + clip[8];
        m_Frustum[RIGHT][3] = clip[15] + clip[12];
        normalizePlane(m_Frustum, RIGHT);
        
        m_Frustum[BOTTOM][0] = clip[3] + clip[1];
        m_Frustum[BOTTOM][1] = clip[7] + clip[5];
        m_Frustum[BOTTOM][2] = clip[11] + clip[9];
        m_Frustum[BOTTOM][3] = clip[15] + clip[13];
        normalizePlane(m_Frustum, BOTTOM);
        
        m_Frustum[TOP][0] = clip[3] - clip[1];
        m_Frustum[TOP][1] = clip[7] - clip[5];
        m_Frustum[TOP][2] = clip[11] - clip[9];
        m_Frustum[TOP][3] = clip[15] - clip[13];
        normalizePlane(m_Frustum, TOP);
        
        m_Frustum[BACK][0] = clip[3] - clip[2];
        m_Frustum[BACK][1] = clip[7] - clip[6];
        m_Frustum[BACK][2] = clip[11] - clip[10];
        m_Frustum[BACK][3] = clip[15] - clip[14];
        normalizePlane(m_Frustum, BACK);
        
        m_Frustum[FRONT][0] = clip[3] + clip[2];
        m_Frustum[FRONT][1] = clip[7] + clip[6];
        m_Frustum[FRONT][2] = clip[11] + clip[10];
        m_Frustum[FRONT][3] = clip[15] + clip[14];
        normalizePlane(m_Frustum, FRONT);
    }
    
    public boolean cubeInFrustum(AABB aabb) {
        return cubeInFrustum(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1);
    }
    
    public boolean cubeInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
        for (int i = 0; i < 6; i++) {
            if (m_Frustum[i][0] * x1 + m_Frustum[i][1] * y1 + m_Frustum[i][2] * z1 + m_Frustum[i][3] <= 0 &&
                m_Frustum[i][0] * x2 + m_Frustum[i][1] * y1 + m_Frustum[i][2] * z1 + m_Frustum[i][3] <= 0 &&
                m_Frustum[i][0] * x1 + m_Frustum[i][1] * y2 + m_Frustum[i][2] * z1 + m_Frustum[i][3] <= 0 &&
                m_Frustum[i][0] * x2 + m_Frustum[i][1] * y2 + m_Frustum[i][2] * z1 + m_Frustum[i][3] <= 0 &&
                m_Frustum[i][0] * x1 + m_Frustum[i][1] * y1 + m_Frustum[i][2] * z2 + m_Frustum[i][3] <= 0 &&
                m_Frustum[i][0] * x2 + m_Frustum[i][1] * y1 + m_Frustum[i][2] * z2 + m_Frustum[i][3] <= 0 &&
                m_Frustum[i][0] * x1 + m_Frustum[i][1] * y2 + m_Frustum[i][2] * z2 + m_Frustum[i][3] <= 0 &&
                m_Frustum[i][0] * x2 + m_Frustum[i][1] * y2 + m_Frustum[i][2] * z2 + m_Frustum[i][3] <= 0) {
                return false;
            }
        }
        return true;
    }
}
