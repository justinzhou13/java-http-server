package edu.upenn.cis.cis455.m1.handling;

public class ShutdownStateWrapper {

    private boolean shouldShutDown;

    public ShutdownStateWrapper() {
        this.shouldShutDown = false;
    }

    public boolean isShouldShutDown() {
        return shouldShutDown;
    }

    public void setShouldShutDown(boolean shouldShutDown) {
        this.shouldShutDown = shouldShutDown;
    }
}
