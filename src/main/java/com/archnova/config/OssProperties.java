package com.archnova.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置，对应 archnova.oss
 */
@Data
@ConfigurationProperties(prefix = "archnova.oss")
public class OssProperties {

    /**
     * local、aliyun、fast_dfs
     */
    private String type = "local";

    private String accessKey;

    private String secretKey;

    private String endpoint;

    private String bucketName;

    private String folder = "uploads";

    /**
     * type=local 时的本地目录
     */
    private String localPath = "uploads";

    private FastDfs fastDfs = new FastDfs();

    @Data
    public static class FastDfs {
        private String endpoint;
        private String upload;
        private String delete;
        private String fileInfo;
        private String scene;
        private String group;
    }
}
