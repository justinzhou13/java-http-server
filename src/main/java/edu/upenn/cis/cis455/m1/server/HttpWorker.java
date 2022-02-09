package edu.upenn.cis.cis455.m1.server;

import java.io.IOException;
import java.net.Socket;

import edu.upenn.cis.cis455.m1.handling.ShutdownStateWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.m1.handling.RouteOrchestrator;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.handling.HttpResponse;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

/**
 * Stub class for a thread worker that handles Web requests
 */
public class HttpWorker implements Runnable {
	
	static final Logger logger = LogManager.getLogger(HttpWorker.class);
	
	private HttpTaskQueue taskQueue;
	private RouteOrchestrator routeOrchestrator;
	private ShutdownStateWrapper shutdownStateWrapper;
	
	public HttpWorker(HttpTaskQueue taskQueue, RouteOrchestrator routeOrchestrator, ShutdownStateWrapper shutdownStateWrapper) {
		this.taskQueue = taskQueue;
		this.routeOrchestrator = routeOrchestrator;
		this.shutdownStateWrapper = shutdownStateWrapper;
	}

    @Override
    public void run() {
    	logger.debug("Running worker");
        while (!shutdownStateWrapper.isShouldShutDown()) {
        	try {
        		HttpTask httpTask = readFromQueue();
        		if (httpTask != null) {
					process(httpTask);
		        }
        		
        	} catch (Exception e) {
        		logger.error(String.format("Error processing HttpTasks from queue: %s", e.getMessage()));
        	}
        }
		logger.info("Shutting down worker");
		return;
    }
    
	private HttpTask readFromQueue() throws InterruptedException {
		logger.debug("Reading from queue");
		while (!shutdownStateWrapper.isShouldShutDown()) {
			synchronized (taskQueue) {
				if (taskQueue.isEmpty()) {
					//If the queue is empty, we push the current thread to waiting state. Way to avoid polling.
					logger.debug("Queue is currently empty");
					taskQueue.wait();
				} else {
					HttpTask httpTask = taskQueue.removeHead();
					logger.debug("Notifying everyone we are removing an item");
					taskQueue.notifyAll();
					logger.debug("Exiting queue with return");
					return httpTask;
				}
			}
		}
		return null;
	}
	
	private void process(HttpTask httpTask) throws IOException {
		Socket socket = httpTask.getSocket();
		
		Request request = HttpIoHandler.parseRequest(socket);
		Response response = new HttpResponse();
		routeOrchestrator.applyRoutes(request, response);
		
		if (!HttpIoHandler.sendResponse(socket, request, response)) {
			socket.close();
		}
	}
    
}
