package com.second.kirby.exception;

import com.second.kirby.dto.ResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseDto<Object>> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getMessage());

        ResponseDto<Object> response = ResponseDto.of(e.getResponseCode());

        if (isRobotApi()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(e.getResponseCode().getStatus()).body(response);
        }
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ResponseDto<List<FieldError>>> handleValidationException(Exception e) {
        log.error("Validation exception: {}", e.getMessage());

        List<FieldError> fieldErrors = extractFieldErrors(e);
        ResponseDto<List<FieldError>> response = ResponseDto.of(
                ResponseCode.INVALID_INPUT_VALUE,
                "입력값 검증에 실패했습니다.",
                fieldErrors
        );

        if (isRobotApi()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Object>> handleException(Exception e) {
        log.error("Unexpected exception: ", e);

        ResponseDto<Object> response = ResponseDto.of(ResponseCode.INTERNAL_SERVER_ERROR);

        if (isRobotApi()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 유틸리티 메서드들
    private boolean isRobotApi() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String path = request.getRequestURI();
                return path.startsWith("/api/robot/") || path.startsWith("/ws/");
            }
        } catch (Exception e) {
            log.warn("Failed to determine request context", e);
        }
        return false;
    }

    private List<FieldError> extractFieldErrors(Exception e) {
        if (e instanceof MethodArgumentNotValidException validException) {
            return validException.getBindingResult().getFieldErrors().stream()
                    .map(error -> new FieldError(error.getField(), error.getRejectedValue(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
        } else if (e instanceof BindException bindException) {
            return bindException.getBindingResult().getFieldErrors().stream()
                    .map(error -> new FieldError(error.getField(), error.getRejectedValue(), error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public record FieldError(String field, Object rejectedValue, String message) {
    }
}