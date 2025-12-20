package com.verchuk.electro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplianceRequest {
    @NotBlank(message = "Appliance name is required")
    @Size(max = 100, message = "Appliance name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Power consumption is required")
    @Positive(message = "Power consumption must be positive")
    private BigDecimal powerConsumption;

    @Positive(message = "Voltage must be positive")
    private BigDecimal voltage;

    @Positive(message = "Current must be positive")
    private BigDecimal current;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;
}

