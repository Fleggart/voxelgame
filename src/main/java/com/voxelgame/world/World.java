package com.voxelgame.world;

import com.voxelgame.physics.BoundingBox;
import java.util.ArrayList;
import java.util.List;

public class World {

    public final int width, height, depth;
    private final byte[] blocks;
    private final List<WorldListener> listeners = new ArrayList<>();
    
    // 光照亮度数组 (简化版)
    private final byte[] light;

    // 内部接口
    public interface WorldListener {
        void blockChanged(int x, int y, int z);
        void lightColumnChanged(int x, int z, int y0, int y1);
        void allChanged();
    }

    public World(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.blocks = new byte[width * height * depth];
        this.light = new byte[width * depth * height];

        generateFlatWorld();
    }

    private int index(int x, int y, int z) {
        return x + z * width + y * width * depth;
    }

    private int lightIndex(int x, int y, int z) {
        return x + z * width + y * width * depth;
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < width
            && y >= 0 && y < height
            && z >= 0 && z < depth;
    }

    public boolean isBlock(int x, int y, int z) {
        if (!inBounds(x, y, z)) return false;
        return blocks[index(x, y, z)] != 0;
    }

    public boolean isSolidBlock(int x, int y, int z) {
        return isBlock(x, y, z);
    }

    public void setBlock(int x, int y, int z, int type) {
        if (!inBounds(x, y, z)) return;
        blocks[index(x, y, z)] = (byte) type;
        
        // 通知监听器
        for (WorldListener l : listeners) {
            l.blockChanged(x, y, z);
        }
    }

    public float getBrightness(int x, int y, int z) {
        if (!inBounds(x, y, z)) return 1.0f;
        // 简单的亮度计算：根据高度
        float brightness = 0.5f + (float) y / height * 0.5f;
        return Math.min(1.0f, Math.max(0.3f, brightness));
    }

    private void setBlockFast(int x, int y, int z, int v) {
        if (inBounds(x, y, z)) {
            blocks[index(x, y, z)] = (byte) v;
        }
    }

    private void generateFlatWorld() {
        int ground = height / 3;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < ground; y++) {
                    setBlockFast(x, y, z, 1);
                }
            }
        }
    }

    public List<BoundingBox> getCubes(BoundingBox bb) {
        List<BoundingBox> list = new ArrayList<>();

        int x0 = (int) Math.floor(bb.x0);
        int x1 = (int) Math.floor(bb.x1 + 0.0001);
        int y0 = (int) Math.floor(bb.y0);
        int y1 = (int) Math.floor(bb.y1 + 0.0001);
        int z0 = (int) Math.floor(bb.z0);
        int z1 = (int) Math.floor(bb.z1 + 0.0001);

        x0 = Math.max(0, x0);
        y0 = Math.max(0, y0);
        z0 = Math.max(0, z0);
        x1 = Math.min(width, x1);
        y1 = Math.min(height, y1);
        z1 = Math.min(depth, z1);

        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (isSolidBlock(x, y, z)) {
                        list.add(new BoundingBox(
                                x, y, z,
                                x + 1f, y + 1f, z + 1f
                        ));
                    }
                }
            }
        }
        return list;
    }

    public void addListener(WorldListener listener) {
        listeners.add(listener);
    }

    public void save() {
        // TODO: 实现世界保存到文件
        System.out.println("World saved (stub)");
    }
}
