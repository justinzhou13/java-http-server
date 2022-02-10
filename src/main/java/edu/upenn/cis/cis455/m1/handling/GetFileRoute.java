package edu.upenn.cis.cis455.m1.handling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.HttpRequestMethod;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;
import edu.upenn.cis.cis455.m1.interfaces.Route;

public class GetFileRoute implements Route {
	
	static final Logger logger = LogManager.getLogger(GetFileRoute.class);
	
	private final String root;
	
	public GetFileRoute(String root) {
		this.root = root;
	}
	
	public Object handle(Request req, Response res) {
		if (!req.requestMethod().equals(HttpRequestMethod.GET.getValue())) {
			throw new IllegalArgumentException("GetFileHandler called without a get method");
		}
		
		String uri = req.uri();
		String fileLocationString;
		if (uri.charAt(uri.length() - 1) == '/') {
			logger.info("looking for an index.html file!");
			fileLocationString = String.format("%s/index.html", root);
		} else {
			logger.info("looking for another file");
			String fileName = uri.substring(uri.indexOf('/') + 1);
			fileLocationString = String.format("%s/%s", root, fileName);
		}
		
		File requestedFile = new File(fileLocationString);
		try {
			Path filePath = requestedFile.toPath();
			String contentType = Files.probeContentType(filePath);
			res.type(contentType);

			byte[] fileData = Files.readAllBytes(filePath);
			res.bodyRaw(fileData);

			successfullyProcessed(res);

			handleModifiedSinceIfNecessary(filePath, req, res);
		} catch (Exception e) {
			logger.error(e.getStackTrace());
			fileNotFound(res);
		}
		return null;
	}
	
	private void fileNotFound(Response res) {
		res.status(404);
	}
	
	private void successfullyProcessed(Response res) {
		res.status(200);
	}

	private void notModified(Response res) {
		res.status(304);
		res.type(null);
		res.bodyRaw(null);
	}

	private void handleModifiedSinceIfNecessary(Path filePath, Request req, Response res) {
		try {
			if (req.headers("if-modified-since") != null) {
				DateTimeFormatter httpDateFormatter = DateTimeFormatter
						.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
						.withZone(ZoneId.of("GMT"));
				LocalDateTime ifModifiedSinceTime = LocalDateTime.parse(req.headers("if-modified-since"), httpDateFormatter);

				BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
				FileTime fileTime = basicFileAttributes.lastModifiedTime();
				LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.of("GMT"));

				if (lastModifiedTime.isBefore(ifModifiedSinceTime)) {
					notModified(res);
				}
			}

		} catch (Exception e) {
			logger.error("Failed to read file attributes for file");
		}
	}
}
