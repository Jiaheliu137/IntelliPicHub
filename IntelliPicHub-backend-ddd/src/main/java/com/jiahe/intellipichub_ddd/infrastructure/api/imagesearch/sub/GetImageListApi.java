package com.jiahe.intellipichub_ddd.infrastructure.api.imagesearch.sub;

import com.jiahe.intellipichub_ddd.infrastructure.api.imagesearch.model.ImageSearchResult;
import com.jiahe.intellipichub_ddd.infrastructure.exception.BusinessException;
import com.jiahe.intellipichub_ddd.infrastructure.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GetImageListApi {

    /**
     * 获取图片列表
     *
     * @param imagePageUrl 搜索结果页面URL
     * @return 图片搜索结果列表
     */
    public static List<ImageSearchResult> getImageList(String imagePageUrl) {
        try {
            log.info("Accessing search result page: {}", imagePageUrl);
            
            // 使用JSoup发起GET请求
            Document document = Jsoup.connect(imagePageUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(30000)
                    .get();
            
            // 处理响应
            return processResponse(document);
            
        } catch (Exception e) {
            log.error("Failed to get image list", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Failed to get image list");
        }
    }

    /**
     * 处理接口响应内容
     *
     * @param document JSoup文档对象
     * @return 图片搜索结果列表
     */
    private static List<ImageSearchResult> processResponse(Document document) {
        List<ImageSearchResult> results = new ArrayList<>();
        
        try {
            // 查找所有包含图片信息的元素，使用完整的class选择器
            Elements serpItems = document.select("div.serp-item.serp-item_type_search.serp-item_group_search");
            log.info("找到 {} 个图片项目", serpItems.size());
            
            for (Element item : serpItems) {
                try {
                    ImageSearchResult result = new ImageSearchResult();
                    
                    // 1. 从data-bem属性中提取缩略图URL
                    String dataBem = item.attr("data-bem");
                    if (dataBem != null && !dataBem.isEmpty()) {
                        JSONObject jsonObject = JSONUtil.parseObj(dataBem);
                        JSONObject serpItem = jsonObject.getJSONObject("serp-item");
                        JSONArray preview = serpItem.getJSONArray("preview");
                        if (preview != null && !preview.isEmpty()) {
                            // 获取preview列表中第一个元素的url
                            String thumbUrl = preview.getJSONObject(0).getStr("url", "");
                            result.setThumbUrl(thumbUrl);
                        }
                    }
                    
                    // 2. 从a标签的href属性中提取原图URL
                    Element linkElement = item.select("a").first();
                    if (linkElement != null) {
                        String href = linkElement.attr("href");
                        // 查找img_url参数
                        int imgUrlIndex = href.indexOf("img_url=");
                        if (imgUrlIndex != -1) {
                            String encodedUrl = href.substring(imgUrlIndex + 8); // 8 是 "img_url=" 的长度
                            // 如果URL中还有其他参数，截取到第一个&符号
                            int andIndex = encodedUrl.indexOf('&');
                            if (andIndex != -1) {
                                encodedUrl = encodedUrl.substring(0, andIndex);
                            }
                            // URL解码
                            String fromUrl = java.net.URLDecoder.decode(encodedUrl, "UTF-8");
                            result.setFromUrl(fromUrl);
                        }
                    }
                    
                    // 只添加至少有一个URL不为空的结果
                    if (!result.getThumbUrl().isEmpty() || !result.getFromUrl().isEmpty()) {
                        results.add(result);
                    }
                    
                } catch (Exception e) {
                    log.warn("解析图片项目失败: {}", e.getMessage());
                    continue;
                }
            }
            
            log.info("成功提取 {} 个图片结果", results.size());
            return results;
            
        } catch (Exception e) {
            log.error("处理响应失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "处理图片搜索结果失败");
        }
    }

    public static void main(String[] args) {
        try {
            // 首先获取搜索结果页面URL
            String imageUrl = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/public/1912515518735192066/2025-04-16_tBtae0quoZ5C4pTd.webp";
            String searchResultUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
            
            System.out.println("Getting image list from: " + searchResultUrl);
            
            // 记录开始时间
            long startTime = System.currentTimeMillis();
            
            // 获取图片列表
            List<ImageSearchResult> imageList = getImageList(searchResultUrl);
            
            // 计算运行时间
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            // 打印结果
            System.out.println("\n=== Test Results ===");
            System.out.println("Total images found: " + imageList.size());
            System.out.println("First 20 results:");
            
            // 直接输出对象原始格式
            imageList.stream().limit(20).forEach(System.out::println);
            
            System.out.println("\nExecution time: " + duration + " ms (" + (duration / 1000.0) + " seconds)");
            
        } catch (Exception e) {
            System.err.println("Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
