package hoyocon.bomberman.api.service;

import hoyocon.bomberman.api.entity.User;

public interface UserService {
    boolean existsByUsername(String username);
    boolean checkCredentials(String username, String password);
    User findByUsername(String username);
    User createUser(String username, String rawPassword);
}

