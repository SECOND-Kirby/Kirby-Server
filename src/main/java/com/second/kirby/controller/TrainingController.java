package com.second.kirby.controller;

import com.second.kirby.dto.ResponseDto;
import com.second.kirby.dto.request.TrainingConfigRequest;
import com.second.kirby.dto.response.TrainingStartResponse;
import com.second.kirby.dto.response.TrainingStatusDto;
import com.second.kirby.service.TrainingService;
import com.second.kirby.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "훈련", description = "테니스 훈련 관리 API")
@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final UserService userService;

    @Operation(summary = "훈련 시작", description = "새로운 훈련 세션을 시작합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/start")
    public ResponseEntity<ResponseDto<TrainingStartResponse>> startTraining(
            Authentication authentication,
            @Valid @RequestBody TrainingConfigRequest request) {
        Long userId = getUserId(authentication);
        TrainingStartResponse response = trainingService.startTraining(userId, request);
        return ResponseEntity.ok(ResponseDto.success(response, "훈련이 시작되었습니다"));
    }

    @Operation(summary = "훈련 일시정지", description = "진행중인 훈련을 일시정지합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/pause")
    public ResponseEntity<ResponseDto<Void>> pauseTraining(Authentication authentication) {
        Long userId = getUserId(authentication);
        trainingService.pauseTraining(userId);
        return ResponseEntity.ok(ResponseDto.success("훈련이 일시정지되었습니다"));
    }

    @Operation(summary = "훈련 재개", description = "일시정지된 훈련을 재개합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/resume")
    public ResponseEntity<ResponseDto<Void>> resumeTraining(Authentication authentication) {
        Long userId = getUserId(authentication);
        trainingService.resumeTraining(userId);
        return ResponseEntity.ok(ResponseDto.success("훈련이 재개되었습니다"));
    }

    @Operation(summary = "훈련 종료", description = "훈련을 완료하고 결과를 저장합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/stop")
    public ResponseEntity<ResponseDto<Void>> stopTraining(Authentication authentication) {
        Long userId = getUserId(authentication);
        trainingService.stopTraining(userId);
        return ResponseEntity.ok(ResponseDto.success("훈련이 완료되었습니다"));
    }

    @Operation(summary = "현재 훈련 상태", description = "현재 진행중인 훈련의 상태를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/status")
    public ResponseEntity<ResponseDto<TrainingStatusDto>> getTrainingStatus(
            Authentication authentication) {
        Long userId = getUserId(authentication);
        TrainingStatusDto status = trainingService.getCurrentTraining(userId);

        if (status == null) {
            return ResponseEntity.ok(ResponseDto.success(null, "진행중인 훈련이 없습니다"));
        }

        return ResponseEntity.ok(ResponseDto.success(status, "훈련 상태 조회 성공"));
    }

    private Long getUserId(Authentication authentication) {
        return userService.findByUsername(authentication.getName()).getId();
    }
}