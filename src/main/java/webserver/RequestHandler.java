package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import db.DataBase;
import javafx.util.Pair;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;
    private DataBase db;

    private static final String DEFAULT_PATH = "/index.html";
    private static final String CREATE_USER_PATH = "/user/create";
    private static final String LOGIN_PATH = "/user/login";
    private static final String LOGIN_FAIL_PATH = "/user/login_failed.html";

    public RequestHandler(Socket connectionSocket, DataBase db) {
        this.connection = connectionSocket;
        this.db = db;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());
        byte[] body;
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            Map<String, String> header = extractClientContext(br);
            DataOutputStream dos = new DataOutputStream(out);
            body = "Hello World".getBytes();

            if (header.get("method").equals("GET")){
                if (header.get("path") != null || !header.get("path").equals("")) {
                    log.debug("request path : {}", header.get("path"));
                    body = Files.readAllBytes(Paths.get("./webapp" + header.get("path")));
                }
                response200Header(dos, body.length);
                responseBody(dos, body);
            } else if (header.get("method").equals("POST")) {
                Pair<String, String> responseInfo = handlePostRequest(header);
                String path = responseInfo.getKey();
                String isLogin = responseInfo.getValue();
                response302Header(dos, path, isLogin);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    private static Hashtable<String, String> extractClientContext(BufferedReader br) throws IOException {
        HashSet<String> methods = new HashSet<>(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        Hashtable<String, String> headers = new Hashtable<>();
        String [] firstLine = br.readLine().split(" ");
        headers.put("method", firstLine[0]);
        headers.put("path", firstLine[1]);
        headers.put("protocol", firstLine[2]);
        String params = getParams(firstLine[1]);
        headers.put("params", params);

        for (String line = br.readLine(); line != null && !line.equals(""); line = br.readLine()) {
            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
            headers.put(pair.getKey(), pair.getValue());
        }
        if (headers.get("method").equals("POST")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            headers.put("body",IOUtils.readData(br, contentLength));
        }
        log.debug("request headers : {}", headers);
        return headers;
    }

    public static String getParams(String url) {
        if (url.contains("?")) {
            String params = url.substring(url.indexOf("?") + 1);
            log.debug("request params : {}", params);
            return params;
        }
        return "";
    }

    private User storeUser(Map<String, String> parameters) {
        User user = new User(parameters.get("userId"), parameters.get("password"), parameters.get("name"), parameters.get("email"));
        log.debug("User : {}", user);
        return user;
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

    private void response302Header(DataOutputStream dos, String location, String isLogin) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            dos.writeBytes("Set-Cookie: logined="+isLogin+ "\r\n");
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
    private Pair<String, String> handlePostRequest(Map<String, String> header) {
        Map<String, String> parameters = HttpRequestUtils.parseQueryString(header.get("body"));
        String responsePath = DEFAULT_PATH;
        String url = header.get("path");
        String isLogin = "false";
        if (url.equals(CREATE_USER_PATH)) {
            User user = storeUser(parameters);

            db.addUser(user);
        }else if (url.equals(LOGIN_PATH)){
            User user = db.findUserById(parameters.get("userId"));
            if (user != null && user.getPassword().equals(parameters.get("password"))) {
                log.debug("Login Success!");
                isLogin = "true";
            } else{
                log.debug("Login Fail!");
                responsePath = LOGIN_FAIL_PATH;
            }
        }
        return new Pair<>(responsePath, isLogin);
    }
}
