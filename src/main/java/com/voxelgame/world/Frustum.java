package com.voxelgame.world;

import com.voxelgame.physics.BoundingBox;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class Frustum {
    private final Vector4f[] planes = new Vector4f[6];
    
    public static final int RIGHT = 0;
    public static final int LEFT = 1;
    public static final int BOTTOM = 2;
    public static final int TOP = 3;
    public static final int BACK = 4;
    public static final int FRONT = 5;
    
    private static Frustum frustum = new Frustum();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final Matrix4f modelViewMatrix = new Matrix4f();
    private final Matrix4f clipMatrix = new Matrix4f();
    
    private Frustum() {
        for (int i = 0; i < 6; i++) {
            planes[i] = new Vector4f();
        }
    }

    public static Frustum getFrustum() {
        frustum.calculateFrustum();
        return frustum;
    }

    private void calculateFrustum() {
        // Get current matrices from OpenGL
        matrixBuffer.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, matrixBuffer);
        projectionMatrix.set(matrixBuffer);
        
        matrixBuffer.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, matrixBuffer);
        modelViewMatrix.set(matrixBuffer);
        
        // Combine matrices: clip = projection * modelView
        clipMatrix.set(projectionMatrix).mul(modelViewMatrix);
        
        // Extract frustum planes from clip matrix
        // Right plane: column 4 - column 1
        planes[RIGHT].x = clipMatrix.m03() - clipMatrix.m00();
        planes[RIGHT].y = clipMatrix.m13() - clipMatrix.m10();
        planes[RIGHT].z = clipMatrix.m23() - clipMatrix.m20();
        planes[RIGHT].w = clipMatrix.m33() - clipMatrix.m30();
        normalizePlane(RIGHT);
        
        // Left plane: column 4 + column 1
        planes[LEFT].x = clipMatrix.m03() + clipMatrix.m00();
        planes[LEFT].y = clipMatrix.m13() + clipMatrix.m10();
        planes[LEFT].z = clipMatrix.m23() + clipMatrix.m20();
        planes[LEFT].w = clipMatrix.m33() + clipMatrix.m30();
        normalizePlane(LEFT);
        
        // Bottom plane: column 4 + column 2
        planes[BOTTOM].x = clipMatrix.m03() + clipMatrix.m01();
        planes[BOTTOM].y = clipMatrix.m13() + clipMatrix.m11();
        planes[BOTTOM].z = clipMatrix.m23() + clipMatrix.m21();
        planes[BOTTOM].w = clipMatrix.m33() + clipMatrix.m31();
        normalizePlane(BOTTOM);
        
        // Top plane: column 4 - column 2
        planes[TOP].x = clipMatrix.m03() - clipMatrix.m01();
        planes[TOP].y = clipMatrix.m13() - clipMatrix.m11();
        planes[TOP].z = clipMatrix.m23() - clipMatrix.m21();
        planes[TOP].w = clipMatrix.m33() - clipMatrix.m31();
        normalizePlane(TOP);
        
        // Back plane: column 4 - column 3
        planes[BACK].x = clipMatrix.m03() - clipMatrix.m02();
        planes[BACK].y = clipMatrix.m13() - clipMatrix.m12();
        planes[BACK].z = clipMatrix.m23() - clipMatrix.m22();
        planes[BACK].w = clipMatrix.m33() - clipMatrix.m32();
        normalizePlane(BACK);
        
        // Front plane: column 4 + column 3
        planes[FRONT].x = clipMatrix.m03() + clipMatrix.m02();
        planes[FRONT].y = clipMatrix.m13() + clipMatrix.m12();
        planes[FRONT].z = clipMatrix.m23() + clipMatrix.m22();
        planes[FRONT].w = clipMatrix.m33() + clipMatrix.m32();
        normalizePlane(FRONT);
    }

    private void normalizePlane(int side) {
        float magnitude = (float) Math.sqrt(
            planes[side].x * planes[side].x + 
            planes[side].y * planes[side].y + 
            planes[side].z * planes[side].z
        );
        planes[side].x /= magnitude;
        planes[side].y /= magnitude;
        planes[side].z /= magnitude;
        planes[side].w /= magnitude;
    }
    
    private float distanceToPlane(Vector4f plane, float x, float y, float z) {
        return plane.x * x + plane.y * y + plane.z * z + plane.w;
    }

    public boolean cubeInFrustum(float x1, float y1, float z1, float x2, float y2, float z2) {
        for (int i = 0; i < 6; i++) {
            // Check all 8 corners of the bounding box
            if (distanceToPlane(planes[i], x1, y1, z1) <= 0.0F &&
                distanceToPlane(planes[i], x2, y1, z1) <= 0.0F &&
                distanceToPlane(planes[i], x1, y2, z1) <= 0.0F &&
                distanceToPlane(planes[i], x2, y2, z1) <= 0.0F &&
                distanceToPlane(planes[i], x1, y1, z2) <= 0.0F &&
                distanceToPlane(planes[i], x2, y1, z2) <= 0.0F &&
                distanceToPlane(planes[i], x1, y2, z2) <= 0.0F &&
                distanceToPlane(planes[i], x2, y2, z2) <= 0.0F) {
                return false;
            }
        }
        return true;
    }

    public boolean cubeInFrustum(BoundingBox aabb) {
        return cubeInFrustum(aabb.x0, aabb.y0, aabb.z0, aabb.x1, aabb.y1, aabb.z1);
    }
}
