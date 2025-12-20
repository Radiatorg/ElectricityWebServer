package com.verchuk.electro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipmentItemResponse {
    private String category;
    private String name;
    private String specification;
    private Integer quantity;
    private String notes;
}

