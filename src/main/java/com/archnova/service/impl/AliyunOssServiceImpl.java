package com.archnova.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.archnova.config.OssProperties;
import com.archnova.domain.oss.FileInfoVo;
import com.archnova.domain.oss.FileTypeEnum;
import com.archnova.service.OssService;
import com.archnova.utils.AssertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AliyunOssServiceImpl implements OssService {

    private static final Set<String> ALLOWED = Set.of(
            "png", "jpg", "jpeg", "webp", "gif", "bmp",
            "mp4", "pdf", "doc", "docx", "xls", "xlsx", "zip"
    );

    private final OSS ossClient;
    private final OssProperties ossProperties;

    @Override
    public FileInfoVo upload(MultipartFile file, FileTypeEnum type) {
        AssertUtil.notNull(file, "请选择文件");
        AssertUtil.isTrue(!file.isEmpty(), "文件不能为空");
        String ext = extension(file.getOriginalFilename());
        AssertUtil.isTrue(ALLOWED.contains(ext), "不支持该文件类型");
        try {
            return save(file.getInputStream(), file.getOriginalFilename(), type);
        } catch (IOException e) {
            throw new com.archnova.exception.ServiceException("读取上传文件失败");
        }
    }

    @Override
    public List<FileInfoVo> upload(MultipartFile[] fileList, FileTypeEnum type) {
        AssertUtil.notNull(fileList, "请选择文件");
        List<FileInfoVo> result = new ArrayList<>();
        for (MultipartFile file : fileList) {
            result.add(upload(file, type));
        }
        return result;
    }

    @Override
    public FileInfoVo save(File file, FileTypeEnum type) {
        AssertUtil.notNull(file, "文件不能为空");
        AssertUtil.isTrue(file.isFile(), "文件不存在");
        return save(FileUtil.getInputStream(file), file.getName(), type);
    }

    @Override
    public List<FileInfoVo> save(List<File> fileList, FileTypeEnum type) {
        AssertUtil.notNull(fileList, "文件不能为空");
        List<FileInfoVo> result = new ArrayList<>();
        for (File file : fileList) {
            result.add(save(file, type));
        }
        return result;
    }

    @Override
    public FileInfoVo save(InputStream inputStream, String fileName, FileTypeEnum type) {
        AssertUtil.notNull(inputStream, "文件不能为空");
        FileTypeEnum fileType = type == null ? FileTypeEnum.OTHER : type;
        String ext = extension(fileName);
        String objectKey = objectKey(fileType, ext);
        ObjectMetadata metadata = new ObjectMetadata();
        String mime = FileUtil.getMimeType(StrUtil.blankToDefault(fileName, "file." + ext));
        if (StrUtil.isNotBlank(mime)) {
            metadata.setContentType(mime);
        }
        ossClient.putObject(ossProperties.getBucketName(), objectKey, inputStream, metadata);
        ossClient.setObjectAcl(ossProperties.getBucketName(), objectKey, CannedAccessControlList.PublicRead);
        FileInfoVo info = new FileInfoVo(FileUtil.getName(fileName), objectKey, null);
        info.setUrl(publicUrl(objectKey));
        return info;
    }

    @Override
    public FileInfoVo save(byte[] bytes, String fileName, FileTypeEnum type) {
        AssertUtil.notNull(bytes, "文件不能为空");
        FileInfoVo info = save(new ByteArrayInputStream(bytes), fileName, type);
        info.setSize((long) bytes.length);
        return info;
    }

    @Override
    public void delete(String... paths) {
        if (paths == null) {
            return;
        }
        for (String path : paths) {
            if (StrUtil.isNotBlank(path)) {
                ossClient.deleteObject(ossProperties.getBucketName(), objectKeyOf(path));
            }
        }
    }

    @Override
    public void delete(List<String> paths) {
        if (paths == null) {
            return;
        }
        delete(paths.toArray(String[]::new));
    }

    @Override
    public boolean exist(String path) {
        AssertUtil.isNotBlank(path, "文件路径不能为空");
        return ossClient.doesObjectExist(ossProperties.getBucketName(), objectKeyOf(path));
    }

    @Override
    public ResponseEntity<byte[]> download(String path) {
        return response(path, true);
    }

    @Override
    public ResponseEntity<byte[]> preview(String path) {
        return response(path, false);
    }

    @Override
    public InputStream getInputStream(String path) {
        AssertUtil.isNotBlank(path, "文件路径不能为空");
        OSSObject object = ossClient.getObject(ossProperties.getBucketName(), objectKeyOf(path));
        return object.getObjectContent();
    }

    @Override
    public byte[] readBytes(String path) {
        try (InputStream inputStream = getInputStream(path)) {
            return IoUtil.readBytes(inputStream);
        } catch (IOException e) {
            throw new com.archnova.exception.ServiceException("读取文件失败");
        }
    }

    @Override
    public String readBase64(String path) {
        return Base64.getEncoder().encodeToString(readBytes(path));
    }

    private ResponseEntity<byte[]> response(String path, boolean attachment) {
        byte[] bytes = readBytes(path);
        String name = FileUtil.getName(objectKeyOf(path));
        String mime = StrUtil.blankToDefault(FileUtil.getMimeType(name), MediaType.APPLICATION_OCTET_STREAM_VALUE);
        ContentDisposition disposition = (attachment ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename(name, StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(mime))
                .body(bytes);
    }

    private String objectKey(FileTypeEnum type, String ext) {
        String folder = StrUtil.removeSuffix(StrUtil.blankToDefault(ossProperties.getFolder(), "uploads"), "/");
        return folder + "/" + type.getFolder() + "/" + IdUtil.fastSimpleUUID() + "." + ext;
    }

    private String objectKeyOf(String path) {
        String value = StrUtil.trim(path);
        if (StrUtil.startWithIgnoreCase(value, "http")) {
            int index = value.indexOf(".aliyuncs.com/");
            AssertUtil.isTrue(index > 0, "文件地址不正确");
            return value.substring(index + ".aliyuncs.com/".length());
        }
        return StrUtil.removePrefix(value, "/");
    }

    private String publicUrl(String objectKey) {
        String endpoint = StrUtil.removePrefix(StrUtil.removePrefix(ossProperties.getEndpoint(), "https://"), "http://");
        return "https://" + ossProperties.getBucketName() + "." + endpoint + "/" + objectKey;
    }

    private String extension(String fileName) {
        return StrUtil.blankToDefault(FileUtil.extName(fileName), "").toLowerCase(Locale.ROOT);
    }
}
