package com.voxelgame.world;

import com.voxelgame.physics.BoundingBox;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class World {
    public final int width, height, depth;
    private final byte[] blocks;
    private final int[] lightDepths;
    private final List<WorldListener> listeners = new ArrayList<>();
    
    private final int yzSize;
    private final int zSize;

    public World(int w, int h, int d) {
        this.width = w;
        this.height = h;
        this.depth = d;
        this.yzSize = height * depth;
        this.zSize = depth;
        this.blocks = new byte[w * h * d];
        this.lightDepths = new int[w * h];
        
        // 修正：Y=0 是地面，Y增加向上
        int groundLevel = 0;  // 地面在 Y=0
        int stoneDepth = d / 3;  // 石头深度
        
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < h; z++) {
                for (int y = 0; y < d; y++) {
                    if (y == groundLevel) {
                        // 地面是草
                        setBlockFast(x, y, z, 0);  // 0 = 草
                    } else if (y < groundLevel) {
                        // 地面以下是石头（空气，不生成）
                        setBlockFast(x, y, z, 0);
                    } else if (y <= stoneDepth) {
                        // 地面以上一定深度是石头
                        setBlockFast(x, y, z, 1);  // 1 = 石头
                    } else {
                        // 更高处是空气
                        setBlockFast(x, y, z, -1);  // -1 = 空气
                    }
                }
            }
        }
        
        // 确保地面是实心的
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < h; z++) {
                setBlockFast(x, groundLevel, z, 0);  // 草地面
            }
        }
        
        calcLightDepths(0, 0, w, h);
        load();
    }
    
    private int index(int x, int y, int z) {
        return (y * height + z) * width + x;
    }
    
    private void setBlockFast(int x, int y, int z, int type) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) {
            return;
        }
        blocks[index(x, y, z)] = (byte)type;
    }
    
    public boolean isBlock(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) {
            return false;
        }
        byte block = blocks[index(x, y, z)];
        return block == 0 || block == 1;  // 草或石头都是固体
    }
    
    public boolean isSolidBlock(int x, int y, int z) {
        return isBlock(x, y, z);
    }
    
    public boolean isLightBlocker(int x, int y, int z) {
        return isBlock(x, y, z);
    }
    
    public void setBlock(int x, int y, int z, int type) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) {
            return;
        }
        
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
                
                // 从顶部向下找到第一个阻挡光线的方块
                int y = depth - 1;
                while (y > 0 && !isLightBlocker(x, y, z)) {
                    y--;
                }
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
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) {
            return 1.0f;
        }
        return y < lightDepths[x + z * width] ? 0.8f : 1.0f;
    }
    
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
        File file = new File("world.dat");
        if (!file.exists()) {
            System.out.println("No save file found, generating new world");
            return;
        }
        
        try (DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)))) {
            dis.readFully(this.blocks);
            this.calcLightDepths(0, 0, this.width, this.height);
            
            for (WorldListener l : listeners) {
                l.allChanged();
            }
            
            System.out.println("World loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load world: " + e.getMessage());
        }
    }
    
    public void save() {
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("world.dat"))))) {
            dos.write(this.blocks);
            System.out.println("World saved successfully");
        } catch (Exception e) {
            System.err.println("Failed to save world: " + e.getMessage());
        }
    }
    
    public void addListener(WorldListener l) {
        listeners.add(l);
    }
    
    public void removeListener(WorldListener l) {
        listeners.remove(l);
    }
}
