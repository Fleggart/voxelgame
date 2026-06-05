package com.voxelgame.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;

public class ShaderProgram {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
    
    // Uniform locations cache
    private int projectionMatrixLoc = -1;
    private int modelViewMatrixLoc = -1;
    private int textureLoc = -1;
    private int hasTextureLoc = -1;
    private int hasColorLoc = -1;
    
    public ShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
        vertexShaderId = loadShader(vertexShaderPath, GL20.GL_VERTEX_SHADER);
        fragmentShaderId = loadShader(fragmentShaderPath, GL20.GL_FRAGMENT_SHADER);
        
        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);
        
        // Bind attribute locations
        GL20.glBindAttribLocation(programId, 0, "in_position");
        GL20.glBindAttribLocation(programId, 1, "in_texCoord");
        GL20.glBindAttribLocation(programId, 2, "in_color");
        
        GL20.glLinkProgram(programId);
        
        int success = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
        if (success == 0) {
            String log = GL20.glGetProgramInfoLog(programId, 1024);
            throw new RuntimeException("Shader linking failed: " + log);
        }
        
        // Get uniform locations after linking
        projectionMatrixLoc = GL20.glGetUniformLocation(programId, "projection");
        modelViewMatrixLoc = GL20.glGetUniformLocation(programId, "modelview");
        textureLoc = GL20.glGetUniformLocation(programId, "texture");
        hasTextureLoc = GL20.glGetUniformLocation(programId, "hasTexture");
        hasColorLoc = GL20.glGetUniformLocation(programId, "hasColor");
    }
    
    private int loadShader(String path, int type) {
        StringBuilder shaderSource = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(path))
            );
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + path, e);
        }
        
        int shaderId = GL20.glCreateShader(type);
        GL20.glShaderSource(shaderId, shaderSource);
        GL20.glCompileShader(shaderId);
        
        int success = GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS);
        if (success == 0) {
            String log = GL20.glGetShaderInfoLog(shaderId, 1024);
            throw new RuntimeException("Shader compilation failed for " + path + ":\n" + log);
        }
        
        return shaderId;
    }
    
    public void use() {
        GL20.glUseProgram(programId);
    }
    
    public void stop() {
        GL20.glUseProgram(0);
    }
    
    public void setUniform(String name, int value) {
        int loc = GL20.glGetUniformLocation(programId, name);
        if (loc != -1) GL20.glUniform1i(loc, value);
    }
    
    public void setUniform(String name, float value) {
        int loc = GL20.glGetUniformLocation(programId, name);
        if (loc != -1) GL20.glUniform1f(loc, value);
    }
    
    public void setUniform(String name, Matrix4f matrix) {
        int loc = GL20.glGetUniformLocation(programId, name);
        if (loc != -1) {
            matrix.get(matrixBuffer);
            GL20.glUniformMatrix4(loc, false, matrixBuffer);
        }
    }
    
    // Optimized matrix upload with cached locations
    public void setProjectionMatrix(Matrix4f matrix) {
        if (projectionMatrixLoc != -1) {
            matrix.get(matrixBuffer);
            GL20.glUniformMatrix4(projectionMatrixLoc, false, matrixBuffer);
        }
    }
    
    public void setModelViewMatrix(Matrix4f matrix) {
        if (modelViewMatrixLoc != -1) {
            matrix.get(matrixBuffer);
            GL20.glUniformMatrix4(modelViewMatrixLoc, false, matrixBuffer);
        }
    }
    
    public void setHasTexture(boolean value) {
        if (hasTextureLoc != -1) {
            GL20.glUniform1i(hasTextureLoc, value ? 1 : 0);
        }
    }
    
    public void setHasColor(boolean value) {
        if (hasColorLoc != -1) {
            GL20.glUniform1i(hasColorLoc, value ? 1 : 0);
        }
    }
    
    public void bindTextureUnit(int unit) {
        if (textureLoc != -1) {
            GL20.glUniform1i(textureLoc, unit);
        }
    }
    
    public void cleanup() {
        if (programId != 0) {
            GL20.glUseProgram(0);
            GL20.glDetachShader(programId, vertexShaderId);
            GL20.glDetachShader(programId, fragmentShaderId);
            GL20.glDeleteShader(vertexShaderId);
            GL20.glDeleteShader(fragmentShaderId);
            GL20.glDeleteProgram(programId);
        }
    }
}
