package com.voxelgame;

import com.voxelgame.level.*;
import com.voxelgame.player.Player;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class VoxelGame implements Runnable {
    private long window;
    private int width = 1024;
    private int height = 768;
    
    private final FloatBuffer fogColor = FloatBuffer.allocate(4);
    private Timer timer;
    private Level level;
    private LevelRenderer levelRenderer;
    private Player player;
    private final IntBuffer viewportBuffer = IntBuffer.allocate(16);
    private final IntBuffer selectBuffer = IntBuffer.allocate(2000);
    private HitResult hitResult = null;
    
    private boolean running = true;
    
    public static void main(String[] args) {
        new Thread(new VoxelGame()).start();
    }
    
    public void init() throws IOException {
        // 初始化 GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // 配置 GLFW
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        
        // 创建窗口
        window = glfwCreateWindow(width, height, "Voxel Game", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }
        
        // 设置键盘回调
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(win, true);
            }
        });
        
        // 设置鼠标输入
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        
        // 创建 OpenGL 上下文
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        
        // 启用垂直同步
        glfwSwapInterval(1);
        
        // 显示窗口
        glfwShowWindow(window);
        
        // 设置背景颜色
        int col = 920330;
        float fr = 0.5F;
        float fg = 0.8F;
        float fb = 1.0F;
        
        fogColor.put(new float[]{
            (col >> 16 & 0xFF) / 255.0F,
            (col >> 8 & 0xFF) / 255.0F,
            (col & 0xFF) / 255.0F,
            1.0F
        });
        fogColor.flip();
        
        // OpenGL 初始化
        glEnable(GL_TEXTURE_2D);
        glShadeModel(GL_SMOOTH);
        glClearColor(fr, fg, fb, 0.0F);
        glClearDepth(1.0);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        
        // 初始化游戏组件
        timer = new Timer(60.0F);
        level = new Level(256, 256, 64);
        levelRenderer = new LevelRenderer(level);
        player = new Player(level);
    }
    
    public void destroy() {
        level.save();
        if (window != NULL) {
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        }
        glfwTerminate();
    }
    
    @Override
    public void run() {
        try {
            init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.toString(), "Failed to start Voxel Game", 0);
            System.exit(0);
        }
        
        long lastTime = System.currentTimeMillis();
        int frames = 0;
        
        try {
            while (running && !glfwWindowShouldClose(window)) {
                timer.advanceTime();
                
                for (int i = 0; i < timer.ticks; i++) {
                    tick();
                }
                
                render(timer.partialTick);
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
        
        // 处理鼠标输入
        double[] mouseX = new double[1];
        double[] mouseY = new double[1];
        glfwGetCursorPos(window, mouseX, mouseY);
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        float dx = (float)(mouseX[0] - centerX);
        float dy = (float)(mouseY[0] - centerY);
        
        player.turn(dx, dy);
        
        // 重置鼠标位置到中心
        glfwSetCursorPos(window, centerX, centerY);
    }
    
    private void moveCameraToPlayer(float partialTick) {
        glTranslatef(0.0F, 0.0F, -0.3F);
        glRotatef(player.xRot, 1.0F, 0.0F, 0.0F);
        glRotatef(player.yRot, 0.0F, 1.0F, 0.0F);
        
        float x = player.xo + (player.x - player.xo) * partialTick;
        float y = player.yo + (player.y - player.yo) * partialTick;
        float z = player.zo + (player.z - player.zo) * partialTick;
        
        glTranslatef(-x, -y, -z);
    }
    
    private void setupCamera(float partialTick) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        
        // 透视投影
        float aspect = (float) width / (float) height;
        float fov = 70.0F;
        float nearPlane = 0.05F;
        float farPlane = 1000.0F;
        
        float top = (float) Math.tan(Math.toRadians(fov / 2)) * nearPlane;
        float bottom = -top;
        float right = top * aspect;
        float left = -right;
        
        glFrustum(left, right, bottom, top, nearPlane, farPlane);
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        moveCameraToPlayer(partialTick);
    }
    
    private void setupPickCamera(float partialTick, int x, int y) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        
        viewportBuffer.clear();
        glGetIntegerv(GL_VIEWPORT, viewportBuffer);
        viewportBuffer.flip();
        
        // 选择模式下的特殊投影
        float aspect = (float) width / (float) height;
        float top = (float) Math.tan(Math.toRadians(35.0)) * 0.05F;
        float bottom = -top;
        float right = top * aspect;
        float left = -right;
        glFrustum(left, right, bottom, top, 0.05F, 1000.0F);
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        moveCameraToPlayer(partialTick);
    }
    
    private void pick(float partialTick) {
        selectBuffer.clear();
        
        // 保存当前状态
        glRenderMode(GL_SELECT);
        
        setupPickCamera(partialTick, width / 2, height / 2);
        
        // 检测点击的方块
        levelRenderer.pick(player);
        
        int hits = glRenderMode(GL_RENDER);
        
        selectBuffer.flip();
        
        if (hits > 0) {
            // 读取选择结果
            int nameCount = selectBuffer.get();
            long minZ = (long) selectBuffer.get();
            selectBuffer.get(); // 跳过 maxZ
            
            if (nameCount >= 5) {
                int x = selectBuffer.get();
                int y = selectBuffer.get();
                int z = selectBuffer.get();
                int side = selectBuffer.get();
                int face = selectBuffer.get();
                hitResult = new HitResult(x, y, z, side, face);
            } else {
                hitResult = null;
            }
        } else {
            hitResult = null;
        }
    }
    
    public void render(float partialTick) {
        // 处理鼠标点击前先进行拾取检测
        pick(partialTick);
        
        // 处理鼠标点击
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS) {
            if (hitResult != null) {
                level.setTile(hitResult.x, hitResult.y, hitResult.z, 0);
            }
        }
        
        if (glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_RIGHT) == GLFW_PRESS) {
            if (hitResult != null) {
                int x = hitResult.x;
                int y = hitResult.y;
                int z = hitResult.z;
                
                // 根据面方向添加方块
                switch (hitResult.face) {
                    case 0 -> y--;
                    case 1 -> y++;
                    case 2 -> z--;
                    case 3 -> z++;
                    case 4 -> x--;
                    case 5 -> x++;
                }
                level.setTile(x, y, z, 1);
            }
        }
        
        // 处理键盘输入 - 保存世界
        if (glfwGetKey(window, GLFW_KEY_ENTER) == GLFW_PRESS) {
            level.save();
        }
        
        // 渲染
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        setupCamera(partialTick);
        
        glEnable(GL_CULL_FACE);
        glEnable(GL_FOG);
        glFogi(GL_FOG_MODE, GL_LINEAR);
        glFogf(GL_FOG_START, 0.2F);
        glFogf(GL_FOG_END, 0.5F);
        glFogfv(GL_FOG_COLOR, fogColor);
        
        // 渲染不透明物体
        glDisable(GL_FOG);
        levelRenderer.render(player, 0);
        
        // 渲染半透明物体
        glEnable(GL_FOG);
        levelRenderer.render(player, 1);
        
        glDisable(GL_TEXTURE_2D);
        
        if (hitResult != null) {
            levelRenderer.renderHit(hitResult);
        }
        
        glDisable(GL_FOG);
        
        glfwSwapBuffers(window);
        glfwPollEvents();
    }
    
    public static void checkError() {
        int e = glGetError();
        if (e != GL_NO_ERROR) {
            throw new IllegalStateException("OpenGL Error: " + e);
        }
    }
}
