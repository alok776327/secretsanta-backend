package com.secretsanta.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateSessionRequest {
    private List<String> members;
}
