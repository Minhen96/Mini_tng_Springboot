package com.example.mhpractice.features.user.controller.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogoutRequest {
    private String email;
}
