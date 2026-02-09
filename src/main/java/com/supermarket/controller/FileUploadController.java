package com.supermarket.controller;

import com.supermarket.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传", description = "图片上传接口")
public class FileUploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private static final Map<String, byte[]> IMAGE_MAGIC_BYTES = Map.of(
            "jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "gif", new byte[]{0x47, 0x49, 0x46},
            "webp", new byte[]{0x52, 0x49, 0x46, 0x46}
    );

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/image")
    @Operation(summary = "上传图片", description = "上传图片文件，返回图片访问URL")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return Result.error("文件名不合法");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return Result.error("仅支持 jpg/jpeg/png/gif/webp 格式的图片");
        }

        if (!isValidImageContent(file)) {
            return Result.error("文件内容不是有效的图片格式");
        }

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String newFilename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path filePath = uploadPath.resolve(newFilename).normalize();

            if (!filePath.startsWith(uploadPath)) {
                return Result.error("非法文件路径");
            }

            file.transferTo(filePath.toFile());

            String imageUrl = "/uploads/" + newFilename;
            return Result.success(imageUrl);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    private boolean isValidImageContent(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[12];
            int bytesRead = is.read(header);
            if (bytesRead < 3) {
                return false;
            }
            for (byte[] magic : IMAGE_MAGIC_BYTES.values()) {
                if (bytesRead >= magic.length && startsWith(header, magic)) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
