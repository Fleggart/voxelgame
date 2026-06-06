package com.voxelgame.level;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Textures {
    private static final HashMap<String, Integer> idMap = new HashMap<>();
    private static int lastId = -9999999;

    public static int loadTexture(String resourceName, int mode) {
        try {
            if (idMap.containsKey(resourceName)) {
                return idMap.get(resourceName);
            }

            // 从资源加载图片
            InputStream is = Textures.class.getResourceAsStream(resourceName);
            if (is == null) {
                throw new RuntimeException("Texture not found: " + resourceName);
            }
            
            byte[] imageBytes = is.readAllBytes();
            ByteBuffer imageBuffer = BufferUtils.createByteBuffer(imageBytes.length);
            imageBuffer.put(imageBytes);
            imageBuffer.flip();
            
            IntBuffer w = BufferUtils.createIntBuffer(1);
            IntBuffer h = BufferUtils.createIntBuffer(1);
            IntBuffer comp = BufferUtils.createIntBuffer(1);
            
            ByteBuffer pixels = STBImage.stbi_load_from_memory(imageBuffer, w, h, comp, 4);
            if (pixels == null) {
                throw new RuntimeException("Failed to load texture: " + resourceName);
            }
            
            int width = w.get(0);
            int height = h.get(0);
            
            IntBuffer idBuffer = BufferUtils.createIntBuffer(1);
            glGenTextures(idBuffer);
            int id = idBuffer.get(0);
            
            glBindTexture(GL_TEXTURE_2D, id);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, mode);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, mode);
            
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            
            STBImage.stbi_image_free(pixels);
            
            idMap.put(resourceName, id);
            return id;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + resourceName, e);
        }
    }

    public static void bind(int id) {
        if (id != lastId) {
            glBindTexture(GL_TEXTURE_2D, id);
            lastId = id;
        }
    }
}
