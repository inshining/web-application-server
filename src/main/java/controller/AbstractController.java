package controller;

import util.HttpRequest;
import util.HttpResponse;

public abstract class AbstractController implements Controller{
    public void service(HttpRequest request, HttpResponse response) {
        if (request.getMethod().isPost()) {
            doPost(request, response);
        } else if (request.getMethod().isGet()) {
            doGet(request, response);
        }
    }

    public void doGet(HttpRequest request, HttpResponse response){
        response.forward("/index.html");
    }
    public void doPost(HttpRequest request, HttpResponse response){
        response.forward("/index.html");
    }
}
