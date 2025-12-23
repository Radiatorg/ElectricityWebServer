package com.verchuk.electro.service;

import com.verchuk.electro.dto.response.ApplianceSummaryResponse;
import com.verchuk.electro.dto.response.CalculationReportResponse;
import com.verchuk.electro.dto.response.RoomCalculationResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.Project;
import com.verchuk.electro.model.ProjectAppliance;
import com.verchuk.electro.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CalculationService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    public CalculationReportResponse getCalculationReport(Long projectId) {
        var designer = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndDesigner(projectId, designer)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<ProjectAppliance> projectAppliances = project.getProjectAppliances();
        if (projectAppliances == null || projectAppliances.isEmpty()) {
            return CalculationReportResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .totalPowerConsumption(BigDecimal.ZERO)
                    .totalCurrent(BigDecimal.ZERO)
                    .totalAppliances(0)
                    .roomCalculations(List.of())
                    .applianceSummaries(List.of())
                    .build();
        }

        // Пересчитываем общую мощность с учетом коэффициентов помещений
        BigDecimal totalPower = projectAppliances.stream()
                .map(pa -> {
                    BigDecimal appliancePower = pa.getTotalPower() != null ? pa.getTotalPower() : BigDecimal.ZERO;
                    // Если прибор привязан к помещению, применяем коэффициент помещения
                    if (pa.getRoom() != null && pa.getRoom().getRoomType() != null) {
                        BigDecimal coefficient = pa.getRoom().getRoomType().getEffectiveCoefficient();
                        return appliancePower.multiply(coefficient);
                    }
                    return appliancePower;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCurrent = projectAppliances.stream()
                .map(pa -> {
                    if (pa.getAppliance().getCurrent() != null && pa.getQuantity() != null) {
                        return pa.getAppliance().getCurrent().multiply(BigDecimal.valueOf(pa.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, List<ProjectAppliance>> appliancesByRoom = projectAppliances.stream()
                .filter(pa -> pa.getRoom() != null)
                .collect(Collectors.groupingBy(pa -> pa.getRoom().getId()));

        List<RoomCalculationResponse> roomCalculations = appliancesByRoom.entrySet().stream()
                .map(entry -> {
                    var room = entry.getValue().get(0).getRoom();
                    // Сумма мощностей всех приборов в помещении
                    BigDecimal rawRoomPower = entry.getValue().stream()
                            .map(pa -> pa.getTotalPower() != null ? pa.getTotalPower() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    
                    // Применяем коэффициент мощности помещения
                    BigDecimal roomCoefficient = room.getRoomType().getEffectiveCoefficient();
                    BigDecimal roomPower = rawRoomPower.multiply(roomCoefficient);
                    
                    int applianceCount = entry.getValue().stream()
                            .mapToInt(pa -> pa.getQuantity() != null ? pa.getQuantity() : 0)
                            .sum();

                    return RoomCalculationResponse.builder()
                            .roomId(room.getId())
                            .roomName(room.getName())
                            .totalPower(roomPower)
                            .applianceCount(applianceCount)
                            .build();
                })
                .collect(Collectors.toList());

        Map<Long, List<ProjectAppliance>> appliancesByAppliance = projectAppliances.stream()
                .collect(Collectors.groupingBy(pa -> pa.getAppliance().getId()));

        List<ApplianceSummaryResponse> applianceSummaries = appliancesByAppliance.entrySet().stream()
                .map(entry -> {
                    var appliance = entry.getValue().get(0).getAppliance();
                    int totalQuantity = entry.getValue().stream()
                            .mapToInt(pa -> pa.getQuantity() != null ? pa.getQuantity() : 0)
                            .sum();
                    BigDecimal totalPowerForAppliance = entry.getValue().stream()
                            .map(pa -> pa.getTotalPower() != null ? pa.getTotalPower() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    return ApplianceSummaryResponse.builder()
                            .applianceId(appliance.getId())
                            .applianceName(appliance.getName())
                            .totalQuantity(totalQuantity)
                            .totalPower(totalPowerForAppliance)
                            .build();
                })
                .collect(Collectors.toList());

        return CalculationReportResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .totalPowerConsumption(totalPower)
                .totalCurrent(totalCurrent)
                .totalAppliances(projectAppliances.size())
                .roomCalculations(roomCalculations)
                .applianceSummaries(applianceSummaries)
                .build();
    }
}

