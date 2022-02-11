package edu.upenn.cis.cis455.m1.handling;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.upenn.cis.cis455.exceptions.HaltException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.HttpRequestMethod;
import edu.upenn.cis.cis455.m1.interfaces.Request;
import edu.upenn.cis.cis455.m1.interfaces.Response;
import edu.upenn.cis.cis455.m1.interfaces.Route;
import spark.http.matching.Halt;

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

			//handling modified-since and unmodified-since headers
			BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
			FileTime fileTime = basicFileAttributes.lastModifiedTime();
			LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.of("GMT"));

			handleModifiedSinceIfNecessary(lastModifiedTime, req, res);
			handleUnmodifiedSinceIfNecessary(lastModifiedTime, req, res);
		} catch (InvalidPathException | IOException e) {
			logger.error(String.format("Could not get file for path %s", fileLocationString));
			throw new HaltException(404);
		}
		return null;
	}

	private void handleModifiedSinceIfNecessary(LocalDateTime lastModifiedTime, Request req, Response res) {
		LocalDateTime ifModifiedSinceTime = attemptParseModifiedSinceHeader(req.headers("if-modified-since"));
		if (ifModifiedSinceTime != null && lastModifiedTime.isBefore(ifModifiedSinceTime)) {
			throw new HaltException(304);
		}
	}

	private void handleUnmodifiedSinceIfNecessary(LocalDateTime lastModifiedTime, Request req, Response res) {
		LocalDateTime ifUnmodifiedSinceTime = attemptParseModifiedSinceHeader(req.headers("if-unmodified-since"));
		if (ifUnmodifiedSinceTime != null && lastModifiedTime.isAfter(ifUnmodifiedSinceTime)) {
			throw new HaltException(412);
		}
	}

	private LocalDateTime attemptParseModifiedSinceHeader(String ifModifiedSince) {
		if (ifModifiedSince == null) {
			return null;
		}

		List<DateTimeFormatter> dateTimeFormatterList = new ArrayList<>();
		dateTimeFormatterList.add(DateTimeFormatter
				.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
				.withZone(ZoneId.of("GMT")));
		dateTimeFormatterList.add(DateTimeFormatter
				.ofPattern("EEEE, dd-MMM-yy HH:mm:ss z", Locale.ENGLISH)
				.withZone(ZoneId.of("GMT")));
		dateTimeFormatterList.add(DateTimeFormatter
				.ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.ENGLISH)
				.withZone(ZoneId.of("GMT")));

		for (DateTimeFormatter formatter : dateTimeFormatterList) {
			try {
				return LocalDateTime.parse(ifModifiedSince, formatter);
			} catch (DateTimeParseException e) {
				logger.info(String.format("ifModifiedSince header %s could not be parsed by %s",
						ifModifiedSince,
						formatter.toString()));
			}
		}
		return null;
	}
}
