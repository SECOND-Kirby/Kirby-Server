package com.second.kirby.dto;

import com.second.kirby.exception.ResponseCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResponseDto<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
    private final LocalDateTime timestamp;

    private ResponseDto(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // ResponseCode를 사용한 생성
    public static <T> ResponseDto<T> of(ResponseCode responseCode) {
        return new ResponseDto<>(
                responseCode == ResponseCode.SUCCESS,
                responseCode.getCode(),
                responseCode.getMessage(),
                null
        );
    }

    public static <T> ResponseDto<T> of(ResponseCode responseCode, T data) {
        return new ResponseDto<>(
                responseCode == ResponseCode.SUCCESS,
                responseCode.getCode(),
                responseCode.getMessage(),
                data
        );
    }

    public static <T> ResponseDto<T> of(ResponseCode responseCode, String customMessage, T data) {
        return new ResponseDto<>(
                responseCode == ResponseCode.SUCCESS,
                responseCode.getCode(),
                customMessage,
                data
        );
    }

    // 빠른 성공 응답
    public static <T> ResponseDto<T> success(T data, String message) {
        return new ResponseDto<>(true, ResponseCode.SUCCESS.getCode(), message, data);
    }

    public static <T> ResponseDto<T> success(String message) {
        return new ResponseDto<>(true, ResponseCode.SUCCESS.getCode(), message, null);
    }

    public static <T> ResponseDto<T> success(T data) {
        return new ResponseDto<>(true, ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }
}