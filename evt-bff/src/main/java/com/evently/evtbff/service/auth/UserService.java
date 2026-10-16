package com.evently.evtbff.service.auth;

import com.evently.evtbff.model.auth.User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserService {

    private final Map<String, User> usersByMobile = Map.of(
            "9876543210",
            new User("user-1", "9876543210", "ADMIN"),

            "8765432109",
            new User("user-2", "8765432109", "VIEWER")
    );

    public Optional<User> findByMobile(String mobile) {
        return Optional.ofNullable(usersByMobile.get(mobile));
    }
}