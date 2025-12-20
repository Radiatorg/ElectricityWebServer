package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.ElectricalPointRequest;
import com.verchuk.electro.dto.response.ElectricalPointResponse;
import com.verchuk.electro.dto.response.ElectricalSymbolResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.ElectricalPoint;
import com.verchuk.electro.model.ElectricalSymbol;
import com.verchuk.electro.model.FloorPlan;
import com.verchuk.electro.model.Room;
import com.verchuk.electro.repository.ElectricalPointRepository;
import com.verchuk.electro.repository.ElectricalSymbolRepository;
import com.verchuk.electro.repository.FloorPlanRepository;
import com.verchuk.electro.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ElectricalPointService {
    @Autowired
    private ElectricalPointRepository electricalPointRepository;

    @Autowired
    private FloorPlanRepository floorPlanRepository;

    @Autowired
    private ElectricalSymbolRepository electricalSymbolRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public ElectricalPointResponse createElectricalPoint(Long projectId, ElectricalPointRequest request) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);
        ElectricalSymbol symbol = electricalSymbolRepository.findById(request.getElectricalSymbolId())
                .orElseThrow(() -> new ResourceNotFoundException("ElectricalSymbol", "id", request.getElectricalSymbolId()));

        Room room = null;
        if (request.getRoomId() != null) {
            room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));
        }

        ElectricalPoint point = ElectricalPoint.builder()
                .floorPlan(floorPlan)
                .room(room)
                .electricalSymbol(symbol)
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .heightFromFloor(request.getHeightFromFloor())
                .rotation(request.getRotation() != null ? request.getRotation() : BigDecimal.ZERO)
                .group(request.getGroup())
                .notes(request.getNotes())
                .build();

        point = electricalPointRepository.save(point);
        return mapToResponse(point);
    }

    @Transactional
    public ElectricalPointResponse updateElectricalPoint(Long projectId, Long pointId, ElectricalPointRequest request) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);
        ElectricalPoint point = electricalPointRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("ElectricalPoint", "id", pointId));

        if (!point.getFloorPlan().getId().equals(floorPlan.getId())) {
            throw new ResourceNotFoundException("ElectricalPoint", "id", pointId);
        }

        if (request.getElectricalSymbolId() != null) {
            ElectricalSymbol symbol = electricalSymbolRepository.findById(request.getElectricalSymbolId())
                    .orElseThrow(() -> new ResourceNotFoundException("ElectricalSymbol", "id", request.getElectricalSymbolId()));
            point.setElectricalSymbol(symbol);
        }

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "id", request.getRoomId()));
            point.setRoom(room);
        } else {
            point.setRoom(null);
        }

        if (request.getPositionX() != null) {
            point.setPositionX(request.getPositionX());
        }
        if (request.getPositionY() != null) {
            point.setPositionY(request.getPositionY());
        }
        if (request.getHeightFromFloor() != null) {
            point.setHeightFromFloor(request.getHeightFromFloor());
        }
        if (request.getRotation() != null) {
            point.setRotation(request.getRotation());
        }
        if (request.getGroup() != null) {
            point.setGroup(request.getGroup());
        }
        if (request.getNotes() != null) {
            point.setNotes(request.getNotes());
        }

        point = electricalPointRepository.save(point);
        return mapToResponse(point);
    }

    @Transactional
    public void deleteElectricalPoint(Long projectId, Long pointId) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);
        ElectricalPoint point = electricalPointRepository.findById(pointId)
                .orElseThrow(() -> new ResourceNotFoundException("ElectricalPoint", "id", pointId));

        if (!point.getFloorPlan().getId().equals(floorPlan.getId())) {
            throw new ResourceNotFoundException("ElectricalPoint", "id", pointId);
        }

        electricalPointRepository.delete(point);
    }

    public List<ElectricalPointResponse> getElectricalPointsByFloorPlan(Long floorPlanId) {
        List<ElectricalPoint> points = electricalPointRepository.findByFloorPlanId(floorPlanId);
        return points.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<ElectricalPointResponse> saveElectricalPoints(Long projectId, List<ElectricalPointRequest> requests) {
        FloorPlan floorPlan = getFloorPlanForProject(projectId);
        
        // Удаляем старые точки
        electricalPointRepository.deleteByFloorPlanId(floorPlan.getId());

        // Создаем новые точки
        List<ElectricalPoint> points = requests.stream()
                .map(request -> {
                    ElectricalSymbol symbol = electricalSymbolRepository.findById(request.getElectricalSymbolId())
                            .orElseThrow(() -> new ResourceNotFoundException("ElectricalSymbol", "id", request.getElectricalSymbolId()));

                    Room room = null;
                    if (request.getRoomId() != null) {
                        room = roomRepository.findById(request.getRoomId())
                                .orElse(null);
                    }

                    return ElectricalPoint.builder()
                            .floorPlan(floorPlan)
                            .room(room)
                            .electricalSymbol(symbol)
                            .positionX(request.getPositionX())
                            .positionY(request.getPositionY())
                            .heightFromFloor(request.getHeightFromFloor())
                            .rotation(request.getRotation() != null ? request.getRotation() : BigDecimal.ZERO)
                            .group(request.getGroup())
                            .notes(request.getNotes())
                            .build();
                })
                .collect(Collectors.toList());

        points = electricalPointRepository.saveAll(points);
        return points.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public FloorPlan getFloorPlanForProject(Long projectId) {
        return floorPlanRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("FloorPlan", "projectId", projectId));
    }

    private ElectricalPointResponse mapToResponse(ElectricalPoint point) {
        ElectricalSymbolResponse symbolResponse = ElectricalSymbolResponse.builder()
                .id(point.getElectricalSymbol().getId())
                .name(point.getElectricalSymbol().getName())
                .svgPath(point.getElectricalSymbol().getSvgPath())
                .type(point.getElectricalSymbol().getType())
                .category(point.getElectricalSymbol().getCategory())
                .defaultWidth(point.getElectricalSymbol().getDefaultWidth())
                .defaultHeight(point.getElectricalSymbol().getDefaultHeight())
                .active(point.getElectricalSymbol().getActive())
                .build();

        return ElectricalPointResponse.builder()
                .id(point.getId())
                .roomId(point.getRoom() != null ? point.getRoom().getId() : null)
                .roomName(point.getRoom() != null ? point.getRoom().getName() : null)
                .electricalSymbol(symbolResponse)
                .positionX(point.getPositionX())
                .positionY(point.getPositionY())
                .heightFromFloor(point.getHeightFromFloor())
                .rotation(point.getRotation())
                .group(point.getGroup())
                .notes(point.getNotes())
                .build();
    }
}

