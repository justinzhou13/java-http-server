package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.handling.HttpIoHandler;
import edu.upenn.cis.cis455.m1.handling.HttpResponse;
import edu.upenn.cis.cis455.m1.interfaces.Response;
import org.apache.logging.log4j.Level;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

public class TestSendException {
    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }
    
    String sampleGetRequest = 
        "GET /a/b/hello.htm?q=x&v=12%200 HTTP/1.1\r\n" +
        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
        "Host: www.cis.upenn.edu\r\n" +
        "Accept-Language: en-us\r\n" +
        "Accept-Encoding: gzip, deflate\r\n" +
        "Cookie: name1=value1; name2=value2; name3=value3\r\n" +
        "Connection: Keep-Alive\r\n\r\n";

    String sampleNoncompliantRequest =
        "GET /a/b/hello.htm?q=x&v=12%200 HTTP/1.1\r\n" +
        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
        "Accept-Language: en-us\r\n" +
        "Accept-Encoding: gzip, deflate\r\n" +
        "Cookie: name1=value1; name2=value2; name3=value3\r\n" +
        "Connection: Keep-Alive\r\n\r\n";

    String sampleCompliantRequestNoHost =
        "GET /definitely/not/gonna/be/anything/here/a/b/hello.htm?q=x&v=12%200 HTTP/1.0\r\n" +
        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
        "Accept-Language: en-us\r\n" +
        "Accept-Encoding: gzip, deflate\r\n" +
        "Cookie: name1=value1; name2=value2; name3=value3\r\n" +
        "Connection: Keep-Alive\r\n\r\n";

    String samplePost =
        "POST /second.html HTTP/1.1\r\n" +
        "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
        "Host: www.cis.upenn.edu\r\n" +
        "Accept-Language: en-us\r\n" +
        "Accept-Encoding: gzip, deflate\r\n" +
        "Cookie: name1=value1; name2=value2; name3=value3\r\n" +
        "Connection: Keep-Alive\r\n\r\n";

    
    @Test
    public void testSendException() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
            sampleGetRequest, 
            byteArrayOutputStream);
        
        HaltException halt = new HaltException(404, "Not found");
        
        HttpIoHandler.sendException(s, null, halt);
        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        System.out.println(result);
        
        assertTrue(result.startsWith("HTTP/1.1 404"));
    }

    @Test
    public void testNoncompliantHostHeader() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleNoncompliantRequest,
                byteArrayOutputStream);

        HttpTask testTask = new HttpTask(s);
        HttpWorker testWorker = new HttpWorker(null, null);
        testWorker.process(testTask);

        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        System.out.println(result);

        assertTrue(result.startsWith("HTTP/1.1 400"));
    }

    @Test
    public void testCompliantHeaderNoHost() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleCompliantRequestNoHost,
                byteArrayOutputStream);

        HttpTask testTask = new HttpTask(s);
        HttpWorker testWorker = new HttpWorker(null, null);
        testWorker.process(testTask);

        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        System.out.println(result);

        assertTrue(result.startsWith("HTTP/1.1 404"));
    }

    @Test
    public void testUnimplementedHeader() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                samplePost,
                byteArrayOutputStream);

        HttpTask testTask = new HttpTask(s);
        HttpWorker testWorker = new HttpWorker(null, null);
        testWorker.process(testTask);

        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        System.out.println(result);

        assertTrue(result.startsWith("HTTP/1.1 501"));
    }

    @Test
    public void test200Response() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                samplePost,
                byteArrayOutputStream);

        Response response = new HttpResponse(new HashMap<>());
        HttpIoHandler.sendResponse(s, null, response);

        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        System.out.println(result);

        assertTrue(result.startsWith("HTTP/1.1 200"));
    }

    @Test
    public void testContainsDateHeader() throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                samplePost,
                byteArrayOutputStream);

        Response response = new HttpResponse(new HashMap<>());
        HttpIoHandler.sendResponse(s, null, response);

        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        System.out.println(result);

        assertTrue(result.contains("Date:"));
    }

    
    @After
    public void tearDown() {}
}
