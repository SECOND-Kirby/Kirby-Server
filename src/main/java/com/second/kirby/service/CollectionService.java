package com.second.kirby.service;

import com.second.kirby.domain.BallCollection;
import com.second.kirby.domain.RobotSession;
import com.second.kirby.dto.response.collection.CollectionStatusDto;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import com.second.kirby.repository.BallCollectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CollectionService {

    private final BallCollectionRepository ballCollectionRepository;
    private final RobotBridgeService robotBridgeService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public CollectionStatusDto startCollection(Long userId) {
        ballCollectionRepository.findByUserIdAndStatus(userId, BallCollection.Status.IN_PROGRESS)
                .ifPresent(s -> { throw new BusinessException(ResponseCode.TRAINING_ALREADY_EXISTS, "진행중인 공 수거가 있습니다."); });

        if (!robotBridgeService.hasRobotAccess(userId)) {
            throw new BusinessException(ResponseCode.ROBOT_NOT_CONNECTED);
        }

        BallCollection session = BallCollection.builder()
                .userId(userId)
                .targetCount(100)
                .status(BallCollection.Status.READY)
                .build();
        session.start();
        session = ballCollectionRepository.save(session);

        robotBridgeService.updateRobotState(userId, RobotSession.RobotState.COLLECTING);
        robotBridgeService.sendCollectionStartCommand();

        CollectionStatusDto dto = CollectionStatusDto.of(session);
        messagingTemplate.convertAndSend("/topic/collection/status", dto);
        log.info("공 수거 시작: sessionId={}, userId={}", session.getId(), userId);
        return dto;
    }

    @Transactional
    public void stopCollection(Long userId) {
        BallCollection session = ballCollectionRepository.findByUserIdAndStatus(userId, BallCollection.Status.IN_PROGRESS)
                .orElseThrow(() -> new BusinessException(ResponseCode.TRAINING_NOT_FOUND, "진행중인 공 수거가 없습니다."));

        session.cancel();
        ballCollectionRepository.save(session);

        robotBridgeService.updateRobotState(userId, RobotSession.RobotState.IDLE);
        robotBridgeService.sendCollectionStopCommand();

        messagingTemplate.convertAndSend("/topic/collection/status", CollectionStatusDto.of(session));
        log.info("공 수거 종료: sessionId={}, userId={}", session.getId(), userId);
    }

    @Transactional
    public void handleCountUpdate(Long sessionId, int collectedCount) {
        BallCollection session = ballCollectionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ResponseCode.TRAINING_NOT_FOUND, "공 수거 세션을 찾을 수 없습니다."));

        session.updateCount(collectedCount);
        if (session.getTargetCount() != null && collectedCount >= session.getTargetCount()) {
            session.complete();
        }
        ballCollectionRepository.save(session);

        messagingTemplate.convertAndSend("/topic/collection/status", CollectionStatusDto.of(session));
    }
}
