package com.verchuk.electro.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

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
    @Deprecated // Оставляем для обратной совместимости
    private String category;

    private List<Long> categoryIds; // Новое поле для множественных категорий
    
    private List<String> newCategoryNames; // Новые категории для создания

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Positive(message = "Width must be positive")
    private BigDecimal width; // ширина прибора в см

    @Positive(message = "Height must be positive")
    private BigDecimal height; // высота прибора в см
}

