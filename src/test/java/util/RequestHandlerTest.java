package util;

import org.junit.Before;
import org.junit.Test;
import webserver.RequestHandler;
import static org.junit.Assert.*;


import java.net.Socket;

public class RequestHandlerTest {
    private RequestHandler requestHandler;

    @Before
    public void setUp() {
        // Given
        requestHandler = new RequestHandler(new Socket());
        // When
        // Then
    }

    @Test
    public void parseParams() {
        // Given
        String line = "GET /user/create?userId=javajigi&password=password&name=JaeSung HTTP/1.1";
        // When
        String result = requestHandler.getParams(line);
        // Then
        assertEquals(result, "userId=javajigi&password=password&name=JaeSung");
    }
    @Test
    public void parseParamsNothing() {
        // Given
        String line = "GET /user/create.html";
        // When
        String result = requestHandler.getParams(line);
        // Then
        assertEquals(result, "");
    }
}
