package com.second.kirby.controller;

import com.second.kirby.dto.request.LoginRequest;
import com.second.kirby.dto.ResponseDto;
import com.second.kirby.dto.request.SignupRequest;
import com.second.kirby.dto.response.TokenResponse;
import com.second.kirby.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "회원가입 및 로그인 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "신규 회원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ResponseDto<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDto.success("회원가입이 완료되었습니다."));
    }

    @Operation(summary = "로그인", description = "사용자 인증을 수행하고 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<ResponseDto<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authService.login(request);
        return ResponseEntity.ok(ResponseDto.success(tokenResponse, "로그인이 완료되었습니다."));
    }

    @Operation(summary = "아이디 중복 확인", description = "아이디 사용 가능 여부를 확인합니다.")
    @GetMapping("/check-username/{username}")
    public ResponseEntity<ResponseDto<Boolean>> checkUsername(@PathVariable String username) {
        log.info("아이디 중복 확인 요청: {}", username);

        boolean isAvailable = authService.checkUsernameAvailable(username);
        String message = isAvailable ? "사용 가능한 아이디입니다." : "이미 사용중인 아이디입니다.";

        log.info("아이디 중복 확인 결과: {} -> {}", username, isAvailable);

        return ResponseEntity.ok(ResponseDto.success(isAvailable, message));
    }
}