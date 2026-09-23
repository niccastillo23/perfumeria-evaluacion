package com.perfumeria.profile;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private ProfileRepository profileRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/{username}")
    public Map<String, Object> getProfile(@PathVariable String username) {
        Optional<Profile> profileOpt = profileRepository.findById(username);
        
        Profile profile;
        if (profileOpt.isEmpty()) {
            // Create default profile if not exists
            try {
                String defaultPrefs = objectMapper.writeValueAsString(Map.of("scentType", "Amaderado", "brand", "Luxe Parfums"));
                profile = new Profile(username, "Usuario " + username, "Amante de las fragancias exclusivas.", LocalDate.now().toString(), defaultPrefs);
                profileRepository.save(profile);
            } catch (Exception e) {
                return Map.of("error", "Failed to create profile");
            }
        } else {
            profile = profileOpt.get();
        }

        try {
            Object preferences = objectMapper.readValue(profile.getPreferencesJson(), Object.class);
            return Map.of(
                "username", profile.getUsername(),
                "fullName", profile.getFullName(),
                "bio", profile.getBio(),
                "memberSince", profile.getMemberSince(),
                "preferences", preferences
            );
        } catch (Exception e) {
            return Map.of("error", "Error parsing profile preferences");
        }
    }

    @GetMapping("/health")
    public String health() {
        return "OK from PerfumerIA Profile Service";
    }
}
