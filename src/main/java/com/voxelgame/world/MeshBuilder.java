package com.voxelgame.world;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class MeshBuilder {
    private static final int MAX_VERTICES = 100000;
    
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    private FloatBuffer colorBuffer;
    private int vertices = 0;
    
    private boolean hasColor = false;
    private boolean hasTexture = false;
    private float u, v, r, g, b;
    private boolean finished = false;
    
    public MeshBuilder() {
        vertexBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 3);
        texCoordBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 2);
        colorBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 3);
    }
    
    public void init() {
        vertices = 0;
        vertexBuffer.clear();
        texCoordBuffer.clear();
        colorBuffer.clear();
        hasColor = false;
        hasTexture = false;
        finished = false;
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
        vertexBuffer.put(x).put(y).put(z);
        if (hasTexture) {
            texCoordBuffer.put(u).put(v);
        }
        if (hasColor) {
            colorBuffer.put(r).put(g).put(b);
        }
        vertices++;
    }
    
    public void flush() {
        // 这个方法保留用于向后兼容，但不再自动提交
    }
    
    public void finish() {
        vertexBuffer.flip();
        texCoordBuffer.flip();
        colorBuffer.flip();
        finished = true;
    }
    
    public FloatBuffer getVertexBuffer() {
        if (!finished) finish();
        return vertexBuffer;
    }
    
    public FloatBuffer getTexCoordBuffer() {
        if (!finished) finish();
        return hasTexture ? texCoordBuffer : null;
    }
    
    public FloatBuffer getColorBuffer() {
        if (!finished) finish();
        return hasColor ? colorBuffer : null;
    }
    
    public int getVertexCount() {
        return vertices;
    }
    
    public boolean hasTexture() {
        return hasTexture;
    }
    
    public boolean hasColor() {
        return hasColor;
    }
}
