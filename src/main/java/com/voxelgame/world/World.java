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
    
    private final int yzSize;  // height * depth
    private final int zSize;   // depth

    public World(int w, int h, int d) {
        this.width = w;
        this.height = h;
        this.depth = d;
        this.yzSize = height * depth;
        this.zSize = depth;
        this.blocks = new byte[w * h * d];
        this.lightDepths = new int[w * h];
        
        int grassLevel = d * 2 / 3;  // 草地层级，2/3高度以下为石头
        
        // 生成地形：Y轴向下为深度
        for (int x = 0; x < w; x++) {
            for (int z = 0; z < h; z++) {
                for (int y = 0; y < d; y++) {
                    // y <= grassLevel 为石头(1)，y > grassLevel 为草(0)
                    setBlockFast(x, y, z, y <= grassLevel ? 1 : 0);
                }
            }
        }
        
        calcLightDepths(0, 0, w, h);
        load();
    }
    
    /**
     * 将3D坐标转换为一维数组索引
     * 存储顺序: Y -> Z -> X (Y轴变化最快)
     */
    private int index(int x, int y, int z) {
        return (y * height + z) * width + x;
    }
    
    private void setBlockFast(int x, int y, int z, int type) {
        blocks[index(x, y, z)] = (byte)type;
    }
    
    /**
     * 检查指定位置是否有方块
     */
    public boolean isBlock(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) {
            return false;
        }
        return blocks[index(x, y, z)] == 1;  // 1表示石头，0表示草
    }
    
    /**
     * 检查是否为固体方块（用于碰撞检测）
     */
    public boolean isSolidBlock(int x, int y, int z) {
        return isBlock(x, y, z);
    }
    
    /**
     * 检查是否为阻挡光线的方块
     */
    public boolean isLightBlocker(int x, int y, int z) {
        return isBlock(x, y, z);
    }
    
    /**
     * 设置指定位置的方块
     * @param type 0=草, 1=石头, 其他=空气
     */
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
    
    /**
     * 计算指定列的光照深度
     */
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
    
    /**
     * 获取指定位置的亮度
     * @return 0.8f（阴影）或 1.0f（光照）
     */
    public float getBrightness(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= width || y >= depth || z >= height) {
            return 1.0f;
        }
        return y < lightDepths[x + z * width] ? 0.8f : 1.0f;
    }
    
    /**
     * 获取与AABB碰撞的所有方块
     */
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
    
    /**
     * 从文件加载世界
     */
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
    
    /**
     * 保存世界到文件
     */
    public void save() {
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("world.dat"))))) {
            dos.write(this.blocks);
            System.out.println("World saved successfully");
        } catch (Exception e) {
            System.err.println("Failed to save world: " + e.getMessage());
        }
    }
    
    /**
     * 添加世界监听器（用于区块更新）
     */
    public void addListener(WorldListener l) {
        listeners.add(l);
    }
    
    /**
     * 移除世界监听器
     */
    public void removeListener(WorldListener l) {
        listeners.remove(l);
    }
}
