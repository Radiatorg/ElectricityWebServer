package com.verchuk.electro.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileService {
    private final Path rootLocation;

    public FileService() {
        // Создаем директорию для хранения файлов (используем абсолютный путь)
        try {
            // Пытаемся использовать системную временную директорию или рабочую директорию
            String workingDir = System.getProperty("user.dir");
            this.rootLocation = Paths.get(workingDir, "uploads").toAbsolutePath().normalize();
            
            Files.createDirectories(rootLocation);
            // Создаем поддиректории
            Files.createDirectories(rootLocation.resolve("profiles"));
            Files.createDirectories(rootLocation.resolve("appliances"));
            Files.createDirectories(rootLocation.resolve("general"));
            
            System.out.println("FileService initialized. Root location: " + rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public String saveFile(MultipartFile file, String type) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Генерируем уникальное имя файла
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        String filename = UUID.randomUUID().toString() + extension;

        // Определяем директорию по типу
        Path targetDir = rootLocation.resolve(type);
        Files.createDirectories(targetDir);

        // Сохраняем файл
        Path targetFile = targetDir.resolve(filename);
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);

        // Возвращаем только имя файла (URL будет формироваться в контроллере)
        return filename;
    }

    public Resource loadFile(String filename) throws IOException {
        // Ищем файл во всех поддиректориях
        Path[] searchDirs = {
            rootLocation.resolve("profiles"),
            rootLocation.resolve("appliances"),
            rootLocation.resolve("general")
        };

        for (Path dir : searchDirs) {
            Path file = dir.resolve(filename).toAbsolutePath().normalize();
            System.out.println("Trying to load file from: " + file);
            if (Files.exists(file) && Files.isReadable(file)) {
                // Используем FileSystemResource для более надежной работы с локальными файлами
                try {
                    Resource resource = new FileSystemResource(file.toFile());
                    if (resource.exists() && resource.isReadable()) {
                        System.out.println("File found and readable: " + file);
                        return resource;
                    } else {
                        System.out.println("File exists but resource is not readable: " + file);
                    }
                } catch (Exception e) {
                    System.out.println("Error creating FileSystemResource: " + e.getMessage());
                    // Попробуем UrlResource как fallback
                    try {
                        Resource resource = new UrlResource(file.toUri());
                        if (resource.exists() && resource.isReadable()) {
                            System.out.println("File loaded using UrlResource (fallback)");
                            return resource;
                        }
                    } catch (Exception e2) {
                        System.out.println("UrlResource fallback also failed: " + e2.getMessage());
                    }
                }
            } else {
                System.out.println("File does not exist or is not readable: " + file);
            }
        }

        throw new IOException("File not found: " + filename + ". Searched in: " + 
            rootLocation.resolve("profiles") + ", " + 
            rootLocation.resolve("appliances") + ", " + 
            rootLocation.resolve("general"));
    }

    public void deleteFile(String filename) throws IOException {
        Path[] searchDirs = {
            rootLocation.resolve("profiles"),
            rootLocation.resolve("appliances"),
            rootLocation.resolve("general")
        };

        for (Path dir : searchDirs) {
            Path file = dir.resolve(filename);
            if (Files.exists(file)) {
                Files.delete(file);
                return;
            }
        }

        throw new IOException("File not found: " + filename);
    }

    public String getContentType(String filename) throws IOException {
        Path[] searchDirs = {
            rootLocation.resolve("profiles"),
            rootLocation.resolve("appliances"),
            rootLocation.resolve("general")
        };

        for (Path dir : searchDirs) {
            Path file = dir.resolve(filename);
            if (Files.exists(file)) {
                return Files.probeContentType(file);
            }
        }

        return "application/octet-stream";
    }
}

