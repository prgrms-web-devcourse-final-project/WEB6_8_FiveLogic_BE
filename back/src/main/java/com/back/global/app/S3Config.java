package com.back.global.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create("minioadmin", "minioadmin") // FIXME : MinIO 아이디, 비밀번호 이후 변경
                        )
                )
                .endpointOverride(java.net.URI.create("http://localhost:9000")) // FIXME : MinIO 엔드포인트 이후 변경
                .region(Region.AP_SOUTHEAST_1) // FIXME : 이후 리전따라 변경
                .build();
    }
}
