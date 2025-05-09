package hoyocon.bomberman.api.entity;

public class RegisterRequest {
    private String username;
    private String password;

    // Constructor không tham số (bắt buộc để Jackson tạo object)
    public RegisterRequest() { }

    // Nếu bạn muốn, có thể thêm constructor đầy đủ
    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // ─── Thêm getter và setter ───

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

