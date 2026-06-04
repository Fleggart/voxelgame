package com.voxelgame.world;

import com.voxelgame.physics.BoundingBox;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class World {
   public final int width;
   public final int height;
   public final int depth;
   private byte[] blocks;
   private int[] lightDepths;
   private ArrayList<WorldListener> worldListeners = new ArrayList();

   public World(int w, int h, int d) {
      this.width = w;
      this.height = h;
      this.depth = d;
      this.blocks = new byte[w * h * d];
      this.lightDepths = new int[w * h];

      for(int x = 0; x < w; ++x) {
         for(int y = 0; y < d; ++y) {
            for(int z = 0; z < h; ++z) {
               int i = (y * this.height + z) * this.width + x;
               this.blocks[i] = (byte)(y <= d * 2 / 3 ? 1 : 0);
            }
         }
      }

      this.calcLightDepths(0, 0, w, h);
      this.load();
   }

   public void load() {
      try {
         DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(new File("world.dat"))));
         dis.readFully(this.blocks);
         this.calcLightDepths(0, 0, this.width, this.height);
         for(int i = 0; i < this.worldListeners.size(); ++i) {
            ((WorldListener)this.worldListeners.get(i)).allChanged();
         }
         dis.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void save() {
      try {
         DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("world.dat"))));
         dos.write(this.blocks);
         dos.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void calcLightDepths(int x0, int y0, int x1, int y1) {
      for(int x = x0; x < x0 + x1; ++x) {
         for(int z = y0; z < y0 + y1; ++z) {
            int oldDepth = this.lightDepths[x + z * this.width];
            int y;
            for(y = this.depth - 1; y > 0 && !this.isLightBlocker(x, y, z); --y) {
            }
            this.lightDepths[x + z * this.width] = y;
            if (oldDepth != y) {
               int yl0 = oldDepth < y ? oldDepth : y;
               int yl1 = oldDepth > y ? oldDepth : y;
               for(int i = 0; i < this.worldListeners.size(); ++i) {
                  ((WorldListener)this.worldListeners.get(i)).lightColumnChanged(x, z, yl0, yl1);
               }
            }
         }
      }
   }

   public void addListener(WorldListener worldListener) {
      this.worldListeners.add(worldListener);
   }

   public void removeListener(WorldListener worldListener) {
      this.worldListeners.remove(worldListener);
   }

   public boolean isBlock(int x, int y, int z) {
      if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
         return this.blocks[(y * this.height + z) * this.width + x] == 1;
      } else {
         return false;
      }
   }

   public boolean isSolidBlock(int x, int y, int z) {
      return this.isBlock(x, y, z);
   }

   public boolean isLightBlocker(int x, int y, int z) {
      return this.isSolidBlock(x, y, z);
   }

   public ArrayList<BoundingBox> getCubes(BoundingBox aABB) {
      ArrayList<BoundingBox> aABBs = new ArrayList();
      int x0 = (int)aABB.x0;
      int x1 = (int)(aABB.x1 + 1.0F);
      int y0 = (int)aABB.y0;
      int y1 = (int)(aABB.y1 + 1.0F);
      int z0 = (int)aABB.z0;
      int z1 = (int)(aABB.z1 + 1.0F);
      if (x0 < 0) {
         x0 = 0;
      }
      if (y0 < 0) {
         y0 = 0;
      }
      if (z0 < 0) {
         z0 = 0;
      }
      if (x1 > this.width) {
         x1 = this.width;
      }
      if (y1 > this.depth) {
         y1 = this.depth;
      }
      if (z1 > this.height) {
         z1 = this.height;
      }
      for(int x = x0; x < x1; ++x) {
         for(int y = y0; y < y1; ++y) {
            for(int z = z0; z < z1; ++z) {
               if (this.isSolidBlock(x, y, z)) {
                  aABBs.add(new BoundingBox((float)x, (float)y, (float)z, (float)(x + 1), (float)(y + 1), (float)(z + 1)));
               }
            }
         }
      }
      return aABBs;
   }

   public float getBrightness(int x, int y, int z) {
      float dark = 0.8F;
      float light = 1.0F;
      if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
         return y < this.lightDepths[x + z * this.width] ? dark : light;
      } else {
         return light;
      }
   }

   public void setBlock(int x, int y, int z, int type) {
      if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
         this.blocks[(y * this.height + z) * this.width + x] = (byte)type;
         this.calcLightDepths(x, z, 1, 1);
         for(int i = 0; i < this.worldListeners.size(); ++i) {
            ((WorldListener)this.worldListeners.get(i)).blockChanged(x, y, z);
         }
      }
   }
}
