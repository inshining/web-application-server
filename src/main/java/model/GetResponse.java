package model;

public class GetResponse {
    public byte[] body;
    public String path;
    public int statusCode;
    public GetResponse(byte[] body, String path, int statusCode) {
        this.body = body;
        this.path = path;
        this.statusCode = statusCode;
    }
}
