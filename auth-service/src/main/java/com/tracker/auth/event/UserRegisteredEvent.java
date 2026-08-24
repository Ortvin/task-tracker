package com.tracker.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisteredEvent {
    private Long userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String middleName;
    private String role;
}