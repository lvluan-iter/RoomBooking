package com.example.RoomBooking.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    @Value("${upload-dir}")
    private String uploadDir;

    @PostMapping("/upload-images")
    public List<String> uploadImages(@RequestParam("images") MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                // Tạo UUID ngẫu nhiên cho tên file
                String fileExtension = getFileExtension(file.getOriginalFilename());
                String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;

                // Lưu file vào thư mục upload với tên duy nhất
                String filePath = uploadDir + File.separator + uniqueFileName;
                File dest = new File(filePath);
                file.transferTo(dest);

                // Tạo URL hoàn chỉnh cho file đã upload
                String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(uniqueFileName)
                        .toUriString();

                fileUrls.add(fileUrl);
            }
            return fileUrls;
        } catch (IOException e) {
            e.printStackTrace();
            // Xử lý lỗi tải lên ở đây
            return null;
        }
    }

    // Phương thức để lấy phần mở rộng của file
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
