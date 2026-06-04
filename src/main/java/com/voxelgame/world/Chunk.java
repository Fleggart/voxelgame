package com.voxelgame.world;

public class Chunk {
   private static final int TEXTURE = TextureManager.loadTexture("/textures/terrain.png", 9728);
   private static final MeshBuilder meshBuilder = new MeshBuilder();
   private static final int[] COORD_OFFSETS = {-1, 0, 1};
   
   private final World world;
   private final int x0, y0, z0, x1, y1, z1;
   private boolean dirty = true;
   private int displayList = -1;
   private static int rebuiltThisFrame = 0;
   public static int updates = 0;

   public Chunk(World world, int x0, int y0, int z0, int x1, int y1, int z1) {
      this.world = world;
      this.x0 = x0; this.y0 = y0; this.z0 = z0;
      this.x1 = x1; this.y1 = y1; this.z1 = z1;
      this.displayList = org.lwjgl.opengl.GL11.glGenLists(2);
   }

   private void rebuild(int layer) {
      if (rebuiltThisFrame == 2) return;
      
      dirty = false;
      updates++;
      rebuiltThisFrame++;
      
      org.lwjgl.opengl.GL11.glNewList(displayList + layer, 4864);
      org.lwjgl.opengl.GL11.glEnable(3553);
      org.lwjgl.opengl.GL11.glBindTexture(3553, TEXTURE);
      
      meshBuilder.init();
      
      for (int x = x0; x < x1; x++) {
         for (int y = y0; y < y1; y++) {
            for (int z = z0; z < z1; z++) {
               if (world.isBlock(x, y, z)) {
                  Block block = y == world.depth * 2 / 3 ? Block.ROCK : Block.GRASS;
                  block.render(meshBuilder, world, layer, x, y, z);
               }
            }
         }
      }
      
      meshBuilder.flush();
      org.lwjgl.opengl.GL11.glDisable(3553);
      org.lwjgl.opengl.GL11.glEndList();
   }

   public void render(int layer) {
      if (dirty) {
         rebuild(0);
         rebuild(1);
      }
      org.lwjgl.opengl.GL11.glCallList(displayList + layer);
   }

   public void setDirty() { dirty = true; }
}
