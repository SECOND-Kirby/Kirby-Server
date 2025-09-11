package com.second.kirby.dto;

import com.second.kirby.exception.ResponseCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 응답 형식")
public record ResponseDto<T>(
        @Schema(description = "성공 여부")
        boolean success,

        @Schema(description = "응답 코드")
        String code,

        @Schema(description = "응답 메시지")
        String message,

        @Schema(description = "응답 데이터")
        T data
) {
    // 성공 응답 (데이터 있음)
    public static <T> ResponseDto<T> success(T data, String message) {
        return new ResponseDto<>(true, "S001", message, data);
    }

    // 성공 응답 (데이터 없음)
    public static ResponseDto<Void> success(String message) {
        return new ResponseDto<>(true, "S001", message, null);
    }

    // 실패 응답 (데이터 있음)
    public static <T> ResponseDto<T> error(ResponseCode responseCode, String message, T data) {
        return new ResponseDto<>(false, responseCode.getCode(), message, data);
    }

    // 실패 응답 (데이터 없음)
    public static ResponseDto<Void> error(ResponseCode responseCode, String message) {
        return new ResponseDto<>(false, responseCode.getCode(), message, null);
    }
}