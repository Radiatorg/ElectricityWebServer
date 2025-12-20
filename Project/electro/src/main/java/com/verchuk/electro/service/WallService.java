package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.WallOpeningRequest;
import com.verchuk.electro.dto.request.WallRequest;
import com.verchuk.electro.dto.response.WallOpeningResponse;
import com.verchuk.electro.dto.response.WallResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.FloorPlan;
import com.verchuk.electro.model.Wall;
import com.verchuk.electro.model.WallOpening;
import com.verchuk.electro.repository.FloorPlanRepository;
import com.verchuk.electro.repository.WallRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WallService {
    @Autowired
    private WallRepository wallRepository;

    @Autowired
    private FloorPlanRepository floorPlanRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public WallResponse createWall(Long projectId, WallRequest request) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);

        Wall wall = Wall.builder()
                .floorPlan(floorPlan)
                .startX(request.getStartX())
                .startY(request.getStartY())
                .endX(request.getEndX())
                .endY(request.getEndY())
                .thickness(request.getThickness() != null ? request.getThickness() : BigDecimal.valueOf(20))
                .wallType(request.getWallType())
                .build();

        if (request.getOpenings() != null && !request.getOpenings().isEmpty()) {
            final Wall wallFinal = wall; // Создаем final переменную для использования в лямбде
            List<WallOpening> openings = request.getOpenings().stream()
                    .map(openingRequest -> WallOpening.builder()
                            .wall(wallFinal)
                            .position(openingRequest.getPosition())
                            .width(openingRequest.getWidth())
                            .height(openingRequest.getHeight())
                            .openingType(openingRequest.getOpeningType())
                            .build())
                    .collect(Collectors.toList());
            wall.setOpenings(openings);
        }

        wall = wallRepository.save(wall);
        return mapToResponse(wall);
    }

    @Transactional
    public WallResponse updateWall(Long projectId, Long wallId, WallRequest request) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);
        Wall wall = wallRepository.findById(wallId)
                .orElseThrow(() -> new ResourceNotFoundException("Wall", "id", wallId));

        if (!wall.getFloorPlan().getId().equals(floorPlan.getId())) {
            throw new ResourceNotFoundException("Wall", "id", wallId);
        }

        wall.setStartX(request.getStartX());
        wall.setStartY(request.getStartY());
        wall.setEndX(request.getEndX());
        wall.setEndY(request.getEndY());
        if (request.getThickness() != null) {
            wall.setThickness(request.getThickness());
        }
        wall.setWallType(request.getWallType());

        // Обновление проемов
        if (request.getOpenings() != null) {
            wall.getOpenings().clear();
            final Wall wallFinal = wall; // Создаем final переменную для использования в лямбде
            request.getOpenings().forEach(openingRequest -> {
                WallOpening opening = WallOpening.builder()
                        .wall(wallFinal)
                        .position(openingRequest.getPosition())
                        .width(openingRequest.getWidth())
                        .height(openingRequest.getHeight())
                        .openingType(openingRequest.getOpeningType())
                        .build();
                wallFinal.getOpenings().add(opening);
            });
        }

        wall = wallRepository.save(wall);
        return mapToResponse(wall);
    }

    @Transactional
    public void deleteWall(Long projectId, Long wallId) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);
        Wall wall = wallRepository.findById(wallId)
                .orElseThrow(() -> new ResourceNotFoundException("Wall", "id", wallId));

        if (!wall.getFloorPlan().getId().equals(floorPlan.getId())) {
            throw new ResourceNotFoundException("Wall", "id", wallId);
        }

        wallRepository.delete(wall);
    }

    public List<WallResponse> getWallsByFloorPlan(Long floorPlanId) {
        List<Wall> walls = wallRepository.findByFloorPlanId(floorPlanId);
        return walls.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<WallResponse> saveWalls(Long projectId, List<WallRequest> requests) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);
        
        // Удаляем старые стены
        wallRepository.deleteByFloorPlanId(floorPlan.getId());

        // Создаем новые стены
        List<Wall> walls = requests.stream()
                .map(request -> {
                    Wall wall = Wall.builder()
                            .floorPlan(floorPlan)
                            .startX(request.getStartX())
                            .startY(request.getStartY())
                            .endX(request.getEndX())
                            .endY(request.getEndY())
                            .thickness(request.getThickness() != null ? request.getThickness() : BigDecimal.valueOf(20))
                            .wallType(request.getWallType())
                            .build();

                    if (request.getOpenings() != null && !request.getOpenings().isEmpty()) {
                        List<WallOpening> openings = request.getOpenings().stream()
                                .map(openingRequest -> WallOpening.builder()
                                        .wall(wall)
                                        .position(openingRequest.getPosition())
                                        .width(openingRequest.getWidth())
                                        .height(openingRequest.getHeight())
                                        .openingType(openingRequest.getOpeningType())
                                        .build())
                                .collect(Collectors.toList());
                        wall.setOpenings(openings);
                    }

                    return wall;
                })
                .collect(Collectors.toList());

        walls = wallRepository.saveAll(walls);
        return walls.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FloorPlan getFloorPlanForProject(Long projectId) {
        var designer = userService.getCurrentUser();
        return floorPlanRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("FloorPlan", "projectId", projectId));
    }

    private WallResponse mapToResponse(Wall wall) {
        List<WallOpeningResponse> openings = wall.getOpenings() != null
                ? wall.getOpenings().stream()
                        .map(this::mapOpeningToResponse)
                        .collect(Collectors.toList())
                : List.of();

        return WallResponse.builder()
                .id(wall.getId())
                .startX(wall.getStartX())
                .startY(wall.getStartY())
                .endX(wall.getEndX())
                .endY(wall.getEndY())
                .thickness(wall.getThickness())
                .wallType(wall.getWallType())
                .openings(openings)
                .build();
    }

    private WallOpeningResponse mapOpeningToResponse(WallOpening opening) {
        return WallOpeningResponse.builder()
                .id(opening.getId())
                .position(opening.getPosition())
                .width(opening.getWidth())
                .height(opening.getHeight())
                .openingType(opening.getOpeningType())
                .build();
    }
}

