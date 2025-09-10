package com.second.kirby.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // ========== 성공 ==========
    SUCCESS(HttpStatus.OK, "S001", "성공"),

    // ========== 일반 에러 ==========
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "E001", "서버 내부 오류가 발생했습니다"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "E002", "잘못된 입력값입니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "E003", "지원하지 않는 HTTP 메서드입니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "E004", "접근 권한이 없습니다"),

    // ========== 인증/인가 ==========
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A001", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "A002", "토큰이 만료되었습니다"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A003", "인증이 필요합니다"),

    // ========== 사용자 관리 ==========
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 사용자명입니다"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U003", "이미 존재하는 이메일입니다"),
    PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "U004", "이미 존재하는 전화번호입니다"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U005", "비밀번호가 일치하지 않습니다"),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, "U006", "비밀번호 확인이 일치하지 않습니다"),

    // ========== 로봇 관리 ==========
    ROBOT_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "R001", "로봇에 연결되지 않았습니다"),
    ROBOT_ALREADY_CONNECTED(HttpStatus.CONFLICT, "R002", "이미 다른 사용자가 로봇을 사용중입니다"),
    ROBOT_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "R003", "로봇 연결에 실패했습니다"),
    ROBOT_COMMUNICATION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "R004", "로봇과의 통신 중 오류가 발생했습니다"),
    ROBOT_SESSION_EXPIRED(HttpStatus.UNAUTHORIZED, "R005", "로봇 세션이 만료되었습니다"),

    // ========== 훈련 관리 ==========
    TRAINING_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "훈련 세션을 찾을 수 없습니다"),
    TRAINING_ALREADY_EXISTS(HttpStatus.CONFLICT, "T002", "이미 진행중인 훈련이 있습니다"),
    TRAINING_INVALID_STATUS(HttpStatus.BAD_REQUEST, "T003", "현재 상태에서는 해당 작업을 수행할 수 없습니다"),
    TRAINING_CONFIG_INVALID(HttpStatus.BAD_REQUEST, "T004", "훈련 설정값이 유효하지 않습니다"),
    TRAINING_TIME_EXCEEDED(HttpStatus.BAD_REQUEST, "T005", "훈련 시간이 초과되었습니다"),
    TRAINING_ROBOT_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "T006", "훈련 중 로봇 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}