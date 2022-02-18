package edu.upenn.cis.cis455;

import org.apache.logging.log4j.Level;

import static edu.upenn.cis.cis455.SparkController.*;

/**
 * Initialization / skeleton class.
 * Note that this should set up a basic web server for Milestone 1.
 * For Milestone 2 you can use this to set up a basic server.
 * 
 * CAUTION - ASSUME WE WILL REPLACE THIS WHEN WE TEST MILESTONE 2,
 * SO ALL OF YOUR METHODS SHOULD USE THE STANDARD INTERFACES.
 * 
 * @author zives
 *
 */
public class WebServer {
	
    public static void main(String[] args) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);

        // TODO: make sure you parse *BOTH* command line arguments properly
        
        // All user routes should go below here...
        if (args.length == 2) {
        	int portNumber = Integer.parseInt(args[0]);
        	port(portNumber);
        	
        	String root = args[1];
        	staticFileLocation(root);
        }
        // ... and above here. Leave this comment for the Spark comparator tool

        get("/add/:part1/:part2", (request, response) -> {
            int part1 = Integer.parseInt(request.params(":part1"));
            int part2 = Integer.parseInt(request.params(":part2"));

            response.type("text/plain");
            return String.valueOf(part1 + part2);
        });

        System.out.println("Waiting to handle requests!");
        awaitInitialization();
    }
}
