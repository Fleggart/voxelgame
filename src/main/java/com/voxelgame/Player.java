package com.voxelgame;

import com.voxelgame.physics.BoundingBox;
import com.voxelgame.world.World;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class Player {

    private static final float WIDTH = 0.3f;
    private static final float HEIGHT = 0.9f;
    private static final float GRAVITY = 0.005f;
    private static final float JUMP = 0.12f;
    private static final float GROUND_SPEED = 0.02f;
    private static final float AIR_SPEED = 0.005f;
    private static final float EPS = 0.0001f;
    private static final float MOUSE_SENSITIVITY = 0.15f;

    private final World world;

    public final Vector3f pos = new Vector3f();
    public final Vector3f prev = new Vector3f();
    public final Vector3f vel = new Vector3f();

    public BoundingBox bb;
    public float yRot, xRot;
    public boolean onGround = false;

    public Player(World world) {
        this.world = world;
        spawn();
    }

    public void spawn() {
        pos.set(world.width / 2f, world.height / 2f, world.depth / 2f);
        vel.zero();
        prev.set(pos);
        updateBB();
    }

    private void updateBB() {
        bb = new BoundingBox(
                pos.x - WIDTH, pos.y - HEIGHT, pos.z - WIDTH,
                pos.x + WIDTH, pos.y + HEIGHT, pos.z + WIDTH
        );
    }

    public void turn(float dx, float dy) {
        yRot += dx * MOUSE_SENSITIVITY;
        xRot -= dy * MOUSE_SENSITIVITY;
        
        // 限制垂直视角
        if (xRot > 90) xRot = 90;
        if (xRot < -90) xRot = -90;
    }

    public void tick() {
        prev.set(pos);

        float xa = 0, za = 0;

        if (Keyboard.isKeyDown(Keyboard.KEY_W)) za--;
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) za++;
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) xa--;
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) xa++;

        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE) && onGround) {
            vel.y = JUMP;
        }

        float speed = onGround ? GROUND_SPEED : AIR_SPEED;
        moveRelative(xa, za, speed);

        vel.y -= GRAVITY;
        move(vel.x, vel.y, vel.z);
        updateBB();
    }

    private void move(float dx, float dy, float dz) {
        onGround = false;
        List<BoundingBox> boxes = world.getCubes(bb.expand(dx, dy, dz));

        // Y
        dy = collideY(boxes, dy);
        bb.move(0, dy, 0);
        if (Math.abs(dy) < EPS && vel.y < 0) {
            onGround = true;
            vel.y = 0;
        }

        // X
        dx = collideX(boxes, dx);
        bb.move(dx, 0, 0);
        if (Math.abs(dx) < EPS) vel.x = 0;

        // Z
        dz = collideZ(boxes, dz);
        bb.move(0, 0, dz);
        if (Math.abs(dz) < EPS) vel.z = 0;

        // 同步位置
        pos.x = bb.centerX();
        pos.y = bb.centerY();
        pos.z = bb.centerZ();

        if (onGround) {
            vel.y = 0;
            pos.y = (float) Math.round(pos.y * 1000f) / 1000f;
        }
    }

    private float collideX(List<BoundingBox> boxes, float dx) {
        for (BoundingBox b : boxes) dx = b.clipXCollide(bb, dx);
        return dx;
    }

    private float collideY(List<BoundingBox> boxes, float dy) {
        for (BoundingBox b : boxes) dy = b.clipYCollide(bb, dy);
        return dy;
    }

    private float collideZ(List<BoundingBox> boxes, float dz) {
        for (BoundingBox b : boxes) dz = b.clipZCollide(bb, dz);
        return dz;
    }

    private void moveRelative(float xa, float za, float speed) {
        float d = xa * xa + za * za;
        if (d < 0.0001f) return;

        d = speed / (float) Math.sqrt(d);
        xa *= d;
        za *= d;

        float rad = (float) Math.toRadians(yRot);
        float sin = (float) Math.sin(rad);
        float cos = (float) Math.cos(rad);

        vel.x += xa * cos - za * sin;
        vel.z += za * cos + xa * sin;
    }
}
