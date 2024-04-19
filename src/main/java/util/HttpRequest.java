package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class HttpRequest {

    private static final Logger log = LoggerFactory.getLogger(HttpRequest.class);

    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, String> params;
    private Map<String, String> cookies;

    public HttpRequest(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line = br.readLine();
        if (line == null){
            throw new IOException("Request is empty");
        }
        headers = new HashMap<String,String>();
        params = new HashMap<String, String>();

        parseFirstLine(line);
        parseHeader(br);

        if (method.equals("POST")){
            String body = IOUtils.readData(br, Integer.parseInt(headers.get("Content-Length")));
            params = HttpRequestUtils.parseQueryString(body);
        }
    }

    private void parseHeader(BufferedReader br) throws IOException {
        String line = br.readLine();
        while (line != null && !line.equals("")){
            log.debug("header : {}", line);

            HttpRequestUtils.Pair pair = HttpRequestUtils.parseHeader(line);
            headers.put(pair.getKey(), pair.getValue());

            if (line.contains("Cookie")) {
                parseCookie(line);
            }
            line = br.readLine();
        }
    }

    private void parseFirstLine(String line){
        String[] tokens = line.split(" ");
        method = tokens[0];
        path = tokens[1];
        if (path.equals("/")){
            path = "/index.html";
        }

        if (path.contains("?")){
            String[] pathTokens = path.split("\\?");
            path = pathTokens[0];
            params = HttpRequestUtils.parseQueryString(pathTokens[1]);
        }

    }

    private void parseCookie(String line) {
        String[] headerTokens = line.split(":");
        cookies = HttpRequestUtils.parseCookies(headerTokens[1].trim());
        String value = cookies.get("logined");
        if (value == null) {
            cookies.replace("logined", "false");
        }
    }
    public boolean isLogined() {
        String value = cookies.get("logined");
        return Boolean.parseBoolean(value);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public String getParameter(String key) {
        return params.get(key);
    }
}