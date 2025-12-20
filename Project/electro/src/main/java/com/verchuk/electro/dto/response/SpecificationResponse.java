package com.verchuk.electro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecificationResponse {
    private Long projectId;
    private String projectName;
    private List<EquipmentItemResponse> equipmentItems;
    private String recommendations;
}

