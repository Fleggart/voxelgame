package com.voxelgame;

public class Timer {
    private static final long NS_PER_SECOND = 1_000_000_000L;
    private static final long MAX_NS_PER_UPDATE = 1_000_000_000L;
    private static final int MAX_TICKS_PER_UPDATE = 100;
    
    private final float ticksPerSecond;
    private long lastTime;
    public int ticks;
    public float partialTick;
    public float timeScale = 1.0F;
    public float fps = 0.0F;
    public float passedTime = 0.0F;
    
    public Timer(float ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        this.lastTime = System.nanoTime();
    }
    
    public void advanceTime() {
        long now = System.nanoTime();
        long passedNs = now - lastTime;
        lastTime = now;
        
        if (passedNs < 0) {
            passedNs = 0;
        }
        if (passedNs > MAX_NS_PER_UPDATE) {
            passedNs = MAX_NS_PER_UPDATE;
        }
        
        fps = NS_PER_SECOND / (float) passedNs;
        passedTime += (float) passedNs * timeScale * ticksPerSecond / 1_000_000_000F;
        ticks = (int) passedTime;
        
        if (ticks > MAX_TICKS_PER_UPDATE) {
            ticks = MAX_TICKS_PER_UPDATE;
        }
        
        passedTime -= (float) ticks;
        partialTick = passedTime;
    }
}
