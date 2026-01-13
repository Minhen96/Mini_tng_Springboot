package com.example.mhpractice.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("USER_404", "User not found"),
    USER_ALREADY_EXISTS("USER_409", "User already exists"),
    INVALID_CREDENTIALS("USER_401", "Invalid credentials"),
    INVALID_TOKEN("USER_401", "Invalid token"),
    REFRESH_TOKEN_EXPIRED("USER_401", "Refresh token expired");

    private final String code;
    private final String message;

}
