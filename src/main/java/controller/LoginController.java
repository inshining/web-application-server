package controller;

import db.DataBase;
import model.User;
import util.HttpRequest;
import util.HttpResponse;

public class LoginController extends AbstractController{
    public void doPost(HttpRequest request, HttpResponse response) {
        try {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
