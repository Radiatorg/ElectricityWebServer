package com.verchuk.electro.service;

import com.verchuk.electro.dto.request.ApplianceRequest;
import com.verchuk.electro.dto.response.ApplianceResponse;
import com.verchuk.electro.dto.response.CategoryResponse;
import com.verchuk.electro.exception.ResourceNotFoundException;
import com.verchuk.electro.model.Appliance;
import com.verchuk.electro.model.Category;
import com.verchuk.electro.repository.ApplianceRepository;
import com.verchuk.electro.repository.CategoryRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApplianceService {
    @Autowired
    private ApplianceRepository applianceRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<ApplianceResponse> getAllActiveAppliances() {
        try {
            List<Appliance> appliances = applianceRepository.findAllActiveOrderedByName();
            return appliances.stream()
                    .map(appliance -> {
                        try {
                            return mapToApplianceResponse(appliance);
                        } catch (Exception e) {
                            System.err.println("Ошибка маппинга прибора ID " + appliance.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                            // Возвращаем прибор без категорий в случае ошибки
                            return ApplianceResponse.builder()
                                    .id(appliance.getId())
                                    .name(appliance.getName())
                                    .description(appliance.getDescription())
                                    .powerConsumption(appliance.getPowerConsumption())
                                    .voltage(appliance.getVoltage())
                                    .current(appliance.getCurrent())
                                    .category(appliance.getCategory())
                                    .categories(List.of()) // Пустой список категорий
                                    .imageUrl(appliance.getImageUrl())
                                    .width(appliance.getWidth())
                                    .height(appliance.getHeight())
                                    .active(appliance.getActive())
                                    .build();
                        }
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Критическая ошибка при получении списка приборов: " + e.getMessage());
            e.printStackTrace();
            // Возвращаем пустой список вместо исключения, чтобы страница не ломалась
            return List.of();
        }
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
                .category(request.getCategory()) // Для обратной совместимости
                .imageUrl(request.getImageUrl())
                .width(request.getWidth())
                .height(request.getHeight())
                .active(true)
                .categories(new HashSet<>())
                .build();

        Set<Category> categories = new HashSet<>();
        
        // Добавляем существующие категории по ID
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> existingCategories = request.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId)))
                    .collect(Collectors.toSet());
            categories.addAll(existingCategories);
        }
        
        // Создаем новые категории по именам
        if (request.getNewCategoryNames() != null && !request.getNewCategoryNames().isEmpty()) {
            for (String categoryName : request.getNewCategoryNames()) {
                if (categoryName != null && !categoryName.trim().isEmpty()) {
                    String trimmedName = categoryName.trim();
                    Category newCategory = categoryRepository.findByName(trimmedName)
                            .orElseGet(() -> {
                                Category cat = Category.builder()
                                        .name(trimmedName)
                                        .description(null)
                                        .build();
                                return categoryRepository.save(cat);
                            });
                    categories.add(newCategory);
                }
            }
        }
        
        // Для обратной совместимости: если указано старое поле category
        if (categories.isEmpty() && request.getCategory() != null && !request.getCategory().isEmpty()) {
            Category category = categoryRepository.findByName(request.getCategory())
                    .orElseGet(() -> {
                        Category newCategory = Category.builder()
                                .name(request.getCategory())
                                .description(null)
                                .build();
                        return categoryRepository.save(newCategory);
                    });
            categories.add(category);
        }
        
        // Правильно устанавливаем коллекцию категорий для нового прибора
        appliance.getCategories().addAll(categories);

        Appliance savedAppliance = applianceRepository.save(appliance);
        
        return mapToApplianceResponse(savedAppliance);
    }

    @Transactional
    public ApplianceResponse updateAppliance(Long id, ApplianceRequest request) {
        // Загружаем прибор с категориями для правильного обновления
        Appliance appliance = applianceRepository.findByIdWithCategories(id)
                .orElseGet(() -> applianceRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Appliance", "id", id)));

        appliance.setName(request.getName());
        appliance.setDescription(request.getDescription());
        appliance.setPowerConsumption(request.getPowerConsumption());
        appliance.setVoltage(request.getVoltage());
        appliance.setCurrent(request.getCurrent());
        appliance.setCategory(request.getCategory()); // Для обратной совместимости
        if (request.getImageUrl() != null) {
            appliance.setImageUrl(request.getImageUrl());
        }
        appliance.setWidth(request.getWidth());
        appliance.setHeight(request.getHeight());

        // Обновляем категории
        Set<Category> categories = new HashSet<>();
        
        // Добавляем существующие категории по ID
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> existingCategories = request.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", categoryId)))
                    .collect(Collectors.toSet());
            categories.addAll(existingCategories);
        }
        
        // Создаем новые категории по именам
        if (request.getNewCategoryNames() != null && !request.getNewCategoryNames().isEmpty()) {
            for (String categoryName : request.getNewCategoryNames()) {
                if (categoryName != null && !categoryName.trim().isEmpty()) {
                    String trimmedName = categoryName.trim();
                    Category newCategory = categoryRepository.findByName(trimmedName)
                            .orElseGet(() -> {
                                Category cat = Category.builder()
                                        .name(trimmedName)
                                        .description(null)
                                        .build();
                                return categoryRepository.save(cat);
                            });
                    categories.add(newCategory);
                }
            }
        }
        
        // Для обратной совместимости: если указано старое поле category
        if (categories.isEmpty() && request.getCategory() != null && !request.getCategory().isEmpty()) {
            Category category = categoryRepository.findByName(request.getCategory())
                    .orElseGet(() -> {
                        Category newCategory = Category.builder()
                                .name(request.getCategory())
                                .description(null)
                                .build();
                        return categoryRepository.save(newCategory);
                    });
            categories.add(category);
        }
        
        // Правильно обновляем коллекцию категорий для Hibernate
        // Используем EntityManager для прямого обновления связей, чтобы избежать проблем с обратной связью
        entityManager.refresh(appliance); // Обновляем состояние из БД
        
        // Очищаем существующие связи через SQL, чтобы избежать проблем с PersistentSet
        entityManager.createNativeQuery("DELETE FROM appliance_categories WHERE appliance_id = :applianceId")
                .setParameter("applianceId", appliance.getId())
                .executeUpdate();
        
        // Устанавливаем новые категории через новый HashSet
        appliance.setCategories(new HashSet<>(categories));
        
        // Сохраняем прибор
        Appliance savedAppliance = applianceRepository.save(appliance);
        entityManager.flush(); // Принудительно сохраняем изменения
        
        // Перезагружаем прибор с категориями для ответа
        Appliance reloadedAppliance = applianceRepository.findByIdWithCategories(savedAppliance.getId())
                .orElse(savedAppliance);
        
        return mapToApplianceResponse(reloadedAppliance);
    }

    @Transactional
    public void deleteAppliance(Long id) {
        Appliance appliance = applianceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appliance", "id", id));
        appliance.setActive(false);
        applianceRepository.save(appliance);
    }

    private ApplianceResponse mapToApplianceResponse(Appliance appliance) {
        if (appliance == null) {
            throw new IllegalArgumentException("Appliance cannot be null");
        }
        
        List<CategoryResponse> categories = List.of();
        try {
            // Безопасно получаем категории, избегая инициализации коллекции если она не загружена
            Set<Category> categorySet = appliance.getCategories();
            if (categorySet != null && !categorySet.isEmpty()) {
                // Создаем копию коллекции, чтобы избежать проблем с PersistentSet
                categories = new java.util.ArrayList<>(categorySet).stream()
                        .filter(cat -> cat != null) // Фильтруем null категории
                        .map(cat -> {
                            try {
                                return CategoryResponse.builder()
                                        .id(cat.getId())
                                        .name(cat.getName() != null ? cat.getName() : "")
                                        .description(cat.getDescription())
                                        .build();
                            } catch (Exception e) {
                                System.err.println("Ошибка маппинга категории: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(cat -> cat != null) // Фильтруем null результаты
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при обработке категорий прибора ID " + appliance.getId() + ": " + e.getMessage());
            e.printStackTrace();
            categories = List.of(); // В случае ошибки возвращаем пустой список
        }

        try {
            return ApplianceResponse.builder()
                    .id(appliance.getId())
                    .name(appliance.getName())
                    .description(appliance.getDescription())
                    .powerConsumption(appliance.getPowerConsumption())
                    .voltage(appliance.getVoltage())
                    .current(appliance.getCurrent())
                    .category(appliance.getCategory()) // Для обратной совместимости
                    .categories(categories)
                    .imageUrl(appliance.getImageUrl())
                    .width(appliance.getWidth())
                    .height(appliance.getHeight())
                    .active(appliance.getActive())
                    .build();
        } catch (Exception e) {
            System.err.println("Ошибка создания ApplianceResponse для прибора ID " + appliance.getId() + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to map appliance to response", e);
        }
    }
}

