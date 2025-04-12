package com.jiahe.intellipichub.manager.upload;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.jiahe.intellipichub.exception.BusinessException;
import com.jiahe.intellipichub.exception.ErrorCode;
import com.jiahe.intellipichub.exception.ThrowUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * 文件图片上传
 */
@Service
public class UrlPictureUpload extends PictureUploadTemplate {
    @Override
    protected void validPicture(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 1. 校验非空
        ThrowUtils.throwIf(StrUtil.isBlank(fileUrl), ErrorCode.PARAMS_ERROR, "File URL cannot be empty");
        // 2. 校验url格式
        try {
            // 校验url格式
            new URL(fileUrl);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Invalid file URL");
        }

        // 3. 校验url协议
        ThrowUtils.throwIf(!fileUrl.startsWith("http://")&&!fileUrl.startsWith("https://"), ErrorCode.PARAMS_ERROR, "File URL must start with http or https");

        // 4. 发送head请求验证文件是否存在
        HttpResponse httpResponse = null;
        try{
            // 发送head请求验证文件是否存在
            httpResponse = HttpUtil.createRequest(Method.HEAD, fileUrl).execute();
            // 未正常返回，无需执行其他判断,因为有的url地址不支持head请求
            if (httpResponse.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 文件存在 文件类型校验
            String contentType = httpResponse.header("Content-Type");
            // 不为空才校验是否合法
            if(StrUtil.isNotBlank(contentType)){
                // 允许的图片类型
                final List<String> ALLOW_FORMAT_LIST = Arrays.asList("image/jpeg", "image/png", "image/jpg", "image/webp");
                ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(contentType), ErrorCode.PARAMS_ERROR, "File format is not allowed");
            }


            // 文件存在 文件大小校验
            String contentLengthStr = httpResponse.header("Content-Length");
            if(StrUtil.isNotBlank(contentLengthStr)){
                try{
                    long contentLength = Long.parseLong(contentLengthStr);
                    long maxFileSize = 1024 * 1024 * 5L;
                    ThrowUtils.throwIf(contentLength > maxFileSize, ErrorCode.PARAMS_ERROR, "File size can not be more than 5MB");
                }catch(NumberFormatException e){
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "File size is not a number");
                }
            }

        }finally{
            // 释放资源
            if (httpResponse != null) {
                httpResponse.close();
            }
        }
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String fileUrl = (String) inputSource;
        // 从 URL 中提取文件名
        return FileUtil.mainName(fileUrl);
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        String fileUrl = (String) inputSource;
        // 下载文件到临时目录
        HttpUtil.downloadFile(fileUrl, file);
    }
}
