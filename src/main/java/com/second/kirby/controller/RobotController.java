package com.second.kirby.controller;

import com.second.kirby.dto.ResponseDto;
import com.second.kirby.dto.response.RobotConnectResponse;
import com.second.kirby.dto.response.RobotStatusResponse;
import com.second.kirby.service.RobotBridgeService;
import com.second.kirby.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "로봇", description = "로봇 연결 및 상태 관리 API")
@RestController
@RequestMapping("/api/robot")
@RequiredArgsConstructor
public class RobotController {

    private final RobotBridgeService robotBridgeService;
    private final UserService userService;

    @Operation(summary = "로봇 상태 조회", description = "현재 로봇의 연결 상태를 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<ResponseDto<RobotStatusResponse>> getRobotStatus() {
        RobotStatusResponse status = robotBridgeService.getRobotStatus();
        return ResponseEntity.ok(ResponseDto.success(status, "로봇 상태 조회 성공"));
    }

    @Operation(summary = "로봇 연결", description = "로봇에 연결을 요청합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/connect")
    public ResponseEntity<ResponseDto<RobotConnectResponse>> connectRobot(Authentication authentication) {
        Long userId = getUserId(authentication);
        RobotConnectResponse response = robotBridgeService.connectRobot(userId);

        if (response.success()) {
            return ResponseEntity.ok(ResponseDto.success(response, "로봇 연결 성공"));
        } else {
            return ResponseEntity.badRequest().body(ResponseDto.success(response, response.message()));
        }
    }

    @Operation(summary = "로봇 연결 해제", description = "로봇 연결을 해제합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/disconnect")
    public ResponseEntity<ResponseDto<Void>> disconnectRobot(Authentication authentication) {
        Long userId = getUserId(authentication);
        boolean success = robotBridgeService.disconnectRobot(userId);

        if (success) {
            return ResponseEntity.ok(ResponseDto.success("로봇 연결 해제 성공"));
        } else {
            return ResponseEntity.badRequest().body(ResponseDto.success("연결된 세션이 없습니다"));
        }
    }

    @Operation(summary = "하트비트", description = "연결 상태를 유지하기 위한 하트비트를 전송합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/heartbeat")
    public ResponseEntity<ResponseDto<Void>> heartbeat(Authentication authentication) {
        Long userId = getUserId(authentication);
        robotBridgeService.updateHeartbeat(userId);
        return ResponseEntity.ok(ResponseDto.success("하트비트 업데이트 완료"));
    }

    private Long getUserId(Authentication authentication) {
        return userService.findByUsername(authentication.getName()).getId();
    }
}