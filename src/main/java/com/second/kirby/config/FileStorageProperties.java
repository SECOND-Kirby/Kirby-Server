package com.second.kirby.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "file.storage")
@Getter
@Setter
public class FileStorageProperties {

    private String uploadDir;
    private String profileImageDir;
    private long maxFileSize;
}
