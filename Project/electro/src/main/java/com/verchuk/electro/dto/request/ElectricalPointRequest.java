package com.verchuk.electro.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElectricalPointRequest {
    @NotNull(message = "Electrical symbol ID is required")
    private Long electricalSymbolId;

    private Long roomId;

    @NotNull(message = "Position X is required")
    private BigDecimal positionX;

    @NotNull(message = "Position Y is required")
    private BigDecimal positionY;

    private BigDecimal heightFromFloor;
    private BigDecimal rotation;
    private String group;
    private String notes;
}

