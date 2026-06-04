package com.thegamersstation.marketplace.post.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkAsSoldRequest {
    @NotNull
    private Boolean soldThroughPlatform;
}
