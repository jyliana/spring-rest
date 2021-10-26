package com.example.api.storages;

import com.example.api.customers.Book;
import com.example.api.customers.Type;
import com.example.api.exceptions.FileStorageException;
import com.example.api.exceptions.MyFileNotFoundException;
import com.example.api.properties.FileStorageProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Optional<File> validateAndConvert(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        File convertedFile = null;
        if (validate(fileName)) {
            convertedFile = convertToJSON(file);
        }
        return Optional.ofNullable(convertedFile);
    }

    public void uploadFile(File file) {
        Path targetLocation = this.fileStorageLocation.resolve(file.getName());
        try {
            Files.copy(new FileInputStream(file), targetLocation);
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + file + " as it already exists!", ex);
        }
    }

    public void updateFile(File file) {
        Path targetLocation = this.fileStorageLocation.resolve(file.getName());
        try {
            Files.copy(new FileInputStream(file), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new FileStorageException("Could not replace file " + file + ". Please try again!", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }

    private boolean validate(String fileName) {
        boolean result = false;

        Pattern pattern = Pattern.compile("(?m)^(.+?)_(.+?)_(.+?)\\.(xml)$");
        Matcher matcher = pattern.matcher(fileName);
        while (matcher.find()) {
            boolean checkDateValid = matcher.group(3).matches("^[+-]?\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$");
            boolean checkDateTypeValid = Arrays.stream(Type.values()).anyMatch(n -> n.toString().equals(matcher.group(2)));
            result = checkDateValid && checkDateTypeValid;
        }
        // Check if the file's name contains invalid characters
        if (fileName.contains("..")) {
            throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
        }
        return result;
    }

    private File convertToJSON(MultipartFile file) {
        String changedName = file.getOriginalFilename().replace("xml", "json");
        File jsonFile = new File(changedName);
        try {
            JacksonXmlModule xmlModule = new JacksonXmlModule();
            xmlModule.setDefaultUseWrapper(false);
            ObjectMapper mapper = new XmlMapper(xmlModule);

            List<Book> books = Arrays.asList(mapper.readValue(file.getInputStream(), Book[].class));
            mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            mapper.writeValue(jsonFile, books);
        } catch (IOException e) {
            throw new FileStorageException("Sorry! File cannot be converted " + file.getOriginalFilename());
        }
        return jsonFile;
    }

    public boolean delete(String fileName) {
        Path targetLocation = fileStorageLocation.resolve(fileName);
        if (Files.exists(targetLocation)) {
            try {
                Files.delete(targetLocation);
                return true;
            } catch (IOException e) {
                throw new FileStorageException("File cannot be deleted " + fileName);
            }
        }
        return false;
    }

    public Stream<Path> getFilesBy(String parameter) {
        final Path root = Paths.get(fileStorageLocation.toString());
        try {
            return Files.walk(root, 1).filter(path -> path.toString().contains(parameter)).map(root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }
}
