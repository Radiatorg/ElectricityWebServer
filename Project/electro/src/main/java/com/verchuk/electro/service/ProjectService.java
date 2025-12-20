package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.ProjectRequest;
import com.verchuk.electro.dto.response.ProjectResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.Project;
import com.verchuk.electro.model.User;
import com.verchuk.electro.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    public List<ProjectResponse> getAllProjectsForCurrentUser() {
        User designer = userService.getCurrentUser();
        return projectRepository.findByDesigner(designer).stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long id) {
        // Загружаем проект с комнатами
        Project project = projectRepository.findByIdWithRooms(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        // Загружаем электроприборы отдельным запросом
        projectRepository.findByIdWithAppliances(id).ifPresent(p -> {
            project.setProjectAppliances(p.getProjectAppliances());
        });
        return mapToProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectByIdForCurrentUser(Long id) {
        User designer = userService.getCurrentUser();
        // Проверяем права доступа
        projectRepository.findByIdAndDesigner(id, designer)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        // Загружаем проект с комнатами
        Project project = projectRepository.findByIdWithRooms(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        // Загружаем электроприборы отдельным запросом
        projectRepository.findByIdWithAppliances(id).ifPresent(p -> {
            project.setProjectAppliances(p.getProjectAppliances());
        });
        return mapToProjectResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request) {
        User designer = userService.getCurrentUser();
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .designer(designer)
                .build();
        return mapToProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        User designer = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndDesigner(id, designer)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        return mapToProjectResponse(projectRepository.save(project));
    }

    @Transactional
    public void deleteProject(Long id) {
        User designer = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndDesigner(id, designer)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        projectRepository.delete(project);
    }

    private ProjectResponse mapToProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .designerId(project.getDesigner().getId())
                .designerUsername(project.getDesigner().getUsername())
                .rooms(project.getRooms() != null ? project.getRooms().stream()
                        .map(room -> com.verchuk.electro.dto.response.RoomResponse.builder()
                                .id(room.getId())
                                .name(room.getName())
                                .area(room.getArea())
                                .roomTypeId(room.getRoomType().getId())
                                .roomTypeName(room.getRoomType().getName())
                                .description(room.getDescription())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .appliances(project.getProjectAppliances() != null ? project.getProjectAppliances().stream()
                        .map(pa -> com.verchuk.electro.dto.response.ProjectApplianceResponse.builder()
                                .id(pa.getId())
                                .applianceId(pa.getAppliance().getId())
                                .applianceName(pa.getAppliance().getName())
                                .roomId(pa.getRoom() != null ? pa.getRoom().getId() : null)
                                .roomName(pa.getRoom() != null ? pa.getRoom().getName() : null)
                                .quantity(pa.getQuantity())
                                .totalPower(pa.getTotalPower())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}

