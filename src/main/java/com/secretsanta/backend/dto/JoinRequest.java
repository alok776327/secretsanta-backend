package com.secretsanta.backend.dto;

import lombok.Data;

@Data
public class JoinRequest {
    private String name;
    private String deviceId;
}
