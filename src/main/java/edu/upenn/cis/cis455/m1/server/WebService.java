/**
 * CIS 455/555 route-based HTTP framework
 * 
 * V. Liu, Z. Ives
 * 
 * Portions excerpted from or inspired by Spark Framework, 
 * 
 *                 http://sparkjava.com,
 * 
 * with license notice included below.
 */

/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.upenn.cis.cis455.m1.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.handling.HandlerOrchestrator;

public class WebService {
    final static Logger logger = LogManager.getLogger(WebService.class);
    
    private static final int DEFAULT_QUEUE_SIZE = 256;

    protected HttpListener listener;
    protected List<Thread> workerThreadPool;
    
    private static final String DEFAULT_IP = "0.0.0.0";
    private static final int DEFAULT_PORT = 45555;
	private static final String DEFAULT_ROOT = "www";
	private static final int DEFAULT_POOL_SIZE = 1;
	
	private static String ip;
	private static int port;
	private static String root;
	private static int poolSize;
	
	public WebService() {
		ip = DEFAULT_IP;
		port = DEFAULT_PORT;
		root = DEFAULT_ROOT;
		poolSize = DEFAULT_POOL_SIZE;
	}

    /**
     * Launches the Web server thread pool and the listener
     * @throws Exception 
     */
    public void start() {
    	HttpTaskQueue taskQueue = new HttpTaskQueue();
    	
		try {
			listener = new HttpListener(ip, port, DEFAULT_QUEUE_SIZE, taskQueue);
			Thread listenerThread = new Thread(listener);
			listenerThread.start();
		} catch (IOException e) {
			logger.error(String.format("Error starting HttpListener: %s", e.getMessage()));
		}
		
		logger.debug("Spinning up worker pool");
		workerThreadPool = new ArrayList<>();
		HandlerOrchestrator handlerOrchestrator = new HandlerOrchestrator(root);
    	for (int i = 0; i < WebService.poolSize; i++) {
    		HttpWorker worker = new HttpWorker(taskQueue, handlerOrchestrator);
    		Thread workerThread = new Thread(worker);
    		workerThread.start();
    		workerThreadPool.add(workerThread);
    	}
    }

    /**
     * Gracefully shut down the server
     */
    public void stop() {}

    /**
     * Hold until the server is fully initialized.
     * Should be called after everything else.
     */
    public void awaitInitialization() {
        logger.info("Initializing server");
        try {
			start();
		} catch (Exception e) {
			logger.error(String.format("Error starting web server: %s", e.getMessage()));
		}
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public HaltException halt() {
        throw new HaltException();
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public HaltException halt(int statusCode) {
        throw new HaltException(statusCode);
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public HaltException halt(String body) {
        throw new HaltException(body);
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public HaltException halt(int statusCode, String body) {
        throw new HaltException(statusCode, body);
    }

    ////////////////////////////////////////////
    // Server configuration
    ////////////////////////////////////////////

    /**
     * Set the root directory of the "static web" files
     */
    public void staticFileLocation(String directory) {
    	WebService.root = directory;
    }

    /**
     * Set the IP address to listen on (default 0.0.0.0)
     */
    public void ipAddress(String ipAddress) {
    	WebService.ip = ipAddress;
    }

    /**
     * Set the TCP port to listen on (default 45555)
     */
    public void port(int port) {
    	WebService.port = port;
    }

    /**
     * Set the size of the thread pool
     */
    public void threadPool(int threads) {
    	WebService.poolSize = threads;
    }

}
