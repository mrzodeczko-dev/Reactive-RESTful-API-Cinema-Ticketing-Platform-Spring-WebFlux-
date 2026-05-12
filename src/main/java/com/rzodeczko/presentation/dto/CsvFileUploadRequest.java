package com.rzodeczko.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CsvFileUploadRequest(
        @Schema(type = "string", format = "binary", description = "CSV file")
        String file
) {
}
