package com.networkattack.demo.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.networkattack.demo.Model.FileEntity;
import com.networkattack.demo.service.FileUploadService;

@Controller
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping("/blockchain")
    public String showUploadForm() {
        return "upload-form"; // HTML template for the upload form
    }

    @PostMapping("/blockchain")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        try {
            String fileHash = fileUploadService.uploadFile(file);
            model.addAttribute("message", "File uploaded successfully!");
            model.addAttribute("fileName", file.getOriginalFilename());
            model.addAttribute("fileHash", fileHash);
        } catch (IOException | NoSuchAlgorithmException e) {
            model.addAttribute("message", "Error uploading file: " + e.getMessage());
        }
        return "upload-result"; // HTML template for the result page
    }

    @GetMapping("/files")
    public String listFiles(Model model) {
        model.addAttribute("files", fileUploadService.getAllFiles());
        return "file-list"; // HTML template to display the list of files
    }

    @PostMapping("/files/delete/{id}")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            fileUploadService.deleteFileById(id);
            redirectAttributes.addFlashAttribute("message", "File deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Error deleting file: " + e.getMessage());
        }
        return "redirect:/files";
    }

//    @GetMapping("/files/download/{id}")
//    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
//        try {
//            FileEntity fileEntity = fileUploadService.deleteFileById(id);
//            
//            return ResponseEntity.ok()
//                    .header(HttpHeaders.CONTENT_DISPOSITION, 
//                            "attachment; filename=\"" + fileEntity.getFileName() + "\"")
//                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                    .body(new ByteArrayResource(fileEntity.getFileData()));
//        } catch (Exception e) {
//            throw new RuntimeException("Error downloading file: " + e.getMessage());
//        }
//    }
}