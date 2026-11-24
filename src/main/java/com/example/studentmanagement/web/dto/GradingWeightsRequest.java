package com.example.studentmanagement.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GradingWeightsRequest(
        @NotNull @Min(0) @Max(100) Integer midtermWeight,
        @NotNull @Min(0) @Max(100) Integer finalWeight,
        @NotNull @Min(0) @Max(100) Integer projectWeight) {
}
