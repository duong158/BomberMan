package hoyocon.bomberman.api.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import hoyocon.bomberman.api.repository.UserRepository;
import hoyocon.bomberman.api.entity.User;

@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true", matchIfMissing = true)

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;  // giờ đã resolve

    @Override
    public boolean checkCredentials(String username, String password) {
        User u = userRepo.findByUsername(username);
        return u != null && passwordEncoder.matches(password, u.getPassword());
    }

    @Override
    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepo.existsByUsername(username);
    }

    @Override
    public User createUser(String username, String rawPassword) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPassword));
        return userRepo.save(u);
    }
}
