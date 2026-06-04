package com.voxelgame.world;

import com.voxelgame.TextureManager;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class Chunk {
    private static final int TEXTURE = TextureManager.loadTexture("/textures/terrain.png", 9728);
    private static final MeshBuilder meshBuilder = new MeshBuilder();
    
    private final World world;
    private final int x0, y0, z0, x1, y1, z1;
    private boolean dirty = true;
    private int vboId;
    private int layerVBOs[] = new int[2];
    private int layerVertexCounts[] = new int[2];
    private static int rebuiltThisFrame = 0;
    public static int updates = 0;

    public Chunk(World world, int x0, int y0, int z0, int x1, int y1, int z1) {
        this.world = world;
        this.x0 = x0; this.y0 = y0; this.z0 = z0;
        this.x1 = x1; this.y1 = y1; this.z1 = z1;
        
        // 为每个层创建VBO
        for (int i = 0; i < 2; i++) {
            layerVBOs[i] = GL15.glGenBuffers();
        }
    }

    private void rebuildLayer(int layer) {
        meshBuilder.init();
        
        int grassLevel = world.depth * 2 / 3;
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (world.isBlock(x, y, z)) {
                        Block block = y == grassLevel ? Block.GRASS : Block.ROCK;
                        block.render(meshBuilder, world, layer, x, y, z);
                    }
                }
            }
        }
        
        // 创建临时缓冲区来存储顶点数据
        java.nio.FloatBuffer vertexData = java.nio.BufferUtils.createFloatBuffer(100000);
        // 注意：这里需要将meshBuilder的数据转换到VBO格式
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, layerVBOs[layer]);
        // GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexData, GL15.GL_STATIC_DRAW);
        layerVertexCounts[layer] = 0; // 设置实际顶点数
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
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        TextureManager.bind(TEXTURE);
        
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, layerVBOs[layer]);
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, 0);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        
        if (layerVertexCounts[layer] > 0) {
            GL11.glDrawArrays(GL11.GL_QUADS, 0, layerVertexCounts[layer]);
        }
        
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
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
        for (int vbo : layerVBOs) {
            GL15.glDeleteBuffers(vbo);
        }
    }
}
