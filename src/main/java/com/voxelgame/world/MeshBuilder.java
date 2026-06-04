package com.voxelgame.world;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class MeshBuilder {
    private static final int MAX_VERTICES = 100000;
    private static final int FLOATS_PER_VERTEX = 3; // position
    private static final int FLOATS_PER_TEX = 2;    // texCoord
    private static final int FLOATS_PER_COLOR = 3;  // color
    
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    private FloatBuffer colorBuffer;
    private int vertices = 0;
    
    private int vboId;
    private int texVboId;
    private int colorVboId;
    
    private boolean hasColor = false;
    private boolean hasTexture = false;
    private float u, v, r, g, b;
    
    public MeshBuilder() {
        vertexBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * FLOATS_PER_VERTEX);
        texCoordBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * FLOATS_PER_TEX);
        colorBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * FLOATS_PER_COLOR);
        
        // 创建VBOs
        vboId = GL15.glGenBuffers();
        texVboId = GL15.glGenBuffers();
        colorVboId = GL15.glGenBuffers();
    }
    
    public void init() {
        clear();
        hasColor = false;
        hasTexture = false;
    }
    
    private void clear() {
        vertices = 0;
        vertexBuffer.clear();
        texCoordBuffer.clear();
        colorBuffer.clear();
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
        
        if (++vertices == MAX_VERTICES) {
            flush();
        }
    }
    
    public void flush() {
        if (vertices == 0) return;
        
        vertexBuffer.flip();
        texCoordBuffer.flip();
        colorBuffer.flip();
        
        // 上传顶点数据到VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STREAM_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        GL20.glEnableVertexAttribArray(0);
        
        if (hasTexture && texCoordBuffer.remaining() > 0) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texVboId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordBuffer, GL15.GL_STREAM_DRAW);
            GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
            GL20.glEnableVertexAttribArray(1);
        }
        
        if (hasColor && colorBuffer.remaining() > 0) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, colorVboId);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_STREAM_DRAW);
            GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
            GL20.glEnableVertexAttribArray(2);
        }
        
        // 绘制
        GL11.glDrawArrays(GL11.GL_QUADS, 0, vertices);
        
        // 清理状态
        GL20.glDisableVertexAttribArray(0);
        if (hasTexture) GL20.glDisableVertexAttribArray(1);
        if (hasColor) GL20.glDisableVertexAttribArray(2);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        
        clear();
    }
    
    public void cleanup() {
        GL15.glDeleteBuffers(vboId);
        GL15.glDeleteBuffers(texVboId);
        GL15.glDeleteBuffers(colorVboId);
    }
}
