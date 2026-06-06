package com.voxelgame;

import com.voxelgame.level.Level;
import com.voxelgame.phys.AABB;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class Player {
    private Level level;
    public float xo, yo, zo;
    public float x, y, z;
    public float xd, yd, zd;
    public float yRot, xRot;
    public AABB bb;
    public boolean onGround = false;
    
    public Player(Level level) {
        this.level = level;
        resetPos();
    }
    
    public void resetPos() {
        float x = (float) Math.random() * level.width;
        float y = level.depth + 10;
        float z = (float) Math.random() * level.height;
        setPos(x, y, z);
    }
    
    private void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float w = 0.3f;
        float h = 0.9f;
        bb = new AABB(x - w, y - h, z - w, x + w, y + h, z + w);
    }
    
    public void turn(float xo, float yo) {
        yRot += xo * 0.15f;
        xRot -= yo * 0.15f;
        if (xRot < -90) xRot = -90;
        if (xRot > 90) xRot = 90;
    }
    
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        
        float xa = 0, za = 0;
        
        // 简化的输入处理，实际应该使用 glfwGetKey
        // 这里为了保持兼容性，我们稍后会通过 VoxelGame 传入输入状态
        // 暂时保持原有逻辑的骨架
        
        moveRelative(xa, za, onGround ? 0.02f : 0.005f);
        yd -= 0.005f;
        move(xd, yd, zd);
        xd *= 0.91f;
        yd *= 0.98f;
        zd *= 0.91f;
        if (onGround) {
            xd *= 0.8f;
            zd *= 0.8f;
        }
    }
    
    public void tick(boolean forward, boolean back, boolean left, boolean right, boolean jump) {
        xo = x;
        yo = y;
        zo = z;
        
        float xa = 0, za = 0;
        if (forward) za--;
        if (back) za++;
        if (left) xa--;
        if (right) xa++;
        
        moveRelative(xa, za, onGround ? 0.02f : 0.005f);
        
        if (jump && onGround) {
            yd = 0.12f;
        }
        
        yd -= 0.005f;
        move(xd, yd, zd);
        xd *= 0.91f;
        yd *= 0.98f;
        zd *= 0.91f;
        if (onGround) {
            xd *= 0.8f;
            zd *= 0.8f;
        }
    }
    
    public void move(float xa, float ya, float za) {
        float xaOrg = xa, yaOrg = ya, zaOrg = za;
        List<AABB> aABBs = level.getCubes(bb.expand(xa, ya, za));
        
        for (AABB aabb : aABBs) {
            ya = aabb.clipYCollide(bb, ya);
        }
        bb.move(0, ya, 0);
        
        for (AABB aabb : aABBs) {
            xa = aabb.clipXCollide(bb, xa);
        }
        bb.move(xa, 0, 0);
        
        for (AABB aabb : aABBs) {
            za = aabb.clipZCollide(bb, za);
        }
        bb.move(0, 0, za);
        
        onGround = yaOrg != ya && yaOrg < 0;
        if (xaOrg != xa) xd = 0;
        if (yaOrg != ya) yd = 0;
        if (zaOrg != za) zd = 0;
        
        x = (bb.x0 + bb.x1) / 2;
        y = bb.y0 + 1.62f;
        z = (bb.z0 + bb.z1) / 2;
    }
    
    public void moveRelative(float xa, float za, float speed) {
        float dist = xa * xa + za * za;
        if (dist < 0.01f) return;
        
        dist = speed / (float) Math.sqrt(dist);
        xa *= dist;
        za *= dist;
        
        float sin = (float) Math.sin(Math.toRadians(yRot));
        float cos = (float) Math.cos(Math.toRadians(yRot));
        xd += xa * cos - za * sin;
        zd += za * cos + xa * sin;
    }
}
