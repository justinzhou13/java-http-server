package edu.upenn.cis.cis455.m2.routehandling;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m2.interfaces.Request;
import edu.upenn.cis.cis455.m2.interfaces.Response;
import edu.upenn.cis.cis455.m2.interfaces.Route;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

public class GetFileRoute implements Route {

    static final Logger logger = LogManager.getLogger(GetFileRoute.class);

    private String root = "./www";

    public void setRoot(String root) {
        synchronized (this.root) {
            this.root = root;
        }
    }

    public String getRoot() {
        return root;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        if (!request.requestMethod().equals("GET") && !request.requestMethod().equals("HEAD")) {
            throw new HaltException(500);
        }

        String pathInfo = request.pathInfo();
        String fileLocationString;
        synchronized (this.root) {
            if (pathInfo.charAt(pathInfo.length() - 1) == '/') {
                logger.info("looking for an index.html file!");
                fileLocationString = String.format("%s/index.html", root);
            } else {
                logger.info("looking for another file");
                String fileName = pathInfo.substring(pathInfo.indexOf('/') + 1);
                fileLocationString = String.format("%s/%s", root, fileName);
            }
        }

        populateResponseWithFile(fileLocationString, root, request, response);

        return null;
    }

    public static void populateResponseWithFile(String fileLocationString, String root, Request request, Response response) {
        File requestedFile = new File(fileLocationString);
        try {
            Path filePath = requestedFile.toPath();

            checkFileIsInRootDirectory(requestedFile, root);

            String contentType = Files.probeContentType(filePath);
            response.type(contentType);
            byte[] fileData = Files.readAllBytes(filePath);
            response.bodyRaw(fileData);

            //handling modified-since and unmodified-since headers
            BasicFileAttributes basicFileAttributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            FileTime fileTime = basicFileAttributes.lastModifiedTime();
            LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.of("GMT"));

            handleModifiedSinceIfNecessary(lastModifiedTime, request);
            handleUnmodifiedSinceIfNecessary(lastModifiedTime, request);
        } catch (InvalidPathException | IOException e) {
            logger.error(String.format("Could not get file for path %s", fileLocationString));
            throw new HaltException(404);
        }
    }

    private static void handleModifiedSinceIfNecessary(LocalDateTime lastModifiedTime, Request req) {
        LocalDateTime ifModifiedSinceTime = attemptParseModifiedSinceHeader(req.headers("if-modified-since"));
        if (ifModifiedSinceTime != null && lastModifiedTime.isBefore(ifModifiedSinceTime)) {
            throw new HaltException(304);
        }
    }

    private static void handleUnmodifiedSinceIfNecessary(LocalDateTime lastModifiedTime, Request req) {
        LocalDateTime ifUnmodifiedSinceTime = attemptParseModifiedSinceHeader(req.headers("if-unmodified-since"));
        if (ifUnmodifiedSinceTime != null && lastModifiedTime.isAfter(ifUnmodifiedSinceTime)) {
            throw new HaltException(412);
        }
    }

    private static LocalDateTime attemptParseModifiedSinceHeader(String ifModifiedSince) {
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

    private static void checkFileIsInRootDirectory(File requestedFile, String root) {
        File rootDirectory = new File(root);
        String rootDirectoryURI = rootDirectory.toURI().normalize().toString();
        String requestedFileURI = requestedFile.toURI().normalize().toString();
        if (!requestedFileURI.startsWith(rootDirectoryURI)) {
            logger.info("User attempted to request a file not in the root directory");
            throw new HaltException(403);
        }
    }
}
