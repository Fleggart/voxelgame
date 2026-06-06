package com.voxelgame.level;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Tesselator {
    private static final int MAX_VERTICES = 100000;
    
    // 使用 Direct Buffer (ByteBuffer 为基础，确保 Native 代码可直接访问)
    private final FloatBuffer vertexBuffer;
    private final FloatBuffer texCoordBuffer;
    private final FloatBuffer colorBuffer;
    
    private int vertices = 0;
    private float u, v;
    private float r, g, b;
    private boolean hasColor = false;
    private boolean hasTexture = false;

    public Tesselator() {
        // 分配 Direct Buffer（ByteBuffer 包装，Native 代码可直接访问）
        ByteBuffer vbb = BufferUtils.createByteBuffer(MAX_VERTICES * 3 * 4); // 3 floats * 4 bytes
        ByteBuffer tbb = BufferUtils.createByteBuffer(MAX_VERTICES * 2 * 4); // 2 floats * 4 bytes
        ByteBuffer cbb = BufferUtils.createByteBuffer(MAX_VERTICES * 3 * 4); // 3 floats * 4 bytes
        
        vertexBuffer = vbb.asFloatBuffer();
        texCoordBuffer = tbb.asFloatBuffer();
        colorBuffer = cbb.asFloatBuffer();
    }

    public void flush() {
        if (vertices == 0) return;
        
        vertexBuffer.flip();
        texCoordBuffer.flip();
        colorBuffer.flip();
        
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(3, GL_FLOAT, 0, vertexBuffer);
        
        if (hasTexture) {
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glTexCoordPointer(2, GL_FLOAT, 0, texCoordBuffer);
        }
        
        if (hasColor) {
            glEnableClientState(GL_COLOR_ARRAY);
            glColorPointer(3, GL_FLOAT, 0, colorBuffer);
        }
        
        // 使用 GL_TRIANGLES 而不是 GL_QUADS (更好的兼容性)
        glDrawArrays(GL_TRIANGLES, 0, vertices);
        
        glDisableClientState(GL_VERTEX_ARRAY);
        if (hasTexture) glDisableClientState(GL_TEXTURE_COORD_ARRAY);
        if (hasColor) glDisableClientState(GL_COLOR_ARRAY);
        
        clear();
    }

    private void clear() {
        vertices = 0;
        vertexBuffer.clear();
        texCoordBuffer.clear();
        colorBuffer.clear();
        hasColor = false;
        hasTexture = false;
    }

    public void init() {
        clear();
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
        int idx = vertices;
        
        vertexBuffer.put(idx * 3, x);
        vertexBuffer.put(idx * 3 + 1, y);
        vertexBuffer.put(idx * 3 + 2, z);
        
        if (hasTexture) {
            texCoordBuffer.put(idx * 2, u);
            texCoordBuffer.put(idx * 2 + 1, v);
        }
        
        if (hasColor) {
            colorBuffer.put(idx * 3, r);
            colorBuffer.put(idx * 3 + 1, g);
            colorBuffer.put(idx * 3 + 2, b);
        }
        
        vertices++;
        
        if (vertices >= MAX_VERTICES) {
            flush();
        }
    }
    
    public void addVertexWithUV(float x, float y, float z, float u, float v) {
        tex(u, v);
        vertex(x, y, z);
    }
    
    public void addVertexWithColor(float x, float y, float z, float r, float g, float b) {
        color(r, g, b);
        vertex(x, y, z);
    }
    
    public void addQuad(float x0, float y0, float z0, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3) {
        // 三角形1
        vertex(x0, y0, z0);
        vertex(x1, y1, z1);
        vertex(x2, y2, z2);
        // 三角形2
        vertex(x0, y0, z0);
        vertex(x2, y2, z2);
        vertex(x3, y3, z3);
    }
}
