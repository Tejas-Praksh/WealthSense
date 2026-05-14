package com.wealthsense.user.service;

import com.wealthsense.common.exception.ResourceNotFoundException;
import com.wealthsense.security.aop.Auditable;
import com.wealthsense.security.encryption.AESEncryptionService;
import com.wealthsense.security.masking.DataMaskingService;
import com.wealthsense.user.domain.User;
import com.wealthsense.user.dto.UpdateProfileRequest;
import com.wealthsense.user.dto.UserProfileDto;
import com.wealthsense.user.mapper.UserMapper;
import com.wealthsense.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AESEncryptionService aesEncryptionService;
    private final DataMaskingService dataMaskingService;

    @Transactional(readOnly = true)
    public UserProfileDto getProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
        UserProfileDto dto = userMapper.toProfileDto(user);
        dto.setPhone(maskDecryptedPhone(user.getPhone()));
        return dto;
    }

    @Transactional
    @Auditable(action = "PROFILE_UPDATE", resource = "USER")
    public UserProfileDto updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(aesEncryptionService.encrypt(request.getPhone()));
        }
        if (StringUtils.hasText(request.getProfileImageUrl())) {
            user.setProfileImageUrl(request.getProfileImageUrl());
        }

        User saved = userRepository.save(user);
        log.info("Profile updated for user: {}", userId);
        UserProfileDto dto = userMapper.toProfileDto(saved);
        dto.setPhone(maskDecryptedPhone(saved.getPhone()));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<UserProfileDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> {
                    UserProfileDto dto = userMapper.toProfileDto(user);
                    dto.setPhone(maskDecryptedPhone(user.getPhone()));
                    return dto;
                });
    }

    private String maskDecryptedPhone(String encryptedPhone) {
        if (!StringUtils.hasText(encryptedPhone)) {
            return encryptedPhone;
        }
        String plain = encryptedPhone;
        if (aesEncryptionService.isEncrypted(encryptedPhone)) {
            plain = aesEncryptionService.decrypt(encryptedPhone);
        }
        return dataMaskingService.maskPhone(plain);
    }
}
