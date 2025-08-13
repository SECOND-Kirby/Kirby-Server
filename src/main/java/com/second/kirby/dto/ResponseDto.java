package com.second.kirby.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResponseDto<T> {

    private final LocalDateTime timeStamp;
    private final String message;
    private final T data;

    public ResponseDto(LocalDateTime timeStamp, String message, T data) {
        this.timeStamp = timeStamp;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseDto<T> of(String message) {
        return new ResponseDto<>(LocalDateTime.now(), message, null);
    }

    public static <T> ResponseDto<T> of(T data) {
        return new ResponseDto<>(LocalDateTime.now(), null, data);
    }

    public static <T> ResponseDto<T> of(T data, String message) {
        return new ResponseDto<>(LocalDateTime.now(), message, data);
    }
}