package com.jiahe.intellipichub_ddd.utils;

import cn.hutool.http.HttpUtil;
import java.io.File;

/**
 * URL图片下载测试类
 */
public class UrlDownloadTest {
    
    /**
     * 主方法，程序入口
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 图片URL
        String imageUrl = "https://vigen-invi.oss-cn-shanghai.aliyuncs.com/service_dashscope/ImageOutPainting/2025-04-22/public/951eaeef-1b97-41aa-a9f3-b678dc3e46ff/result-ac722e13-357f-4be7-b607-8fd9f13264d7.jpg?OSSAccessKeyId=LTAI5t7aiMEUzu1F2xPMCdFj&Expires=1745357593&Signature=CJJr9dsY%2FBb%2BM67DiPCz0F93vqU%3D";
        
        // 保存路径
        String savePath = "D:\\downloaded_image.webp";
        
        try {
            // 使用Hutool的HttpUtil下载文件
            long size = HttpUtil.downloadFile(imageUrl, new File(savePath));
            System.out.println("图片下载成功，文件大小: " + size + " 字节");
            System.out.println("文件保存路径: " + savePath);
        } catch (Exception e) {
            System.err.println("图片下载失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}