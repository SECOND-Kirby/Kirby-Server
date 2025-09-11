package com.second.kirby.dto.response;

import com.second.kirby.domain.RobotSession;

import java.time.LocalDateTime;

// 로봇 연결 응답
public record RobotConnectResponse(
        boolean success,
        String message,
        RobotSession.RobotState state,
        LocalDateTime connectedAt
) {
    public static RobotConnectResponse success(RobotSession session) {
        return new RobotConnectResponse(
                true,
                "로봇에 연결되었습니다.",
                session.getState(),
                session.getConnectedAt()
        );
    }

    public static RobotConnectResponse failure(String message) {
        return new RobotConnectResponse(
                false,
                message,
                null,
                null
        );
    }
}