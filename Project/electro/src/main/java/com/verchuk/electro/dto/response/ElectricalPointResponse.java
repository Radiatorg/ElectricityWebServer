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
public class ElectricalPointResponse {
    private Long id;
    private Long roomId;
    private String roomName;
    private ElectricalSymbolResponse electricalSymbol;
    private BigDecimal positionX;
    private BigDecimal positionY;
    private BigDecimal heightFromFloor;
    private BigDecimal rotation;
    private String group;
    private String notes;
}

