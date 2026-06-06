package com.voxelgame.world;

import com.voxelgame.HitResult;
import com.voxelgame.Player;
import com.voxelgame.physics.BoundingBox;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class WorldRenderer implements WorldListener {
    private static final int CHUNK_SIZE = 16;
    private final World world;
    private final Chunk[] chunks;
    private final int xChunks, yChunks, zChunks;
    private final FloatBuffer tempBuffer = BufferUtils.createFloatBuffer(10000);

    public WorldRenderer(World world) {
        this.world = world;
        world.addListener(this);

        this.xChunks = world.width / CHUNK_SIZE;
        this.yChunks = world.depth / CHUNK_SIZE;
        this.zChunks = world.height / CHUNK_SIZE;
        this.chunks = new Chunk[xChunks * yChunks * zChunks];

        for (int x = 0; x < xChunks; x++) {
            for (int y = 0; y < yChunks; y++) {
                for (int z = 0; z < zChunks; z++) {
                    int x0 = x * CHUNK_SIZE;
                    int y0 = y * CHUNK_SIZE;
                    int z0 = z * CHUNK_SIZE;
                    int x1 = Math.min((x + 1) * CHUNK_SIZE, world.width);
                    int y1 = Math.min((y + 1) * CHUNK_SIZE, world.depth);
                    int z1 = Math.min((z + 1) * CHUNK_SIZE, world.height);
                    chunks[(x + y * xChunks) * zChunks + z] = new Chunk(world, x0, y0, z0, x1, y1, z1);
                }
            }
        }
    }

    public void render(Player player, int layer) {
        Chunk.resetRebuiltThisFrame();
        Frustum frustum = Frustum.getFrustum();
        for (Chunk chunk : chunks) {
            if (frustum.cubeInFrustum(chunk.getBoundingBox())) {
                chunk.render(layer);
            }
        }
    }

    /**
     * 射线与方块的交点检测
     * @return HitResult 包含击中的方块坐标和击中的面
     */
    public HitResult pickRay(Player player, float reach) {
        // 射线起点
        float ox = player.pos.x;
        float oy = player.pos.y;
        float oz = player.pos.z;
        
        // 射线方向（单位向量）
        float dx = (float) (-Math.sin(Math.toRadians(player.yRot)) * Math.cos(Math.toRadians(player.xRot)));
        float dy = (float) Math.sin(Math.toRadians(player.xRot));
        float dz = (float) (Math.cos(Math.toRadians(player.yRot)) * Math.cos(Math.toRadians(player.xRot)));
        
        // 步长
        float step = 0.05f;
        
        for (float t = 0; t < reach; t += step) {
            float px = ox + dx * t;
            float py = oy + dy * t;
            float pz = oz + dz * t;
            
            int x = (int) Math.floor(px);
            int y = (int) Math.floor(py);
            int z = (int) Math.floor(pz);
            
            if (world.isSolidBlock(x, y, z)) {
                // 计算击中的面，传入射线参数
                int face = getHitFace(ox, oy, oz, dx, dy, dz, x, y, z);
                return new HitResult(x, y, z, 0, face);
            }
        }
        
        return null;
    }
    
    /**
     * 计算射线击中立方体的哪个面
     * 通过比较射线与6个平面的交点来确定
     */
    private int getHitFace(float ox, float oy, float oz, 
                           float dx, float dy, float dz,
                           int x, int y, int z) {
        float minDist = Float.MAX_VALUE;
        int hitFace = 0;
        
        // 检查 Y- 面 (y = y)
        if (dy != 0) {
            float t = (y - oy) / dy;
            if (t > 0) {
                float ix = ox + dx * t;
                float iz = oz + dz * t;
                if (ix >= x && ix <= x + 1 && iz >= z && iz <= z + 1) {
                    float dist = (float) Math.sqrt(Math.pow(ix - ox, 2) + Math.pow(y - oy, 2) + Math.pow(iz - oz, 2));
                    if (dist < minDist) {
                        minDist = dist;
                        hitFace = 0;
                    }
                }
            }
        }
        
        // 检查 Y+ 面 (y = y + 1)
        if (dy != 0) {
            float t = (y + 1 - oy) / dy;
            if (t > 0) {
                float ix = ox + dx * t;
                float iz = oz + dz * t;
                if (ix >= x && ix <= x + 1 && iz >= z && iz <= z + 1) {
                    float dist = (float) Math.sqrt(Math.pow(ix - ox, 2) + Math.pow(y + 1 - oy, 2) + Math.pow(iz - oz, 2));
                    if (dist < minDist) {
                        minDist = dist;
                        hitFace = 1;
                    }
                }
            }
        }
        
        // 检查 Z- 面 (z = z)
        if (dz != 0) {
            float t = (z - oz) / dz;
            if (t > 0) {
                float ix = ox + dx * t;
                float iy = oy + dy * t;
                if (ix >= x && ix <= x + 1 && iy >= y && iy <= y + 1) {
                    float dist = (float) Math.sqrt(Math.pow(ix - ox, 2) + Math.pow(iy - oy, 2) + Math.pow(z - oz, 2));
                    if (dist < minDist) {
                        minDist = dist;
                        hitFace = 2;
                    }
                }
            }
        }
        
        // 检查 Z+ 面 (z = z + 1)
        if (dz != 0) {
            float t = (z + 1 - oz) / dz;
            if (t > 0) {
                float ix = ox + dx * t;
                float iy = oy + dy * t;
                if (ix >= x && ix <= x + 1 && iy >= y && iy <= y + 1) {
                    float dist = (float) Math.sqrt(Math.pow(ix - ox, 2) + Math.pow(iy - oy, 2) + Math.pow(z + 1 - oz, 2));
                    if (dist < minDist) {
                        minDist = dist;
                        hitFace = 3;
                    }
                }
            }
        }
        
        // 检查 X- 面 (x = x)
        if (dx != 0) {
            float t = (x - ox) / dx;
            if (t > 0) {
                float iy = oy + dy * t;
                float iz = oz + dz * t;
                if (iy >= y && iy <= y + 1 && iz >= z && iz <= z + 1) {
                    float dist = (float) Math.sqrt(Math.pow(x - ox, 2) + Math.pow(iy - oy, 2) + Math.pow(iz - oz, 2));
                    if (dist < minDist) {
                        minDist = dist;
                        hitFace = 4;
                    }
                }
            }
        }
        
        // 检查 X+ 面 (x = x + 1)
        if (dx != 0) {
            float t = (x + 1 - ox) / dx;
            if (t > 0) {
                float iy = oy + dy * t;
                float iz = oz + dz * t;
                if (iy >= y && iy <= y + 1 && iz >= z && iz <= z + 1) {
                    float dist = (float) Math.sqrt(Math.pow(x + 1 - ox, 2) + Math.pow(iy - oy, 2) + Math.pow(iz - oz, 2));
                    if (dist < minDist) {
                        minDist = dist;
                        hitFace = 5;
                    }
                }
            }
        }
        
        return hitFace;
    }

    public void renderHit(HitResult h) {
        if (h == null) return;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4f(1f, 1f, 1f, (float) Math.sin(System.currentTimeMillis() / 100.0) * 0.2f + 0.4f);
        GL11.glBegin(GL11.GL_QUADS);
        Block.STONE.renderFaceImmediate(h.x, h.y, h.z, h.f);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void setDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
        int cx0 = Math.max(0, x0 / CHUNK_SIZE);
        int cx1 = Math.min(xChunks - 1, x1 / CHUNK_SIZE);
        int cy0 = Math.max(0, y0 / CHUNK_SIZE);
        int cy1 = Math.min(yChunks - 1, y1 / CHUNK_SIZE);
        int cz0 = Math.max(0, z0 / CHUNK_SIZE);
        int cz1 = Math.min(zChunks - 1, z1 / CHUNK_SIZE);

        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cy = cy0; cy <= cy1; cy++) {
                for (int cz = cz0; cz <= cz1; cz++) {
                    chunks[(cx + cy * xChunks) * zChunks + cz].setDirty();
                }
            }
        }
    }

    public void cleanup() {
        for (Chunk chunk : chunks) {
            if (chunk != null) chunk.cleanup();
        }
    }

    @Override
    public void blockChanged(int x, int y, int z) {
        setDirty(x - 1, y - 1, z - 1, x + 1, y + 1, z + 1);
    }

    @Override
    public void lightColumnChanged(int x, int z, int y0, int y1) {
        setDirty(x - 1, y0 - 1, z - 1, x + 1, y1 + 1, z + 1);
    }

    @Override
    public void allChanged() {
        setDirty(0, 0, 0, world.width, world.depth, world.height);
    }
}
