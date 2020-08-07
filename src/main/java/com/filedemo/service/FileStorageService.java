package com.filedemo.service;

import com.filedemo.exception.FileStorageException;
import com.filedemo.exception.MyFileNotFoundException;
import com.filedemo.property.FileStorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the upload", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }

            // Copy file to the target location (Replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new FileStorageException("ERROR " + fileName + "ERROR", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File doesnt exit " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File doesnt exit " + fileName, ex);
        }
    }


    public Resource deleteFile(String fileName) {

            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
        try {
            Files.delete(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String copyAndLoadFileAsResource(String fileName) {

        Path originalFilePath = this.fileStorageLocation.resolve(fileName).normalize();
        String copiedFileName = fileName+"_remake";
        Path copiedPath = this.fileStorageLocation.resolve(copiedFileName).normalize();
        try {
            Files.copy(originalFilePath, copiedPath);
        } catch (IOException e) {
            e.printStackTrace();
            return "successful";
        }

        return "File can be found in"+copiedFileName;
    }
}
