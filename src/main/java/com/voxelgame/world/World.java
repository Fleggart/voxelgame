package com.voxelgame.world;

import com.voxelgame.physics.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class World {

    public final int width, height, depth;
    private final byte[] blocks;

    public World(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.blocks = new byte[width * height * depth];

        generateFlatWorld();
    }

    // =========================
    // index: X + Z*W + Y*W*D
    // =========================
    private int index(int x, int y, int z) {
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

    private void setBlockFast(int x, int y, int z, int v) {
        blocks[index(x, y, z)] = (byte) v;
    }

    // 简单地面
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

    // =========================
    // AABB 查询（关键修复）
    // =========================
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
}
