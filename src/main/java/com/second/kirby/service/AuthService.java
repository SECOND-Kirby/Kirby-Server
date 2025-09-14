package com.second.kirby.service;

import com.second.kirby.domain.User;
import com.second.kirby.dto.request.LoginRequest;
import com.second.kirby.dto.request.SignupRequest;
import com.second.kirby.dto.response.TokenResponse;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
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

import java.time.LocalDateTime;
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
            throw new BusinessException(ResponseCode.PASSWORD_MISMATCH);
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

    @Transactional
    public TokenResponse login(LoginRequest request) {
        log.info("로그인 요청: username={}", request.username());

        // 사용자 조회
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ResponseCode.INVALID_PASSWORD);
        }

        // 로그인 시간 업데이트 (토큰 유효성 검증용)
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        log.info("로그인 성공: userId={}, username={}", user.getId(), user.getUsername());

        return TokenResponse.of(accessToken, refreshToken, 3600L);
    }

    // ========== 로그아웃 ==========

    @Transactional
    public void logout(String accessToken) {
        log.info("로그아웃 요청");

        try {
            // 토큰이 만료되었더라도 사용자 정보는 추출 가능
            Long userId = jwtUtil.getUserId(accessToken);
            String username = jwtUtil.getUsername(accessToken);

            // 사용자 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND));

            // 로그아웃 시간 기록 (이 시간 이전 토큰은 무효)
            user.setLastLogoutAt(LocalDateTime.now());
            userRepository.save(user);

            log.info("로그아웃 완료: userId={}, username={}", userId, username);

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류", e);
            // 로그아웃은 실패해도 큰 문제가 없으므로 관대하게 처리
            log.info("토큰이 유효하지 않지만 로그아웃 요청을 정상 처리합니다");
        }
    }

    // ========== 토큰 유효성 검증 (JWT 필터에서 사용) ==========

    public boolean isTokenValidForUser(String token) {
        try {
            Long userId = jwtUtil.getUserId(token);
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                return false;
            }

            // 기본 JWT 검증
            if (!jwtUtil.validateToken(token)) {
                return false;
            }

            // 로그아웃 이후 발급된 토큰인지 확인
            if (user.getLastLogoutAt() != null) {
                long tokenIssuedAt = jwtUtil.getIssuedAt(token);
                long lastLogoutTime = user.getLastLogoutAt().atZone(java.time.ZoneId.systemDefault())
                        .toInstant().toEpochMilli();

                // 로그아웃 시간 이전에 발급된 토큰이면 무효
                if (tokenIssuedAt < lastLogoutTime) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // ========== 아이디 중복 확인 ==========

    public void checkUsernameAvailable(String username) {
        log.info("아이디 중복 확인: username={}", username);

        validateUsernameFormat(username);

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ResponseCode.USERNAME_ALREADY_EXISTS);
        }

        log.info("사용 가능한 아이디: {}", username);
    }

    // ========== Spring Security 연동 ==========

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

    // ========== 검증 메서드들 ==========

    private void validateDuplication(String username, String email, String phoneNumber) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(ResponseCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ResponseCode.EMAIL_ALREADY_EXISTS);
        }

        if (userRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException(ResponseCode.PHONE_NUMBER_ALREADY_EXISTS);
        }
    }

    private void validateUsernameFormat(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE, "아이디를 입력해주세요");
        }

        if (username.length() < 4 || username.length() > 50) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE, "아이디는 4-50자 사이로 입력해주세요");
        }

        if (!username.matches("^[a-zA-Z0-9]+$")) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE, "아이디는 영문자와 숫자만 사용 가능합니다");
        }
    }
}