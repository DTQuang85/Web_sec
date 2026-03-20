package com.electronicshop.service;

import com.electronicshop.entity.User;
import com.electronicshop.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByLoginId(String loginId) {
        if (loginId == null || loginId.isBlank()) {
            return Optional.empty();
        }
        String normalized = loginId.trim();
        if (normalized.contains("@")) {
            return userRepository.findByEmailIgnoreCase(normalized);
        }
        return userRepository.findByUsernameIgnoreCase(normalized)
                .or(() -> userRepository.findByEmailIgnoreCase(normalized));
    }

    public boolean passwordMatches(User user, String rawPassword) {
        return user != null && user.getPassword() != null && user.getPassword().equals(rawPassword);
    }

    public User register(String username, String email, String fullName, String password) {
        User user = new User();
        user.setUsername(username != null ? username.trim() : null);
        user.setEmail(email != null ? email.trim() : null);
        user.setFullname(fullName != null ? fullName.trim() : null);
        user.setPassword(password);
        user.setRole("USER");
        return userRepository.save(user);
    }

    public boolean usernameExists(String username) {
        return username != null && userRepository.existsByUsernameIgnoreCase(username.trim());
    }

    public boolean emailExists(String email) {
        return email != null && userRepository.existsByEmailIgnoreCase(email.trim());
    }

    public Page<User> getUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return userRepository.findAll(pageable);
        }
        return userRepository.findByUsernameContainingIgnoreCase(keyword, pageable);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    public long countUsers() {
        return userRepository.count();
    }
}
