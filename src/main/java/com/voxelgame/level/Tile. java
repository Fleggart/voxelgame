package com.voxelgame.level;

public class Tile {
    public static final Tile ROCK = new Tile(0);
    public static final Tile GRASS = new Tile(1);
    
    private final int tex;
    private static final float TEX_SIZE = 1.0F / 16.0F;
    private static final float TEX_OFFSET = 0.001F;

    private Tile(int tex) {
        this.tex = tex;
    }

    public void render(Tesselator t, Level level, int layer, int x, int y, int z) {
        float u0 = tex * TEX_SIZE;
        float u1 = u0 + TEX_SIZE - TEX_OFFSET;
        float v0 = 0.0F;
        float v1 = v0 + TEX_SIZE - TEX_OFFSET;
        
        float c1 = 1.0F;
        float c2 = 0.8F;
        float c3 = 0.6F;
        
        float x0 = x;
        float x1 = x + 1.0F;
        float y0 = y;
        float y1 = y + 1.0F;
        float z0 = z;
        float z1 = z + 1.0F;
        
        // 底面 (Y-)
        if (!level.isSolidTile(x, y - 1, z)) {
            float br = level.getBrightness(x, y - 1, z) * c1;
            if (br == c1 ^ layer == 1) {
                t.color(br, br, br);
                t.tex(u0, v1); t.vertex(x0, y0, z1);
                t.tex(u0, v0); t.vertex(x0, y0, z0);
                t.tex(u1, v0); t.vertex(x1, y0, z0);
                t.tex(u1, v1); t.vertex(x1, y0, z1);
            }
        }
        
        // 顶面 (Y+)
        if (!level.isSolidTile(x, y + 1, z)) {
            float br = level.getBrightness(x, y, z) * c1;
            if (br == c1 ^ layer == 1) {
                t.color(br, br, br);
                t.tex(u1, v1); t.vertex(x1, y1, z1);
                t.tex(u1, v0); t.vertex(x1, y1, z0);
                t.tex(u0, v0); t.vertex(x0, y1, z0);
                t.tex(u0, v1); t.vertex(x0, y1, z1);
            }
        }
        
        // 后面 (Z-)
        if (!level.isSolidTile(x, y, z - 1)) {
            float br = level.getBrightness(x, y, z - 1) * c2;
            if (br == c2 ^ layer == 1) {
                t.color(br, br, br);
                t.tex(u1, v0); t.vertex(x0, y1, z0);
                t.tex(u0, v0); t.vertex(x1, y1, z0);
                t.tex(u0, v1); t.vertex(x1, y0, z0);
                t.tex(u1, v1); t.vertex(x0, y0, z0);
            }
        }
        
        // 前面 (Z+)
        if (!level.isSolidTile(x, y, z + 1)) {
            float br = level.getBrightness(x, y, z + 1) * c2;
            if (br == c2 ^ layer == 1) {
                t.color(br, br, br);
                t.tex(u0, v0); t.vertex(x0, y1, z1);
                t.tex(u0, v1); t.vertex(x0, y0, z1);
                t.tex(u1, v1); t.vertex(x1, y0, z1);
                t.tex(u1, v0); t.vertex(x1, y1, z1);
            }
        }
        
        // 左面 (X-)
        if (!level.isSolidTile(x - 1, y, z)) {
            float br = level.getBrightness(x - 1, y, z) * c3;
            if (br == c3 ^ layer == 1) {
                t.color(br, br, br);
                t.tex(u1, v0); t.vertex(x0, y1, z1);
                t.tex(u0, v0); t.vertex(x0, y1, z0);
                t.tex(u0, v1); t.vertex(x0, y0, z0);
                t.tex(u1, v1); t.vertex(x0, y0, z1);
            }
        }
        
        // 右面 (X+)
        if (!level.isSolidTile(x + 1, y, z)) {
            float br = level.getBrightness(x + 1, y, z) * c3;
            if (br == c3 ^ layer == 1) {
                t.color(br, br, br);
                t.tex(u0, v1); t.vertex(x1, y0, z1);
                t.tex(u1, v1); t.vertex(x1, y0, z0);
                t.tex(u1, v0); t.vertex(x1, y1, z0);
                t.tex(u0, v0); t.vertex(x1, y1, z1);
            }
        }
    }

    public void renderFace(Tesselator t, int x, int y, int z, int face) {
        float x0 = x;
        float x1 = x + 1.0F;
        float y0 = y;
        float y1 = y + 1.0F;
        float z0 = z;
        float z1 = z + 1.0F;
        
        t.color(1.0F, 1.0F, 1.0F);
        
        switch (face) {
            case 0 -> {
                t.vertex(x0, y0, z1);
                t.vertex(x0, y0, z0);
                t.vertex(x1, y0, z0);
                t.vertex(x1, y0, z1);
            }
            case 1 -> {
                t.vertex(x1, y1, z1);
                t.vertex(x1, y1, z0);
                t.vertex(x0, y1, z0);
                t.vertex(x0, y1, z1);
            }
            case 2 -> {
                t.vertex(x0, y1, z0);
                t.vertex(x1, y1, z0);
                t.vertex(x1, y0, z0);
                t.vertex(x0, y0, z0);
            }
            case 3 -> {
                t.vertex(x0, y1, z1);
                t.vertex(x0, y0, z1);
                t.vertex(x1, y0, z1);
                t.vertex(x1, y1, z1);
            }
            case 4 -> {
                t.vertex(x0, y1, z1);
                t.vertex(x0, y1, z0);
                t.vertex(x0, y0, z0);
                t.vertex(x0, y0, z1);
            }
            case 5 -> {
                t.vertex(x1, y0, z1);
                t.vertex(x1, y0, z0);
                t.vertex(x1, y1, z0);
                t.vertex(x1, y1, z1);
            }
        }
    }
}
