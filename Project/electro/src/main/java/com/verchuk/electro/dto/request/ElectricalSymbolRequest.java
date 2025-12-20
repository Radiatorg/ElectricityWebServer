package com.verchuk.electro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectricalSymbolRequest {
    @NotBlank(message = "Name is required")
    private String name;

    private String svgPath;

    @NotBlank(message = "Type is required")
    private String type;

    private String category;
    private Double defaultWidth;
    private Double defaultHeight;
    private Boolean active;
}

