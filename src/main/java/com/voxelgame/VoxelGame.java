package com.voxelgame;

import com.voxelgame.world.Chunk;
import com.voxelgame.world.World;
import com.voxelgame.world.WorldRenderer;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

public class VoxelGame implements Runnable {

    private static final boolean FULLSCREEN_MODE = false;

    private int width;
    private int height;

    private FloatBuffer fogColor;
    private GameTimer timer = new GameTimer(60.0F);

    private World world;
    private WorldRenderer worldRenderer;
    private Player player;

    private IntBuffer viewportBuffer;
    private IntBuffer selectBuffer;

    private HitResult hitResult = null;

    public void init() throws LWJGLException, IOException {

        System.out.println("INIT A");

        Display.setDisplayMode(new DisplayMode(1280, 720));
        Display.create();

        System.out.println("INIT C");

        GL11.glViewport(0, 0, 1280, 720);

        fogColor = BufferUtils.createFloatBuffer(4);
        viewportBuffer = BufferUtils.createIntBuffer(16);
        selectBuffer = BufferUtils.createIntBuffer(2000);

        float fr = 0.5F;
        float fg = 0.8F;
        float fb = 1.0F;

        fogColor.put(new float[]{fr, fg, fb, 1.0F});
        fogColor.flip();

        width = Display.getDisplayMode().getWidth();
        height = Display.getDisplayMode().getHeight();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glClearColor(fr, fg, fb, 0.0F);

        GL11.glClearDepth(1.0D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        world = new World(256, 256, 64);
        worldRenderer = new WorldRenderer(world);
        player = new Player(world);

        Mouse.setGrabbed(true);

        System.out.println("INIT DONE");
    }

    public void destroy() {
        try {
            world.save();
        } catch (Exception ignored) {}

        try { Mouse.destroy(); } catch (Exception ignored) {}
        try { Keyboard.destroy(); } catch (Exception ignored) {}
        try { Display.destroy(); } catch (Exception ignored) {}
    }

    public void run() {
        try {
            System.out.println("STEP 1: init start");
            init();
            System.out.println("STEP 2: init success");

        } catch (Exception e) {
            System.err.println("INIT FAILED");
            e.printStackTrace();
            return;
        }

        long lastTime = System.currentTimeMillis();
        int frames = 0;

        System.out.println("STEP 3: entering loop");

        try {
            while (!Display.isCloseRequested() && !Keyboard.isKeyDown(1)) {

                timer.advanceTime();

                for (int i = 0; i < timer.ticks; i++) {
                    tick();
                }

                render(timer.a);
                frames++;

                while (System.currentTimeMillis() >= lastTime + 1000L) {
                    System.out.println(frames + " fps, " + Chunk.updates);
                    Chunk.updates = 0;
                    lastTime += 1000L;
                    frames = 0;
                }
            }
        } catch (Exception e) {
            System.err.println("GAME CRASH");
            e.printStackTrace();
        } finally {
            destroy();
        }
    }

    public void tick() {
        player.tick();
    }

    private void moveCameraToPlayer(float a) {
        GL11.glTranslatef(0, 0, -0.3F);
        GL11.glRotatef(player.xRot, 1, 0, 0);
        GL11.glRotatef(player.yRot, 0, 1, 0);

        float x = player.xo + (player.x - player.xo) * a;
        float y = player.yo + (player.y - player.yo) * a;
        float z = player.zo + (player.z - player.zo) * a;

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

    public void render(float a) {

        float dx = Mouse.getDX();
        float dy = Mouse.getDY();
        player.turn(dx, dy);

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        setupCamera(a);

        worldRenderer.render(player, 0);
        worldRenderer.render(player, 1);

        if (hitResult != null) {
            worldRenderer.renderHit(hitResult);
        }

        Display.update();
    }

    public static void main(String[] args) {
        System.out.println("MAIN START");

        VoxelGame game = new VoxelGame();

        System.out.println("MAIN AFTER CONSTRUCTOR");

        game.run();
    }
}
