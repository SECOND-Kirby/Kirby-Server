package com.second.kirby.service;

import com.second.kirby.config.FileStorageProperties;
import com.second.kirby.exception.BusinessException;
import com.second.kirby.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final FileStorageProperties fileStorageProperties;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 프로필 이미지를 저장하고 저장된 파일명을 반환합니다.
     *
     * @param file 업로드할 이미지 파일
     * @return 저장된 파일명
     */
    public String storeProfileImage(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = generateUniqueFilename(fileExtension);

        try {
            Path uploadPath = getProfileImagePath();
            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("프로필 이미지 저장 완료: {}", storedFilename);
            return storedFilename;

        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalFilename, e);
            throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR, "파일 저장에 실패했습니다.");
        }
    }

    /**
     * 프로필 이미지 파일을 삭제합니다.
     *
     * @param filename 삭제할 파일명
     */
    public void deleteProfileImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            Path filePath = getProfileImagePath().resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("프로필 이미지 삭제 완료: {}", filename);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", filename, e);
            // 파일 삭제 실패는 치명적이지 않으므로 예외를 던지지 않음
        }
    }

    /**
     * 프로필 이미지 파일을 로드합니다.
     *
     * @param filename 로드할 파일명
     * @return 파일 리소스
     */
    public Resource loadProfileImage(String filename) {
        try {
            Path filePath = getProfileImagePath().resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException(ResponseCode.BAD_REQUEST, "파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            log.error("파일 로드 실패: {}", filename, e);
            throw new BusinessException(ResponseCode.BAD_REQUEST, "파일을 로드할 수 없습니다.");
        }
    }

    /**
     * 프로필 이미지 저장 경로를 반환합니다.
     */
    private Path getProfileImagePath() {
        String uploadDir = fileStorageProperties.getUploadDir();
        String profileDir = fileStorageProperties.getProfileImageDir();
        return Paths.get(uploadDir, profileDir).toAbsolutePath().normalize();
    }

    /**
     * UUID를 사용하여 고유한 파일명을 생성합니다.
     */
    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID().toString() + "." + extension;
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE, "파일 확장자가 없습니다.");
        }
        return filename.substring(lastIndexOf + 1).toLowerCase();
    }

    /**
     * 파일 유효성을 검증합니다.
     */
    private void validateFile(MultipartFile file) {
        // 파일이 비어있는지 확인
        if (file.isEmpty()) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE, "파일이 비어있습니다.");
        }

        // 파일 크기 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE,
                    "파일 크기는 5MB를 초과할 수 없습니다.");
        }

        // 파일 확장자 확인
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE, "파일명이 올바르지 않습니다.");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE,
                    "지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 가능)");
        }

        // Content-Type 확인
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ResponseCode.INVALID_INPUT_VALUE, "이미지 파일만 업로드 가능합니다.");
        }
    }
}