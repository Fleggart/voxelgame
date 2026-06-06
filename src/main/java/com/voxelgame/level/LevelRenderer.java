package com.voxelgame.level;

import com.voxelgame.HitResult;
import com.voxelgame.player.Player;
import com.voxelgame.physics.AABB;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class LevelRenderer implements LevelListener {
    private static final int CHUNK_SIZE = 16;
    
    private final Level level;
    private final Chunk[] chunks;
    private final int xChunks, yChunks, zChunks;
    private final Tesselator t = new Tesselator();

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
        Frustum frustum = Frustum.getInstance();
        
        for (Chunk chunk : chunks) {
            if (frustum.cubeInFrustum(chunk.aabb)) {
                chunk.render(layer);
            }
        }
    }

    public void pick(Player player) {
        float r = 3.0F;
        AABB box = player.bb.grow(r, r, r);
        
        int x0 = (int) box.x0;
        int x1 = (int) (box.x1 + 1.0F);
        int y0 = (int) box.y0;
        int y1 = (int) (box.y1 + 1.0F);
        int z0 = (int) box.z0;
        int z1 = (int) (box.z1 + 1.0F);
        
        glInitNames();
        
        for (int x = x0; x < x1; x++) {
            glPushName(x);
            for (int y = y0; y < y1; y++) {
                glPushName(y);
                for (int z = z0; z < z1; z++) {
                    glPushName(z);
                    if (level.isSolidTile(x, y, z)) {
                        glPushName(0);
                        for (int i = 0; i < 6; i++) {
                            glPushName(i);
                            t.init();
                            Tile.ROCK.renderFace(t, x, y, z, i);
                            t.flush();
                            glPopName();
                        }
                        glPopName();
                    }
                    glPopName();
                }
                glPopName();
            }
            glPopName();
        }
    }

    public void renderHit(HitResult hit) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE);
        float alpha = (float) (Math.sin(System.currentTimeMillis() / 100.0) * 0.2F + 0.4F);
        glColor4f(1.0F, 1.0F, 1.0F, alpha);
        
        t.init();
        Tile.ROCK.renderFace(t, hit.x, hit.y, hit.z, hit.face);
        t.flush();
        
        glDisable(GL_BLEND);
    }

    private void setDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
        x0 = Math.max(x0 / CHUNK_SIZE, 0);
        x1 = Math.min(x1 / CHUNK_SIZE, xChunks - 1);
        y0 = Math.max(y0 / CHUNK_SIZE, 0);
        y1 = Math.min(y1 / CHUNK_SIZE, yChunks - 1);
        z0 = Math.max(z0 / CHUNK_SIZE, 0);
        z1 = Math.min(z1 / CHUNK_SIZE, zChunks - 1);
        
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
