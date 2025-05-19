package com.jiahe.intellipichub.api.imagesearch.sub.bing;

public class GetImagePageUrlApi_bing {

    /**
     * 根据图片URL构造Bing搜图页面地址
     *
     * @param imageUrl 待搜索的图片URL
     * @return Bing搜图跳转地址
     */
    public static String getImagePageUrl(String imageUrl) {
        return "https://www.bing.com/images/search?view=detailv2&iss=sbi&q=imgurl:" + imageUrl;
    }

    public static void main(String[] args) {
        String imageUrl = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/space/null/20250420005702128-0dv2l2dL.webp";
        String bingUrl = getImagePageUrl(imageUrl);
        System.out.println("构造的 Bing 搜图 URL:");
        System.out.println(bingUrl);
    }
}
