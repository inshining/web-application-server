package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    DataOutputStream dos;
    Map<String, String> headers;
    public HttpResponse(OutputStream outputStream) {
         dos = new DataOutputStream(outputStream);
         headers = new HashMap<>();
    }

    public void forward(String url) {
        String contentType = "text/html";
        try {
            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
            if (url.endsWith(".css")) {
                contentType = "text/css";
            }
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            if (headers.size() > 0){
                responseHeader();
            }
            responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void forwardBody(byte[] body){
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            if (headers.size() > 0){
                responseHeader();
            }
            responseBody(body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void sendRedirect(String redirectUrl) throws IOException {
        try {
            dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
            if (headers.size() > 0){
                responseHeader();
            }
            dos.writeBytes("Location: " + redirectUrl + "\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseHeader() {
        try {
            for (String key : headers.keySet()) {
                dos.writeBytes(key + ": " + headers.get(key) + "\r\n");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.writeBytes("\r\n");
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
