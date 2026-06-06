package com.voxelgame.physics;

public class BoundingBox {

    private static final float EPSILON = 0.0001f;

    public float x0, y0, z0;
    public float x1, y1, z1;

    public BoundingBox(float x0, float y0, float z0,
                       float x1, float y1, float z1) {
        this.x0 = x0;
        this.y0 = y0;
        this.z0 = z0;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
    }

    // 复制方法
    public BoundingBox copy() {
        return new BoundingBox(x0, y0, z0, x1, y1, z1);
    }

    public BoundingBox expand(float xa, float ya, float za) {
        float _x0 = x0, _y0 = y0, _z0 = z0;
        float _x1 = x1, _y1 = y1, _z1 = z1;

        if (xa < 0) _x0 += xa;
        else if (xa > 0) _x1 += xa;

        if (ya < 0) _y0 += ya;
        else if (ya > 0) _y1 += ya;

        if (za < 0) _z0 += za;
        else if (za > 0) _z1 += za;

        return new BoundingBox(_x0, _y0, _z0, _x1, _y1, _z1);
    }

    public BoundingBox grow(float xa, float ya, float za) {
        return new BoundingBox(
                x0 - xa, y0 - ya, z0 - za,
                x1 + xa, y1 + ya, z1 + za
        );
    }

    public void move(float xa, float ya, float za) {
        x0 += xa; y0 += ya; z0 += za;
        x1 += xa; y1 += ya; z1 += za;
    }

    public boolean intersects(BoundingBox b) {
        return x1 > b.x0 && x0 < b.x1
            && y1 > b.y0 && y0 < b.y1
            && z1 > b.z0 && z0 < b.z1;
    }

    private float clip(BoundingBox c, float value, int axis) {
        if (axis != 0 && (c.x1 <= x0 || c.x0 >= x1)) return value;
        if (axis != 1 && (c.y1 <= y0 || c.y0 >= y1)) return value;
        if (axis != 2 && (c.z1 <= z0 || c.z0 >= z1)) return value;

        if (value > 0) {
            float limit;
            if (axis == 0) limit = c.x0 - x1 - EPSILON;
            else if (axis == 1) limit = c.y0 - y1 - EPSILON;
            else limit = c.z0 - z1 - EPSILON;

            if (limit < value) return limit;
        }

        if (value < 0) {
            float limit;
            if (axis == 0) limit = c.x1 - x0 + EPSILON;
            else if (axis == 1) limit = c.y1 - y0 + EPSILON;
            else limit = c.z1 - z0 + EPSILON;

            if (limit > value) return limit;
        }
        return value;
    }

    public float clipXCollide(BoundingBox c, float xa) {
        return clip(c, xa, 0);
    }

    public float clipYCollide(BoundingBox c, float ya) {
        return clip(c, ya, 1);
    }

    public float clipZCollide(BoundingBox c, float za) {
        return clip(c, za, 2);
    }

    public float centerX() {
        return (x0 + x1) * 0.5f;
    }

    public float centerY() {
        return (y0 + y1) * 0.5f;
    }

    public float centerZ() {
        return (z0 + z1) * 0.5f;
    }
}
