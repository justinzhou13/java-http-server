package edu.upenn.cis.cis455.m1.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.m1.handling.HandlerOrchestrator;
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
	private HandlerOrchestrator handlerOrchestrator;
	
	public HttpWorker(HttpTaskQueue taskQueue, HandlerOrchestrator handlerOrchestrator) {
		this.taskQueue = taskQueue;
		this.handlerOrchestrator = handlerOrchestrator;
	}

    @Override
    public void run() {
    	logger.debug("Running worker");
        while (true) {
        	try {
        		HttpTask httpTask = readFromQueue();
        		process(httpTask);
        		
        	} catch (Exception e) {
        		logger.error(String.format("Error processing HttpTasks from queue: %s", e.getMessage()));
        	}
        }
    }
    
	private HttpTask readFromQueue() throws InterruptedException {
		logger.debug("Reading from queue");
		while (true) {
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
	}
	
	private void process(HttpTask httpTask) throws IOException {
		Socket socket = httpTask.getSocket();
		
		Request request = HttpIoHandler.parseRequest(socket);
		Response response = new HttpResponse();
		handlerOrchestrator.handle(request, response);
		
		if (!HttpIoHandler.sendResponse(socket, request, response)) {
			socket.close();
		}
		//PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		//out.println("HTTP/1.1 200 OK\r\nContent-Length: 40\r\n\r\n<html><body>Hello world!</body></html>\n");
		//socket.close();
	}
    
}
