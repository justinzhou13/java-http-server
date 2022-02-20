package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.handling.RequestHandler;
import edu.upenn.cis.cis455.m1.handling.ShutdownStateWrapper;
import edu.upenn.cis.cis455.m2.core.HttpResponse;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub class for a thread worker that handles Web requests
 */
public class HttpWorker implements Runnable {
	
	static final Logger logger = LogManager.getLogger(HttpWorker.class);
	
	private final HttpTaskQueue taskQueue;
	private final Map<String, String> workerNameToStatuses;
	private String workerThreadName = "";
	
	public HttpWorker(HttpTaskQueue taskQueue,
	                  Map<String, String> workerNameToStatuses) {
		this.taskQueue = taskQueue;
		this.workerNameToStatuses = workerNameToStatuses;
	}

	public void setWorkerThreadName(String workerThreadName) {
		this.workerThreadName = workerThreadName;
	}

    @Override
    public void run() {
    	logger.debug("Running worker");
		boolean shouldShutDown = false;
        while (!shouldShutDown) {
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
			synchronized (ShutdownStateWrapper.class) {
				shouldShutDown = ShutdownStateWrapper.isShouldShutDown();
			}
        }
		logger.info("Shutting down worker");
    }
    
	private HttpTask readFromQueue() throws InterruptedException {
		logger.debug("Reading from queue");
		boolean shouldShutDown = false;
		while (!shouldShutDown) {
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
			synchronized (ShutdownStateWrapper.class) {
				shouldShutDown = ShutdownStateWrapper.isShouldShutDown();
			}
		}
		return null;
	}
	
	public void process(HttpTask httpTask) {
		Socket socket = httpTask.getSocket();

		Request request = null;
		try {
			request = HttpIoHandler.parseRequest(socket);
		} catch (IOException e) {
			logger.error(e.toString());
			closeSocket(socket);
			return;
		} catch (HaltException e) {
			if (!HttpIoHandler.sendException(socket, request, e)) {
				closeSocket(socket);
			};
			return;
		}

		updateControlPanelStatus(request.url());
		Response response = new HttpResponse(new HashMap<>());

		try {
			RequestHandler.applyRoutes(request, response);
			if (!HttpIoHandler.sendResponse(socket, request, response)) {
				closeSocket(socket);
			}
		} catch (HaltException e) {
			if (!HttpIoHandler.sendException(socket, request, e)) {
				closeSocket(socket);
			};
		} catch (Exception e) {
			logger.error("Exception occured while processing request: ", e);
			HttpIoHandler.sendException(socket, request, new HaltException(500));
			closeSocket(socket);
		}

		updateControlPanelStatus(WebService.WORKER_WAITING_LABEL);
	}

	private void updateControlPanelStatus(String status) {
		try {
			synchronized (workerNameToStatuses) {
				workerNameToStatuses.put(workerThreadName, status);
			}
		} catch (Exception e) {
			logger.error("Error updating control panel status");
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
