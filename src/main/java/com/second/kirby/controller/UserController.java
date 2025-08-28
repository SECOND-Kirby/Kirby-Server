package com.second.kirby.controller;

import com.second.kirby.domain.User;
import com.second.kirby.dto.ResponseDto;
import com.second.kirby.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "사용자 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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