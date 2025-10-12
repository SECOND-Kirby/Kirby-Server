package com.second.kirby.controller;

import com.second.kirby.domain.User;
import com.second.kirby.dto.ResponseDto;
import com.second.kirby.dto.request.user.DeleteAccountRequest;
import com.second.kirby.dto.request.user.PasswordChangeRequest;
import com.second.kirby.dto.request.user.ProfileUpdateRequest;
import com.second.kirby.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
                user.getPhoneNumber(),
                user.getProfileImageUrl()
        );

        return ResponseEntity.ok(ResponseDto.success(userInfo, "사용자 정보 조회 성공"));
    }

    @Operation(summary = "프로필 정보 변경", description = "사용자의 이름, 이메일, 전화번호를 변경합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/profile")
    public ResponseEntity<ResponseDto<Void>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request) {

        User user = userService.findByUsername(authentication.getName());
        userService.updateProfile(user.getId(), request);

        return ResponseEntity.ok(ResponseDto.success("프로필 정보가 변경되었습니다."));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/password")
    public ResponseEntity<ResponseDto<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody PasswordChangeRequest request) {

        User user = userService.findByUsername(authentication.getName());
        userService.changePassword(user.getId(), request);

        return ResponseEntity.ok(ResponseDto.success("비밀번호가 변경되었습니다."));
    }

    @Operation(summary = "프로필 이미지 변경", description = "사용자의 프로필 이미지를 변경합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/profile/image")
    public ResponseEntity<ResponseDto<ProfileImageResponse>> updateProfileImage(
            Authentication authentication,
            @RequestParam("image") MultipartFile file) {

        User user = userService.findByUsername(authentication.getName());
        String filename = userService.updateProfileImage(user.getId(), file);

        ProfileImageResponse response = new ProfileImageResponse(filename);
        return ResponseEntity.ok(ResponseDto.success(response, "프로필 이미지가 변경되었습니다."));
    }

    @Operation(summary = "프로필 이미지 삭제", description = "사용자의 프로필 이미지를 삭제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/profile/image")
    public ResponseEntity<ResponseDto<Void>> deleteProfileImage(Authentication authentication) {

        User user = userService.findByUsername(authentication.getName());
        userService.deleteProfileImage(user.getId());

        return ResponseEntity.ok(ResponseDto.success("프로필 이미지가 삭제되었습니다."));
    }

    @Operation(summary = "회원 탈퇴", description = "현재 비밀번호 확인 후 모든 개인 데이터를 삭제하고 회원 탈퇴합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/me")
    public ResponseEntity<ResponseDto<Void>> deleteAccount(
            Authentication authentication,
            @Valid @RequestBody DeleteAccountRequest request) {

        User user = userService.findByUsername(authentication.getName());
        userService.deleteAccount(user.getId(), request);

        return ResponseEntity.ok(ResponseDto.success("회원 탈퇴가 완료되었습니다."));
    }

    // 사용자 정보 응답 DTO
    public record UserInfoDto(
            Long id,
            String username,
            String name,
            String email,
            String phoneNumber,
            String profileImageUrl
    ) {
    }

    // 프로필 이미지 응답 DTO
    public record ProfileImageResponse(
            String filename
    ) {
    }
}