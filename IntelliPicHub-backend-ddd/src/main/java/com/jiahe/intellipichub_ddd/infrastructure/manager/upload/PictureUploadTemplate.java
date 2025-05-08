package com.jiahe.intellipichub_ddd.infrastructure.manager.upload;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.jiahe.intellipichub_ddd.infrastructure.config.CosClientConfig;
import com.jiahe.intellipichub_ddd.infrastructure.exception.BusinessException;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ErrorCode;
import com.jiahe.intellipichub_ddd.infrastructure.CosManager;
import com.jiahe.intellipichub_ddd.infrastructure.manager.upload.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.jiahe.intellipichub_ddd.utils.HexColorExpander.expandHexColor;


/**
 * 图片上传模板
 */
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    protected CosManager cosManager;

    @Resource
    protected CosClientConfig cosClientConfig;

    /**
     * 模板方法，定义上传流程,final修饰的类不能被继承，final修饰的方法不能被override，final修饰的变量不能被更改
     */
    public final UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        // 1. 先校验
        validPicture(inputSource);
        // 获取文件原始名称
        String originFilename = getOriginFilename(inputSource);
        // 2. 构造存储路径为 aa/bb/cc/yyyyMMddHHmmssSSS-随机数.jpg
        String suffix = FileUtil.getSuffix(originFilename);
        String uuid = RandomUtil.randomString(8);
        String filename = DateUtil.format(new Date(), "yyyyMMddHHmmssSSS") + "-" + uuid;
        String uploadPath;
        if (StrUtil.isBlank(suffix)) {
            uploadPath = uploadPathPrefix + "/" + filename;
        } else {
            uploadPath = uploadPathPrefix + "/" + filename + "." + suffix;
        }

        // 检查是否为GIF
        boolean isGif = !StrUtil.isBlank(suffix) && suffix.toLowerCase().equals("gif");

        File file = null;
        // 生成原图URL
        String originalUrl = cosClientConfig.getHost() + "/" + uploadPath;

        try {
            // 3. 在服务器创建临时文件
            file = File.createTempFile(uploadPath, null);
            // 处理文件来源（本地或 URL）
            processFile(inputSource, file);

            // 4. 上传图片到对象存储
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);

            // 5. 获取图片信息对象，并返回封装结果
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            //  获取图片处理后的结果
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            
            if (CollUtil.isNotEmpty(objectList)) {
                // 获取处理结果(对于非GIF为压缩图，对于GIF为缩略图)
                CIObject ciObject = objectList.get(0);
                
                // 如果是GIF图片，使用原图URL作为压缩图URL
                if (isGif) {
                    return buildResult(originFilename, file, imageInfo, originalUrl, objectList);
                }
                
                // 缩略图默认等于第一个结果
                CIObject thumbnailCiObject = ciObject;
                // 有生成缩略图才获取缩略图(第二个结果)
                if(objectList.size() > 1){
                    thumbnailCiObject = objectList.get(1);
                }
                
                // 使用新方法，传入file参数以获取原图大小
                return buildResult(originFilename, ciObject, file, originalUrl, thumbnailCiObject, imageInfo);
            }
            
            // 如果没有生成任何处理后的结果，使用原图
            return buildResult(originFilename, file, uploadPath, imageInfo, originalUrl);
        } catch (Exception e) {
            log.error("Failed to upload image to object storage, filepath = {}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Upload failed");
        } finally {
            // 6. 清理临时文件
            this.deleteTempFile(file);
        }
    }



    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract void validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名，protected：同一个包内的所有类都能访问。不同包中的子类可以访问。不同包中的非子类不能访问。
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;
    
    /**
     * 从原始文件名中提取图片格式
     * 如果不是常见图片格式，则返回webp
     * @param originFilename 原始文件名
     * @param fallbackFormat 备用格式（如果无法从原始文件名中提取出有效格式）
     * @return 图片格式
     */
    protected String extractFormatFromOriginalUrl(String originFilename, String fallbackFormat) {
        if (StrUtil.isBlank(originFilename)) {
            return fallbackFormat;
        }
        
        // 从原始文件名获取后缀作为格式
        String format = FileUtil.getSuffix(originFilename);
        if (StrUtil.isBlank(format)) {
            return fallbackFormat;
        }
        
        // 转换为小写
        format = format.toLowerCase();
        
        // 常见图片格式列表
        List<String> commonFormats = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff");
        
        // 如果不是常见图片格式，则使用备用格式
        if (!commonFormats.contains(format)) {
            return fallbackFormat;
        }
        
        return format;
    }


    /**
     * 封装返回结果（新增方法，增加file参数获取原图大小）
     * @param originFilename 原始文件名
     * @param compressedCiObject 压缩后的对象
     * @param file 原始文件
     * @param originalUrl 原始图像URL
     * @param thumbnailCiObject 缩略图对象
     * @param imageInfo 图片信息
     * @return 上传结果
     */
    private UploadPictureResult buildResult(String originFilename, CIObject compressedCiObject, File file, String originalUrl, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicColor(expandHexColor(imageInfo.getAve()));

        // 使用从原始URL中提取的格式，如果无法提取则使用压缩后的webp格式
        String picFormat = extractFormatFromOriginalUrl(originFilename, compressedCiObject.getFormat());
        uploadPictureResult.setPicFormat(picFormat);
        
        // 使用原图文件大小
        uploadPictureResult.setPicSize(FileUtil.size(file));
        // 设置压缩为webp的原图urlzz
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        // 设置原图url
        uploadPictureResult.setOriginalUrl(originalUrl);
        // 设置缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }


    /**
     * 为GIF图片构建结果
     *
     * @param originFilename 原始文件名
     * @param file 文件
     * @param imageInfo 图片信息
     * @param originalUrl 原始URL
     * @param objectList 处理结果列表
     * @return 上传结果
     */
    private UploadPictureResult buildResult(String originFilename, File file, ImageInfo imageInfo, String originalUrl, List<CIObject> objectList) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicColor(expandHexColor(imageInfo.getAve()));

        // 设置图片格式为gif
        uploadPictureResult.setPicFormat("gif");
        
        uploadPictureResult.setPicSize(FileUtil.size(file));
        // 对于GIF，压缩图URL使用原图URL
        uploadPictureResult.setUrl(originalUrl);
        uploadPictureResult.setOriginalUrl(originalUrl);
        
        // 设置缩略图URL(如果有)
        if (CollUtil.isNotEmpty(objectList)) {
            uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + objectList.get(0).getKey());
        } else {
            // 没有缩略图也用原图
            uploadPictureResult.setThumbnailUrl(originalUrl);
        }
        
        return uploadPictureResult;
    }

    /**
     * 封装返回结果（不带缩略图路径）
     *
     * @param originFilename 原始文件名
     * @param file 文件
     * @param uploadPath 上传路径
     * @param imageInfo 对象存储返回的图片信息
     * @param originalUrl 原始图像URL
     * @return
     */
    private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo, String originalUrl) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicColor(expandHexColor(imageInfo.getAve()));


        // 使用从原始URL中提取的格式，如果无法提取则使用imageInfo中的格式
        String picFormat = extractFormatFromOriginalUrl(originFilename, imageInfo.getFormat());
        uploadPictureResult.setPicFormat(picFormat);
        
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setOriginalUrl(originalUrl);
        // 如果没有缩略图，缩略图地址也使用原图
        uploadPictureResult.setThumbnailUrl(originalUrl);
        return uploadPictureResult;
    }

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }

    /**
     * 封装返回结果（保留原方法，兼容已有调用）
     * @param originFilename 原始文件名
     * @param compressedCiObject 压缩后的对象
     * @param originalUrl 原始图像URL
     * @param thumbnailCiObject 缩略图对象
     * @param imageInfo 图片主色调
     * @return 上传结果
     */
    @Deprecated
    private UploadPictureResult buildResult(String originFilename, CIObject compressedCiObject, String originalUrl, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicColor(expandHexColor(imageInfo.getAve()));

        // 使用从原始URL中提取的格式，如果无法提取则使用压缩后的webp格式
        String picFormat = extractFormatFromOriginalUrl(originFilename, compressedCiObject.getFormat());
        uploadPictureResult.setPicFormat(picFormat);
        
        // 使用压缩后的webp大小
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        // 设置压缩为webp的原图urlzz
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        // 设置原图url
        uploadPictureResult.setOriginalUrl(originalUrl);
        // 设置缩略图地址
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }
}


