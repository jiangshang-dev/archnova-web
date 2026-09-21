package com.archnova.service;

import com.archnova.domain.oss.FileInfoVo;
import com.archnova.domain.oss.FileTypeEnum;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * 文件存储服务
 */
public interface OssService {

    FileInfoVo upload(MultipartFile file, FileTypeEnum type);

    List<FileInfoVo> upload(MultipartFile[] fileList, FileTypeEnum type);

    FileInfoVo save(File file, FileTypeEnum type);

    List<FileInfoVo> save(List<File> fileList, FileTypeEnum type);

    FileInfoVo save(InputStream inputStream, String fileName, FileTypeEnum type);

    FileInfoVo save(byte[] bytes, String fileName, FileTypeEnum type);

    void delete(String... paths);

    void delete(List<String> paths);

    boolean exist(String path);

    ResponseEntity<byte[]> download(String path);

    ResponseEntity<byte[]> preview(String path);

    InputStream getInputStream(String path);

    byte[] readBytes(String path);

    String readBase64(String path);
}
