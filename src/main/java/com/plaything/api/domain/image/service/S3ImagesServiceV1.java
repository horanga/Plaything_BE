package com.plaything.api.domain.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.plaything.api.common.exception.CustomException;
import com.plaything.api.common.exception.ErrorCode;
import com.plaything.api.common.generator.IdGenerator;
import com.plaything.api.domain.image.service.model.SavedImage;
import com.plaything.api.domain.profile.model.request.ProfileImageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static com.plaything.api.common.constants.Constants.IMAGE_MIME_TYPE;
import static com.plaything.api.common.constants.Constants.MAX_FILE_SIZE;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ImagesServiceV1 {
    private final List<String> imagesToRemove = new ArrayList<>();
    private final AmazonS3 s3Client;
    private final IdGenerator idGenerator;

    private final List<String> fileExtenstionWhiteList = List.of(".jpg", ".jpeg", ".png", ".heic");
    private static final int MAX_WIDTH = 450;
    private static final int MAX_HEIGHT = 450;
    private static final String IMAGE_FORMAT = "jpg";
    private static final String CONTENT_TYPE = "image/jpeg";

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<SavedImage> uploadImages(List<ProfileImageRequest> images) {
        return images.stream()
                .map(this::uploadImage).toList();
    }

    public SavedImage uploadImage(ProfileImageRequest request) {
        String filename = saveImageToS3(request);
        return new SavedImage(filename, request.isMainPhoto());
    }

    public void deleteImage(List<String> fileNames) {
        imagesToRemove.addAll(fileNames);
    }

    public void rollbackS3Images(List<SavedImage> list) {
        list.forEach(i -> {
            try {
                this.deleteImage(i.fileName());
            } catch (Exception e) {
                log.error("Failed to delete image from S3: {}", i.fileName(), e);
            }
        });
    }

    private void deleteImage(String filename) {
        s3Client.deleteObject(new DeleteObjectRequest(bucket, filename));
    }


    private String saveImageToS3(ProfileImageRequest request) {
        MultipartFile multipartFile = request.file();
        validateImage(multipartFile);
        String filename = createUniqueFilename(multipartFile.getOriginalFilename());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 한번에 리사이징과 스트림 변환 처리
            byte[] resizedImageBytes = resizeImage(multipartFile, outputStream);
            ObjectMetadata objectMetadata = createObjectMetadata(resizedImageBytes.length);

            s3Client.putObject(new PutObjectRequest(bucket, filename,
                    new ByteArrayInputStream(resizedImageBytes), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.Private));

            return filename;
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_SAVED_FAILED);
        }
    }

    private String createUniqueFilename(String filename) {
        long epochMilli = new Timestamp(System.currentTimeMillis()).toInstant().toEpochMilli();
        return String.valueOf(idGenerator.generateId(epochMilli)).concat(getFileExtension(filename));
    }

    private String getFileExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if (!fileExtenstionWhiteList.contains(extension)) {
            throw new CustomException(ErrorCode.EXTENSION_IS_INVALID);
        }
        return extension;
    }

    private void validateImage(MultipartFile file) {
        long size = file.getSize();
        if (size > MAX_FILE_SIZE) throw new CustomException(ErrorCode.IMAGE_SIZE_EXCEEDED);

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank() || !contentType.startsWith(IMAGE_MIME_TYPE)) {
            throw new CustomException(ErrorCode.CONTENT_TYPE_IS_INVALID);
        }
    }

    private ObjectMetadata createObjectMetadata(long contentLength) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(CONTENT_TYPE);
        metadata.setContentLength(contentLength);
        return metadata;
    }


    private byte[] resizeImage(MultipartFile multipartFile, ByteArrayOutputStream outputStream) throws IOException {
        Thumbnails.of(multipartFile.getInputStream())
                .size(MAX_WIDTH, MAX_HEIGHT)
                .outputFormat(IMAGE_FORMAT)
                .outputQuality(0.8) // 이미지 품질 80%로 설정하여 용량 감소
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }
}
