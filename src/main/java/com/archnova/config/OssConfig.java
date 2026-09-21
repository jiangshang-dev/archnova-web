package com.archnova.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssConfig {

    @Bean(destroyMethod = "shutdown")
    public OSS ossClient(OssProperties properties) {
        String endpoint = properties.getEndpoint();
        if (!endpoint.startsWith("http")) {
            endpoint = "https://" + endpoint;
        }
        return new OSSClientBuilder().build(endpoint, properties.getAccessKey(), properties.getSecretKey());
    }
}
