package com.pgmanager.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private String userId;
    private String tenantId;
    private String name;
    private String role;
    private String department;
}
