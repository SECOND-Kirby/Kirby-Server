package com.second.kirby.service;

import com.second.kirby.domain.RobotSession;
import com.second.kirby.domain.User;
import com.second.kirby.dto.request.TrainingConfigRequest;
import com.second.kirby.dto.response.RobotConnectResponse;
import com.second.kirby.dto.response.RobotStatusResponse;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import com.second.kirby.repository.RobotSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RobotBridgeService {

    private final RobotSessionRepository robotSessionRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RestTemplate restTemplate;

    @Value("${robot.api.base-url:http://localhost:8081}")
    private String robotApiBaseUrl;

    @Value("${robot.api.timeout:5000}")
    private int robotApiTimeout;

    // ========== 로봇 상태 조회 ==========

    public RobotStatusResponse getRobotStatus() {
        RobotSession session = getOrCreateRobotSession();

        // 세션 만료 체크 후 자동 해제
        if (session.isExpired()) {
            disconnectExpiredSession(session);
        }

        String connectedUsername = null;
        if (session.isConnected()) {
            try {
                User connectedUser = userService.findById(session.getConnectedUserId());
                connectedUsername = connectedUser.getName();
            } catch (BusinessException e) {
                log.warn("연결된 사용자를 찾을 수 없음: userId={}", session.getConnectedUserId());
                disconnectExpiredSession(session);
            }
        }

        // 실제 로봇에서 실시간 상태 조회
        Map<String, Object> robotRealTimeStatus = getRobotRealTimeStatus();

        return RobotStatusResponse.of(
                session,
                connectedUsername,
                (Integer) robotRealTimeStatus.getOrDefault("ballCount", 0),
                (Integer) robotRealTimeStatus.getOrDefault("batteryPercentage", 0)
        );
    }

    // ========== 로봇 연결 ==========

    @Transactional
    public RobotConnectResponse connectRobot(Long userId) {
        log.info("로봇 연결 요청: userId={}", userId);

        RobotSession session = getOrCreateRobotSession();

        if (session.isConnected()) {
            if (session.isConnectedBy(userId)) {
                session.updateHeartbeat();
                robotSessionRepository.save(session);
                return RobotConnectResponse.success(session);
            } else {
                User connectedUser = userService.findById(session.getConnectedUserId());
                return RobotConnectResponse.failure(
                        String.format("%s님이 이미 사용중입니다.", connectedUser.getName())
                );
            }
        }

        // 실제 로봇 연결 요청
        boolean robotConnected = requestRobotConnection(userId);
        if (!robotConnected) {
            throw new BusinessException(ResponseCode.ROBOT_CONNECTION_FAILED);
        }

        session.connect(userId);
        robotSessionRepository.save(session);

        broadcastRobotStatus();

        log.info("로봇 연결 성공: userId={}", userId);
        return RobotConnectResponse.success(session);
    }

    // ========== 로봇 연결 해제 ==========

    @Transactional
    public boolean disconnectRobot(Long userId) {
        log.info("로봇 연결 해제 요청: userId={}", userId);

        Optional<RobotSession> sessionOpt = robotSessionRepository.findByConnectedUserId(userId);
        if (sessionOpt.isEmpty()) {
            return false;
        }

        // 실제 로봇 연결 해제 요청
        requestRobotDisconnection(userId);

        RobotSession session = sessionOpt.get();
        session.disconnect();
        robotSessionRepository.save(session);

        broadcastRobotStatus();

        log.info("로봇 연결 해제 성공: userId={}", userId);
        return true;
    }

    // ========== 하트비트 업데이트 ==========

    @Transactional
    public void updateHeartbeat(Long userId) {
        Optional<RobotSession> sessionOpt = robotSessionRepository.findByConnectedUserId(userId);
        if (sessionOpt.isPresent()) {
            RobotSession session = sessionOpt.get();
            session.updateHeartbeat();
            robotSessionRepository.save(session);
        }
    }

    // ========== 로봇 상태 변경 ==========

    @Transactional
    public void updateRobotState(Long userId, RobotSession.RobotState newState) {
        Optional<RobotSession> sessionOpt = robotSessionRepository.findByConnectedUserId(userId);
        if (sessionOpt.isEmpty()) {
            throw new BusinessException(ResponseCode.ACCESS_DENIED, "로봇에 연결되지 않았습니다.");
        }

        RobotSession session = sessionOpt.get();
        session.setState(newState);
        session.updateHeartbeat();
        robotSessionRepository.save(session);

        broadcastRobotStatus();

        log.info("로봇 상태 변경: userId={}, state={}", userId, newState);
    }

    // ========== 로봇 명령 전송 메서드들 ==========

    public void sendTrainingStartCommand(Long trainingId, TrainingConfigRequest config) {
        log.info("로봇에 훈련 시작 명령 전송: trainingId={}, config={}", trainingId, config);

        Map<String, Object> command = Map.of(
                "command", "START_TRAINING",
                "trainingId", trainingId,
                "intensity", config.intensity(),
                "direction", config.direction(),
                "frequency", config.frequency(),
                "durationMinutes", config.durationMinutes()
        );

        sendCommandToRobot(command);
    }

    public void sendTrainingPauseCommand(Long trainingId) {
        log.info("로봇에 훈련 일시정지 명령 전송: trainingId={}", trainingId);

        Map<String, Object> command = Map.of(
                "command", "PAUSE_TRAINING",
                "trainingId", trainingId
        );

        sendCommandToRobot(command);
    }

    public void sendTrainingResumeCommand(Long trainingId) {
        log.info("로봇에 훈련 재개 명령 전송: trainingId={}", trainingId);

        Map<String, Object> command = Map.of(
                "command", "RESUME_TRAINING",
                "trainingId", trainingId
        );

        sendCommandToRobot(command);
    }

    public void sendTrainingStopCommand(Long trainingId) {
        log.info("로봇에 훈련 종료 명령 전송: trainingId={}", trainingId);

        Map<String, Object> command = Map.of(
                "command", "STOP_TRAINING",
                "trainingId", trainingId
        );

        sendCommandToRobot(command);
    }

    // ========== 공 수거 명령 ==========

    public void sendCollectionStartCommand() {
        log.info("로봇에 공 수거 시작 명령 전송");
        Map<String, Object> command = new HashMap<>();
        command.put("command", "START_COLLECTION");
        sendCommandToRobot(command);
    }

    public void sendCollectionStopCommand() {
        log.info("로봇에 공 수거 종료 명령 전송");
        Map<String, Object> command = Map.of(
                "command", "STOP_COLLECTION"
        );
        sendCommandToRobot(command);
    }

    // ========== 연결 권한 확인 ==========

    public boolean hasRobotAccess(Long userId) {
        Optional<RobotSession> sessionOpt = robotSessionRepository.findByConnectedUserId(userId);
        return sessionOpt.isPresent() && !sessionOpt.get().isExpired();
    }

    // ========== 만료된 세션 정리 (5분마다) ==========

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void cleanupExpiredSessions() {
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusMinutes(30);
            List<RobotSession> expiredSessions = robotSessionRepository.findExpiredSessions(expireTime);

            for (RobotSession session : expiredSessions) {
                log.info("만료된 세션 정리: userId={}", session.getConnectedUserId());
                requestRobotDisconnection(session.getConnectedUserId());
                session.disconnect();
                robotSessionRepository.save(session);
            }

            if (!expiredSessions.isEmpty()) {
                broadcastRobotStatus();
            }
        } catch (Exception e) {
            log.error("만료된 세션 정리 중 오류 발생", e);
        }
    }

    // ========== 실제 로봇 통신 메서드들 ==========

    private boolean requestRobotConnection(Long userId) {
        try {
            Map<String, Object> request = Map.of("userId", userId);

            String url = robotApiBaseUrl + "/api/robot/connect";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            return response != null && Boolean.TRUE.equals(response.get("success"));
        } catch (Exception e) {
            log.error("로봇 연결 요청 실패: userId={}", userId, e);
            return false;
        }
    }

    private void requestRobotDisconnection(Long userId) {
        try {
            Map<String, Object> request = Map.of("userId", userId);

            String url = robotApiBaseUrl + "/api/robot/disconnect";
            restTemplate.postForObject(url, request, Map.class);
        } catch (Exception e) {
            log.error("로봇 연결 해제 요청 실패: userId={}", userId, e);
        }
    }

    private Map<String, Object> getRobotRealTimeStatus() {
        try {
            String url = robotApiBaseUrl + "/api/robot/status";
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            return response != null ? response : new HashMap<>();
        } catch (Exception e) {
            log.error("로봇 실시간 상태 조회 실패", e);
            return Map.of("ballCount", 0, "batteryPercentage", 0);
        }
    }

    private void sendCommandToRobot(Map<String, Object> command) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(command, headers);

            String url = robotApiBaseUrl + "/api/robot/command";
            restTemplate.postForObject(url, entity, Map.class);

            log.info("로봇 명령 전송 성공: {}", command.get("command"));
        } catch (Exception e) {
            log.error("로봇 명령 전송 실패: command={}", command.get("command"), e);
            throw new BusinessException(ResponseCode.ROBOT_COMMUNICATION_ERROR);
        }
    }

    // ========== 유틸리티 메서드들 ==========

    private RobotSession getOrCreateRobotSession() {
        Optional<RobotSession> sessionOpt = robotSessionRepository.findActiveSession();

        if (sessionOpt.isPresent()) {
            return sessionOpt.get();
        }

        RobotSession session = RobotSession.builder()
                .state(RobotSession.RobotState.IDLE)
                .build();

        return robotSessionRepository.save(session);
    }

    @Transactional
    protected void disconnectExpiredSession(RobotSession session) {
        log.info("만료된 로봇 세션 자동 해제: userId={}", session.getConnectedUserId());
        requestRobotDisconnection(session.getConnectedUserId());
        session.disconnect();
        robotSessionRepository.save(session);
        broadcastRobotStatus();
    }

    private void broadcastRobotStatus() {
        try {
            RobotStatusResponse status = getRobotStatus();
            messagingTemplate.convertAndSend("/topic/robot/status", status);
        } catch (Exception e) {
            log.error("로봇 상태 브로드캐스트 중 오류 발생", e);
        }
    }
}