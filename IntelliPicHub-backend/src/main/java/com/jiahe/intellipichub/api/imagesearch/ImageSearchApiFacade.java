package com.jiahe.intellipichub.api.imagesearch;

import com.jiahe.intellipichub.api.imagesearch.model.ImageSearchResult;
import com.jiahe.intellipichub.api.imagesearch.sub.bing.GetImageListApi_bing;
import com.jiahe.intellipichub.api.imagesearch.sub.bing.GetImagePageUrlApi_bing;
import com.jiahe.intellipichub.api.imagesearch.sub.yandex.GetImageListApi_yandex;
import com.jiahe.intellipichub.api.imagesearch.sub.yandex.GetImagePageUrlApi_yandex;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 以图搜图（默认使用Bing）
     * @param imgUrl 图片URL
     * @return 搜索结果
     */
    public static List<ImageSearchResult> searchImage(String imgUrl) {
        return searchImage(imgUrl, "bing");
    }

    /**
     * 以图搜图（可选搜索引擎）
     * @param imgUrl 图片URL
     * @param engine 搜索引擎类型（"bing" 或 "yandex"）
     * @return 搜索结果
     */
    public static List<ImageSearchResult> searchImage(String imgUrl, String engine) {
        String imagePageUrl;
        List<ImageSearchResult> imageList;

        switch (engine.toLowerCase()) {
            case "yandex":
                imagePageUrl = GetImagePageUrlApi_yandex.getImagePageUrl(imgUrl);
                imageList = GetImageListApi_yandex.getImageList(imagePageUrl);
                break;
            case "bing":
            default:
                imagePageUrl = GetImagePageUrlApi_bing.getImagePageUrl(imgUrl);
                imageList = GetImageListApi_bing.getImageList(imagePageUrl);
                break;
        }

        return imageList;
    }

    public static void main(String[] args) {
        String testImageUrl = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/public/1912515518735192066/2025-04-17_FHYH4M3mZXNjwQFI_thumbnail.png";

        List<ImageSearchResult> imageListBing = searchImage(testImageUrl);
        System.out.println("Bing Search Result: " + imageListBing);

        List<ImageSearchResult> imageListYandex = searchImage(testImageUrl, "yandex");
        System.out.println("Yandex Search Result: " + imageListYandex);
    }
}
