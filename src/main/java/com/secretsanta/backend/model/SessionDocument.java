package com.secretsanta.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Document(collection = "sessions")
public class SessionDocument {

    @Id
    private String id;

    private List<String> members;

    private Map<String, String> assignedTargets;

    private boolean locked;

    private LocalDateTime createdAt;

    // deviceId -> name
    private Map<String, String> deviceBindings;

    // deviceId -> one-time token
    private Map<String, String> accessTokens;
}
