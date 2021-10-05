package com.kds.ourmemory.config;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.kds.ourmemory.advice.v1.user.exception.UserProfileImageUploadException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;   // S3 버킷 이름

    public String upload(MultipartFile multipartFile, String dirName) {
        checkNotNull(multipartFile, "Upload target file is null. Check request parameter.");
        File uploadFile = convert(multipartFile)  // 파일 변환할 수 없으면 에러
                .orElseThrow(() -> new UserProfileImageUploadException("Failed to convert multipartFile to file"));

        return upload(uploadFile, dirName);
    }

    public Optional<Boolean> delete(String urlString) {
        try {
            var key = URLDecoder.decode(
                    new URL(urlString).getPath().replace("/", ""), StandardCharsets.UTF_8
            );
            deleteFromS3(key);

            return Optional.of(true);
        } catch (MalformedURLException e) {
            log.error(e.getMessage());
        }

        return Optional.empty();
    }

    private void deleteFromS3(String key) {
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucket, key);
        amazonS3Client.deleteObject(deleteObjectRequest);
    }

    // S3로 파일 업로드하기
    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + File.pathSeparator + UUID.randomUUID() + uploadFile.getName();   // S3에 저장된 파일 이름
        String uploadImageUrl = putS3(uploadFile, fileName); // s3로 업로드
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    // S3로 업로드
    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 로컬에 저장된 이미지 지우기
    private void removeNewFile(File targetFile) {
        targetFile.delete();
    }

    // 로컬에 파일 업로드 하기
    private Optional<File> convert(MultipartFile file) {
        File convertFile = Paths.get(System.getProperty("user.dir"), file.getOriginalFilename()).toFile();

        try {
            if (convertFile.createNewFile()) { // 바로 위에서 지정한 경로에 File이 생성됨 (경로가 잘못되었다면 생성 불가능)
                try (FileOutputStream fos = new FileOutputStream(convertFile)) { // FileOutputStream 데이터를 파일에 바이트 스트림으로 저장하기 위함
                    fos.write(file.getBytes());
                }
                return Optional.of(convertFile);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return Optional.empty();
    }
}
