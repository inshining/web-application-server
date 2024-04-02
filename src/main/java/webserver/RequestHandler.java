package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        byte[] body;
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            Hashtable<String, String> header = extractClientHeader(br);
            DataOutputStream dos = new DataOutputStream(out);
            if (header.get("path") != null || !header.get("path").equals("")) {
                header.get("path");
                body = Files.readAllBytes(Paths.get("./webapp" + header.get("path")));
            } else{
                body = "Hello World".getBytes();
            }
            body = "Hello World".getBytes();
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private static Hashtable extractClientHeader(BufferedReader br) throws IOException {
        Hashtable<String, String> headers = new Hashtable<>();
        for (String line = br.readLine(); line != null && !line.equals(""); line = br.readLine()) {
            String path = getPath(line);
            if (!path.equals("")){
                headers.put("path", path) ;
            }
        }
        return headers;
    }

    private static String getPath(String line) {
        String [] tokens = line.split(" ");
        if (tokens[0].equals("GET") && tokens[1].startsWith("/")) {
            String path =  tokens[1];
            log.debug("request path : {}", path);
            return path;
        }
        return "";
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
