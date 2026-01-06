package com.litmind.config;

import io.minio.MinioClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "minio.endpoint")
public class MinIOConfig {

    @Value("${minio.endpoint:}")
    private String endpoint;

    @Value("${minio.access-key:}")
    private String accessKey;

    @Value("${minio.secret-key:}")
    private String secretKey;

    @Value("${minio.bucket:}")
    private String bucket;

    @Bean
    @ConditionalOnProperty(name = "minio.endpoint")
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}

