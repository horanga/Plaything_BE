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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.List;
import static com.plaything.api.common.constants.Constants.IMAGE_MIME_TYPE;
import static com.plaything.api.common.constants.Constants.MAX_FILE_SIZE;

@RequiredArgsConstructor
@Service
public class S3ImagesServiceV1 {

    private final AmazonS3 s3Client;
    private final IdGenerator idGenerator;

    private final List<String> fileExtenstionWhiteList = List.of(".jpg", ".jpeg", ".png", ".heic");


    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public SavedImage uploadSingleFile(MultipartFile multipartFile) {
        validateFile(multipartFile);
        String filename = saveImageToS3(multipartFile);
        return new SavedImage(s3Client.getUrl(bucket, filename).toString(), filename);
    }

    public List<SavedImage> uploadFiles(List<MultipartFile> multipartFiles) {
        return multipartFiles.stream()
                .map(this::uploadSingleFile)
                .toList();
    }

    public void deleteFile(String filename) {
        s3Client.deleteObject(new DeleteObjectRequest(bucket, filename));
    }

    private void validateFile(MultipartFile file) {
        long size = file.getSize();
        if (size > MAX_FILE_SIZE) throw new CustomException(ErrorCode.SIZE_IS_INVALID);

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank() || !contentType.startsWith(IMAGE_MIME_TYPE)) {
            throw new CustomException(ErrorCode.CONTENT_TYPE_IS_INVALID);
        }

    }

    private String saveImageToS3(MultipartFile multipartFile) {
        String filename = createFilename(multipartFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try (InputStream inputStream = multipartFile.getInputStream()) {
            s3Client.putObject(new PutObjectRequest(bucket, filename, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            throw new IllegalStateException("파일 업로드 실패");
        }
        return filename;
    }

    private String createFilename(String filename) {
        long epochMilli = new Timestamp(System.currentTimeMillis()).toInstant().toEpochMilli();
        return String.valueOf(idGenerator.generateId(epochMilli)).concat(getFileExtension(filename));
    }

    private String getFileExtension(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        if(!fileExtenstionWhiteList.contains(extension)) {
            throw new CustomException(ErrorCode.EXTENSION_IS_INVALID);
        }
        return extension;
    }
}
