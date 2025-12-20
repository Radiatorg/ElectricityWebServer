package com.verchuk.electro.service;

import com.verchuk.electro.dto.response.ApplianceStatisticsResponse;
import com.verchuk.electro.model.Appliance;
import com.verchuk.electro.model.ProjectAppliance;
import com.verchuk.electro.repository.ApplianceRepository;
import com.verchuk.electro.repository.ProjectApplianceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private ProjectApplianceRepository projectApplianceRepository;

    public List<ApplianceStatisticsResponse> getApplianceStatistics() {
        List<Appliance> allAppliances = applianceRepository.findAll();

        return allAppliances.stream()
                .map(appliance -> {
                    List<ProjectAppliance> projectAppliances = projectApplianceRepository.findAll().stream()
                            .filter(pa -> pa.getAppliance().getId().equals(appliance.getId()))
                            .collect(Collectors.toList());

                    long usageCountSum = projectAppliances.stream()
                            .mapToLong(pa -> pa.getQuantity() != null ? pa.getQuantity().longValue() : 0L)
                            .sum();
                    Long usageCount = usageCountSum;

                    BigDecimal totalPowerConsumption = projectAppliances.stream()
                            .map(pa -> pa.getTotalPower() != null ? pa.getTotalPower() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Set<Long> projectIds = projectAppliances.stream()
                            .map(pa -> pa.getProject().getId())
                            .collect(Collectors.toSet());

                    return ApplianceStatisticsResponse.builder()
                            .applianceId(appliance.getId())
                            .applianceName(appliance.getName())
                            .usageCount(usageCount)
                            .totalPowerConsumption(totalPowerConsumption)
                            .projectsCount(projectIds.size())
                            .build();
                })
                .collect(Collectors.toList());
    }
}

