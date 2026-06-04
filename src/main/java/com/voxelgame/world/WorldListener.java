package com.voxelgame.world;

public interface WorldListener {
   void blockChanged(int x, int y, int z);
   void lightColumnChanged(int x, int z, int y0, int y1);
   void allChanged();
}
