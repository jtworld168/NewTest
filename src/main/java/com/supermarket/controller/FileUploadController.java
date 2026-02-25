package com.supermarket.controller;

import com.supermarket.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/file")
@Tag(name = "文件管理", description = "文件上传与下载接口")
public class FileUploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private static final Map<String, byte[]> IMAGE_MAGIC_BYTES = Map.of(
            "jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            "png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            "gif", new byte[]{0x47, 0x49, 0x46},
            "webp", new byte[]{0x52, 0x49, 0x46, 0x46}
    );

    private static final int MAX_IMAGE_WIDTH = 1920;
    private static final int MAX_IMAGE_HEIGHT = 1920;
    private static final float JPEG_QUALITY = 0.8f;
    private static final int THUMBNAIL_SIZE = 200;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${cdn.base-url:}")
    private String cdnBaseUrl;

    @PostMapping("/upload")
    @Operation(summary = "上传图片（自动压缩）", description = "上传图片文件，自动压缩并生成缩略图，返回图片访问URL")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
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

            String baseName = UUID.randomUUID().toString().replace("-", "");

            // Read original image for compression
            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            String compressedFilename;
            String thumbnailFilename = null;

            if (originalImage != null && !extension.equals(".gif")) {
                // Compress: resize if too large, reduce JPEG quality
                BufferedImage compressed = resizeImage(originalImage, MAX_IMAGE_WIDTH, MAX_IMAGE_HEIGHT);
                compressedFilename = baseName + ".jpg";
                Path compressedPath = uploadPath.resolve(compressedFilename).normalize();
                if (!compressedPath.startsWith(uploadPath)) {
                    return Result.error("非法文件路径");
                }
                writeCompressedJpeg(compressed, compressedPath, JPEG_QUALITY);

                // Generate thumbnail
                BufferedImage thumb = resizeImage(originalImage, THUMBNAIL_SIZE, THUMBNAIL_SIZE);
                thumbnailFilename = baseName + "_thumb.jpg";
                Path thumbPath = uploadPath.resolve(thumbnailFilename).normalize();
                writeCompressedJpeg(thumb, thumbPath, 0.7f);
            } else {
                // GIF or unreadable: save as-is
                compressedFilename = baseName + extension;
                Path filePath = uploadPath.resolve(compressedFilename).normalize();
                if (!filePath.startsWith(uploadPath)) {
                    return Result.error("非法文件路径");
                }
                file.transferTo(filePath.toFile());
            }

            String imageUrl = "/file/" + compressedFilename;
            String fullUrl = cdnBaseUrl.isEmpty() ? imageUrl : cdnBaseUrl + imageUrl;

            Map<String, String> result = new LinkedHashMap<>();
            result.put("url", fullUrl);
            result.put("path", imageUrl);
            if (thumbnailFilename != null) {
                String thumbUrl = "/file/" + thumbnailFilename;
                result.put("thumbnail", cdnBaseUrl.isEmpty() ? thumbUrl : cdnBaseUrl + thumbUrl);
            }
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("文件上传失败：" + e.getMessage());
        }
    }

    private BufferedImage resizeImage(BufferedImage original, int maxWidth, int maxHeight) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return original;
        }

        double ratio = Math.min((double) maxWidth / width, (double) maxHeight / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newWidth, newHeight, null);
        g.dispose();
        return resized;
    }

    private void writeCompressedJpeg(BufferedImage image, Path path, float quality) throws IOException {
        // Ensure no alpha channel
        BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(image, 0, 0, Color.WHITE, null);
        g.dispose();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            ImageIO.write(rgb, "jpg", path.toFile());
            return;
        }
        ImageWriter writer = writers.next();
        ImageWriteParam param = writer.getDefaultWriteParam();
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(path.toFile())) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(rgb, null, null), param);
        } finally {
            writer.dispose();
        }
    }

    @GetMapping("/{filename}")
    @Operation(summary = "预览图片", description = "根据文件名预览图片（带HTTP缓存头）")
    public ResponseEntity<Resource> previewFile(
            @Parameter(description = "文件名") @PathVariable String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();

            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // HTTP Cache: images are immutable (UUID filenames), cache for 7 days
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/download/{filename}")
    @Operation(summary = "下载文件", description = "根据文件名下载已上传的文件")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "文件名") @PathVariable String filename) {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadPath.resolve(filename).normalize();

            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + encodedFilename + "\"; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/list")
    @Operation(summary = "获取文件列表", description = "获取已上传的所有文件名列表")
    public Result<List<String>> listFiles() {
        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                return Result.success(List.of());
            }
            try (Stream<Path> paths = Files.list(uploadPath)) {
                List<String> filenames = paths
                        .filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .sorted()
                        .collect(Collectors.toList());
                return Result.success(filenames);
            }
        } catch (IOException e) {
            return Result.error("获取文件列表失败：" + e.getMessage());
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
