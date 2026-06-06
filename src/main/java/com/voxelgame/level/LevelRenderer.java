package com.voxelgame.level;

import com.voxelgame.HitResult;
import com.voxelgame.Player;
import com.voxelgame.phys.AABB;
import static org.lwjgl.opengl.GL11.*;

public class LevelRenderer implements LevelListener {
    private static final int CHUNK_SIZE = 16;
    private Level level;
    private Chunk[] chunks;
    private int xChunks, yChunks, zChunks;
    Tesselator t = new Tesselator();
    
    public LevelRenderer(Level level) {
        this.level = level;
        level.addListener(this);
        
        xChunks = level.width / CHUNK_SIZE;
        yChunks = level.depth / CHUNK_SIZE;
        zChunks = level.height / CHUNK_SIZE;
        chunks = new Chunk[xChunks * yChunks * zChunks];
        
        for (int x = 0; x < xChunks; x++) {
            for (int y = 0; y < yChunks; y++) {
                for (int z = 0; z < zChunks; z++) {
                    int x0 = x * CHUNK_SIZE;
                    int y0 = y * CHUNK_SIZE;
                    int z0 = z * CHUNK_SIZE;
                    int x1 = Math.min((x + 1) * CHUNK_SIZE, level.width);
                    int y1 = Math.min((y + 1) * CHUNK_SIZE, level.depth);
                    int z1 = Math.min((z + 1) * CHUNK_SIZE, level.height);
                    chunks[(x + y * xChunks) * zChunks + z] = new Chunk(level, x0, y0, z0, x1, y1, z1);
                }
            }
        }
    }
    
    public void render(Player player, int layer) {
        Chunk.rebuiltThisFrame = 0;
        Frustum frustum = Frustum.getFrustum();
        
        for (Chunk chunk : chunks) {
            if (frustum.cubeInFrustum(chunk.aabb)) {
                chunk.render(layer);
            }
        }
    }
    
    public void renderHit(HitResult h) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        glColor4f(1.0f, 1.0f, 1.0f, (float) Math.sin(System.currentTimeMillis() / 100.0) * 0.2f + 0.4f);
        t.init();
        Tile.rock.renderFace(t, h.x, h.y, h.z, h.f);
        t.flush();
        glDisable(GL_BLEND);
    }
    
    private void setDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
        x0 /= CHUNK_SIZE;
        x1 /= CHUNK_SIZE;
        y0 /= CHUNK_SIZE;
        y1 /= CHUNK_SIZE;
        z0 /= CHUNK_SIZE;
        z1 /= CHUNK_SIZE;
        
        x0 = Math.max(0, x0);
        y0 = Math.max(0, y0);
        z0 = Math.max(0, z0);
        x1 = Math.min(xChunks - 1, x1);
        y1 = Math.min(yChunks - 1, y1);
        z1 = Math.min(zChunks - 1, z1);
        
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    chunks[(x + y * xChunks) * zChunks + z].setDirty();
                }
            }
        }
    }
    
    @Override
    public void tileChanged(int x, int y, int z) {
        level.chunkUpdates++;
        setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }
    
    @Override
    public void lightColumnChanged(int x, int z, int y0, int y1) {
        setDirty(x - 1, y0 - 1, z - 1, x + 1, y1 + 1, z + 1);
    }
    
    @Override
    public void allChanged() {
        setDirty(0, 0, 0, level.width, level.depth, level.height);
    }
}
