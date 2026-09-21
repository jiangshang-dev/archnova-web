package com.archnova.domain.oss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfoVo {

    private String name;

    /**
     * 对象相对路径
     */
    private String path;

    private Long size;

    /**
     * 可直接访问的地址
     */
    private String url;

    public FileInfoVo(String name, String path, Long size) {
        this.name = name;
        this.path = path;
        this.size = size;
    }
}
