package webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;

import model.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HttpRequest;
import db.DataBase;
import util.HttpResponse;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            HttpRequest request = new HttpRequest(in);
            HttpResponse response = new HttpResponse(out);
            boolean logined = request.isLogined();

            String url = request.getPath();
            if ("/user/create".equals(url)) {
                User user = new User(request.getParameter("userId"), request.getParameter("password"), request.getParameter("name"),
                        request.getParameter("email"));
                log.debug("user : {}", user);
                DataBase.addUser(user);
                response.sendRedirect("/index.html");
            } else if ("/user/login".equals(url)) {
                User user = DataBase.findUserById(request.getParameter("userId"));
                if (user != null) {
                    if (user.login(request.getParameter("password"))) {
                        response.addHeader("Set-Cookie", "logined=true");
                        response.sendRedirect("/index.html");
                    } else {
                        response.sendRedirect("/user/login_failed.html");
                    }
                } else {
                    response.sendRedirect("/user/login_failed.html");
                }
            } else if ("/user/list".equals(url)) {
                if (!logined) {
                    response.sendRedirect("/user/login.html");
                    return;
                }

                Collection<User> users = DataBase.findAll();
                StringBuilder sb = new StringBuilder();
                sb.append("<table border='1'>");
                for (User user : users) {
                    sb.append("<tr>");
                    sb.append("<td>" + user.getUserId() + "</td>");
                    sb.append("<td>" + user.getName() + "</td>");
                    sb.append("<td>" + user.getEmail() + "</td>");
                    sb.append("</tr>");
                }
                sb.append("</table>");
                byte[] body = sb.toString().getBytes();
                response.forwardBody(body);
            } else if (url.endsWith(".css")) {
                response.forward(url);
            } else {
                response.forward(url);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
