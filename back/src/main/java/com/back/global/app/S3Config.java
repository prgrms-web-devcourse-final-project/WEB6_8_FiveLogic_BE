package com.back.global.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
public class S3Config {

    private static final String ACCESS_KEY = "minioadmin"; // FIXME: 실제 키로 교체
    private static final String SECRET_KEY = "minioadmin"; // FIXME: 실제 비밀키로 교체
    private static final String ENDPOINT = "http://localhost:9000"; // FIXME: 실제 엔드포인트로 교체
    private static final Region REGION = Region.AP_SOUTHEAST_1; // FIXME: 실제 리전으로 교체

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
                        )
                )
                .endpointOverride(URI.create(ENDPOINT))
                .region(REGION)
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)
                        )
                )
                .endpointOverride(URI.create(ENDPOINT))
                .region(REGION)
                .build();
    }
}
