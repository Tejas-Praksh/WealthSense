package com.wealthsense.user.service;

import com.wealthsense.common.enums.UserRole;
import com.wealthsense.common.exception.ResourceNotFoundException;
import com.wealthsense.security.encryption.AESEncryptionService;
import com.wealthsense.security.masking.DataMaskingService;
import com.wealthsense.user.domain.User;
import com.wealthsense.user.dto.UpdateProfileRequest;
import com.wealthsense.user.dto.UserProfileDto;
import com.wealthsense.user.mapper.UserMapper;
import com.wealthsense.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;
    @Mock
    private AESEncryptionService aesEncryptionService;
    @Mock
    private DataMaskingService dataMaskingService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .id(userId)
                .email("test@wealthsense.com")
                .firstName("Test")
                .lastName("User")
                .phone("enc-phone")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void getProfile_existingUser_returnsDto() {
        UserProfileDto expectedDto = UserProfileDto.builder()
                .id(userId)
                .email("test@wealthsense.com")
                .firstName("Test")
                .lastName("User")
                .role("USER")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toProfileDto(testUser)).thenReturn(expectedDto);
        when(aesEncryptionService.isEncrypted(anyString())).thenReturn(true);
        when(aesEncryptionService.decrypt(anyString())).thenReturn("+911234567890");
        when(dataMaskingService.maskPhone("+911234567890")).thenReturn("******7890");

        UserProfileDto result = userService.getProfile(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("test@wealthsense.com", result.getEmail());
        assertEquals("******7890", result.getPhone());
    }

    @Test
    void getProfile_nonExistentUser_throwsNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getProfile(unknownId));
    }

    @Test
    void updateProfile_validRequest_updatesUser() {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Updated")
                .lastName("Name")
                .phone("+911234567890")
                .build();

        UserProfileDto updatedDto = UserProfileDto.builder()
                .id(userId)
                .firstName("Updated")
                .lastName("Name")
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(aesEncryptionService.encrypt("+911234567890")).thenReturn("enc-new-phone");
        when(aesEncryptionService.isEncrypted(anyString())).thenReturn(true);
        when(aesEncryptionService.decrypt(anyString())).thenReturn("+911234567890");
        when(dataMaskingService.maskPhone("+911234567890")).thenReturn("******7890");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toProfileDto(any(User.class))).thenReturn(updatedDto);

        UserProfileDto result = userService.updateProfile(userId, request);

        assertNotNull(result);
        assertEquals("Updated", result.getFirstName());
        verify(userRepository).save(any(User.class));
    }
}
