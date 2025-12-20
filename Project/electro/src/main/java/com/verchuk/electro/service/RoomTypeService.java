package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.RoomTypeRequest;
import com.verchuk.electro.dto.response.RoomTypeResponse;
import com.verchuk.electro.exception.BadRequestException;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.RoomType;
import com.verchuk.electro.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomTypeService {
    @Autowired
    private RoomTypeRepository roomTypeRepository;

    public List<RoomTypeResponse> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::mapToRoomTypeResponse)
                .collect(Collectors.toList());
    }

    public RoomTypeResponse getRoomTypeById(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", id));
        return mapToRoomTypeResponse(roomType);
    }

    @Transactional
    public RoomTypeResponse createRoomType(RoomTypeRequest request) {
        if (roomTypeRepository.existsByName(request.getName())) {
            throw new BadRequestException("Room type with this name already exists");
        }

        RoomType roomType = RoomType.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return mapToRoomTypeResponse(roomTypeRepository.save(roomType));
    }

    @Transactional
    public RoomTypeResponse updateRoomType(Long id, RoomTypeRequest request) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", id));

        if (!roomType.getName().equals(request.getName()) && roomTypeRepository.existsByName(request.getName())) {
            throw new BadRequestException("Room type with this name already exists");
        }

        roomType.setName(request.getName());
        roomType.setDescription(request.getDescription());

        return mapToRoomTypeResponse(roomTypeRepository.save(roomType));
    }

    @Transactional
    public void deleteRoomType(Long id) {
        RoomType roomType = roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RoomType", "id", id));
        roomTypeRepository.delete(roomType);
    }

    private RoomTypeResponse mapToRoomTypeResponse(RoomType roomType) {
        return RoomTypeResponse.builder()
                .id(roomType.getId())
                .name(roomType.getName())
                .description(roomType.getDescription())
                .build();
    }
}

