package util;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static junit.framework.TestCase.assertEquals;

public class HttpRequestTest {
    private String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws IOException {
        InputStream in = new FileInputStream(testDirectory + "Http_GET.txt");
        HttpRequest request = new HttpRequest(in);

        assertEquals("GET", request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javaUser", request.getParameter("userId"));
    }

    @Test
    public void request_POST() throws Exception{
        InputStream in = new FileInputStream(testDirectory + "Http_POST.txt");
        HttpRequest request = new HttpRequest(in);

        assertEquals("POST", request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javaUser", request.getParameter("userId"));
    }

}
