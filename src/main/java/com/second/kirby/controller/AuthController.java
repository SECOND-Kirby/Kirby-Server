package com.second.kirby.controller;

import com.second.kirby.dto.request.LoginRequest;
import com.second.kirby.dto.ResponseDto;
import com.second.kirby.dto.request.LogoutRequest;
import com.second.kirby.dto.request.SignupRequest;
import com.second.kirby.dto.request.UsernameCheckRequest;
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

@Tag(name = "인증", description = "회원가입, 로그인, 로그아웃 API")
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

    @Operation(summary = "로그아웃", description = "현재 토큰을 무효화합니다. 토큰이 만료되어도 정상 처리됩니다.")
    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestBody(required = false) LogoutRequest request) {

        log.info("로그아웃 요청");

        // 토큰 추출 우선순위: Authorization 헤더 > RequestBody
        String accessToken = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            accessToken = authorizationHeader.substring(7);
        } else if (request != null && request.accessToken() != null) {
            accessToken = request.accessToken();
        }

        if (accessToken == null) {
            log.info("토큰 없이 로그아웃 요청 - 정상 처리");
            return ResponseEntity.ok(ResponseDto.success("로그아웃이 완료되었습니다."));
        }

        authService.logout(accessToken);
        return ResponseEntity.ok(ResponseDto.success("로그아웃이 완료되었습니다."));
    }

    @Operation(summary = "아이디 중복 확인", description = "아이디 사용 가능 여부를 확인합니다.")
    @PostMapping("/check-username")
    public ResponseEntity<ResponseDto<Void>> checkUsername(@Valid @RequestBody UsernameCheckRequest request) {
        authService.checkUsernameAvailable(request.username());
        return ResponseEntity.ok(ResponseDto.success("사용 가능한 아이디입니다."));
    }
}