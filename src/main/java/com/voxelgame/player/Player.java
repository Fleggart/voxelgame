package com.voxelgame.player;

import com.voxelgame.level.Level;
import com.voxelgame.physics.AABB;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

public class Player {
    private final Level level;
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
    
    private void resetPos() {
        float x = (float) Math.random() * level.width;
        float y = level.depth + 10;
        float z = (float) Math.random() * level.height;
        setPos(x, y, z);
    }
    
    private void setPos(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
        float w = 0.3F;
        float h = 0.9F;
        bb = new AABB(x - w, y - h, z - w, x + w, y + h, z + w);
    }
    
    public void turn(float dx, float dy) {
        yRot += dx * 0.15F;
        xRot -= dy * 0.15F;
        
        if (xRot < -90.0F) xRot = -90.0F;
        if (xRot > 90.0F) xRot = 90.0F;
    }
    
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        
        float xa = 0.0F;
        float za = 0.0F;
        
        long window = GLFW.glfwGetCurrentContext();
        
        // 移动控制
        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) za--;
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) za++;
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) xa--;
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) xa++;
        
        // 重置位置
        if (glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS) resetPos();
        
        // 跳跃
        if ((glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) && onGround) {
            yd = 0.12F;
        }
        
        moveRelative(xa, za, onGround ? 0.02F : 0.005F);
        yd -= 0.005;
        move(xd, yd, zd);
        
        xd *= 0.91F;
        yd *= 0.98F;
        zd *= 0.91F;
        
        if (onGround) {
            xd *= 0.8F;
            zd *= 0.8F;
        }
    }
    
    public void move(float xa, float ya, float za) {
        float xaOrg = xa;
        float yaOrg = ya;
        float zaOrg = za;
        
        List<AABB> aabbs = level.getCubes(bb.expand(xa, ya, za));
        
        for (AABB aabb : aabbs) {
            ya = aabb.clipYCollide(bb, ya);
        }
        bb.move(0.0F, ya, 0.0F);
        
        for (AABB aabb : aabbs) {
            xa = aabb.clipXCollide(bb, xa);
        }
        bb.move(xa, 0.0F, 0.0F);
        
        for (AABB aabb : aabbs) {
            za = aabb.clipZCollide(bb, za);
        }
        bb.move(0.0F, 0.0F, za);
        
        onGround = yaOrg != ya && yaOrg < 0.0F;
        
        if (xaOrg != xa) xd = 0.0F;
        if (yaOrg != ya) yd = 0.0F;
        if (zaOrg != za) zd = 0.0F;
        
        x = (bb.x0 + bb.x1) / 2.0F;
        y = bb.y0 + 1.62F;
        z = (bb.z0 + bb.z1) / 2.0F;
    }
    
    public void moveRelative(float xa, float za, float speed) {
        float dist = xa * xa + za * za;
        if (dist < 0.01F) return;
        
        dist = speed / (float) Math.sqrt(dist);
        xa *= dist;
        za *= dist;
        
        float sin = (float) Math.sin(Math.toRadians(yRot));
        float cos = (float) Math.cos(Math.toRadians(yRot));
        
        xd += xa * cos - za * sin;
        zd += za * cos + xa * sin;
    }
}
