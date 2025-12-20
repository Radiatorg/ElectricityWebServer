package com.verchuk.electro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalculationReportResponse {
    private Long projectId;
    private String projectName;
    private BigDecimal totalPowerConsumption;
    private BigDecimal totalCurrent;
    private Integer totalAppliances;
    private List<RoomCalculationResponse> roomCalculations;
    private List<ApplianceSummaryResponse> applianceSummaries;
}

