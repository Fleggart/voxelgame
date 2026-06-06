package com.voxelgame;

public class Timer {
    private static final long NS_PER_SECOND = 1000000000L;
    private static final long MAX_NS_PER_UPDATE = 1000000000L;
    private static final int MAX_TICKS_PER_UPDATE = 100;
    
    private float ticksPerSecond;
    private long lastTime;
    public int ticks;
    public float a;
    public float timeScale = 1.0f;
    public float fps = 0.0f;
    public float passedTime = 0.0f;
    
    public Timer(float ticksPerSecond) {
        this.ticksPerSecond = ticksPerSecond;
        this.lastTime = System.nanoTime();
    }
    
    public void advanceTime() {
        long now = System.nanoTime();
        long passedNs = now - lastTime;
        lastTime = now;
        
        if (passedNs < 0) passedNs = 0;
        if (passedNs > MAX_NS_PER_UPDATE) passedNs = MAX_NS_PER_UPDATE;
        
        fps = (float) (NS_PER_SECOND / passedNs);
        passedTime += (float) passedNs * timeScale * ticksPerSecond / 1.0e9f;
        ticks = (int) passedTime;
        if (ticks > MAX_TICKS_PER_UPDATE) ticks = MAX_TICKS_PER_UPDATE;
        
        passedTime -= ticks;
        a = passedTime;
    }
}
