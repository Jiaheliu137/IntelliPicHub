#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import urllib.parse
import requests
import logging
import sys
import time
import json
import os
import random

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

class YandexImageSearcher:
    """
     * Yandex图片搜索工具
     * 根据图片URL获取Yandex相似图片搜索结果
     */
    """
    
    # 从HTML中提取serpid的正则表达式
    SERPID_PATTERN = re.compile(r'"serpid":"([^"]+)"')
    
    # 从HTML中提取cbir_id的正则表达式
    CBIR_ID_PATTERN = re.compile(r'cbir_id=([^&"]+)')
    
    # 从HTML中提取cbirSimilar的正则表达式
    CBIR_SIMILAR_PATTERN = re.compile(r'"cbirSimilar":\s*{\s*"thumbs":\s*(\[.*?\])')
    
    @staticmethod
    def get_image_page_url(image_url):
        """
        * 获取图片页面地址
        *
        * @param image_url 要搜索的图片URL
        * @return 搜索结果页面的URL
        """
        # 1. 准备请求参数
        encoded_image_url = urllib.parse.quote(image_url)
        
        # 请求地址
        search_url = f"https://yandex.com/images/search?rpt=imageview&url={encoded_image_url}"
        logger.info(f"访问Yandex搜索URL: {search_url}")
        
        try:
            # 2. 发送请求并获取HTML
            headers = YandexImageSearcher.get_random_headers()
            response = requests.get(search_url, headers=headers, timeout=20)
            response.raise_for_status()
            
            # 保存初始响应的HTML
            with open('initial_response.html', 'w', encoding='utf-8') as f:
                f.write(response.text)
            logger.info("已保存初始响应HTML到 initial_response.html")
            
            # 3. 提取参数
            html = response.text
            cbir_id = YandexImageSearcher.extract_parameter(html, YandexImageSearcher.CBIR_ID_PATTERN)
            serp_id = YandexImageSearcher.extract_parameter(html, YandexImageSearcher.SERPID_PATTERN)
            
            if not cbir_id or not serp_id:
                logger.error(f"无法提取所需的搜索参数, cbir_id: {cbir_id}, serp_id: {serp_id}")
                raise Exception("无法提取图片搜索参数")
            
            # 构建最终的搜索结果URL
            result_url = (
                f"https://yandex.com/images/search?cbir_id={cbir_id}&cbir_page=similar&rpt=imageview"
                f"&source-serpid={serp_id}&url=https://avatars.mds.yandex.net/get-images-cbir/{cbir_id}/orig"
            )
            
            logger.info(f"生成搜索结果URL: {result_url}")
            return result_url
            
        except Exception as e:
            logger.error(f"获取图片搜索结果URL时出错: {str(e)}")
            raise Exception(f"图片搜索失败: {str(e)}")
    
    @staticmethod
    def extract_parameter(html, pattern):
        """
        * 从HTML中提取参数
        * 
        * @param html HTML内容
        * @param pattern 匹配模式
        * @return 提取的参数值
        """
        match = pattern.search(html)
        return match.group(1) if match else None
    
    @staticmethod
    def get_random_headers():
        """
        * 生成随机的请求头，模拟不同的浏览器
        *
        * @return 随机请求头字典
        """
        user_agents = [
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:89.0) Gecko/20100101 Firefox/89.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Edg/91.0.864.59"
        ]
        
        return {
            "User-Agent": random.choice(user_agents),
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.5",
            "Referer": "https://yandex.com/",
            "Connection": "keep-alive",
            "Upgrade-Insecure-Requests": "1",
            "Cache-Control": "max-age=0"
        }
    
    @staticmethod
    def get_image_urls(search_result_url):
        """
        * 从搜索结果页面获取缩略图和原图URL
        *
        * @param search_result_url 搜索结果页面URL
        * @return 包含缩略图和原图URL的列表
        """
        logger.info(f"开始获取图片URL，搜索结果页面: {search_result_url}")
        
        try:
            headers = YandexImageSearcher.get_random_headers()
            
            response = requests.get(search_result_url, headers=headers, timeout=30)
            response.raise_for_status()
            
            # 保存搜索结果页面的HTML
            html_dir = 'html_responses'
            os.makedirs(html_dir, exist_ok=True)
            html_file_path = os.path.join(html_dir, f'search_results_{int(time.time())}.html')
            
            with open(html_file_path, 'w', encoding='utf-8') as f:
                f.write(response.text)
            logger.info(f"已保存搜索结果页面HTML到 {html_file_path}")
            
            html = response.text
            results = YandexImageSearcher.extract_images_from_cbir_similar(html)
            
            logger.info(f"成功获取 {len(results)} 个图片URL")
            return results
            
        except Exception as e:
            logger.error(f"获取图片URL时出错: {str(e)}")
            raise Exception(f"获取图片URL失败: {str(e)}")
    
    @staticmethod
    def extract_images_from_cbir_similar(html):
        """
        * 直接从HTML中提取cbirSimilar部分的图片URL
        *
        * @param html HTML内容
        * @return 包含缩略图和原图URL的列表
        """
        results = []
        
        try:
            # 查找cbirSimilar部分
            html_decoded = html.replace('&quot;', '"').replace('&amp;', '&')
            
            # 使用正则表达式直接提取JSON数据
            thumbs_match = YandexImageSearcher.CBIR_SIMILAR_PATTERN.search(html_decoded)
            if not thumbs_match:
                logger.warning("未找到cbirSimilar数据")
                return []
            
            # 提取JSON字符串
            thumbs_json = thumbs_match.group(1)
            # 处理可能的转义字符
            thumbs_json = thumbs_json.replace('\\', '')
            
            try:
                # 解析JSON数据
                thumbs_list = json.loads(thumbs_json)
                logger.info(f"从cbirSimilar中找到 {len(thumbs_list)} 个图片")
                
                for item in thumbs_list:
                    # 确保imageUrl是完整URL
                    thumbnail_url = item.get('imageUrl', '')
                    if isinstance(thumbnail_url, str) and thumbnail_url.startswith('//'):
                        thumbnail_url = 'https:' + thumbnail_url
                    
                    # 从linkUrl提取原图URL
                    link_url = item.get('linkUrl', '')
                    if not isinstance(link_url, str):
                        continue
                    
                    # 提取原图URL
                    img_url_match = re.search(r'img_url=([^&]+)', link_url)
                    if not img_url_match:
                        continue
                    
                    original_url = urllib.parse.unquote(img_url_match.group(1))
                    
                    # 只保留缩略图和原图URL
                    results.append({
                        'thumbnail_url': thumbnail_url,
                        'original_url': original_url
                    })
                
            except json.JSONDecodeError as e:
                logger.error(f"解析JSON数据失败: {str(e)}")
                
        except Exception as e:
            logger.error(f"提取cbirSimilar数据时出错: {str(e)}")
        
        return results


