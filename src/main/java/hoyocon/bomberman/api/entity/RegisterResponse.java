package hoyocon.bomberman.api.entity;

public class RegisterResponse {
    private String status;
    private Object user;  // hoặc UserDTO

    public RegisterResponse(String status, Object user) {
        this.status = status;
        this.user   = user;
    }
    // getters & setters
}
