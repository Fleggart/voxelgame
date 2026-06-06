package com.voxelgame;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Textures {
    private static HashMap<String, Integer> idMap = new HashMap<>();
    private static int lastId = -1;
    
    public static int loadTexture(String resourceName, int mode) {
        if (idMap.containsKey(resourceName)) {
            return idMap.get(resourceName);
        }
        
        try {
            // 从资源加载图片
            ByteBuffer imageBuffer;
            try (InputStream is = Textures.class.getResourceAsStream(resourceName)) {
                if (is == null) {
                    throw new RuntimeException("Texture not found: " + resourceName);
                }
                byte[] bytes = is.readAllBytes();
                imageBuffer = BufferUtils.createByteBuffer(bytes.length);
                imageBuffer.put(bytes);
                imageBuffer.flip();
            }
            
            IntBuffer width = BufferUtils.createIntBuffer(1);
            IntBuffer height = BufferUtils.createIntBuffer(1);
            IntBuffer channels = BufferUtils.createIntBuffer(1);
            
            ByteBuffer pixels = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
            if (pixels == null) {
                throw new RuntimeException("Failed to load texture: " + STBImage.stbi_failure_reason());
            }
            
            int id = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, id);
            
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mode);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mode);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            glGenerateMipmap(GL_TEXTURE_2D);
            
            STBImage.stbi_image_free(pixels);
            
            idMap.put(resourceName, id);
            return id;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture", e);
        }
    }
    
    public static void bind(int id) {
        if (id != lastId) {
            glBindTexture(GL_TEXTURE_2D, id);
            lastId = id;
        }
    }
}
