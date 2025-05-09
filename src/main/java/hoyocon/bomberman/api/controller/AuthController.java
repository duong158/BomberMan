package hoyocon.bomberman.api.controller;

import hoyocon.bomberman.api.entity.LoginRequest;
import hoyocon.bomberman.api.entity.LoginResponse;
import hoyocon.bomberman.api.entity.RegisterRequest;
import hoyocon.bomberman.api.entity.RegisterResponse;
import hoyocon.bomberman.api.service.UserService;
import hoyocon.bomberman.api.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")  // cho phép JavaFX client gọi
@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true", matchIfMissing = true)
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        boolean ok = userService.checkCredentials(req.getUsername(), req.getPassword());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Đăng nhập thất bại");
        }
        // hoặc trả token / user info
        return ResponseEntity.ok(new LoginResponse("success", userService.findByUsername(req.getUsername())));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        // 1. Kiểm tra trùng username
        if (userService.existsByUsername(req.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Username đã tồn tại");
        }
        // 2. Tạo tài khoản mới
        User u = userService.createUser(req.getUsername(), req.getPassword());
        // 3. Trả về thông tin đã tạo
        return ResponseEntity
                .ok(new RegisterResponse("created", u));
    }
}
