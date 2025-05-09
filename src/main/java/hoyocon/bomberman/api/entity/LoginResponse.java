package hoyocon.bomberman.api.entity;

public class LoginResponse {
    private String status;
    private Object user;  // Bạn có thể đổi thành UserDTO hoặc entity.User nếu thích

    public LoginResponse(String status, Object user) {
        this.status = status;
        this.user   = user;
    }

    public String getStatus() {
        return status;
    }

    public Object getUser() {
        return user;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUser(Object user) {
        this.user = user;
    }
}
