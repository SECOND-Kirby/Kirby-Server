package com.second.kirby.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // 성공
    SUCCESS(HttpStatus.OK, "SUCCESS", "성공"),

    // 인증 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "USERNAME_ALREADY_EXISTS", "이미 사용중인 아이디입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 사용중인 이메일입니다."),
    PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PHONE_NUMBER_ALREADY_EXISTS", "이미 사용중인 전화번호입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "비밀번호가 일치하지 않습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "INVALID_PASSWORD", "비밀번호가 틀렸습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "EXPIRED_TOKEN", "만료된 토큰입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근이 거부되었습니다."),

    // 로봇 관련
    ROBOT_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "ROBOT_NOT_CONNECTED", "로봇이 연결되지 않았습니다."),
    INVALID_COMMAND(HttpStatus.BAD_REQUEST, "INVALID_COMMAND", "유효하지 않은 명령입니다."),
    ROBOT_BUSY(HttpStatus.CONFLICT, "ROBOT_BUSY", "로봇이 다른 작업을 수행 중입니다."),
    COMMAND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "COMMAND_FAILED", "명령 실행에 실패했습니다."),

    // 일반 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    WEBSOCKET_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WEBSOCKET_ERROR", "WebSocket 통신 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}