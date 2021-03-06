package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.core.HttpRequest;
import edu.upenn.cis.cis455.m2.interfaces.Session;
import org.apache.logging.log4j.Level;

import java.math.BigInteger;

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

        threadPool(4);
        
        // All user routes should go below here...
        if (args.length == 2) {
        	int portNumber = Integer.parseInt(args[0]);
        	port(portNumber);
        	
        	String root = args[1];
        	staticFileLocation(root);
        }
        // ... and above here. Leave this comment for the Spark comparator tool
        before("/add-odds/:part1/:part2", ((request, response) -> {
            String userAgent = request.userAgent();
            if (userAgent == null || userAgent.isEmpty()) {
                throw new HaltException(403);
            }
        }));

        before("/add-odds/:part1/:part2", ((request, response) -> {
            request.attribute("wasArithmeticFunction", true);
        }));

        get("/add-odds/:part1/:part2", (request, response) -> {
            int part1 = Integer.parseInt(request.params(":part1"));
            int part2 = Integer.parseInt(request.params(":part2"));

            response.type("text/plain");
            return String.valueOf(part1 + part2);
        });

        get("/hello/:name", (request, response) -> {
            String name = request.params(":name");
            response.type("text/plain");
            return "Hello, " + name + "!";
        });

        get("/count-accesses", ((request, response) -> {
            Session session = request.session();
            int accessCount = 1;
            if (session.attribute("accesses") != null) {
                accessCount = (int) session.attribute("accesses") + 1;
            }
            session.attribute("accesses", accessCount);

            if (accessCount > 10) {
                response.redirect("/congratulations");
                return null;
            }

            response.type("text/plain");
            return String.format("You've attempted to get this page %s times.", accessCount);
        }));

        get("/congratulations", ((request, response) -> {
            response.type("text/plain");
            return "Congratulations! You've goten the page more than 10 times.";
        }));

        get("/factorial/:target", ((request, response) -> {
            BigInteger out = BigInteger.valueOf(1L);
            try {
                Integer target = Integer.parseInt(request.params(":target"));
                while (target > 0) {
                    out = out.multiply(BigInteger.valueOf(target));
                    target--;
                }
            } catch (NumberFormatException | NullPointerException e) {
                throw new HaltException(400);
            }

            response.type("text/plain");
            return out.toString();
        }));

        get("/*/splat/*", ((request, response) -> {
            StringBuilder res = new StringBuilder("Here were the splat params from this request: \n");
            for (String splatItem : ((HttpRequest) request).splat()) {
                res.append(splatItem).append("\n");
            }

            response.type("text/plain");
            return res;
        }));

        System.out.println("Waiting to handle requests!");
        awaitInitialization();
    }
}
