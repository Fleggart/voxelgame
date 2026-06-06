package com.voxelgame;

public class HitResult {
    public int x;
    public int y;
    public int z;
    public int side;
    public int face;
    
    public HitResult(int x, int y, int z, int side, int face) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.side = side;
        this.face = face;
    }
}
