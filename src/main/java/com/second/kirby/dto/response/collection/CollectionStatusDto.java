package com.second.kirby.dto.response.collection;

import com.second.kirby.domain.BallCollection;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공 수거 상태")
public record CollectionStatusDto(
        Long sessionId,
        Integer collectedCount,
        String status
) {
    public static CollectionStatusDto of(BallCollection s) {
        return new CollectionStatusDto(s.getId(), s.getCollectedCount(), s.getStatus().name());
    }
}
