package edu.upenn.cis.cis455.m1.server;

import java.util.ArrayList;
import java.util.List;

/**
 * Stub class for implementing the queue of HttpTasks
 */
public class HttpTaskQueue {
	
	List<HttpTask> taskQueue;

	public HttpTaskQueue() {
		taskQueue = new ArrayList<HttpTask>();
	}
	
	public int size() {
		return taskQueue.size();
	}

	public void add(HttpTask httpTask) {
		taskQueue.add(httpTask);
	}
	
	public HttpTask removeHead() {
		return taskQueue.remove(0);
	}

	public boolean isEmpty() {
		return taskQueue.isEmpty();
	}
}