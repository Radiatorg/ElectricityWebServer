package com.verchuk.electro.service;

import com.verchuk.electro.dto.response.EquipmentItemResponse;
import com.verchuk.electro.dto.response.SpecificationResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.Project;
import com.verchuk.electro.model.ProjectAppliance;
import com.verchuk.electro.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SpecificationService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    public SpecificationResponse getSpecification(Long projectId) {
        var designer = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndDesigner(projectId, designer)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        List<ProjectAppliance> projectAppliances = project.getProjectAppliances();
        List<EquipmentItemResponse> equipmentItems = new ArrayList<>();

        if (projectAppliances != null && !projectAppliances.isEmpty()) {
            BigDecimal totalPower = projectAppliances.stream()
                    .map(pa -> pa.getTotalPower() != null ? pa.getTotalPower() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, List<ProjectAppliance>> byCategory = projectAppliances.stream()
                    .collect(Collectors.groupingBy(pa -> pa.getAppliance().getCategory() != null ?
                            pa.getAppliance().getCategory() : "Other"));

            for (Map.Entry<String, List<ProjectAppliance>> entry : byCategory.entrySet()) {
                String category = entry.getKey();
                List<ProjectAppliance> appliances = entry.getValue();

                for (ProjectAppliance pa : appliances) {
                    equipmentItems.add(EquipmentItemResponse.builder()
                            .category(category)
                            .name(pa.getAppliance().getName())
                            .specification(String.format("Power: %s W, Voltage: %s V, Quantity: %d",
                                    pa.getAppliance().getPowerConsumption(),
                                    pa.getAppliance().getVoltage() != null ? pa.getAppliance().getVoltage() : "N/A",
                                    pa.getQuantity()))
                            .quantity(pa.getQuantity())
                            .notes(pa.getRoom() != null ? "Room: " + pa.getRoom().getName() : null)
                            .build());
                }
            }

            equipmentItems.add(EquipmentItemResponse.builder()
                    .category("Electrical Panel")
                    .name("Main Circuit Breaker")
                    .specification(String.format("Rated current: %.0f A", totalPower.divide(BigDecimal.valueOf(220), 2, RoundingMode.UP)))
                    .quantity(1)
                    .notes("Based on total power consumption")
                    .build());
        }

        String recommendations = generateRecommendations(projectAppliances);

        return SpecificationResponse.builder()
                .projectId(project.getId())
                .projectName(project.getName())
                .equipmentItems(equipmentItems)
                .recommendations(recommendations)
                .build();
    }

    private String generateRecommendations(List<ProjectAppliance> projectAppliances) {
        if (projectAppliances == null || projectAppliances.isEmpty()) {
            return "No appliances in project. Add appliances to generate recommendations.";
        }

        BigDecimal totalPower = projectAppliances.stream()
                .map(pa -> pa.getTotalPower() != null ? pa.getTotalPower() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<String> recommendations = new ArrayList<>();
        recommendations.add(String.format("Total power consumption: %.2f W", totalPower));

        if (totalPower.compareTo(BigDecimal.valueOf(5000)) > 0) {
            recommendations.add("Consider installing a three-phase electrical system for high power consumption.");
        }

        recommendations.add("Ensure proper grounding and protection devices are installed.");
        recommendations.add("Use appropriate wire cross-sections based on current load.");

        return String.join("\n", recommendations);
    }
}

