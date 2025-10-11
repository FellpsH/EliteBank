package com.fellps.apibank.dto;

import com.fellps.apibank.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String cpf;
    private Role role;
    private Boolean active;
    private LocalDateTime createdAt;
}

