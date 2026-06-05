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

    public World(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        this.blocks = new byte[width * height * depth];
        this.lightDepths = new int[width * depth];

        generateTerrain();
        recalcLightDepths(0, 0, width, depth);
    }

    // -------------------------
    // 坐标映射: (x, y, z) -> blocks[]
    // -------------------------
    private int index(int x, int y, int z) {
        return x + width * (z + depth * y); // x fastest, z next, y slowest
    }

    // -------------------------
    // 简单地形生成
    // -------------------------
    private void generateTerrain() {
        int grassLevel = depth * 2 / 3;
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                for (int y = 0; y < height; y++) {
                    setBlockFast(x, y, z, y <= grassLevel ? 1 : 0); // 1=石头，0=空气
                }
            }
        }
    }

    private void setBlockFast(int x, int y, int z, int type) {
        blocks[index(x, y, z)] = (byte) type;
    }

    public boolean isBlock(int x, int y, int z) {
        if (!inBounds(x, y, z)) return false;
        return blocks[index(x, y, z)] == 1;
    }

    public boolean isSolidBlock(int x, int y, int z) {
        return isBlock(x, y, z);
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    public void setBlock(int x, int y, int z, int type) {
        if (!inBounds(x, y, z)) return;
        setBlockFast(x, y, z, type);
        recalcLightDepths(x, z, 1, 1);
        for (WorldListener l : listeners) l.blockChanged(x, y, z);
    }

    // -------------------------
    // 光照计算（简单版）
    // -------------------------
    public void recalcLightDepths(int x0, int z0, int w, int h) {
        for (int x = x0; x < x0 + w; x++) {
            for (int z = z0; z < z0 + h; z++) {
                int y = height - 1;
                while (y > 0 && !isBlock(x, y, z)) y--;
                lightDepths[x + z * width] = y;

                for (WorldListener l : listeners) l.lightColumnChanged(x, z, 0, y);
            }
        }
    }

    public float getBrightness(int x, int y, int z) {
        if (!inBounds(x, y, z)) return 1.0f;
        return y < lightDepths[x + z * width] ? 0.8f : 1.0f;
    }

    // -------------------------
    // 获取碰撞盒
    // -------------------------
    public List<BoundingBox> getCubes(BoundingBox aabb) {
        List<BoundingBox> boxes = new ArrayList<>();

        int x0 = Math.max(0, (int) Math.floor(aabb.x0));
        int x1 = Math.min(width, (int) Math.ceil(aabb.x1));
        int y0 = Math.max(0, (int) Math.floor(aabb.y0) - 1); // 向下扩1格，保证脚下方块被包含
        int y1 = Math.min(height, (int) Math.ceil(aabb.y1));
        int z0 = Math.max(0, (int) Math.floor(aabb.z0));
        int z1 = Math.min(depth, (int) Math.ceil(aabb.z1));

        for (int x = x0; x < x1; x++) {
            for (int y = y0; y < y1; y++) {
                for (int z = z0; z < z1; z++) {
                    if (isSolidBlock(x, y, z)) {
                        boxes.add(new BoundingBox(x, y, z, x + 1f, y + 1f, z + 1f));
                    }
                }
            }
        }

        return boxes;
    }

    // -------------------------
    // 世界存档
    // -------------------------
    public void save() {
        try (DataOutputStream dos = new DataOutputStream(
                new GZIPOutputStream(new FileOutputStream("world.dat")))) {
            dos.write(blocks);
            System.out.println("World saved successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File file = new File("world.dat");
        if (!file.exists()) return;

        try (DataInputStream dis = new DataInputStream(
                new GZIPInputStream(new FileInputStream(file)))) {
            dis.readFully(blocks);
            recalcLightDepths(0, 0, width, depth);
            for (WorldListener l : listeners) l.allChanged();
            System.out.println("World loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // -------------------------
    // Listener
    // -------------------------
    public void addListener(WorldListener l) {
        listeners.add(l);
    }

    public void removeListener(WorldListener l) {
        listeners.remove(l);
    }
}
