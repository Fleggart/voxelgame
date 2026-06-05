package com.voxelgame;

import com.voxelgame.physics.BoundingBox;
import com.voxelgame.world.World;
import org.joml.Vector3f;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class Player {
    private static final float WIDTH = 0.3f;
    private static final float HEIGHT = 0.9f;
    private static final float JUMP_FORCE = 0.12f;
    private static final float GRAVITY = 0.005f;
    private static final float MOVE_SPEED_GROUND = 0.02f;
    private static final float MOVE_SPEED_AIR = 0.005f;
    private static final float EPSILON = 0.001f;

    private final World world;

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
        pos.set((float) Math.random() * world.width, world.depth + 10, (float) Math.random() * world.height);
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

        // --- Handle input ---
        float xa = 0, za = 0;
        if ((Keyboard.isKeyDown(Keyboard.KEY_SPACE) || Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) && onGround) {
            vel.y = JUMP_FORCE;
        }

        if (isKeyDown(Keyboard.KEY_UP, Keyboard.KEY_W)) za--;
        if (isKeyDown(Keyboard.KEY_DOWN, Keyboard.KEY_S)) za++;
        if (isKeyDown(Keyboard.KEY_LEFT, Keyboard.KEY_A)) xa--;
        if (isKeyDown(Keyboard.KEY_RIGHT, Keyboard.KEY_D)) xa++;
        if (Keyboard.isKeyDown(Keyboard.KEY_R)) resetPos();

        float speed = onGround ? MOVE_SPEED_GROUND : MOVE_SPEED_AIR;
        moveRelative(xa, za, speed);

        // --- Apply gravity ---
        vel.y -= GRAVITY;

        // --- Sub-step movement for stability ---
        int steps = 2; // 可以根据速度提高 sub-step
        float stepX = vel.x / steps;
        float stepY = vel.y / steps;
        float stepZ = vel.z / steps;

        for (int i = 0; i < steps; i++) {
            move(stepX, stepY, stepZ);
        }

        // --- Apply friction ---
        if (onGround) {
            vel.x *= 0.8f;
            vel.z *= 0.8f;
        } else {
            vel.x *= 0.98f;
            vel.z *= 0.98f;
        }
        vel.y *= 0.98f; // slight air damping

        updateBoundingBox();
    }

    private boolean isKeyDown(int key1, int key2) {
        return Keyboard.isKeyDown(key1) || Keyboard.isKeyDown(key2);
    }

    public void move(float xa, float ya, float za) {
        float xaOrg = xa, yaOrg = ya, zaOrg = za;
        List<BoundingBox> boxes = world.getCubes(bb.expand(xa, ya, za));

        // Clip each axis
        ya = collideY(boxes, ya);
        bb.move(0, ya, 0);

        xa = collideX(boxes, xa);
        bb.move(xa, 0, 0);

        za = collideZ(boxes, za);
        bb.move(0, 0, za);

        // Ground detection with epsilon
        onGround = yaOrg < 0 && Math.abs(yaOrg - ya) > EPSILON;

        // Zero velocity if collision
        if (xaOrg != xa) vel.x = 0;
        if (yaOrg != ya) vel.y = 0;
        if (zaOrg != za) vel.z = 0;

        // Update pos from bounding box
        pos.x = (bb.x0 + bb.x1) / 2f;
        pos.y = bb.y0 + HEIGHT; // 更稳定的 Y 同步，贴地更稳
        pos.z = (bb.z0 + bb.z1) / 2f;
    }

    private float collideX(List<BoundingBox> boxes, float xa) {
        for (BoundingBox box : boxes) xa = box.clipXCollide(bb, xa);
        return xa;
    }

    private float collideY(List<BoundingBox> boxes, float ya) {
        for (BoundingBox box : boxes) ya = box.clipYCollide(bb, ya);
        return ya;
    }

    private float collideZ(List<BoundingBox> boxes, float za) {
        for (BoundingBox box : boxes) za = box.clipZCollide(bb, za);
        return za;
    }

    public void moveRelative(float xa, float za, float speed) {
        float dist = xa * xa + za * za;
        if (dist < 0.0001f) return;

        dist = speed / (float) Math.sqrt(dist);
        xa *= dist;
        za *= dist;

        float rad = yRot * (float) Math.PI / 180f;
        float sin = (float) Math.sin(rad);
        float cos = (float) Math.cos(rad);

        vel.x += xa * cos - za * sin;
        vel.z += za * cos + xa * sin;
    }
}
