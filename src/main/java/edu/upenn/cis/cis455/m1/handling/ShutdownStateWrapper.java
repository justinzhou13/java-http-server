package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.m1.server.WebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

public class ShutdownStateWrapper {

    final static Logger logger = LogManager.getLogger(WebService.class);

    private static boolean shouldShutDown;
    private static ServerSocket serverSocket;

    public static boolean isShouldShutDown() {
        return shouldShutDown;
    }

    public static void setShouldShutDown(boolean shutDown) {
        shouldShutDown = shutDown;
        if (serverSocket != null) {
            synchronized (serverSocket) {
                try {
                    logger.info("Attempting to close serverSocket");
                    serverSocket.close();
                } catch (IOException e) {
                    logger.error("Error attempting to close server socket");
                }
            }
        }
    }

    public static void setServerSocket(ServerSocket socket) {
        serverSocket = socket;
    }
}
