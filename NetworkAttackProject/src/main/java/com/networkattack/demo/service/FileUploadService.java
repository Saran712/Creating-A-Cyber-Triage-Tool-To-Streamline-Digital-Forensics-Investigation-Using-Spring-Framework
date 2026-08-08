package com.networkattack.demo.service;



import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.networkattack.demo.Model.FileEntity;
import com.networkattack.demo.repository.FileRepository;

@Service
public class FileUploadService {

    @Autowired
    private FileRepository fileRepository;

    public String uploadFile(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        // Step 1: Generate SHA-256 hash of the file
        String fileHash = generateSHA256Hash(file.getBytes());

        // Step 2: Save file name and hash in the database
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileName(file.getOriginalFilename());
        fileEntity.setFileHash(fileHash);
        fileRepository.save(fileEntity);

        return fileHash;
    }

    private String generateSHA256Hash(byte[] fileData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(fileData);

        // Convert byte array to hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
    // Get all files from the database
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }
    // Delete a file by its ID
    public void deleteFileById(Long id) {
        fileRepository.deleteById(id);
    }
    public FileEntity getFileById(Long id) {
        return fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
    }
}