def main():
    """主函数，用于测试"""
    try:
        # 测试图片URL
        test_image_url = "https://jiahe-intellipichub-1352763103.cos.ap-hongkong.myqcloud.com/space/null/2025-04-17_61VBNYkUpWIvT5s0.webp"
        
        print("开始测试获取图片搜索URL...")
        print(f"测试图片URL: {test_image_url}")
        
        # 记录开始时间
        start_time = time.time()
        
        # 获取搜索结果页面URL
        result_url = YandexImageSearcher.get_image_page_url(test_image_url)
        
        # 获取图片URL
        image_results = YandexImageSearcher.get_image_urls(result_url)
        
        # 计算运行时间
        end_time = time.time()
        duration = end_time - start_time
        
        # 打印结果
        print("\n=== 测试结果 ===")
        print(f"搜索结果URL: {result_url}")
        print(f"找到 {len(image_results)} 个图片")
        
        # 打印前几个结果作为示例
        for i, result in enumerate(image_results[:3], 1):
            print(f"\n图片 {i}:")
            print(f"  缩略图URL: {result['thumbnail_url']}")
            print(f"  原图URL: {result['original_url']}")
        
        print(f"\n...共 {len(image_results)} 个结果")
        print(f"\n执行时间: {duration:.3f} 秒")
        print("测试完成")
        
        # 将完整结果保存到文件
        with open('image_results.json', 'w', encoding='utf-8') as f:
            json.dump(image_results, f, ensure_ascii=False, indent=2)
        print("\n完整结果已保存到 image_results.json")
        
    except Exception as e:
        print(f"测试过程中出错: {str(e)}")
        import traceback
        traceback.print_exc()


if __name__ == "__main__":
    main()
