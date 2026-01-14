package com.example.mhpractice.features.user.service.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.mhpractice.common.exception.BusinessException;
import com.example.mhpractice.features.user.models.User;
import com.example.mhpractice.features.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userProfileService;

    @Test
    void success_get_user_profile() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().email(email).build()));

        userProfileService.getUserProfile(email);

        verify(userRepository).findByEmail(email);
    }

    @Test
    void fail_get_user_profile() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userProfileService.getUserProfile(email));
    }

    @Test
    void fail_update_user_profile() {
        String email = "test@example.com";
        String name = "test";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> userProfileService.updateUserProfile(email, name));
    }

    @Test
    void success_update_user_profile() {
        String email = "test@example.com";
        String name = "test";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(User.builder().email(email).build()));

        userProfileService.updateUserProfile(email, name);

        verify(userRepository).save(argThat(user -> {
            return user.getEmail().equals(email) && user.getName().equals(name);
        }));
    }
}
