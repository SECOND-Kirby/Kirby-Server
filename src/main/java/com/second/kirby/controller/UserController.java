package com.second.kirby.controller;

import com.second.kirby.domain.User;
import com.second.kirby.dto.ResponseDto;
import com.second.kirby.dto.request.PasswordChangeRequest;
import com.second.kirby.dto.request.ProfileUpdateRequest;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import com.second.kirby.service.UserService;
import com.second.kirby.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자", description = "사용자 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ResponseDto<UserInfoDto>> getMyInfo(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());

        UserInfoDto userInfo = new UserInfoDto(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber()
        );

        return ResponseEntity.ok(ResponseDto.success(userInfo, "사용자 정보 조회 성공"));
    }

    @Operation(summary = "프로필 정보 변경", description = "사용자의 이름, 이메일, 전화번호를 변경합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public ResponseEntity<ResponseDto<Void>> updateProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ProfileUpdateRequest request) {

        String accessToken = extractTokenFromHeader(authorizationHeader);
        Long userId = jwtUtil.getUserId(accessToken);

        userService.updateProfile(userId, request);

        return ResponseEntity.ok(ResponseDto.success("프로필 정보가 변경되었습니다."));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/password")
    public ResponseEntity<ResponseDto<Void>> changePassword(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody PasswordChangeRequest request) {

        String accessToken = extractTokenFromHeader(authorizationHeader);
        Long userId = jwtUtil.getUserId(accessToken);

        userService.changePassword(userId, request);

        return ResponseEntity.ok(ResponseDto.success("비밀번호가 변경되었습니다."));
    }

    // Bearer 토큰 추출 헬퍼 메서드
    private String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED, "액세스 토큰이 필요합니다");
        }
        return authorizationHeader.substring(7);
    }

    // 사용자 정보 응답 DTO
    public record UserInfoDto(
            Long id,
            String username,
            String name,
            String email,
            String phoneNumber
    ) {
    }
}