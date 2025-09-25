package com.second.kirby.service;

import com.second.kirby.domain.User;
import com.second.kirby.dto.request.user.PasswordChangeRequest;
import com.second.kirby.dto.request.user.ProfileUpdateRequest;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import com.second.kirby.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ========== 사용자 조회 ==========

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));
    }

    // ========== 프로필 정보 변경 ==========

    @Transactional
    public void updateProfile(Long userId, ProfileUpdateRequest request) {
        log.info("프로필 정보 변경 요청: userId={}", userId);

        User user = findById(userId);

        // 중복 검사 (본인 제외)
        if (userRepository.existsByEmailAndIdNot(request.email(), userId)) {
            throw new BusinessException(ResponseCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhoneNumberAndIdNot(request.phoneNumber(), userId)) {
            throw new BusinessException(ResponseCode.PHONE_NUMBER_ALREADY_EXISTS);
        }

        // 정보 업데이트
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());

        userRepository.save(user);
        log.info("프로필 정보 변경 완료: userId={}", userId);
    }

    // ========== 비밀번호 변경 ==========

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        log.info("비밀번호 변경 요청: userId={}", userId);

        User user = findById(userId);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BusinessException(ResponseCode.INVALID_PASSWORD);
        }

        // 새 비밀번호 확인 일치 검증
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new BusinessException(ResponseCode.PASSWORD_MISMATCH);
        }

        // 새 비밀번호가 현재 비밀번호와 동일한지 검증
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BusinessException(ResponseCode.SAME_AS_CURRENT_PASSWORD);
        }

        // 비밀번호 변경
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        log.info("비밀번호 변경 완료: userId={}", userId);
    }
}