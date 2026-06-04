package com.voxelgame.world;

import com.voxelgame.physics.BoundingBox;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class World {
    public final int width, height, depth;
    private final byte[] blocks;
    private final int[] lightDepths;
    private final List<WorldListener> listeners = new ArrayList<>();
    
    // 预计算索引偏移量
    private final int yzSize;
    private final int zSize;

    public World(int w, int h, int d) {
        this.width = w;
        this.height = h;
        this.depth = d;
        this.yzSize = height * depth;
        this.zSize = height;
        this.blocks = new byte[w * h * d];
        this.lightDepths = new int[w * h];
        
        // 初始化地形
        int grassLevel = d * 2 / 3;
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < h; z++) {
                for (int y = 0; y < d; y++) {
                    setBlockFast(x, y, z, y <= grassLevel ? 1 : 0);
                }
            }
        }
        
        calcLightDepths(0, 0, w, h);
        load();
    }
    
    // 快速索引计算
    private int index(int x, int y, int z) {
        return (y * zSize + z) * width + x;
    }
    
    private void setBlockFast(int x, int y, int z, int type) {
        blocks[index(x, y, z)] = (byte)type;
    }
    
    public boolean isBlock(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) return false;
        return blocks[index(x, y, z)] == 1;
    }
    
    public void setBlock(int x, int y, int z, int type) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) return;
        setBlockFast(x, y, z, type);
        calcLightDepths(x, z, 1, 1);
        for (WorldListener l : listeners) {
            l.blockChanged(x, y, z);
        }
    }
    
    public void calcLightDepths(int x0, int z0, int w, int h) {
        for (int x = x0; x < x0 + w; x++) {
            for (int z = z0; z < z0 + h; z++) {
                int idx = x + z * width;
                int oldDepth = lightDepths[idx];
                int y;
                for (y = depth - 1; y > 0 && !isLightBlocker(x, y, z); y--);
                lightDepths[idx] = y;
                
                if (oldDepth != y) {
                    int yMin = Math.min(oldDepth, y);
                    int yMax = Math.max(oldDepth, y);
                    for (WorldListener l : listeners) {
                        l.lightColumnChanged(x, z, yMin, yMax);
                    }
                }
            }
        }
    }
    
    public float getBrightness(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) return 1.0f;
        return y < lightDepths[x + z * width] ? 0.8f : 1.0f;
    }
    
    public boolean isLightBlocker(int x, int y, int z) { 
        return isBlock(x, y, z); 
    }
    
    public boolean isSolidBlock(int x, int y, int z) { 
        return isBlock(x, y, z); 
    }
    
    // 简化碰撞盒获取
    public List<BoundingBox> getCubes(BoundingBox aabb) {
        List<BoundingBox> boxes = new ArrayList<>();
        int x0 = Math.max(0, (int)aabb.x0);
        int x1 = Math.min(width, (int)(aabb.x1 + 1));
        int y0 = Math.max(0, (int)aabb.y0);
        int y1 = Math.min(depth, (int)(aabb.y1 + 1));
        int z0 = Math.max(0, (int)aabb.z0);
        int z1 = Math.min(height, (int)(aabb.z1 + 1));
        
        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (isSolidBlock(x, y, z)) {
                        boxes.add(new BoundingBox(x, y, z, x + 1, y + 1, z + 1));
                    }
                }
            }
        }
        return boxes;
    }
    
    public void load() {
        try {
            DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(new File("world.dat"))));
            dis.readFully(this.blocks);
            this.calcLightDepths(0, 0, this.width, this.height);
            for (WorldListener l : listeners) {
                l.allChanged();
            }
            dis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void save() {
        try {
            DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("world.dat"))));
            dos.write(this.blocks);
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void addListener(WorldListener l) { 
        listeners.add(l); 
    }
    
    public void removeListener(WorldListener l) { 
        listeners.remove(l); 
    }
}
