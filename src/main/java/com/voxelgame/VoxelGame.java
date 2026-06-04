package com.voxelgame;

import com.voxelgame.world.Chunk;
import com.voxelgame.world.World;
import com.voxelgame.world.WorldRenderer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import javax.swing.JOptionPane;
import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class VoxelGame implements Runnable {
    private int width = 1024;
    private int height = 768;
    private GameTimer timer = new GameTimer(60.0F);
    private World world;
    private WorldRenderer worldRenderer;
    private Player player;
    private HitResult hitResult = null;
    
    private long window;
    private boolean[] keys = new boolean[GLFW_KEY_LAST];
    private boolean[] mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST];
    private double lastMouseX = 0, lastMouseY = 0;
    private boolean firstMouse = true;
    
    // 着色器程序
    private int shaderProgram;
    private int vao;
    
    // 矩阵
    private FloatBuffer projectionMatrix;
    private FloatBuffer modelViewMatrix;
    
    // Uniform 位置
    private int projectionLoc;
    private int modelviewLoc;
    private int textureLoc;
    private int hasTextureLoc;
    private int hasColorLoc;

    public void init() throws LWJGLException, IOException {
        // 初始化 GLFW
        if (!glfwInit()) {
            throw new LWJGLException("Failed to initialize GLFW");
        }
        
        // Android 上使用简化的窗口配置
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        
        // 创建窗口
        window = glfwCreateWindow(width, height, "Voxel Game", NULL, NULL);
        if (window == NULL) {
            throw new LWJGLException("Failed to create GLFW window");
        }
        
        // 设置输入回调
        setupCallbacks();
        
        // 创建 OpenGL 上下文
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        
        glfwSwapInterval(1);
        glfwShowWindow(window);
        
        // OpenGL 设置
        glClearColor(0.5f, 0.8f, 1.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        
        // 创建 VAO
        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        
        // 初始化矩阵
        projectionMatrix = BufferUtils.createFloatBuffer(16);
        modelViewMatrix = BufferUtils.createFloatBuffer(16);
        
        // 加载着色器
        initShaders();
        
        this.world = new World(256, 256, 64);
        this.worldRenderer = new WorldRenderer(this.world);
        this.player = new Player(this.world);
        
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }
    
    private void initShaders() {
        // 顶点着色器
        String vertexShaderSource = 
            "#version 100\n" +
            "attribute vec3 in_position;\n" +
            "attribute vec2 in_texCoord;\n" +
            "attribute vec3 in_color;\n" +
            "uniform mat4 projection;\n" +
            "uniform mat4 modelview;\n" +
            "varying vec2 texCoord;\n" +
            "varying vec3 color;\n" +
            "void main() {\n" +
            "    gl_Position = projection * modelview * vec4(in_position, 1.0);\n" +
            "    texCoord = in_texCoord;\n" +
            "    color = in_color;\n" +
            "}\n";
        
        // 片段着色器
        String fragmentShaderSource = 
            "#version 100\n" +
            "precision mediump float;\n" +
            "uniform sampler2D texture_sampler;\n" +
            "uniform bool hasTexture;\n" +
            "uniform bool hasColor;\n" +
            "varying vec2 texCoord;\n" +
            "varying vec3 color;\n" +
            "void main() {\n" +
            "    if (hasTexture) {\n" +
            "        vec4 texColor = texture2D(texture_sampler, texCoord);\n" +
            "        if (hasColor) {\n" +
            "            gl_FragColor = texColor * vec4(color, 1.0);\n" +
            "        } else {\n" +
            "            gl_FragColor = texColor;\n" +
            "        }\n" +
            "    } else if (hasColor) {\n" +
            "        gl_FragColor = vec4(color, 1.0);\n" +
            "    } else {\n" +
            "        gl_FragColor = vec4(1.0, 1.0, 1.0, 1.0);\n" +
            "    }\n" +
            "}\n";
        
        // 编译顶点着色器
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        checkShaderError(vertexShader, "VERTEX");
        
        // 编译片段着色器
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        checkShaderError(fragmentShader, "FRAGMENT");
        
        // 链接程序
        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        
        glBindAttribLocation(shaderProgram, 0, "in_position");
        glBindAttribLocation(shaderProgram, 1, "in_texCoord");
        glBindAttribLocation(shaderProgram, 2, "in_color");
        
        glLinkProgram(shaderProgram);
        checkProgramError(shaderProgram);
        
        // 获取 uniform 位置
        projectionLoc = glGetUniformLocation(shaderProgram, "projection");
        modelviewLoc = glGetUniformLocation(shaderProgram, "modelview");
        textureLoc = glGetUniformLocation(shaderProgram, "texture_sampler");
        hasTextureLoc = glGetUniformLocation(shaderProgram, "hasTexture");
        hasColorLoc = glGetUniformLocation(shaderProgram, "hasColor");
        
        // 清理着色器对象
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
        
        System.out.println("Shaders loaded successfully");
    }
    
    private void checkShaderError(int shader, String type) {
        int[] status = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, status);
        if (status[0] == 0) {
            String log = glGetShaderInfoLog(shader);
            throw new RuntimeException(type + " shader compilation failed: " + log);
        }
    }
    
    private void checkProgramError(int program) {
        int[] status = new int[1];
        glGetProgramiv(program, GL_LINK_STATUS, status);
        if (status[0] == 0) {
            String log = glGetProgramInfoLog(program);
            throw new RuntimeException("Program linking failed: " + log);
        }
    }
    
    private void setupCallbacks() {
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS) {
                keys[key] = true;
                if (key == GLFW_KEY_ESCAPE) glfwSetWindowShouldClose(window, true);
                if (key == GLFW_KEY_ENTER) world.save();
                if (key == GLFW_KEY_R) player.resetPos();
            } else if (action == GLFW_RELEASE) {
                keys[key] = false;
            }
        });
        
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (action == GLFW_PRESS) {
                mouseButtons[button] = true;
            } else if (action == GLFW_RELEASE) {
                mouseButtons[button] = false;
            }
        });
        
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }
            float dx = (float)(xpos - lastMouseX);
            float dy = (float)(ypos - lastMouseY);
            lastMouseX = xpos;
            lastMouseY = ypos;
            player.turn(dx, dy);
        });
    }
    
    private void updateProjection() {
        projectionMatrix.clear();
        float fov = 70.0f;
        float aspect = (float)width / height;
        float near = 0.05f;
        float far = 1000.0f;
        
        float yScale = (float) (1.0f / Math.tan(Math.toRadians(fov / 2)));
        float xScale = yScale / aspect;
        float frustumLength = far - near;
        
        projectionMatrix.put(xScale).put(0).put(0).put(0);
        projectionMatrix.put(0).put(yScale).put(0).put(0);
        projectionMatrix.put(0).put(0).put(-((far + near) / frustumLength)).put(-1);
        projectionMatrix.put(0).put(0).put(-((2 * far * near) / frustumLength)).put(0);
        projectionMatrix.flip();
    }
    
    private void updateModelView(float a) {
        modelViewMatrix.clear();
        // 单位矩阵
        modelViewMatrix.put(1).put(0).put(0).put(0);
        modelViewMatrix.put(0).put(1).put(0).put(0);
        modelViewMatrix.put(0).put(0).put(1).put(0);
        modelViewMatrix.put(0).put(0).put(0).put(1);
        modelViewMatrix.flip();
    }

    public void destroy() {
        if (shaderProgram != 0) {
            glDeleteProgram(shaderProgram);
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao);
        }
        if (worldRenderer != null) {
            worldRenderer.cleanup();
        }
        this.world.save();
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    public void run() {
        try {
            this.init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.toString(), "Failed to start VoxelGame", 0);
            System.exit(0);
            return;
        }

        long lastTime = System.currentTimeMillis();
        int frames = 0;

        try {
            while (!glfwWindowShouldClose(window)) {
                this.timer.advanceTime();

                for (int i = 0; i < this.timer.ticks; ++i) {
                    this.tick();
                }

                this.render(this.timer.a);
                ++frames;

                while (System.currentTimeMillis() >= lastTime + 1000L) {
                    System.out.println(frames + " fps, " + Chunk.updates);
                    Chunk.updates = 0;
                    lastTime += 1000L;
                    frames = 0;
                }
                
                glfwPollEvents();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.destroy();
        }
    }

    public void tick() {
        float xa = 0, za = 0;
        if (keys[GLFW_KEY_W] || keys[GLFW_KEY_UP]) za--;
        if (keys[GLFW_KEY_S] || keys[GLFW_KEY_DOWN]) za++;
        if (keys[GLFW_KEY_A] || keys[GLFW_KEY_LEFT]) xa--;
        if (keys[GLFW_KEY_D] || keys[GLFW_KEY_RIGHT]) xa++;
        
        if (keys[GLFW_KEY_SPACE] && player.onGround) {
            player.yd = 0.12f;
        }
        
        player.xa = xa;
        player.za = za;
        player.tick();
    }
    
    private void handleMouseInput() {
        if (mouseButtons[GLFW_MOUSE_BUTTON_LEFT]) {
            if (hitResult != null) {
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
            mouseButtons[GLFW_MOUSE_BUTTON_LEFT] = false;
        }
        
        if (mouseButtons[GLFW_MOUSE_BUTTON_RIGHT]) {
            if (hitResult != null) {
                world.setBlock(hitResult.x, hitResult.y, hitResult.z, 0);
            }
            mouseButtons[GLFW_MOUSE_BUTTON_RIGHT] = false;
        }
    }
    
    private void pick(float a) {
        // Android 上禁用拾取功能
        this.hitResult = null;
    }

    public void render(float a) {
        this.pick(a);
        this.handleMouseInput();

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        
        updateProjection();
        updateModelView(a);
        
        glUseProgram(shaderProgram);
        
        glUniformMatrix4fv(projectionLoc, false, projectionMatrix);
        glUniformMatrix4fv(modelviewLoc, false, modelViewMatrix);
        glUniform1i(textureLoc, 0);
        glUniform1i(hasTextureLoc, 1);
        glUniform1i(hasColorLoc, 1);
        
        this.worldRenderer.render(this.player, 0);
        this.worldRenderer.render(this.player, 1);
        
        glUseProgram(0);
        
        glfwSwapBuffers(window);
    }

    public static void main(String[] args) {
        new Thread(new VoxelGame()).start();
    }
    
    public static class LWJGLException extends Exception {
        public LWJGLException(String message) {
            super(message);
        }
    }
}
