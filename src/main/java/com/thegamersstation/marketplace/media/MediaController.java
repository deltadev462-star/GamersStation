package com.thegamersstation.marketplace.media;

import com.thegamersstation.marketplace.media.dto.ImageUploadResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    /**
     * Upload a single image
     */
    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponseDto> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "posts") String folder
    ) {
        log.info("Uploading image to folder: {}", folder);
        String imageUrl = mediaService.uploadImage(file, folder);
        
        return ResponseEntity.ok(ImageUploadResponseDto.builder()
                .url(imageUrl)
                .filename(file.getOriginalFilename())
                .size(file.getSize())
                .contentType(file.getContentType())
                .build());
    }

    /**
     * Upload multiple images
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<List<ImageUploadResponseDto>> uploadMultipleImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "posts") String folder
    ) {
        log.info("Uploading {} images to folder: {}", files.size(), folder);
        
        List<ImageUploadResponseDto> responses = files.stream()
                .map(file -> {
                    String imageUrl = mediaService.uploadImage(file, folder);
                    return ImageUploadResponseDto.builder()
                            .url(imageUrl)
                            .filename(file.getOriginalFilename())
                            .size(file.getSize())
                            .contentType(file.getContentType())
                            .build();
                })
                .toList();
        
        return ResponseEntity.ok(responses);
    }
}
