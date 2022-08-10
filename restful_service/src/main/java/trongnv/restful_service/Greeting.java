package trongnv.restful_service;



// Our service will return JSON: {"id": 1, "content": "Hello, World!"} and return code 200 OK
// This class is used to represent this output
public class Greeting {
    private final int id;
    private final String content;

    public Greeting(int id, String content) {
        this.id = id;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

}