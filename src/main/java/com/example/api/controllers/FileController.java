package com.example.api.controllers;

import com.example.api.payloads.UploadFileResponse;
import com.example.api.storages.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;

@RestController
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/uploadFile")
    public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file) {
        File returnedFile = fileStorageService.validateAndConvert(file).get();
        fileStorageService.uploadFile(returnedFile);
        return getFileResponse(returnedFile);
    }

    @PostMapping("/replaceFile")
    public UploadFileResponse replaceFile(@RequestParam("file") MultipartFile file) {
        File returnedFile = fileStorageService.validateAndConvert(file).get();
        fileStorageService.updateFile(returnedFile);
        return getFileResponse(returnedFile);
    }

    @DeleteMapping("/deleteFile/{fileName:.+}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileName) {
        boolean result = fileStorageService.delete(fileName);

        return result ? ResponseEntity.ok().body(fileName + " was successfully deleted") :
                new ResponseEntity<>(fileName + " cannot be deleted", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            logger.info("Could not determine file type.");
        }

        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private UploadFileResponse getFileResponse(File returnedFile) {
        String fileName = returnedFile.getName();

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                "application/json", returnedFile.length());
    }
}
