package com.voxelgame;

import com.voxelgame.graphics.ShaderProgram;
import com.voxelgame.world.Chunk;
import com.voxelgame.world.World;
import com.voxelgame.world.WorldRenderer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.glu.GLU;

import javax.swing.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VoxelGame implements Runnable {

    private int width;
    private int height;

    private World world;
    private WorldRenderer worldRenderer;
    private Player player;

    private ShaderProgram shader;

    private FloatBuffer fogColor;
    private GameTimer timer = new GameTimer(60.0F);

    private IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
    private IntBuffer selectBuffer = BufferUtils.createIntBuffer(2000);

    private HitResult hitResult;

    private final Matrix4f projectionMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public void init() throws LWJGLException, IOException {

        Display.setDisplayMode(new DisplayMode(1024, 768));
        Display.create();
        Keyboard.create();
        Mouse.create();

        this.width = Display.getDisplayMode().getWidth();
        this.height = Display.getDisplayMode().getHeight();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        fogColor = BufferUtils.createFloatBuffer(4);
        fogColor.put(new float[]{0.5f, 0.8f, 1.0f, 1.0f}).flip();

        try {
            shader = new ShaderProgram("/vertex.glsl", "/fragment.glsl");
        } catch (Exception e) {
            shader = null;
        }

        world = new World(64, 64, 64);
        worldRenderer = new WorldRenderer(world);
        player = new Player(world);

        Mouse.setGrabbed(true);
    }

    public void run() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(1)) {

            timer.advanceTime();

            for (int i = 0; i < timer.ticks; i++) {
                tick();
            }

            render(timer.a);
        }

        destroy();
    }

    public void tick() {
        player.tick();
    }

    private void destroy() {
        if (shader != null) shader.cleanup();
        if (worldRenderer != null) worldRenderer.cleanup();
        Display.destroy();
        Keyboard.destroy();
        Mouse.destroy();
    }

    // -----------------------------
    // CAMERA
    // -----------------------------

    private void moveCamera(float a) {

        float x = player.prevPos.x + (player.pos.x - player.prevPos.x) * a;
        float y = player.prevPos.y + (player.pos.y - player.prevPos.y) * a;
        float z = player.prevPos.z + (player.pos.z - player.prevPos.z) * a;

        GL11.glRotatef(player.xRot, 1, 0, 0);
        GL11.glRotatef(player.yRot, 0, 1, 0);
        GL11.glTranslatef(-x, -y, -z);
    }

    private void setupCamera(float a) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(70, (float) width / height, 0.05f, 1000f);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        moveCamera(a);
    }

    // -----------------------------
    // PICK
    // -----------------------------

    private void pick(float a) {

        selectBuffer.clear();
        GL11.glSelectBuffer(selectBuffer);

        GL11.glRenderMode(GL11.GL_SELECT);

        setupCamera(a);

        worldRenderer.pick(player);

        int hits = GL11.glRenderMode(GL11.GL_RENDER);

        if (hits > 0) {
            hitResult = new HitResult(0, 0, 0, 0, 0);
        } else {
            hitResult = null;
        }
    }

    // -----------------------------
    // RENDER
    // -----------------------------

    public void render(float a) {

        float mx = Mouse.getDX();
        float my = Mouse.getDY();

        player.turn(mx, my);

        pick(a);

        while (Mouse.next()) {

            if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState() && hitResult != null) {
                world.setBlock(hitResult.x, hitResult.y, hitResult.z, 0);
            }

            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && hitResult != null) {
                world.setBlock(hitResult.x, hitResult.y, hitResult.z, 1);
            }
        }

        while (Keyboard.next()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
                world.save();
            }
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        setupCamera(a);

        if (shader != null) shader.use();

        GL11.glEnable(GL11.GL_CULL_FACE);

        worldRenderer.render(player, 0);
        worldRenderer.render(player, 1);

        if (hitResult != null) {
            worldRenderer.renderHit(hitResult);
        }

        if (shader != null) shader.stop();

        Display.update();
    }

    public static void main(String[] args) {
        new Thread(new VoxelGame()).start();
    }
}
