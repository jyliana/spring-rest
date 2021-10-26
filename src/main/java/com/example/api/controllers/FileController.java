package com.example.api.controllers;

import com.example.api.customers.FileInfo;
import com.example.api.payloads.UploadFileResponse;
import com.example.api.storages.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

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

    @PutMapping("/replaceFile")
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

    @GetMapping("/getFile/{fileName:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/getFilesByCustomer/{customer:.+}")
    public ResponseEntity<List<FileInfo>> getFilesByCustomer(@PathVariable String customer) {
        return getListOfFiles(customer);
    }

    @GetMapping("/getFilesByType/{type:.+}")
    public ResponseEntity<List<FileInfo>> getFilesByType(@PathVariable String type) {
        return getListOfFiles(type);
    }

    @GetMapping("/getFilesByDate/{date:.+}")
    public ResponseEntity<List<FileInfo>> getFilesByDate(@PathVariable String date) {
        return getListOfFiles(date);
    }

    private ResponseEntity<List<FileInfo>> getListOfFiles(String filter) {
        List<FileInfo> fileInfos = fileStorageService.getFilesBy(filter).map(path -> {
            String filename = path.getFileName().toString();
            String url = MvcUriComponentsBuilder
                    .fromMethodName(FileController.class, "getFile", path.getFileName().toString()).build().toString();

            return new FileInfo(filename, url);
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
    }

    private UploadFileResponse getFileResponse(File file) {
        String fileName = file.getName();

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/downloadFile/")
                .path(fileName)
                .toUriString();

        return new UploadFileResponse(fileName, fileDownloadUri,
                "application/json", file.length());
    }
}
