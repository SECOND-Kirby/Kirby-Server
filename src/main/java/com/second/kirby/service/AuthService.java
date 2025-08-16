package com.second.kirby.service;

import com.second.kirby.domain.User;
import com.second.kirby.dto.LoginRequest;
import com.second.kirby.dto.SignupRequest;
import com.second.kirby.dto.TokenResponse;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ErrorCode;
import com.second.kirby.repository.UserRepository;
import com.second.kirby.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // ========== 회원가입 ==========

    @Transactional
    public void signup(SignupRequest request) {
        log.info("회원가입 요청: username={}, email={}", request.username(), request.email());

        // 비밀번호 확인 검증
        if (!request.password().equals(request.passwordConfirm())) {
            throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 중복 검증
        validateDuplication(request.username(), request.email(), request.phoneNumber());

        // 사용자 생성 및 저장
        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .name(request.name())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
        log.info("회원가입 완료: userId={}, username={}", user.getId(), user.getUsername());
    }

    // ========== 로그인 ==========

    public TokenResponse login(LoginRequest request) {
        log.info("로그인 요청: username={}", request.username());

        // 사용자 조회
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        log.info("로그인 성공: userId={}, username={}", user.getId(), user.getUsername());

        return TokenResponse.of(accessToken, refreshToken, 3600L);
    }

    // ========== Spring Security 연동 (JWT 필터용) ==========

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .build();
    }

    // ========== 중복 검증 ==========

    private void validateDuplication(String username, String email, String phoneNumber) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }
    }
}