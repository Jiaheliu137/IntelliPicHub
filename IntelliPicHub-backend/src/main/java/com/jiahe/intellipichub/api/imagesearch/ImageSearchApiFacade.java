package com.jiahe.intellipichub.api.imagesearch;

import com.jiahe.intellipichub.api.imagesearch.model.ImageSearchResult;
import com.jiahe.intellipichub.api.imagesearch.sub.GetImageListApi;
import com.jiahe.intellipichub.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {
    /**
     * 以图搜图
     * @param imgUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imgUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imgUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imagePageUrl);
        return imageList;
    }

    public static void main(String[] args) {
        List<ImageSearchResult> imageList = searchImage("https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/public/1912515518735192066/2025-04-17_FHYH4M3mZXNjwQFI_thumbnail.png");
        System.out.println(imageList);
    }

}



