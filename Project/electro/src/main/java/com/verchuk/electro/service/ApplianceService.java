package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.ApplianceRequest;
import com.verchuk.electro.dto.response.ApplianceResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.Appliance;
import com.verchuk.electro.repository.ApplianceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplianceService {
    @Autowired
    private ApplianceRepository applianceRepository;

    public List<ApplianceResponse> getAllActiveAppliances() {
        return applianceRepository.findAllActiveOrderedByName().stream()
                .map(this::mapToApplianceResponse)
                .collect(Collectors.toList());
    }

    public ApplianceResponse getApplianceById(Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance", "id", id));
        return mapToApplianceResponse(appliance);
    }

    @Transactional
    public ApplianceResponse createAppliance(ApplianceRequest request) {
        Appliance appliance = Appliance.builder()
                .name(request.getName())
                .description(request.getDescription())
                .powerConsumption(request.getPowerConsumption())
                .voltage(request.getVoltage())
                .current(request.getCurrent())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();

        return mapToApplianceResponse(applianceRepository.save(appliance));
    }

    @Transactional
    public ApplianceResponse updateAppliance(Long id, ApplianceRequest request) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance", "id", id));

        appliance.setName(request.getName());
        appliance.setDescription(request.getDescription());
        appliance.setPowerConsumption(request.getPowerConsumption());
        appliance.setVoltage(request.getVoltage());
        appliance.setCurrent(request.getCurrent());
        appliance.setCategory(request.getCategory());
        if (request.getImageUrl() != null) {
            appliance.setImageUrl(request.getImageUrl());
        }

        return mapToApplianceResponse(applianceRepository.save(appliance));
    }

    @Transactional
    public void deleteAppliance(Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance", "id", id));
        appliance.setActive(false);
        applianceRepository.save(appliance);
    }

    private ApplianceResponse mapToApplianceResponse(Appliance appliance) {
        return ApplianceResponse.builder()
                .id(appliance.getId())
                .name(appliance.getName())
                .description(appliance.getDescription())
                .powerConsumption(appliance.getPowerConsumption())
                .voltage(appliance.getVoltage())
                .current(appliance.getCurrent())
                .category(appliance.getCategory())
                .imageUrl(appliance.getImageUrl())
                .active(appliance.getActive())
                .build();
    }
}

