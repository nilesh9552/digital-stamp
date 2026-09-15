package com.digitalstamp.controller;

import com.digitalstamp.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/debug")
public class DebugController {

    private final UserRepository userRepository;

    public DebugController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> recentUsers() {
        var page = userRepository.findAll(PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "id")));
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        for (var u : page.getContent()) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", u.getId());
            m.put("email", u.getEmail());
            m.put("name", u.getName());
            m.put("role", u.getRole());
            m.put("active", u.isActive());
            m.put("qrToken", u.getQrToken());
            list.add(m);
        }
        return ResponseEntity.ok(list);
    }
}
