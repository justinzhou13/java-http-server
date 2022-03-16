/**
 * CIS 455/555 route-based HTTP framework.
 * 
 * Basic service handler and route manager.
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

package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.core.WebSession;
import edu.upenn.cis.cis455.m2.interfaces.Filter;
import edu.upenn.cis.cis455.m2.interfaces.Route;
import edu.upenn.cis.cis455.m2.interfaces.Session;
import edu.upenn.cis.cis455.m2.server.WebService;
import edu.upenn.cis.cis455.m2.session.SessionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SparkController {

    final static Logger logger = LogManager.getLogger(SparkController.class);

    private static final WebService webService = new WebService();

    // We don't want people to use the constructor
    protected SparkController() {}

    /**
     * Milestone 2 only: Handle an HTTP GET request to the path
     */
    public static void get(String path, Route route) {
        webService.get(path, route);
    }

    /**
     * Milestone 2 only: Handle an HTTP POST request to the path
     */
    public static void post(String path, Route route) {
        webService.post(path, route);
    }

    /**
     * Milestone 2 only: Handle an HTTP PUT request to the path
     */
    public static void put(String path, Route route) {
        webService.put(path, route);
    }

    /**
     * Milestone 2 only: Handle an HTTP DELETE request to the path
     */
    public static void delete(String path, Route route) {
        webService.delete(path, route);
    }

    /**
     * Milestone 2 only: Handle an HTTP HEAD request to the path
     */
    public static void head(String path, Route route) {
        webService.head(path, route);
    }

    /**
     * Milestone 2 only: Handle an HTTP OPTIONS request to the path
     */
    public static void options(String path, Route route) {
        webService.options(path, route);
    }

    ///////////////////////////////////////////////////
    // HTTP request filtering
    ///////////////////////////////////////////////////

    /**
     * Milestone 2 only: Add filters that get called before a request
     */
    public static void before(Filter... filters) {
        if (filters != null) {
            for (Filter filter : filters) {
                webService.before(filter);
            }
        }
    }

    /**
     * Milestone 2 only: Add filters that get called after a request
     */
    public static void after(Filter... filters) {
        if (filters != null) {
            for (Filter filter : filters) {
                webService.after(filter);
            }
        }
    }

    /**
     * Milestone 2 only: Add filters that get called before a request
     */
    public static void before(String path, Filter... filters) {
        if (filters != null) {
            for (Filter filter : filters) {
                webService.before(path, filter);
            }
        }
    }

    public static void before(String path1, String path2, Filter... filters) {
            if (filters != null) {
                for (Filter filter : filters) {
                    webService.before(path1, filter);
                    webService.before(path2, filter);
                }
            }
    }

    /**
     * Milestone 2 only: Add filters that get called after a request
     */
    public static void after(String path, Filter... filters) {
        if (filters != null) {
            for (Filter filter : filters) {
                webService.after(path, filter);
            }
        }
    }
    
    
    // The following 2 functions are OPTIONAL for Milestone 1.
    // They will be used in Milestone 2 so user code can fail
    // and we will gracefully return something.

    /**
     * Triggers a HaltException that terminates the request
     */
    public static HaltException halt() {
        return webService.halt();
    }

    /**
     * Triggers a HaltException that terminates the request
     */
    public static HaltException halt(int statusCode, String body) {
        return webService.halt(statusCode, body);
    }

    ////////////////////////////////////////////
    // Server configuration
    ////////////////////////////////////////////

    /**
     * Set the IP address to listen on (default 0.0.0.0)
     */
    public static void ipAddress(String ipAddress) {
        if (ipAddress != null) {
            webService.ipAddress(ipAddress);
        } else {
            logger.error("Null ip address passed to webservice");
        }
    }

    /**
     * Set the port to listen on (default 45555)
     */
    public static void port(int port) {
        webService.port(port);
    }

    /**
     * Set the size of the thread pool
     */
    public static void threadPool(int threads) {
        if (threads > 0) {
            webService.threadPool(threads);
        } else {
            logger.error("Threadpool size <= 1 passed to webservice");
        }
    }

    /**
     * Set the root directory of the "static web" files
     */
    public static void staticFileLocation(String directory) {
         if (directory != null) {
             webService.staticFileLocation(directory);
         } else {
             logger.error("Null directory passed to webservice");
         }
    }

    /**
     * Hold until the server is fully initialized
     */
    public static void awaitInitialization() {
        webService.awaitInitialization();
    }

    /**
     * Gracefully shut down the server
     */
    public static void stop() {
        webService.stop();
    }

    public static String createSession() {
        return new WebSession().id();
    }

    public static Session getSession(String id) {
        return SessionManager.getSession(id);
    }
}
