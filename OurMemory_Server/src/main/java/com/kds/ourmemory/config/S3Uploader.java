package com.kds.ourmemory.config;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Getter
@Component
public class S3Uploader implements Uploader{

    private final static String TEMP_FILE_PATH = "/src/main/resources/";

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}") // load bucket info from property
    public String bucket;

    @Override
    public String upload(MultipartFile multipartFile, String dirName) {
        File convertedFile = convert(multipartFile);
        return upload(convertedFile, dirName);
    }

    private String upload(File uploadFile, String dirName) {
        String fileName = dirName + "/" + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);
        return uploadImageUrl;
    }

    private String putS3(File uploadFile, String fileName) {
        amazonS3Client.putObject(new PutObjectRequest(
                getBucket(), fileName, uploadFile)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return amazonS3Client.getUrl(getBucket(), fileName).toString();
    }

    private void removeNewFile(File targetFile) {
        Optional.of(targetFile)
                .filter(file -> !file.delete())
                .ifPresent(f -> log.info("Failed to delete tmpFile '{}'.", f.getName()));
    }

    private File convert(MultipartFile file){
        File convertFile = new File(TEMP_FILE_PATH + file.getOriginalFilename());
        return Optional.of(convertFile)
                .map(f -> {
                    try {
                        f.createNewFile();
                        FileOutputStream fos = new FileOutputStream(f);
                        fos.write(file.getBytes());
                    } catch (IOException e) {
                        throw new IllegalArgumentException("Failed to file transfer " + f.getName());
                    }

                    return f;
                })
                .get();
    }
}
