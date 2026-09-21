package com.archnova.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.archnova.utils.AssertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final Set<String> IMAGES = Set.of("png", "jpg", "jpeg", "webp", "gif");

    @Value("${archnova.upload-dir:uploads}")
    private String uploadDir;

    public String saveImage(MultipartFile file) throws IOException {
        AssertUtil.notNull(file, "请选择文件");
        AssertUtil.isTrue(!file.isEmpty(), "文件不能为空");
        String ext = StrUtil.blankToDefault(FileUtil.extName(file.getOriginalFilename()), "").toLowerCase(Locale.ROOT);
        AssertUtil.isTrue(IMAGES.contains(ext), "仅支持 png、jpg、webp、gif");
        FileUtil.mkdir(uploadDir);
        String filename = IdUtil.fastSimpleUUID() + "." + ext;
        FileUtil.writeFromStream(file.getInputStream(), FileUtil.file(uploadDir, filename));
        return "/uploads/" + filename;
    }
}
