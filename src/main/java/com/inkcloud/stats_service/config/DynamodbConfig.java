package com.inkcloud.stats_service.config;

import java.net.URI;

import software.amazon.awssdk.regions.Region;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

@Configuration
public class DynamodbConfig {

    @Bean
    public DynamoDbClient dynamoDbClient() {
        // 기존 코드 (로컬/실제 환경에 맞게 설정)
        return DynamoDbClient.builder()
                .region(Region.of("ap-northeast-2"))
                .credentialsProvider(DefaultCredentialsProvider.create()) // 자동 감지
                // .endpointOverride(URI.create("http://localhost:8000"))     // 로컬 테스트 시
                .build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }
}
