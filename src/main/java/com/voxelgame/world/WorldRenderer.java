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

    /** 用射线替代过时 picking */
    public HitResult pickRay(Player player, float reach) {
        float step = 0.1f;
        BoundingBox bb = player.bb.copy();
        float dx = - (float) Math.sin(Math.toRadians(player.yRot)) * (float) Math.cos(Math.toRadians(player.xRot));
        float dy = (float) Math.sin(Math.toRadians(player.xRot));
        float dz = (float) Math.cos(Math.toRadians(player.yRot)) * (float) Math.cos(Math.toRadians(player.xRot));

        for (float t = 0; t < reach; t += step) {
            int x = (int) (player.pos.x + dx * t);
            int y = (int) (player.pos.y + dy * t);
            int z = (int) (player.pos.z + dz * t);

            if (world.isSolidBlock(x, y, z)) {
                int face = 0; // 简单版本，可扩展
                return new HitResult(x, y, z, face, 0);
            }
        }
        return null;
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
