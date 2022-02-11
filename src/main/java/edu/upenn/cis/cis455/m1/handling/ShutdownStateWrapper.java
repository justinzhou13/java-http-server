package edu.upenn.cis.cis455.m1.handling;

public class ShutdownStateWrapper {

    private static boolean shouldShutDown;

    public static boolean isShouldShutDown() {
        return shouldShutDown;
    }

    public static void setShouldShutDown(boolean shutDown) {
        shouldShutDown = shutDown;
    }
}
