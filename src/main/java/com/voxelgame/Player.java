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
    private boolean wasOnGround = false;

    public Player(World world) {
        this.world = world;
        resetPos();
    }

    public void resetPos() {
        pos.set((float) Math.random() * world.width, world.height + 10, (float) Math.random() * world.depth);
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

        // -------------------------
        // 输入
        // -------------------------
        if (isKeyDown(Keyboard.KEY_SPACE, Keyboard.KEY_LCONTROL) && onGround) {
            vel.y = JUMP_FORCE;
        }

        if (isKeyDown(Keyboard.KEY_UP, Keyboard.KEY_W)) za--;
        if (isKeyDown(Keyboard.KEY_DOWN, Keyboard.KEY_S)) za++;
        if (isKeyDown(Keyboard.KEY_LEFT, Keyboard.KEY_A)) xa--;
        if (isKeyDown(Keyboard.KEY_RIGHT, Keyboard.KEY_D)) xa++;

        if (Keyboard.isKeyDown(Keyboard.KEY_R)) resetPos();

        // -------------------------
        // 水平移动
        // -------------------------
        float speed = onGround ? MOVE_SPEED_GROUND : MOVE_SPEED_AIR;
        moveRelative(xa, za, speed);

        // -------------------------
        // 重力
        // -------------------------
        vel.y -= GRAVITY;

        // -------------------------
        // 碰撞检测
        // -------------------------
        moveWithCollision(vel.x, vel.y, vel.z);

        // -------------------------
        // 摩擦
        // -------------------------
        applyFriction();

        // -------------------------
        // 更新 AABB
        // -------------------------
        updateBoundingBox();

        // -------------------------
        // 更新上帧地面状态
        // -------------------------
        wasOnGround = onGround;
    }

    private boolean isKeyDown(int key1, int key2) {
        return Keyboard.isKeyDown(key1) || Keyboard.isKeyDown(key2);
    }

    private void moveWithCollision(float xa, float ya, float za) {
        float xaOrg = xa, yaOrg = ya, zaOrg = za;

        List<BoundingBox> boxes = world.getCubes(bb.expand(xa, ya, za));

        // Y 方向
        ya = clipCollideY(boxes, ya);
        bb.move(0, ya, 0);

        // X 方向
        xa = clipCollideX(boxes, xa);
        bb.move(xa, 0, 0);

        // Z 方向
        za = clipCollideZ(boxes, za);
        bb.move(0, 0, za);

        // 判断地面
        onGround = yaOrg < 0 && Math.abs(yaOrg - ya) > EPSILON;

        // 修正速度
        if (Math.abs(xaOrg - xa) > EPSILON) vel.x = 0;
        if (Math.abs(yaOrg - ya) > EPSILON) vel.y = onGround ? 0 : vel.y;
        if (Math.abs(zaOrg - za) > EPSILON) vel.z = 0;

        // 吸附地面
        if (onGround && vel.y < 0) {
            vel.y = 0;
            pos.y = bb.y0 + HEIGHT; // 保持脚底在方块上
        }

        // 更新中心位置
        pos.x = (bb.x0 + bb.x1) / 2;
        pos.y = (bb.y0 + bb.y1) / 2;
        pos.z = (bb.z0 + bb.z1) / 2;
    }

    private float clipCollideX(List<BoundingBox> boxes, float xa) {
        for (BoundingBox box : boxes) xa = box.clipXCollide(bb, xa);
        return xa;
    }

    private float clipCollideY(List<BoundingBox> boxes, float ya) {
        for (BoundingBox box : boxes) ya = box.clipYCollide(bb, ya);
        return ya;
    }

    private float clipCollideZ(List<BoundingBox> boxes, float za) {
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

    private void applyFriction() {
        float friction = 0.91f;
        vel.x *= friction;
        vel.z *= friction;
        vel.y *= 0.98f;

        if (onGround) {
            vel.x *= 0.8f;
            vel.z *= 0.8f;
        }
    }
}
