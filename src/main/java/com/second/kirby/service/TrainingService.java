package com.second.kirby.service;

import com.second.kirby.domain.RobotSession;
import com.second.kirby.domain.Training;
import com.second.kirby.dto.request.TrainingConfigRequest;
import com.second.kirby.dto.response.TrainingStartResponse;
import com.second.kirby.dto.response.TrainingStatusDto;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import com.second.kirby.repository.TrainingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TrainingService {

    private final TrainingRepository trainingRepository;
    private final RobotBridgeService robotBridgeService;
    private final SimpMessagingTemplate messagingTemplate;

    // 활성 상태 목록을 상수로 정의
    private static final List<Training.TrainingStatus> ACTIVE_STATUSES = Arrays.asList(
            Training.TrainingStatus.READY,
            Training.TrainingStatus.IN_PROGRESS,
            Training.TrainingStatus.PAUSED
    );

    // ========== 훈련 시작 ==========

    @Transactional
    public TrainingStartResponse startTraining(Long userId, TrainingConfigRequest request) {
        log.info("훈련 시작 요청: userId={}, config={}", userId, request);

        // 입력값 검증
        validateTrainingConfig(request);

        // 로봇 연결 권한 확인
        if (!robotBridgeService.hasRobotAccess(userId)) {
            throw new BusinessException(ResponseCode.ROBOT_NOT_CONNECTED);
        }

        // 진행중인 훈련 확인
        Optional<Training> activeTraining = trainingRepository.findByUserIdAndStatusIn(userId, ACTIVE_STATUSES);
        if (activeTraining.isPresent()) {
            throw new BusinessException(ResponseCode.TRAINING_ALREADY_EXISTS);
        }

        // 새 훈련 세션 생성
        Training training = Training.builder()
                .userId(userId)
                .intensity(request.intensity())
                .direction(request.direction())
                .frequency(request.frequency())
                .durationMinutes(request.durationMinutes())
                .status(Training.TrainingStatus.READY)
                .build();

        training.start();
        Training savedTraining = trainingRepository.save(training);

        // 로봇 상태를 훈련중으로 변경
        robotBridgeService.updateRobotState(userId, RobotSession.RobotState.TRAINING);

        // 로봇에 훈련 시작 명령 전송
        robotBridgeService.sendTrainingStartCommand(savedTraining.getId(), request);

        // 실시간 상태 전송
        sendTrainingStatusUpdate(savedTraining);

        log.info("훈련 시작 완료: trainingId={}", savedTraining.getId());
        return TrainingStartResponse.of(savedTraining);
    }

    // ========== 훈련 일시정지 ==========

    @Transactional
    public void pauseTraining(Long userId) {
        log.info("훈련 일시정지 요청: userId={}", userId);

        Training training = getActiveTrainingByUserId(userId);

        if (training.getStatus() != Training.TrainingStatus.IN_PROGRESS) {
            throw new BusinessException(ResponseCode.TRAINING_INVALID_STATUS, "진행중인 훈련만 일시정지할 수 있습니다.");
        }

        training.pause();
        trainingRepository.save(training);

        // 로봇에 일시정지 명령 전송
        robotBridgeService.sendTrainingPauseCommand(training.getId());

        sendTrainingStatusUpdate(training);
        log.info("훈련 일시정지 완료: trainingId={}", training.getId());
    }

    // ========== 훈련 재개 ==========

    @Transactional
    public void resumeTraining(Long userId) {
        log.info("훈련 재개 요청: userId={}", userId);

        Training training = getActiveTrainingByUserId(userId);

        if (training.getStatus() != Training.TrainingStatus.PAUSED) {
            throw new BusinessException(ResponseCode.TRAINING_INVALID_STATUS, "일시정지된 훈련만 재개할 수 있습니다.");
        }

        training.resume();
        trainingRepository.save(training);

        // 로봇에 재개 명령 전송
        robotBridgeService.sendTrainingResumeCommand(training.getId());

        sendTrainingStatusUpdate(training);
        log.info("훈련 재개 완료: trainingId={}", training.getId());
    }

    // ========== 훈련 종료 ==========

    @Transactional
    public void stopTraining(Long userId) {
        log.info("훈련 종료 요청: userId={}", userId);

        Training training = getActiveTrainingByUserId(userId);

        training.complete();
        trainingRepository.save(training);

        // 로봇 상태를 연결됨으로 변경
        robotBridgeService.updateRobotState(userId, RobotSession.RobotState.CONNECTED);

        // 로봇에 훈련 종료 명령 전송
        robotBridgeService.sendTrainingStopCommand(training.getId());

        sendTrainingCompleteNotification(training);
        log.info("훈련 종료 완료: trainingId={}", training.getId());
    }

    // ========== 현재 훈련 상태 조회 ==========

    public TrainingStatusDto getCurrentTraining(Long userId) {
        Optional<Training> activeTraining = trainingRepository.findByUserIdAndStatusIn(userId, ACTIVE_STATUSES);
        return activeTraining.map(TrainingStatusDto::from).orElse(null);
    }

    // ========== 유틸리티 메서드들 ==========

    private void validateTrainingConfig(TrainingConfigRequest request) {
        if (request.intensity() < 1 || request.intensity() > 10) {
            throw new BusinessException(ResponseCode.TRAINING_CONFIG_INVALID, "강도는 1~10 사이여야 합니다.");
        }
        if (request.direction() < 1 || request.direction() > 10) {
            throw new BusinessException(ResponseCode.TRAINING_CONFIG_INVALID, "방향은 1~10 사이여야 합니다.");
        }
        if (request.frequency() < 1 || request.frequency() > 10) {
            throw new BusinessException(ResponseCode.TRAINING_CONFIG_INVALID, "빈도는 1~10 사이여야 합니다.");
        }
        if (request.durationMinutes() < 5 || request.durationMinutes() > 180) {
            throw new BusinessException(ResponseCode.TRAINING_CONFIG_INVALID, "훈련시간은 5~180분 사이여야 합니다.");
        }
    }

    private Training getActiveTrainingByUserId(Long userId) {
        Optional<Training> activeTraining = trainingRepository.findByUserIdAndStatusIn(userId, ACTIVE_STATUSES);
        if (activeTraining.isEmpty()) {
            throw new BusinessException(ResponseCode.TRAINING_NOT_FOUND, "진행중인 훈련이 없습니다.");
        }
        return activeTraining.get();
    }

    private void sendTrainingStatusUpdate(Training training) {
        TrainingStatusDto status = TrainingStatusDto.from(training);
        messagingTemplate.convertAndSend(
                "/topic/training/" + training.getUserId() + "/status",
                status
        );
    }

    private void sendTrainingCompleteNotification(Training training) {
        TrainingStatusDto status = TrainingStatusDto.from(training);
        messagingTemplate.convertAndSend(
                "/topic/training/" + training.getUserId() + "/complete",
                status
        );
    }
}