package com.voxelgame.level;

import com.voxelgame.phys.AABB;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Level {
    public final int width;
    public final int height;
    public final int depth;
    private byte[] blocks;
    private int[] lightDepths;
    private ArrayList<LevelListener> levelListeners = new ArrayList<>();
    public int chunkUpdates = 0;
    
    public Level(int w, int h, int d) {
        this.width = w;
        this.height = h;
        this.depth = d;
        this.blocks = new byte[w * h * d];
        this.lightDepths = new int[w * h];
        
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < d; y++) {
                for (int z = 0; z < h; z++) {
                    int i = (y * height + z) * width + x;
                    blocks[i] = (byte) (y <= d * 2 / 3 ? 1 : 0);
                }
            }
        }
        
        calcLightDepths(0, 0, w, h);
        load();
    }
    
    public void load() {
        File file = new File("level.dat");
        if (!file.exists()) return;
        
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            dis.readFully(blocks);
            calcLightDepths(0, 0, width, height);
            for (LevelListener listener : levelListeners) {
                listener.allChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void save() {
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream("level.dat")))) {
            dos.write(blocks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void calcLightDepths(int x0, int z0, int x1, int z1) {
        for (int x = x0; x < x0 + x1; x++) {
            for (int z = z0; z < z0 + z1; z++) {
                int oldDepth = lightDepths[x + z * width];
                int y;
                for (y = depth - 1; y > 0 && !isLightBlocker(x, y, z); y--);
                lightDepths[x + z * width] = y;
                
                if (oldDepth != y) {
                    int yl0 = oldDepth < y ? oldDepth : y;
                    int yl1 = oldDepth > y ? oldDepth : y;
                    for (LevelListener listener : levelListeners) {
                        listener.lightColumnChanged(x, z, yl0, yl1);
                    }
                }
            }
        }
    }
    
    public void addListener(LevelListener listener) {
        levelListeners.add(listener);
    }
    
    public void removeListener(LevelListener listener) {
        levelListeners.remove(listener);
    }
    
    public boolean isTile(int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < width && y < depth && z < height) {
            return blocks[(y * height + z) * width + x] == 1;
        }
        return false;
    }
    
    public boolean isSolidTile(int x, int y, int z) {
        return isTile(x, y, z);
    }
    
    public boolean isLightBlocker(int x, int y, int z) {
        return isSolidTile(x, y, z);
    }
    
    public ArrayList<AABB> getCubes(AABB aabb) {
        ArrayList<AABB> aABBs = new ArrayList<>();
        int x0 = (int) aabb.x0;
        int x1 = (int) (aabb.x1 + 1.0f);
        int y0 = (int) aabb.y0;
        int y1 = (int) (aabb.y1 + 1.0f);
        int z0 = (int) aabb.z0;
        int z1 = (int) (aabb.z1 + 1.0f);
        
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (z0 < 0) z0 = 0;
        if (x1 > width) x1 = width;
        if (y1 > depth) y1 = depth;
        if (z1 > height) z1 = height;
        
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (isSolidTile(x, y, z)) {
                        aABBs.add(new AABB(x, y, z, x + 1, y + 1, z + 1));
                    }
                }
            }
        }
        return aABBs;
    }
    
    public float getBrightness(int x, int y, int z) {
        float dark = 0.8f;
        float light = 1.0f;
        if (x >= 0 && y >= 0 && z >= 0 && x < width && y < depth && z < height) {
            return y < lightDepths[x + z * width] ? dark : light;
        }
        return light;
    }
    
    public void setTile(int x, int y, int z, int type) {
        if (x >= 0 && y >= 0 && z >= 0 && x < width && y < depth && z < height) {
            blocks[(y * height + z) * width + x] = (byte) type;
            calcLightDepths(x, z, 1, 1);
            for (LevelListener listener : levelListeners) {
                listener.tileChanged(x, y, z);
            }
        }
    }
    
    public boolean isTile(int x, int y, int z, int type) {
        if (x >= 0 && y >= 0 && z >= 0 && x < width && y < depth && z < height) {
            return blocks[(y * height + z) * width + x] == type;
        }
        return false;
    }
}
