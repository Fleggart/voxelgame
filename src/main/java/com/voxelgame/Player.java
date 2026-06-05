package com.voxelgame;

import com.voxelgame.physics.BoundingBox;
import com.voxelgame.world.World;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

public class Player {
    private static final float WIDTH = 0.3f;
    private static final float HEIGHT = 0.9f;
    private static final float JUMP_FORCE = 0.12f;
    private static final float GRAVITY = 0.005f;
    private static final float MOVE_SPEED_GROUND = 0.02f;
    private static final float MOVE_SPEED_AIR = 0.005f;
    
    private final World world;
    
    // Position using JOML vectors
    public final Vector3f pos = new Vector3f();
    public final Vector3f prevPos = new Vector3f();
    public final Vector3f vel = new Vector3f();
    
    public float yRot, xRot;
    public BoundingBox bb;
    public boolean onGround = false;

    public Player(World world) {
        this.world = world;
        resetPos();
    }

    public void resetPos() {
        pos.set((float)Math.random() * world.width, world.depth + 10, (float)Math.random() * world.height);
        prevPos.set(pos);
        vel.zero();
        updateBoundingBox();
    }
    
    private void updateBoundingBox() {
        bb = new BoundingBox(
            pos.x - WIDTH, pos.y - HEIGHT, pos.z - WIDTH,
            pos.x + WIDTH, pos.y + HEIGHT, pos.z + WIDTH
        );
    }

    public void turn(float dx, float dy) {
        yRot += dx * 0.15f;
        xRot = Math.min(90f, Math.max(-90f, xRot - dy * 0.15f));
    }

    public void tick() {
        prevPos.set(pos);
        
        float xa = 0, za = 0;
        
        // Input handling
        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            if (onGround) {
                vel.y = JUMP_FORCE;
            }
        }
        
        // Movement keys: W/S/A/D or arrow keys
        if (isKeyDown(Keyboard.KEY_UP, Keyboard.KEY_W)) za--;
        if (isKeyDown(Keyboard.KEY_DOWN, Keyboard.KEY_S)) za++;
        if (isKeyDown(Keyboard.KEY_LEFT, Keyboard.KEY_A)) xa--;
        if (isKeyDown(Keyboard.KEY_RIGHT, Keyboard.KEY_D)) xa++;
        
        if (Keyboard.isKeyDown(Keyboard.KEY_R)) resetPos();
        
        float speed = onGround ? MOVE_SPEED_GROUND : MOVE_SPEED_AIR;
        moveRelative(xa, za, speed);
        
        vel.y -= GRAVITY;
        move(vel.x, vel.y, vel.z);
        
        // Apply friction
        float friction = 0.91f;
        vel.x *= friction;
        vel.y *= 0.98f;
        vel.z *= friction;
        
        if (onGround) {
            vel.x *= 0.8f;
            vel.z *= 0.8f;
        }
        
        updateBoundingBox();
    }
    
    private boolean isKeyDown(int key1, int key2) {
        return Keyboard.isKeyDown(key1) || Keyboard.isKeyDown(key2);
    }

    public void move(float xa, float ya, float za) {
        float xaOrg = xa, yaOrg = ya, zaOrg = za;
        var boxes = world.getCubes(bb.expand(xa, ya, za));
        
        ya = collideY(boxes, ya);
        bb.move(0, ya, 0);
        xa = collideX(boxes, xa);
        bb.move(xa, 0, 0);
        za = collideZ(boxes, za);
        bb.move(0, 0, za);
        
        onGround = yaOrg != ya && yaOrg < 0;
        
        if (xaOrg != xa) vel.x = 0;
        if (yaOrg != ya) vel.y = 0;
        if (zaOrg != za) vel.z = 0;
        
        pos.x = (bb.x0 + bb.x1) / 2;
        pos.y = bb.y0 + 1.62f;
        pos.z = (bb.z0 + bb.z1) / 2;
    }
    
    private float collideX(java.util.List<BoundingBox> boxes, float xa) {
        for (var box : boxes) xa = box.clipXCollide(bb, xa);
        return xa;
    }
    
    private float collideY(java.util.List<BoundingBox> boxes, float ya) {
        for (var box : boxes) ya = box.clipYCollide(bb, ya);
        return ya;
    }
    
    private float collideZ(java.util.List<BoundingBox> boxes, float za) {
        for (var box : boxes) za = box.clipZCollide(bb, za);
        return za;
    }

    public void moveRelative(float xa, float za, float speed) {
        float dist = xa * xa + za * za;
        if (dist < 0.01f) return;
        
        dist = speed / (float)Math.sqrt(dist);
        xa *= dist;
        za *= dist;
        
        float rad = yRot * (float)Math.PI / 180f;
        float sin = (float)Math.sin(rad);
        float cos = (float)Math.cos(rad);
        
        vel.x += xa * cos - za * sin;
        vel.z += za * cos + xa * sin;
    }
}
