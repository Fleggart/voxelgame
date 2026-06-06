package com.voxelgame.level;

import org.lwjgl.BufferUtils;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Tesselator {
    private static final int MAX_VERTICES = 100000;
    private FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 3);
    private FloatBuffer texCoordBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 2);
    private FloatBuffer colorBuffer = BufferUtils.createFloatBuffer(MAX_VERTICES * 3);
    private int vertices = 0;
    private float u, v;
    private float r, g, b;
    private boolean hasColor = false;
    private boolean hasTexture = false;
    
    public void flush() {
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
        
        glDrawArrays(GL_QUADS, 0, vertices);
        
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
        vertexBuffer.put(vertices * 3, x);
        vertexBuffer.put(vertices * 3 + 1, y);
        vertexBuffer.put(vertices * 3 + 2, z);
        
        if (hasTexture) {
            texCoordBuffer.put(vertices * 2, u);
            texCoordBuffer.put(vertices * 2 + 1, v);
        }
        
        if (hasColor) {
            colorBuffer.put(vertices * 3, r);
            colorBuffer.put(vertices * 3 + 1, g);
            colorBuffer.put(vertices * 3 + 2, b);
        }
        
        vertices++;
        
        if (vertices >= MAX_VERTICES - 4) {
            flush();
        }
    }
}
