package com.archnova.controller.oss;

import com.archnova.common.R;
import com.archnova.domain.oss.FileInfoVo;
import com.archnova.domain.oss.FileTypeEnum;
import com.archnova.service.OssService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssService ossService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<FileInfoVo> upload(@RequestPart("file") MultipartFile file,
                                @RequestParam(defaultValue = "IMAGE", required = false) FileTypeEnum type) {
        return R.ok(ossService.upload(file, type));
    }

    @PostMapping(value = "/uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<List<FileInfoVo>> uploadFiles(@RequestPart("files") MultipartFile[] files,
                                            @RequestParam(defaultValue = "OTHER", required = false) FileTypeEnum type) {
        return R.ok(ossService.upload(files, type));
    }

    @PostMapping("/delete")
    public R<Void> delete(@RequestParam("path") String path) {
        ossService.delete(path);
        return R.ok();
    }

    @PostMapping("/delByPaths")
    public R<Void> delByPaths(@RequestParam("paths") List<String> paths) {
        ossService.delete(paths);
        return R.ok();
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("path") String path) {
        return ossService.download(path);
    }

    @GetMapping("/preview")
    public ResponseEntity<byte[]> preview(@RequestParam("path") String path) {
        return ossService.preview(path);
    }

    @GetMapping("/getBase64")
    public R<String> getBase64(@RequestParam("path") String path) {
        return R.ok(ossService.readBase64(path));
    }

    @GetMapping("/exist")
    public R<Boolean> exist(@RequestParam("path") String path) {
        return R.ok(ossService.exist(path));
    }
}
