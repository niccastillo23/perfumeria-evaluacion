package com.perfumeria.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return Map.of("success", false, "message", "El usuario ya existe");
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("CLIENT");
        }
        userRepository.save(user);
        return Map.of("success", true, "message", "Usuario registrado correctamente con rol " + user.getRole());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<User> user = userRepository.findByUsername(username);

        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return Map.of(
                "success", true, 
                "message", "Inicio de sesión exitoso",
                "user", Map.of(
                    "username", username, 
                    "email", user.get().getEmail(),
                    "role", user.get().getRole()
                )
            );
        }

        return Map.of("success", false, "message", "Credenciales inválidas");
    }

    @GetMapping("/users")
    public java.util.List<User> getUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/health")
    public String health() {
        return "OK from PerfumerIA Auth Service";
    }
}
