package com.archnova.domain.oss;

import lombok.Getter;

/**
 * 文件分类，决定 OSS 中的目录
 */
@Getter
public enum FileTypeEnum {

    IMAGE("image"),
    VIDEO("video"),
    DOC("doc"),
    OTHER("other");

    private final String folder;

    FileTypeEnum(String folder) {
        this.folder = folder;
    }
}
