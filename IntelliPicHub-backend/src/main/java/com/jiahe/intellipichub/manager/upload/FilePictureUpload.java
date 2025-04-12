package com.jiahe.intellipichub.manager.upload;

import cn.hutool.core.io.FileUtil;
import com.jiahe.intellipichub.exception.ErrorCode;
import com.jiahe.intellipichub.exception.ThrowUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;


/**
 * File upload service
 */
@Service
public class FilePictureUpload extends PictureUploadTemplate {

    @Override
    protected void validPicture(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "File cannot be empty");
        // 1. 校验文件大小
        long fileSize = multipartFile.getSize();
        final long ONE_M = 1024 * 1024L;
        ThrowUtils.throwIf(fileSize > 5 * ONE_M, ErrorCode.PARAMS_ERROR, "File size cannot exceed 2M");
        // 2. 校验文件后缀
        String originalFilename = multipartFile.getOriginalFilename();
        ThrowUtils.throwIf(originalFilename == null || originalFilename.isEmpty(), ErrorCode.PARAMS_ERROR, "Invalid file name");
        String fileSuffix = FileUtil.getSuffix(originalFilename).toLowerCase();
        // 允许上传的文件后缀
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "jpg", "png", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "Invalid file type");
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
