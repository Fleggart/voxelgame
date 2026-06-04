package com.voxelgame.world;

import com.voxelgame.HitResult;
import com.voxelgame.Player;
import com.voxelgame.physics.BoundingBox;
import org.lwjgl.opengl.GL11;

public class WorldRenderer implements WorldListener {
    private static final int CHUNK_SIZE = 16;
    private World world;
    private Chunk[] chunks;
    private int xChunks, yChunks, zChunks;
    private MeshBuilder t = new MeshBuilder();

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

    public void pick(Player player) {
        float r = 3.0F;
        BoundingBox box = player.bb.grow(r, r, r);
        int x0 = (int)box.x0;
        int x1 = (int)(box.x1 + 1.0F);
        int y0 = (int)box.y0;
        int y1 = (int)(box.y1 + 1.0F);
        int z0 = (int)box.z0;
        int z1 = (int)(box.z1 + 1.0F);
        
        GL11.glInitNames();
        for (int x = x0; x < x1; x++) {
            GL11.glPushName(x);
            for (int y = y0; y < y1; y++) {
                GL11.glPushName(y);
                for (int z = z0; z < z1; z++) {
                    GL11.glPushName(z);
                    if (world.isSolidBlock(x, y, z)) {
                        for (int i = 0; i < 6; i++) {
                            GL11.glPushName(i);
                            t.init();
                            Block.ROCK.renderFace(t, x, y, z, i);
                            t.finish();
                            
                            // 使用顶点数组方式绘制拾取
                            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                            java.nio.FloatBuffer vertexData = t.getVertexBuffer();
                            if (vertexData != null && vertexData.remaining() > 0) {
                                GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexData);
                                GL11.glDrawArrays(GL11.GL_QUADS, 0, t.getVertexCount());
                            }
                            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                            
                            GL11.glPopName();
                        }
                    }
                    GL11.glPopName();
                }
                GL11.glPopName();
            }
            GL11.glPopName();
        }
    }

    public void renderHit(HitResult h) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, (float)Math.sin(System.currentTimeMillis() / 100.0) * 0.2F + 0.4F);
        
        t.init();
        Block.ROCK.renderFace(t, h.x, h.y, h.z, h.f);
        t.finish();
        
        // 渲染高亮方块
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        java.nio.FloatBuffer vertexData = t.getVertexBuffer();
        if (vertexData != null && vertexData.remaining() > 0) {
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, vertexData);
            GL11.glDrawArrays(GL11.GL_QUADS, 0, t.getVertexCount());
        }
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        
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
            if (chunk != null) {
                chunk.cleanup();
            }
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
