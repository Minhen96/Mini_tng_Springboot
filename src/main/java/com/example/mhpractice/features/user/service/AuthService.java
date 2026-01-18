package com.example.mhpractice.features.user.service;

import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.service.result.LoginResult;

public interface AuthService {

    public User getUserByEmail(String email);

    public void register(String email, String name, String password);

    public LoginResult login(String email, String password);

    public LoginResult refreshToken(String refreshToken);

    public void logout(String refreshToken);

}
