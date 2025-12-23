package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.WallRequest;
import com.verchuk.electro.dto.response.WallOpeningResponse;
import com.verchuk.electro.dto.response.WallResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.FloorPlan;
import com.verchuk.electro.model.Wall;
import com.verchuk.electro.model.WallOpening;
import com.verchuk.electro.repository.FloorPlanRepository;
import com.verchuk.electro.repository.WallRepository;
import com.verchuk.electro.repository.RoomRepository;
import com.verchuk.electro.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WallService {

    @Autowired
    private WallRepository wallRepository;

    @Autowired
    private FloorPlanRepository floorPlanRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private UserService userService;

    /**
     * Создание одной стены (используется при обычном рисовании)
     */
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
                .openings(new ArrayList<>())
                .build();

        if (request.getOpenings() != null && !request.getOpenings().isEmpty()) {
            Wall finalWall = wall;
            List<WallOpening> openings = request.getOpenings().stream()
                    .map(opReq -> WallOpening.builder()
                            .wall(finalWall)
                            .position(opReq.getPosition())
                            .width(opReq.getWidth())
                            .height(opReq.getHeight())
                            .openingType(opReq.getOpeningType())
                            .build())
                    .toList();
            wall.getOpenings().addAll(openings);
        }

        wall = wallRepository.save(wall);
        return mapToResponse(wall);
    }

    /**
     * Обновление существующей стены
     */
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

        // Обновление проемов (очистка и замена)
        if (request.getOpenings() != null) {
            wall.getOpenings().clear();
            Wall finalWall = wall;
            List<WallOpening> newOpenings = request.getOpenings().stream()
                    .map(opReq -> WallOpening.builder()
                            .wall(finalWall)
                            .position(opReq.getPosition())
                            .width(opReq.getWidth())
                            .height(opReq.getHeight())
                            .openingType(opReq.getOpeningType())
                            .build())
                    .toList();
            wall.getOpenings().addAll(newOpenings);
        }

        wall = wallRepository.save(wall);
        return mapToResponse(wall);
    }

    /**
     * Пакетное сохранение стен.
     * Используется визуальным редактором для синхронизации всего плана сразу.
     */
    @Transactional
    public List<WallResponse> saveWalls(Long projectId, List<WallRequest> requests) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);

        // Удаляем старые стены этого плана перед сохранением новых
        wallRepository.deleteByFloorPlanId(floorPlan.getId());

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
                            .openings(new ArrayList<>())
                            .build();

                    if (request.getOpenings() != null && !request.getOpenings().isEmpty()) {
                        List<WallOpening> openings = request.getOpenings().stream()
                                .map(opReq -> WallOpening.builder()
                                        .wall(wall)
                                        .position(opReq.getPosition())
                                        .width(opReq.getWidth())
                                        .height(opReq.getHeight())
                                        .openingType(opReq.getOpeningType())
                                        .build())
                                .toList();
                        wall.getOpenings().addAll(openings);
                    }
                    return wall;
                })
                .collect(Collectors.toList());

        List<Wall> savedWalls = wallRepository.saveAll(walls);

        // Место для вызова автоматического определения комнат, если план изменился радикально
        // autoDetectRooms(floorPlan);

        return savedWalls.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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
        return wallRepository.findByFloorPlanId(floorPlanId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FloorPlan getFloorPlanForProject(Long projectId) {
        return floorPlanRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("FloorPlan", "projectId", projectId));
    }

    /**
     * Заглушка для логики автоматического распознавания комнат по стенам.
     */
    @Transactional
    public void autoDetectRooms(FloorPlan floorPlan) {
        // Здесь реализуется алгоритм поиска замкнутых циклов в графе стен.
        // На данный момент логика создания комнаты "авто-стенами" реализована на фронтенде
        // для обеспечения моментального отклика UI.
    }

    private WallResponse mapToResponse(Wall wall) {
        List<WallOpeningResponse> openings = wall.getOpenings() != null
                ? wall.getOpenings().stream()
                .map(this::mapOpeningToResponse)
                .collect(Collectors.toList())
                : new ArrayList<>();

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