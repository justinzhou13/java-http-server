package edu.upenn.cis.cis455.m1.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.handling.ShutdownStateWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.m1.handling.RequestHandler;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.handling.HttpResponse;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

/**
 * Stub class for a thread worker that handles Web requests
 */
public class HttpWorker implements Runnable {
	
	static final Logger logger = LogManager.getLogger(HttpWorker.class);
	
	private final HttpTaskQueue taskQueue;
	private final RequestHandler requestHandler;
	private final ShutdownStateWrapper shutdownStateWrapper;
	private final Map<String, String> workerNameToStatuses;
	private String workerThreadName;
	
	public HttpWorker(HttpTaskQueue taskQueue,
	                  RequestHandler requestHandler,
	                  ShutdownStateWrapper shutdownStateWrapper,
	                  Map<String, String> workerNameToStatuses) {
		this.taskQueue = taskQueue;
		this.requestHandler = requestHandler;
		this.shutdownStateWrapper = shutdownStateWrapper;
		this.workerNameToStatuses = workerNameToStatuses;
	}

	public void setWorkerThreadName(String workerThreadName) {
		this.workerThreadName = workerThreadName;
	}

    @Override
    public void run() {
    	logger.debug("Running worker");
        while (!shutdownStateWrapper.isShouldShutDown()) {
			HttpTask httpTask = null;
        	try {
        		httpTask = readFromQueue();
        		if (httpTask != null) {
					process(httpTask);
		        }
        		
        	} catch (Exception e) {
        		logger.error(String.format("Error processing HttpTasks from queue: %s", e.getMessage()));
				if (httpTask != null) {
					try {
						httpTask.getSocket().close();
					} catch (IOException ex) {
						logger.error(ex.getStackTrace());
					}
				}
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
	
	private void process(HttpTask httpTask) {
		Socket socket = httpTask.getSocket();

		Request request = null;
		try {
			request = HttpIoHandler.parseRequest(socket);
		} catch (IOException e) {
			logger.error(e.toString());
			closeSocket(socket);
			return;
		}

		updateControlPanelStatus(request.url());
		Response response = new HttpResponse();

		try {
			requestHandler.applyRoutes(request, response);
			if (!HttpIoHandler.sendResponse(socket, request, response)) {
				closeSocket(socket);
			}
		} catch (HaltException e) {
			if (!HttpIoHandler.sendException(socket, request, e)) {
				closeSocket(socket);
			};
		}

		updateControlPanelStatus(WebService.WORKER_WAITING_LABEL);
	}

	private void updateControlPanelStatus(String status) {
		synchronized (workerNameToStatuses) {
			workerNameToStatuses.put(workerThreadName, status);
		}
	}

	private void closeSocket(Socket socket) {
		try {
			socket.close();
		} catch (IOException e) {
			logger.error(e.toString());
		}
	}
}
