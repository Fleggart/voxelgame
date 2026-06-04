package com.voxelgame.graphics;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;

public class ShaderProgram {
    private int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    
    public ShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
        vertexShaderId = loadShader(vertexShaderPath, GL20.GL_VERTEX_SHADER);
        fragmentShaderId = loadShader(fragmentShaderPath, GL20.GL_FRAGMENT_SHADER);
        
        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShaderId);
        GL20.glAttachShader(programId, fragmentShaderId);
        
        // 绑定属性位置
        GL20.glBindAttribLocation(programId, 0, "in_position");
        GL20.glBindAttribLocation(programId, 1, "in_texCoord");
        GL20.glBindAttribLocation(programId, 2, "in_color");
        
        GL20.glLinkProgram(programId);
        
        // 检查链接错误
        IntBuffer success = BufferUtils.createIntBuffer(1);
        GL20.glGetProgram(programId, GL20.GL_LINK_STATUS, success);
        if (success.get(0) == 0) {
            String log = GL20.glGetProgramInfoLog(programId, 1024);
            throw new RuntimeException("Shader linking failed: " + log);
        }
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
        
        // 检查编译错误
        IntBuffer success = BufferUtils.createIntBuffer(1);
        GL20.glGetShader(shaderId, GL20.GL_COMPILE_STATUS, success);
        if (success.get(0) == 0) {
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
    
    public void setUniform(String name, float value) {
        int loc = GL20.glGetUniformLocation(programId, name);
        if (loc != -1) GL20.glUniform1f(loc, value);
    }
    
    public void setUniform(String name, float x, float y, float z) {
        int loc = GL20.glGetUniformLocation(programId, name);
        if (loc != -1) GL20.glUniform3f(loc, x, y, z);
    }
    
    public void setUniform(String name, float x, float y, float z, float w) {
        int loc = GL20.glGetUniformLocation(programId, name);
        if (loc != -1) GL20.glUniform4f(loc, x, y, z, w);
    }
    
    public void setUniform(String name, int value) {
        int loc = GL20.glGetUniformLocation(programId, name);
        if (loc != -1) GL20.glUniform1i(loc, value);
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
