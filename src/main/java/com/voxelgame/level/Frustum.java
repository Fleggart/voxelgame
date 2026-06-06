package com.voxelgame.level;

import com.voxelgame.physics.AABB;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class Frustum {
    private final float[][] frustum = new float[6][4];
    private static Frustum instance = new Frustum();
    
    private final FloatBuffer projBuffer = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer modlBuffer = BufferUtils.createFloatBuffer(16);
    private final float[] proj = new float[16];
    private final float[] modl = new float[16];
    private final float[] clip = new float[16];

    private Frustum() {}

    public static Frustum getInstance() {
        instance.calculateFrustum();
        return instance;
    }

    private void normalizePlane(int side) {
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
        projBuffer.clear();
        modlBuffer.clear();
        
        // LWJGL 3 的正确用法
        glGetFloatv(GL_PROJECTION_MATRIX, projBuffer);
        glGetFloatv(GL_MODELVIEW_MATRIX, modlBuffer);
        
        projBuffer.flip();
        modlBuffer.flip();
        projBuffer.get(proj);
        modlBuffer.get(modl);
        
        // 计算裁剪矩阵
        clip[0]  = modl[0] * proj[0] + modl[1] * proj[4] + modl[2] * proj[8] + modl[3] * proj[12];
        clip[1]  = modl[0] * proj[1] + modl[1] * proj[5] + modl[2] * proj[9] + modl[3] * proj[13];
        clip[2]  = modl[0] * proj[2] + modl[1] * proj[6] + modl[2] * proj[10] + modl[3] * proj[14];
        clip[3]  = modl[0] * proj[3] + modl[1] * proj[7] + modl[2] * proj[11] + modl[3] * proj[15];
        
        clip[4]  = modl[4] * proj[0] + modl[5] * proj[4] + modl[6] * proj[8] + modl[7] * proj[12];
        clip[5]  = modl[4] * proj[1] + modl[5] * proj[5] + modl[6] * proj[9] + modl[7] * proj[13];
        clip[6]  = modl[4] * proj[2] + modl[5] * proj[6] + modl[6] * proj[10] + modl[7] * proj[14];
        clip[7]  = modl[4] * proj[3] + modl[5] * proj[7] + modl[6] * proj[11] + modl[7] * proj[15];
        
        clip[8]  = modl[8] * proj[0] + modl[9] * proj[4] + modl[10] * proj[8] + modl[11] * proj[12];
        clip[9]  = modl[8] * proj[1] + modl[9] * proj[5] + modl[10] * proj[9] + modl[11] * proj[13];
        clip[10] = modl[8] * proj[2] + modl[9] * proj[6] + modl[10] * proj[10] + modl[11] * proj[14];
        clip[11] = modl[8] * proj[3] + modl[9] * proj[7] + modl[10] * proj[11] + modl[11] * proj[15];
        
        clip[12] = modl[12] * proj[0] + modl[13] * proj[4] + modl[14] * proj[8] + modl[15] * proj[12];
        clip[13] = modl[12] * proj[1] + modl[13] * proj[5] + modl[14] * proj[9] + modl[15] * proj[13];
        clip[14] = modl[12] * proj[2] + modl[13] * proj[6] + modl[14] * proj[10] + modl[15] * proj[14];
        clip[15] = modl[12] * proj[3] + modl[13] * proj[7] + modl[14] * proj[11] + modl[15] * proj[15];
        
        // 提取平面
        // 右平面
        frustum[0][0] = clip[3] - clip[0];
        frustum[0][1] = clip[7] - clip[4];
        frustum[0][2] = clip[11] - clip[8];
        frustum[0][3] = clip[15] - clip[12];
        normalizePlane(0);
        
        // 左平面
        frustum[1][0] = clip[3] + clip[0];
        frustum[1][1] = clip[7] + clip[4];
        frustum[1][2] = clip[11] + clip[8];
        frustum[1][3] = clip[15] + clip[12];
        normalizePlane(1);
        
        // 底平面
        frustum[2][0] = clip[3] + clip[1];
        frustum[2][1] = clip[7] + clip[5];
        frustum[2][2] = clip[11] + clip[9];
        frustum[2][3] = clip[15] + clip[13];
        normalizePlane(2);
        
        // 顶平面
        frustum[3][0] = clip[3] - clip[1];
        frustum[3][1] = clip[7] - clip[5];
        frustum[3][2] = clip[11] - clip[9];
        frustum[3][3] = clip[15] - clip[13];
        normalizePlane(3);
        
        // 远平面
        frustum[4][0] = clip[3] - clip[2];
        frustum[4][1] = clip[7] - clip[6];
        frustum[4][2] = clip[11] - clip[10];
        frustum[4][3] = clip[15] - clip[14];
        normalizePlane(4);
        
        // 近平面
        frustum[5][0] = clip[3] + clip[2];
        frustum[5][1] = clip[7] + clip[6];
        frustum[5][2] = clip[11] + clip[10];
        frustum[5][3] = clip[15] + clip[14];
        normalizePlane(5);
    }

    public boolean pointInFrustum(float x, float y, float z) {
        for (int i = 0; i < 6; i++) {
            if (frustum[i][0] * x + frustum[i][1] * y + frustum[i][2] * z + frustum[i][3] <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean sphereInFrustum(float x, float y, float z, float radius) {
        for (int i = 0; i < 6; i++) {
            if (frustum[i][0] * x + frustum[i][1] * y + frustum[i][2] * z + frustum[i][3] <= -radius) {
                return false;
            }
        }
        return true;
    }

    public boolean cubeInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
        for (int i = 0; i < 6; i++) {
            if (frustum[i][0] * x1 + frustum[i][1] * y1 + frustum[i][2] * z1 + frustum[i][3] <= 0 &&
                frustum[i][0] * x2 + frustum[i][1] * y1 + frustum[i][2] * z1 + frustum[i][3] <= 0 &&
                frustum[i][0] * x1 + frustum[i][1] * y2 + frustum[i][2] * z1 + frustum[i][3] <= 0 &&
                frustum[i][0] * x2 + frustum[i][1] * y2 + frustum[i][2] * z1 + frustum[i][3] <= 0 &&
                frustum[i][0] * x1 + frustum[i][1] * y1 + frustum[i][2] * z2 + frustum[i][3] <= 0 &&
                frustum[i][0] * x2 + frustum[i][1] * y1 + frustum[i][2] * z2 + frustum[i][3] <= 0 &&
                frustum[i][0] * x1 + frustum[i][1] * y2 + frustum[i][2] * z2 + frustum[i][3] <= 0 &&
                frustum[i][0] * x2 + frustum[i][1] * y2 + frustum[i][2] * z2 + frustum[i][3] <= 0) {
                return false;
            }
        }
        return true;
    }

    public boolean cubeInFrustum(AABB aabb) {
        return cubeInFrustum(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1);
    }
}
