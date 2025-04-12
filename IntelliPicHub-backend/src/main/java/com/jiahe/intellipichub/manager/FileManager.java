package com.jiahe.intellipichub.manager;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.jiahe.intellipichub.config.CosClientConfig;
import com.jiahe.intellipichub.exception.BusinessException;
import com.jiahe.intellipichub.exception.ErrorCode;
import com.jiahe.intellipichub.exception.ThrowUtils;
import com.jiahe.intellipichub.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


// 上传图片通用方法
@Slf4j
@Service
@Deprecated
public class FileManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * @param multipartFile    文件
     * @param uploadPathPrefix 上传的路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(MultipartFile multipartFile, String uploadPathPrefix) {
        // 校验图片
        validPicture(multipartFile);
        // 图片上传地址
        String uuid = RandomUtil.randomString(16);
        String originalFilename = multipartFile.getOriginalFilename();
        // 自己拼接文件路径而不是使用原始文件名称，可以增加安全性
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, FileUtil.getSuffix(originalFilename));
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);
        // 解析结果并返回
        File file = null;
        try {
            // 创建临时文件
            file = File.createTempFile(uploadPath, null); // 本地临时路径
            multipartFile.transferTo(file); // 前端请求的文件参数，存到临时文件中
            // 上传图片
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);// 上传到对象存储，并获得上传结果对象，用于解析信息
            // 获取图片信息对象，封装返回结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            UploadPictureResult uploadPictureResult = new UploadPictureResult();
            String format = imageInfo.getFormat();
            int picWidth = imageInfo.getWidth();
            int picHeight = imageInfo.getHeight();
            // 计算宽高
            double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();


            // 封装返回结果
            uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            uploadPictureResult.setPicName(FileUtil.mainName(originalFilename));
            uploadPictureResult.setPicSize(FileUtil.size(file));
            uploadPictureResult.setPicWidth(picWidth);
            uploadPictureResult.setPicHeight(picHeight);
            uploadPictureResult.setPicScale(picScale);
            uploadPictureResult.setPicFormat(format);

            // Return accessible URL
            return uploadPictureResult;
        } catch (Exception e) {
            log.error("Failed to upload image to object storage, filepath = ", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Upload failed");
        } finally {
            // Clean up temporary files
            deleteTempFile(file);
        }
        // 临时文件清理
    }


    /**
     * 校验图片
     *
     * @param multipartFile
     */
    private void validPicture(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "File can not be empty");
        // 1. 校验文件大小
        long fileSzie = multipartFile.getSize();
        final long MAX_FILE_SIZE = 1024 * 1024 * 2L;
        ThrowUtils.throwIf(fileSzie > MAX_FILE_SIZE, ErrorCode.PARAMS_ERROR, "File size can not be more than 2M");
        // 2. 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        if (fileSuffix != null) {
            fileSuffix = fileSuffix.toLowerCase(); // 转为小写，不区分大小写
        }
        // 运行上传的文件后缀列表
        final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpg", "png", "jpeg", "webp");
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, "File format is not allowed");
    }

    /**
     * 清理临时文件
     *
     * @param file
     */
    public static void deleteTempFile(File file) {
        if (file == null) {
            return;
        }

        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsoluteFile());
        }

    }

}

