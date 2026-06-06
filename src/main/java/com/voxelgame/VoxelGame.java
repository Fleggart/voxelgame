package com.voxelgame;

import com.voxelgame.level.Level;
import com.voxelgame.level.LevelRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class VoxelGame implements Runnable {
    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final String TITLE = "Voxel Game";
    
    private long window;
    private Timer timer;
    private Level level;
    private LevelRenderer levelRenderer;
    private Player player;
    private HitResult hitResult = null;
    
    private float lastMouseX = WIDTH / 2f;
    private float lastMouseY = HEIGHT / 2f;
    private boolean firstMouse = true;
    
    // 雾的颜色
    private FloatBuffer fogColor;
    
    public void init() {
        // 初始化 GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // 配置窗口
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        
        // 创建窗口
        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        
        // 鼠标输入回调
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            if (firstMouse) {
                lastMouseX = (float) xpos;
                lastMouseY = (float) ypos;
                firstMouse = false;
            }
            
            float dx = (float) xpos - lastMouseX;
            float dy = (float) ypos - lastMouseY;
            lastMouseX = (float) xpos;
            lastMouseY = (float) ypos;
            
            if (player != null) {
                player.turn(dx, dy);
            }
        });
        
        // 鼠标按钮回调
        glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
            if (hitResult != null && action == GLFW_PRESS) {
                if (button == GLFW_MOUSE_BUTTON_RIGHT) {
                    // 右键添加方块
                    int x = hitResult.x;
                    int y = hitResult.y;
                    int z = hitResult.z;
                    int f = hitResult.f;
                    
                    if (f == 0) y--;
                    if (f == 1) y++;
                    if (f == 2) z--;
                    if (f == 3) z++;
                    if (f == 4) x--;
                    if (f == 5) x++;
                    
                    level.setTile(x, y, z, 1);
                } else if (button == GLFW_MOUSE_BUTTON_LEFT) {
                    // 左键删除方块
                    level.setTile(hitResult.x, hitResult.y, hitResult.z, 0);
                }
            }
        });
        
        // 键盘回调
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
                level.save();
                System.out.println("Game saved!");
            }
            if (key == GLFW_KEY_R && action == GLFW_PRESS) {
                player.resetPos();
                System.out.println("Position reset!");
            }
        });
        
        // 创建 OpenGL 上下文
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        
        // 启用 VSync
        glfwSwapInterval(1);
        
        // 显示窗口
        glfwShowWindow(window);
        
        // 锁定鼠标到窗口
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        
        // 初始化雾颜色
        int col = 920330;
        fogColor = BufferUtils.createFloatBuffer(4);
        fogColor.put(new float[]{
            (col >> 16 & 255) / 255.0f,
            (col >> 8 & 255) / 255.0f,
            (col & 255) / 255.0f,
            1.0f
        });
        fogColor.flip();
        
        // OpenGL 初始化
        float fr = 0.5f;
        float fg = 0.8f;
        float fb = 1.0f;
        
        glEnable(GL_TEXTURE_2D);
        glShadeModel(GL_SMOOTH);
        glClearColor(fr, fg, fb, 0.0f);
        glClearDepth(1.0);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        
        // 初始化世界
        level = new Level(256, 256, 64);
        levelRenderer = new LevelRenderer(level);
        player = new Player(level);
        
        timer = new Timer(60.0f);
    }
    
    public void destroy() {
        if (level != null) {
            level.save();
        }
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    
    @Override
    public void run() {
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        
        long lastTime = System.currentTimeMillis();
        int frames = 0;
        
        while (!glfwWindowShouldClose(window)) {
            timer.advanceTime();
            
            for (int i = 0; i < timer.ticks; i++) {
                tick();
            }
            
            render(timer.a);
            frames++;
            
            if (System.currentTimeMillis() >= lastTime + 1000) {
                System.out.println(frames + " fps, " + level.chunkUpdates);
                level.chunkUpdates = 0;
                lastTime += 1000;
                frames = 0;
            }
            
            glfwPollEvents();
        }
        
        destroy();
    }
    
    public void tick() {
        // 获取键盘输入
        boolean forward = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS || 
                         glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS;
        boolean back = glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS || 
                      glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS;
        boolean left = glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS || 
                      glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS;
        boolean right = glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS || 
                       glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS;
        boolean jump = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
        
        player.tick(forward, back, left, right, jump);
    }
    
    private void moveCameraToPlayer(float a) {
        glTranslatef(0.0f, 0.0f, -0.3f);
        glRotatef(player.xRot, 1.0f, 0.0f, 0.0f);
        glRotatef(player.yRot, 0.0f, 1.0f, 0.0f);
        
        float x = player.xo + (player.x - player.xo) * a;
        float y = player.yo + (player.y - player.yo) * a;
        float z = player.zo + (player.z - player.zo) * a;
        glTranslatef(-x, -y, -z);
    }
    
    private void setupCamera(float a) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        
        float aspect = (float) WIDTH / HEIGHT;
        float fov = 70.0f;
        float nearPlane = 0.05f;
        float farPlane = 1000.0f;
        
        // 手动构建透视投影矩阵
        float yScale = (float) (1.0 / Math.tan(Math.toRadians(fov / 2.0)));
        float xScale = yScale / aspect;
        float frustumLength = farPlane - nearPlane;
        
        // 创建投影矩阵
        float[] projection = new float[16];
        projection[0] = xScale;
        projection[5] = yScale;
        projection[10] = -((farPlane + nearPlane) / frustumLength);
        projection[11] = -1.0f;
        projection[14] = -((2.0f * nearPlane * farPlane) / frustumLength);
        projection[15] = 0.0f;
        
        glMultMatrixf(projection);
        
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        moveCameraToPlayer(a);
    }
    
    private void pick(float a) {
        // 射线检测拾取方块
        float maxDist = 5.0f;
        float step = 0.05f;
        
        // 获取玩家视线方向
        float yaw = (float) Math.toRadians(player.yRot);
        float pitch = (float) Math.toRadians(player.xRot);
        
        float dirX = (float) (Math.cos(pitch) * Math.sin(yaw));
        float dirY = (float) (-Math.sin(pitch));
        float dirZ = (float) (Math.cos(pitch) * Math.cos(yaw));
        
        float lastX = player.x;
        float lastY = player.y - 1.62f;
        float lastZ = player.z;
        
        for (float dist = 0; dist < maxDist; dist += step) {
            float rayX = player.x + dirX * dist;
            float rayY = (player.y - 1.62f) + dirY * dist;
            float rayZ = player.z + dirZ * dist;
            
            int ix = (int) Math.floor(rayX);
            int iy = (int) Math.floor(rayY);
            int iz = (int) Math.floor(rayZ);
            
            if (ix >= 0 && iy >= 0 && iz >= 0 && 
                ix < level.width && iy < level.depth && iz < level.height) {
                if (level.isSolidTile(ix, iy, iz)) {
                    // 计算命中的面
                    int face = getHitFace(lastX, lastY, lastZ, rayX, rayY, rayZ, ix, iy, iz);
                    hitResult = new HitResult(ix, iy, iz, 0, face);
                    return;
                }
            }
            
            lastX = rayX;
            lastY = rayY;
            lastZ = rayZ;
        }
        hitResult = null;
    }
    
    private int getHitFace(float lastX, float lastY, float lastZ, 
                           float rayX, float rayY, float rayZ,
                           int bx, int by, int bz) {
        // 检测从哪个面进入方块
        float dx = rayX - lastX;
        float dy = rayY - lastY;
        float dz = rayZ - lastZ;
        
        // 计算进入点相对于方块中心的位置
        float hitX = rayX - (bx + 0.5f);
        float hitY = rayY - (by + 0.5f);
        float hitZ = rayZ - (bz + 0.5f);
        
        float absX = Math.abs(hitX);
        float absY = Math.abs(hitY);
        float absZ = Math.abs(hitZ);
        
        // 根据法线方向决定面
        if (absX > absY && absX > absZ) {
            return dx > 0 ? 5 : 4;
        } else if (absY > absX && absY > absZ) {
            return dy > 0 ? 1 : 0;
        } else {
            return dz > 0 ? 3 : 2;
        }
    }
    
    public void render(float a) {
        pick(a);
        
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        setupCamera(a);
        
        glEnable(GL_CULL_FACE);
        glEnable(GL_FOG);
        glFogi(GL_FOG_MODE, GL_LINEAR);
        glFogf(GL_FOG_START, 0.2f);
        glFogf(GL_FOG_END, 0.8f);
        glFogfv(GL_FOG_COLOR, fogColor);
        
        // 渲染背光面（无雾）
        glDisable(GL_FOG);
        levelRenderer.render(player, 0);
        
        // 渲染向光面（有雾）
        glEnable(GL_FOG);
        levelRenderer.render(player, 1);
        
        // 渲染高亮轮廓
        glDisable(GL_TEXTURE_2D);
        if (hitResult != null) {
            levelRenderer.renderHit(hitResult);
        }
        
        glDisable(GL_FOG);
        
        glfwSwapBuffers(window);
    }
    
    public static void main(String[] args) {
        VoxelGame game = new VoxelGame();
        Thread gameThread = new Thread(game);
        gameThread.start();
    }
}
