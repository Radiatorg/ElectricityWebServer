package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.RoomRequest;
import com.verchuk.electro.dto.response.RoomResponse;
import com.verchuk.electro.dto.response.WallResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.Project;
import com.verchuk.electro.model.Room;
import com.verchuk.electro.model.RoomType;
import com.verchuk.electro.repository.ProjectRepository;
import com.verchuk.electro.repository.RoomRepository;
import com.verchuk.electro.repository.RoomTypeRepository;
import com.verchuk.electro.service.WallService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WallService wallService;

    public List<RoomResponse> getRoomsByProject(Long projectId) {
        Project project = getProjectForCurrentUser(projectId);
        return roomRepository.findByProject(project).stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
    }

    public RoomResponse getRoomById(Long projectId, Long roomId) {
        Project project = getProjectForCurrentUser(projectId);
        Room room = roomRepository.findByIdAndProject(roomId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        return mapToRoomResponse(room);
    }

    @Transactional
    public RoomResponse createRoom(Long projectId, RoomRequest request) {
        Project project = getProjectForCurrentUser(projectId);
        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", request.getRoomTypeId()));

        Room room = Room.builder()
                .name(request.getName())
                .area(request.getArea())
                .roomType(roomType)
                .project(project)
                .description(request.getDescription())
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .width(request.getWidth())
                .height(request.getHeight())
                .polygonPoints(request.getPolygonPoints())
                .build();

        return mapToRoomResponse(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse updateRoom(Long projectId, Long roomId, RoomRequest request) {
        Project project = getProjectForCurrentUser(projectId);
        Room room = roomRepository.findByIdAndProject(roomId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));

        room.setName(request.getName());
        room.setArea(request.getArea());
        room.setDescription(request.getDescription());
        room.setPositionX(request.getPositionX());
        room.setPositionY(request.getPositionY());
        room.setWidth(request.getWidth());
        room.setHeight(request.getHeight());
        room.setPolygonPoints(request.getPolygonPoints());

        if (request.getRoomTypeId() != null && !request.getRoomTypeId().equals(room.getRoomType().getId())) {
            RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", request.getRoomTypeId()));
            room.setRoomType(roomType);
        }

        return mapToRoomResponse(roomRepository.save(room));
    }

    @Transactional
    public void deleteRoom(Long projectId, Long roomId) {
        Project project = getProjectForCurrentUser(projectId);
        Room room = roomRepository.findByIdAndProject(roomId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        // Удаляем внутренние стены комнаты перед удалением комнаты
        wallService.deleteWallsByRoom(projectId, roomId);
        roomRepository.delete(room);
    }

    /**
     * Получение внутренних стен и перегородок комнаты
     */
    public List<WallResponse> getRoomWalls(Long projectId, Long roomId) {
        Project project = getProjectForCurrentUser(projectId);
        Room room = roomRepository.findByIdAndProject(roomId, project)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", roomId));
        return wallService.getWallsByRoom(projectId, roomId);
    }

    private Project getProjectForCurrentUser(Long projectId) {
        var designer = userService.getCurrentUser();
        return projectRepository.findByIdAndDesigner(projectId, designer)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .area(room.getArea())
                .roomTypeId(room.getRoomType().getId())
                .roomTypeName(room.getRoomType().getName())
                .description(room.getDescription())
                .positionX(room.getPositionX())
                .positionY(room.getPositionY())
                .width(room.getWidth())
                .height(room.getHeight())
                .polygonPoints(room.getPolygonPoints())
                .build();
    }
}

