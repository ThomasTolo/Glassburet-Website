package com.glassburet.controller;

import com.glassburet.model.Member;
import com.glassburet.repository.MemberRepository;
import com.glassburet.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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
        String role = body.getOrDefault("role", "ROLE_MEMBER");

        // Bootstrap: if no members exist, allow the first registration regardless of role.
        long count = memberRepository.count();
        if (count > 0 && "ROLE_ADMIN".equals(role)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(403).body(Map.of("error", "admin required"));
            }
        }

        if (memberRepository.existsByName(name)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Member exists"));
        }

        Member m = new Member();
        m.setName(name);
        m.setPasswordHash(passwordEncoder.encode(password));
        m.setRole(role);

        memberRepository.save(m);
        return ResponseEntity.ok(Map.of("created", true));
    }

    @PutMapping("/members/{name}/role")
    public ResponseEntity<?> updateRole(@PathVariable String name, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        if (newRole == null || (!newRole.equals("ROLE_ADMIN") && !newRole.equals("ROLE_MEMBER"))) {
            return ResponseEntity.badRequest().body(Map.of("error", "role must be ROLE_ADMIN or ROLE_MEMBER"));
        }
        return memberRepository.findByName(name)
                .map(m -> {
                    m.setRole(newRole);
                    memberRepository.save(m);
                    return ResponseEntity.ok(Map.of("updated", true, "name", name, "role", newRole));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Member not found")));
    }
}
