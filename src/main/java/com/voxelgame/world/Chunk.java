package com.voxelgame.world;

import com.voxelgame.TextureManager;
import com.voxelgame.physics.BoundingBox;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

public class Chunk {
    private static final int TEXTURE = TextureManager.loadTexture("/textures/terrain.png", 9728);
    private static final MeshBuilder meshBuilder = new MeshBuilder();
    
    private final World world;
    private final int x0, y0, z0, x1, y1, z1;
    private boolean dirty = true;
    private int[] layerVBOs = new int[2];
    private int[] layerTexVBOs = new int[2];
    private int[] layerColorVBOs = new int[2];
    private int[] layerVertexCounts = new int[2];
    private static int rebuiltThisFrame = 0;
    public static int updates = 0;

    public Chunk(World world, int x0, int y0, int z0, int x1, int y1, int z1) {
        this.world = world;
        this.x0 = x0; this.y0 = y0; this.z0 = z0;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        
        // 为每个层创建VBO
        for (int i = 0; i < 2; i++) {
            layerVBOs[i] = GL15.glGenBuffers();
            layerTexVBOs[i] = GL15.glGenBuffers();
            layerColorVBOs[i] = GL15.glGenBuffers();
        }
    }

    private void rebuildLayer(int layer) {
        meshBuilder.init();
        
        int grassLevel = world.depth * 2 / 3;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (world.isBlock(x, y, z)) {
                        Block block = (y == grassLevel || y > grassLevel + 5) ? Block.GRASS : Block.ROCK;
                        block.render(meshBuilder, world, layer, x, y, z);
                    }
                }
            }
        }
        
        // 完成meshBuilder并获取数据
        meshBuilder.finish();
        
        // 获取顶点、纹理坐标、颜色数据
        FloatBuffer vertexData = meshBuilder.getVertexBuffer();
        FloatBuffer texData = meshBuilder.getTexCoordBuffer();
        FloatBuffer colorData = meshBuilder.getColorBuffer();
        
        int vertexCount = meshBuilder.getVertexCount();
        layerVertexCounts[layer] = vertexCount;
        
        if (vertexCount > 0) {
            // 上传顶点数据到VBO
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, layerVBOs[layer]);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
            
            // 上传纹理坐标数据
            if (texData != null && texData.remaining() > 0) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, layerTexVBOs[layer]);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texData, GL15.GL_STATIC_DRAW);
            }
            
            // 上传颜色数据
            if (colorData != null && colorData.remaining() > 0) {
                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, layerColorVBOs[layer]);
                GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorData, GL15.GL_STATIC_DRAW);
            }
        }
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private void rebuild() {
        if (rebuiltThisFrame == 2) return;
        
        dirty = false;
        updates++;
        rebuiltThisFrame++;
        
        rebuildLayer(0);
        rebuildLayer(1);
    }

    public void render(int layer) {
        if (dirty) rebuild();
        
        if (layerVertexCounts[layer] == 0) return;
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        TextureManager.bind(TEXTURE);
        
        // 绑定顶点数据
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, layerVBOs[layer]);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        
        // 绑定纹理坐标
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, layerTexVBOs[layer]);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, 0);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        
        // 绑定颜色数据
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, layerColorVBOs[layer]);
        GL11.glColorPointer(3, GL11.GL_FLOAT, 0, 0);
        GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        
        // 绘制
        GL11.glDrawArrays(GL11.GL_QUADS, 0, layerVertexCounts[layer]);
        
        // 清理状态
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
    }

    public void setDirty() { 
        dirty = true; 
    }
    
    public BoundingBox getBoundingBox() {
        return new BoundingBox(x0, y0, z0, x1, y1, z1);
    }
    
    public static void resetRebuiltThisFrame() {
        rebuiltThisFrame = 0;
    }
    
    public void cleanup() {
        for (int i = 0; i < 2; i++) {
            GL15.glDeleteBuffers(layerVBOs[i]);
            GL15.glDeleteBuffers(layerTexVBOs[i]);
            GL15.glDeleteBuffers(layerColorVBOs[i]);
        }
    }
}
