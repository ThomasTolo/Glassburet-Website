package com.glassburet.controller;

import com.glassburet.model.Member;
import com.glassburet.realtime.SiteUpdateBroadcaster;
import com.glassburet.repository.MemberRepository;
import com.glassburet.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SiteUpdateBroadcaster siteUpdateBroadcaster;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String password = body.get("password");

        return memberRepository.findByName(name)
                .filter(m -> passwordEncoder.matches(password, m.getPasswordHash()))
                .map(m -> {
                    String token = jwtUtil.generateToken(m.getName(), m.getRole());
                    return ResponseEntity.ok(Map.of("token", token));
                })
                .orElseGet(() -> ResponseEntity.status(401).build());
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String password = body.get("password");
        String role = "ROLE_MEMBER";

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
        }
        ResponseEntity<?> passwordError = validatePassword(password);
        if (passwordError != null) {
            return passwordError;
        }
        if (memberRepository.existsByName(name)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Member exists"));
        }

        Member m = new Member();
        m.setName(name);
        m.setPasswordHash(passwordEncoder.encode(password));
        m.setRole(role);

        memberRepository.save(m);
        siteUpdateBroadcaster.publish("members");
        String token = jwtUtil.generateToken(m.getName(), m.getRole());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> body, Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            return ResponseEntity.status(401).build();
        }

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        if (currentPassword == null) {
            return ResponseEntity.status(403).body(Map.of("error", "Current password is incorrect"));
        }
        ResponseEntity<?> passwordError = validatePassword(newPassword);
        if (passwordError != null) {
            return passwordError;
        }

        return memberRepository.findByName(authentication.getName())
                .map(m -> {
                    if (!passwordEncoder.matches(currentPassword, m.getPasswordHash())) {
                        return ResponseEntity.status(403).body(Map.of("error", "Current password is incorrect"));
                    }
                    m.setPasswordHash(passwordEncoder.encode(newPassword));
                    memberRepository.save(m);
                    String token = jwtUtil.generateToken(m.getName(), m.getRole());
                    return ResponseEntity.ok(Map.of("token", token));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Member not found")));
    }

    @GetMapping("/members")
    public ResponseEntity<?> listMembers() {
        List<Map<String, String>> members = memberRepository.findAll().stream()
                .map(m -> Map.of("name", m.getName(), "role", m.getRole()))
                .toList();
        return ResponseEntity.ok(members);
    }

    @PutMapping("/members/{name}/role")
    public ResponseEntity<?> updateRole(@PathVariable String name, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        if (newRole == null || (!newRole.equals("ROLE_ADMIN") && !newRole.equals("ROLE_MEMBER"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "role must be ROLE_ADMIN or ROLE_MEMBER"));
        }
        return memberRepository.findByName(name)
                .map(m -> {
                    if ("ROLE_OWNER".equals(m.getRole())) {
                        return ResponseEntity.status(403).body(Map.of("error", "Cannot modify an Owner account"));
                    }
                    m.setRole(newRole);
                    memberRepository.save(m);
                    siteUpdateBroadcaster.publish("members");
                    siteUpdateBroadcaster.publish("auth");
                    return ResponseEntity.ok(Map.of("updated", true, "name", name, "role", newRole));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Member not found")));
    }

    private ResponseEntity<?> validatePassword(String password) {
        if (password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 8 characters"));
        }
        if (!password.matches(".*\\d.*")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password must contain at least one number"));
        }
        return null;
    }
}
