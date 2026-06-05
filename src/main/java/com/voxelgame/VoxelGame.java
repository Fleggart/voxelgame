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

public class VoxelGame implements Runnable {

    private static final boolean FULLSCREEN_MODE = false;
    private int width, height;
    private FloatBuffer fogColor;
    private GameTimer timer = new GameTimer(60.0F);
    private World world;
    private WorldRenderer worldRenderer;
    private Player player;
    private ShaderProgram shader;
    
    // JOML matrices
    private final Matrix4f projectionMatrix = new Matrix4f();
    private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    // Picking
    private HitResult hitResult = null;

    public void init() throws LWJGLException, IOException {

        int col = 0xE0CCFA; // 背景色
        float fr = 0.5F, fg = 0.8F, fb = 1.0F;

        // 雾颜色
        fogColor = BufferUtils.createFloatBuffer(4);
        fogColor.put(new float[]{
            (float)(col >> 16 & 255) / 255.0F,
            (float)(col >> 8 & 255) / 255.0F,
            (float)(col & 255) / 255.0F,
            1.0F
        }).flip();

        Display.setDisplayMode(new DisplayMode(1024, 768));
        Display.create();
        Keyboard.create();
        Mouse.create();
        Mouse.setGrabbed(true);

        width = Display.getDisplayMode().getWidth();
        height = Display.getDisplayMode().getHeight();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(fr, fg, fb, 0.0F);
        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        // Load Shader
        try {
            shader = new ShaderProgram("/vertex.glsl", "/fragment.glsl");
            System.out.println("Shaders loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to load shaders, falling back to fixed function");
            e.printStackTrace();
            shader = null;
        }

        // Setup texture unit
        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        // 初始化世界
        world = new World(256, 256, 64);
        worldRenderer = new WorldRenderer(world);
        player = new Player(world);

        // 初始化投影矩阵
        updateProjectionMatrix();
    }

    private void updateProjectionMatrix() {
        float aspect = (float) width / (float) height;
        projectionMatrix.setPerspective((float) Math.toRadians(70.0F), aspect, 0.05F, 1000.0F);
    }

    public void destroy() {
        if (shader != null) shader.cleanup();
        if (worldRenderer != null) worldRenderer.cleanup();
        if (world != null) world.save();

        Mouse.destroy();
        Keyboard.destroy();
        Display.destroy();
    }

    @Override
    public void run() {
        try {
            init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.toString(), "Failed to start VoxelGame", 0);
            System.exit(0);
            return;
        }

        long lastTime = System.currentTimeMillis();
        int frames = 0;

        try {
            while (!Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && !Display.isCloseRequested()) {

                timer.advanceTime();

                for (int i = 0; i < timer.ticks; i++) tick();

                render(timer.a);
                frames++;

                if (System.currentTimeMillis() >= lastTime + 1000L) {
                    System.out.println(frames + " fps, " + Chunk.updates);
                    Chunk.updates = 0;
                    lastTime += 1000L;
                    frames = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            destroy();
        }
    }

    public void tick() {
        player.tick();
    }

    private void moveCameraToPlayer(float a) {
        float x = player.prevPos.x + (player.pos.x - player.prevPos.x) * a;
        float y = player.prevPos.y + (player.pos.y - player.prevPos.y) * a;
        float z = player.prevPos.z + (player.pos.z - player.prevPos.z) * a;

        GL11.glTranslatef(0.0F, 0.0F, -0.3F);
        GL11.glRotatef(player.xRot, 1.0F, 0.0F, 0.0F);
        GL11.glRotatef(player.yRot, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-x, -y, -z);
    }

    private void setupCamera(float a) {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GLU.gluPerspective(70.0F, (float) width / height, 0.05F, 1000.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        moveCameraToPlayer(a);
    }

    // ===============================
    // ✅ 新 Picking 方式 (raycast)
    // ===============================
    public void render(float a) {

        float xo = Mouse.getDX();
        float yo = Mouse.getDY();

        player.turn(xo, yo);

        // 使用 raycast 代替旧 GL_SELECT
        hitResult = worldRenderer.pickRay(player, 5.0f);

        while (Mouse.next()) {

            if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState() && hitResult != null) {
                world.setBlock(hitResult.x, hitResult.y, hitResult.z, 0);
            }

            if (Mouse.getEventButton() == 0 && Mouse.getEventButtonState() && hitResult != null) {
                int x = hitResult.x;
                int y = hitResult.y;
                int z = hitResult.z;

                if (hitResult.f == 0) --y;
                if (hitResult.f == 1) ++y;
                if (hitResult.f == 2) --z;
                if (hitResult.f == 3) ++z;
                if (hitResult.f == 4) --x;
                if (hitResult.f == 5) ++x;

                world.setBlock(x, y, z, 1);
            }
        }

        while (Keyboard.next()) {
            if (Keyboard.getEventKey() == Keyboard.KEY_RETURN && Keyboard.getEventKeyState()) {
                world.save();
            }
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        setupCamera(a);

        if (shader != null) {
            shader.use();
            shader.setUniform("hasTexture", 1);
            shader.setUniform("hasColor", 1);
        }

        GL11.glEnable(GL11.GL_CULL_FACE);

        // 渲染世界
        worldRenderer.render(player, 0);
        worldRenderer.render(player, 1);

        // 渲染高亮方块
        if (hitResult != null) {
            worldRenderer.renderHit(hitResult);
        }

        if (shader != null) shader.stop();

        Display.update();
    }

    public static void main(String[] args) throws LWJGLException {
        new Thread(new VoxelGame()).start();
    }
}
