package com.jiahe.intellipichub.manager;


import cn.hutool.core.io.FileUtil;
import com.jiahe.intellipichub.config.CosClientConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


// 通用方法
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    // 将public COSClient cosClient()中返回的COSClient对象注入到CosManager中的cosClient
    @Resource
    private COSClient cosClient;

    /**
     * 上传对象(通用的，不一定是图片)
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }

    /**
     * 上传对象并解析（附带图片信息）
     * 通用上传图片方法
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        // 参考文档https://cloud.tencent.com/document/product/436/55377
        // https://cloud.tencent.com/document/product/436/113308
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);

        // 图片处理操作列表
        List<PicOperations.Rule> rules = new ArrayList<>();

        // 检查是否为GIF格式
        boolean isGif = FileUtil.getSuffix(key).toLowerCase().equals("gif");
        
        // 1. 非GIF图片才进行webp转换
        if (!isGif) {
            String webpKey = FileUtil.mainName(key)+".webp";
            PicOperations.Rule compressRule = new PicOperations.Rule();
            compressRule.setFileId(webpKey);
            compressRule.setBucket(cosClientConfig.getBucket());
            compressRule.setRule("imageMogr2/format/webp");
            rules.add(compressRule);
        }
        
        // 2. 缩略图处理（原图处理为缩略图），仅对20kB以上的图片生成缩略图
        if (file.length() > 20 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            // 拼接缩略图路径
            String thumbnailKey = FileUtil.mainName(key)+"_thumbnail."+FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            // 缩放规则，/thumbnail/<Width>x<Height>>，（如果处理后的图片大于原图宽高则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>",256,256));
            rules.add(thumbnailRule);
        }

        // 构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }


}

