package edu.upenn.cis.cis455.m1.handling;

import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;

import static edu.upenn.cis.cis455.m1.handling.HttpComplianceHandler.isCompliant;

public class HandlerOrchestrator {

	private GetFileHandler getFileHandler;

	public HandlerOrchestrator(String root) {
		this.getFileHandler = new GetFileHandler(root);
	}

	public void handle(Request req, Response res) {
		if (isCompliant(req, res)) {
			handleFileRequest(req, res);
		}
	}

	public void handleFileRequest(Request req, Response res) {
		getFileHandler.handle(req, res);
	}
}
