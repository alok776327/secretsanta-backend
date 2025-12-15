package com.secretsanta.backend.controller;

import com.secretsanta.backend.dto.AssignmentRequest;
import com.secretsanta.backend.dto.CreateSessionRequest;
import com.secretsanta.backend.dto.JoinRequest;
import com.secretsanta.backend.model.SessionDocument;
import com.secretsanta.backend.service.SessionService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/session")
// @CrossOrigin(
//     origins = {
//         "http://localhost:3000",
//         "https://secret-santa-frontend-5pyx.onrender.com"
//     },
//     allowedHeaders = "*",
//     methods = {
//         RequestMethod.GET,
//         RequestMethod.POST,
//         RequestMethod.OPTIONS
//     }
// )
@RequiredArgsConstructor
@CrossOrigin
public class SessionController {

    private final SessionService sessionService;

    // 🔑 THIS IS THE MISSING PIECE
    // @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    // public ResponseEntity<Void> handleOptions() {
    //     return ResponseEntity.ok().build();
    // }

    @PostMapping("/create")
    public Map<String, String> create(@RequestBody CreateSessionRequest req) {
        return Map.of(
            "sessionId",
            sessionService.createSession(req.getMembers()).getId()
        );
    }

    @PostMapping("/join/{sessionId}")
    public Map<String, String> join(
            @PathVariable String sessionId,
            @RequestParam String name,
            @RequestParam String deviceId) {

        String token = sessionService.joinSession(sessionId, name, deviceId);
        return Map.of("token", token);
    }

    @GetMapping("/assignment/{sessionId}")
    public Map<String, String> assignment(
            @PathVariable String sessionId,
            @RequestParam String deviceId,
            @RequestParam String token) {

        return sessionService.getAssignment(sessionId, deviceId, token);
    }

    @GetMapping("/admin/{sessionId}")
    public Map<String, Object> adminStats(@PathVariable String sessionId) {
        return sessionService.getAdminStats(sessionId.toLowerCase());
    }
}
