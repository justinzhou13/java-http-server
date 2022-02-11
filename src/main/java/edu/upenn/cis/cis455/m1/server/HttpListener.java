package edu.upenn.cis.cis455.m1.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import edu.upenn.cis.cis455.m1.handling.ShutdownStateWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stub for your HTTP server, which listens on a ServerSocket and handles
 * requests
 */
public class HttpListener implements Runnable {
	
	final static Logger logger = LogManager.getLogger(HttpListener.class);
	
	private final static int DEFAULT_SOCKET_BACKLOG = 100;
	private ServerSocket serverSocket;
	
	private final int queueSize;
	private final HttpTaskQueue taskQueue;

	public HttpListener(String ip, int port, int queueSize, HttpTaskQueue taskQueue) throws IOException {
		if (queueSize <= 0) {
			throw new IllegalArgumentException("Queue size must be greater than 0");
		}
		
		InetAddress ipAddress = InetAddress.getByName(ip);
		this.serverSocket = new ServerSocket(port, DEFAULT_SOCKET_BACKLOG, ipAddress);
		
		this.queueSize = queueSize;
		this.taskQueue = taskQueue;
	}

    @Override
    public void run(){
        while (!ShutdownStateWrapper.isShouldShutDown()) {
        	try {
        		Socket socket = serverSocket.accept();
        		HttpTask httpTask = new HttpTask(socket);
        		addToQueue(httpTask);
        	} catch (Exception e) {
        		System.out.println(e.toString());
        	}
        }
		logger.info("Shutting down down http listener");
    }
    
    private void addToQueue(HttpTask httpTask) throws InterruptedException {
    	while (!ShutdownStateWrapper.isShouldShutDown()) {
			synchronized (taskQueue) {
				if (taskQueue.size() == queueSize) {
					// Synchronizing on the sharedQueue to make sure no more than one
					// thread is accessing the queue same time.
					logger.debug("Queue is full!");
					taskQueue.wait();
					// We use wait as a way to avoid polling the queue to see if
					// there was any space for the producer to push.
				} else {
					//Adding element to queue and notifying all waiting consumers
					taskQueue.add(httpTask);
					logger.debug("Notifying after add");//This would be logged in the log file created and to the console.
					taskQueue.notifyAll();
					break;
				}
			}
		}
    }
}
