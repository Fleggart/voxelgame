package com.voxelgame.level;

public class Tile {
    public static Tile rock = new Tile(0);
    public static Tile grass = new Tile(1);
    private int tex;
    
    private Tile(int tex) {
        this.tex = tex;
    }
    
    public void render(Tesselator t, Level level, int layer, int x, int y, int z) {
        float u0 = tex / 16.0f;
        float u1 = u0 + 0.0624375f;
        float v0 = 0.0f;
        float v1 = v0 + 0.0624375f;
        float c1 = 1.0f;
        float c2 = 0.8f;
        float c3 = 0.6f;
        float x0 = x;
        float x1 = x + 1;
        float y0 = y;
        float y1 = y + 1;
        float z0 = z;
        float z1 = z + 1;
        
        // 底面 (Y-)
        if (!level.isSolidTile(x, y - 1, z)) {
            float br = level.getBrightness(x, y - 1, z) * c1;
            if ((br == c1) != (layer == 1)) {
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
            if ((br == c1) != (layer == 1)) {
                t.color(br, br, br);
                t.tex(u1, v1); t.vertex(x1, y1, z1);
                t.tex(u1, v0); t.vertex(x1, y1, z0);
                t.tex(u0, v0); t.vertex(x0, y1, z0);
                t.tex(u0, v1); t.vertex(x0, y1, z1);
            }
        }
        
        // 背面 (Z-)
        if (!level.isSolidTile(x, y, z - 1)) {
            float br = level.getBrightness(x, y, z - 1) * c2;
            if ((br == c2) != (layer == 1)) {
                t.color(br, br, br);
                t.tex(u1, v0); t.vertex(x0, y1, z0);
                t.tex(u0, v0); t.vertex(x1, y1, z0);
                t.tex(u0, v1); t.vertex(x1, y0, z0);
                t.tex(u1, v1); t.vertex(x0, y0, z0);
            }
        }
        
        // 正面 (Z+)
        if (!level.isSolidTile(x, y, z + 1)) {
            float br = level.getBrightness(x, y, z + 1) * c2;
            if ((br == c2) != (layer == 1)) {
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
            if ((br == c3) != (layer == 1)) {
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
            if ((br == c3) != (layer == 1)) {
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
        float x1 = x + 1;
        float y0 = y;
        float y1 = y + 1;
        float z0 = z;
        float z1 = z + 1;
        
        switch (face) {
            case 0: // 底面
                t.vertex(x0, y0, z1);
                t.vertex(x0, y0, z0);
                t.vertex(x1, y0, z0);
                t.vertex(x1, y0, z1);
                break;
            case 1: // 顶面
                t.vertex(x1, y1, z1);
                t.vertex(x1, y1, z0);
                t.vertex(x0, y1, z0);
                t.vertex(x0, y1, z1);
                break;
            case 2: // 背面
                t.vertex(x0, y1, z0);
                t.vertex(x1, y1, z0);
                t.vertex(x1, y0, z0);
                t.vertex(x0, y0, z0);
                break;
            case 3: // 正面
                t.vertex(x0, y1, z1);
                t.vertex(x0, y0, z1);
                t.vertex(x1, y0, z1);
                t.vertex(x1, y1, z1);
                break;
            case 4: // 左面
                t.vertex(x0, y1, z1);
                t.vertex(x0, y1, z0);
                t.vertex(x0, y0, z0);
                t.vertex(x0, y0, z1);
                break;
            case 5: // 右面
                t.vertex(x1, y0, z1);
                t.vertex(x1, y0, z0);
                t.vertex(x1, y1, z0);
                t.vertex(x1, y1, z1);
                break;
        }
    }
}
