package com.voxelgame.level;

import com.voxelgame.Textures;
import com.voxelgame.phys.AABB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.*;

public class Chunk {
    public AABB aabb;
    public final Level level;
    public final int x0, y0, z0, x1, y1, z1;
    private boolean dirty = true;
    private int lists = -1;
    private static int texture = Textures.loadTexture("/terrain.png", GL_NEAREST_MIPMAP_LINEAR);
    private static Tesselator t = new Tesselator();
    public static int rebuiltThisFrame = 0;
    public static int updates = 0;
    
    public Chunk(Level level, int x0, int y0, int z0, int x1, int y1, int z1) {
        this.level = level;
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.aabb = new AABB(x0, y0, z0, x1, y1, z1);
        this.lists = glGenLists(2);
    }
    
    private void rebuild(int layer) {
        if (rebuiltThisFrame >= 2) return;
        
        dirty = false;
        updates++;
        rebuiltThisFrame++;
        
        glNewList(lists + layer, GL_COMPILE);
        glEnable(GL_TEXTURE_2D);
        Textures.bind(texture);
        t.init();
        
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (level.isTile(x, y, z)) {
                        int tex = y == level.depth * 2 / 3 ? 0 : 1;
                        if (tex == 0) {
                            Tile.rock.render(t, level, layer, x, y, z);
                        } else {
                            Tile.grass.render(t, level, layer, x, y, z);
                        }
                    }
                }
            }
        }
        
        t.flush();
        glDisable(GL_TEXTURE_2D);
        glEndList();
    }
    
    public void render(int layer) {
        if (dirty) {
            rebuild(0);
            rebuild(1);
        }
        glCallList(lists + layer);
    }
    
    public void setDirty() {
        dirty = true;
    }
}
