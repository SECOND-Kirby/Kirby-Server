package com.second.kirby.controller;

import com.second.kirby.dto.ResponseDto;
import com.second.kirby.dto.response.collection.CollectionStatusDto;
import com.second.kirby.service.CollectionService;
import com.second.kirby.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "공 수거", description = "공 수거 세션 관리 API")
@RestController
@RequestMapping("/api/collection")
@RequiredArgsConstructor
public class CollectionController {

    private final CollectionService collectionService;
    private final UserService userService;

    @Operation(summary = "공 수거 시작")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/start")
    public ResponseEntity<ResponseDto<CollectionStatusDto>> start(
            Authentication authentication
    ) {
        Long userId = userService.findByUsername(authentication.getName()).getId();
        CollectionStatusDto dto = collectionService.startCollection(userId);
        return ResponseEntity.ok(ResponseDto.success(dto, "공 수거 시작"));
    }

    @Operation(summary = "공 수거 종료")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/stop")
    public ResponseEntity<ResponseDto<Void>> stop(Authentication authentication) {
        Long userId = userService.findByUsername(authentication.getName()).getId();
        collectionService.stopCollection(userId);
        return ResponseEntity.ok(ResponseDto.success("공 수거 종료"));
    }
}
