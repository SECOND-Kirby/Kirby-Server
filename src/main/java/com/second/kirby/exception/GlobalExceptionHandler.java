package com.second.kirby.exception;

import com.second.kirby.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseDto<Void>> handleBusinessException(BusinessException e) {
        log.warn("Business Exception: {}", e.getMessage());
        ResponseDto<Void> response = ResponseDto.error(e.getResponseCode(), e.getMessage());
        return ResponseEntity.status(e.getResponseCode().getHttpStatus()).body(response);
    }

    // 입력값 검증 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ResponseDto<Map<String, String>> response = ResponseDto.error(
                ResponseCode.INVALID_INPUT_VALUE,
                "입력값 검증에 실패했습니다",
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleGeneralException(Exception e) {
        log.error("Unexpected Exception: ", e);
        ResponseDto<Void> response = ResponseDto.error(
                ResponseCode.INTERNAL_SERVER_ERROR,
                "서버 내부 오류가 발생했습니다"
        );
        return ResponseEntity.internalServerError().body(response);
    }
}