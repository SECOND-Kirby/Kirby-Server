package com.second.kirby.dto.response;

import com.second.kirby.domain.RobotSession;

import java.time.LocalDateTime;

// 로봇 상태 응답
public record RobotStatusResponse(
        RobotSession.RobotState state,
        boolean isConnected,
        Long connectedUserId,
        String connectedUsername,
        LocalDateTime connectedAt,
        LocalDateTime lastHeartbeat,
        boolean canConnect,
        String message,
        Integer ballCount,
        Integer batteryPercentage
) {
    public static RobotStatusResponse of(RobotSession session, String connectedUsername, Integer ballCount, Integer batteryPercentage) {
        boolean canConnect = !session.isConnected();
        String message = generateMessage(session, connectedUsername, canConnect);

        return new RobotStatusResponse(
                session.getState(),
                session.isConnected(),
                session.getConnectedUserId(),
                connectedUsername,
                session.getConnectedAt(),
                session.getLastHeartbeat(),
                canConnect,
                message,
                ballCount,
                batteryPercentage
        );
    }

    private static String generateMessage(RobotSession session, String connectedUsername, boolean canConnect) {
        if (canConnect) {
            return "로봇이 대기중입니다. 연결하세요!";
        } else {
            return String.format("%s님이 사용중입니다.", connectedUsername);
        }
    }
}