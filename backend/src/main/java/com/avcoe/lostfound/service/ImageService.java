package com.avcoe.lostfound.service;

import com.avcoe.lostfound.dto.ImageResponse;
import com.avcoe.lostfound.entity.Item;
import com.avcoe.lostfound.entity.ItemImage;
import com.avcoe.lostfound.repository.ItemImageRepository;
import com.avcoe.lostfound.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
public class ImageService {

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    private final ItemImageRepository imageRepository;
    private final ItemRepository itemRepository;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public ImageService(ItemImageRepository imageRepository, ItemRepository itemRepository) {
        this.imageRepository = imageRepository;
        this.itemRepository = itemRepository;
    }

    public List<ImageResponse> uploadImages(Long itemId, MultipartFile[] files) throws IOException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        List<ItemImage> existingImages = imageRepository.findByItemIdOrderBySortOrderAsc(itemId);
        int startOrder = existingImages.size();

        Path uploadPath = Paths.get(uploadDir, "items", itemId.toString());
        Files.createDirectories(uploadPath);

        List<ImageResponse> responses = new ArrayList<>();

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];

            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                throw new IllegalArgumentException("File type not allowed: " + file.getContentType());
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File size exceeds 10MB limit");
            }

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "/uploads/items/" + itemId + "/" + fileName;

            ItemImage itemImage = ItemImage.builder()
                    .item(item)
                    .imageUrl(imageUrl)
                    .fileName(fileName)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .sortOrder(startOrder + i)
                    .isPrimary(i == 0 && existingImages.isEmpty())
                    .createdAt(java.time.LocalDateTime.now())
                    .build();

            itemImage = imageRepository.save(itemImage);

            ImageResponse response = new ImageResponse();
            response.setId(itemImage.getId());
            response.setImageUrl(itemImage.getImageUrl());
            response.setFileName(itemImage.getFileName());
            response.setContentType(itemImage.getContentType());
            response.setFileSize(itemImage.getFileSize());
            response.setSortOrder(itemImage.getSortOrder());
            response.setPrimary(itemImage.isPrimary());
            responses.add(response);
        }

        return responses;
    }

    public void deleteImage(Long imageId) throws IOException {
        ItemImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));

        Path filePath = Paths.get(uploadDir, "items", image.getItem().getId().toString(), image.getFileName());
        Files.deleteIfExists(filePath);

        imageRepository.delete(image);
    }

    public List<ImageResponse> getItemImages(Long itemId) {
        List<ItemImage> images = imageRepository.findByItemIdOrderBySortOrderAsc(itemId);
        List<ImageResponse> responses = new ArrayList<>();
        for (ItemImage img : images) {
            ImageResponse response = new ImageResponse();
            response.setId(img.getId());
            response.setImageUrl(img.getImageUrl());
            response.setFileName(img.getFileName());
            response.setContentType(img.getContentType());
            response.setFileSize(img.getFileSize());
            response.setSortOrder(img.getSortOrder());
            response.setPrimary(img.isPrimary());
            responses.add(response);
        }
        return responses;
    }
}
