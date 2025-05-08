#!/usr/bin/env python
# -*- coding: utf-8 -*-

import time
import json
import re
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from webdriver_manager.chrome import ChromeDriverManager
import logging

# 配置日志 - 只显示警告和错误
logging.basicConfig(level=logging.WARNING, format='%(asctime)s - %(levelname)s - %(message)s')

class YandexImageSearchSimple:
    """
    简化版Yandex图像搜索，只返回原图URL列表
    """
    def __init__(self, headless=True):
        """
        初始化WebDriver
        """
        self.options = Options()
        if headless:
            self.options.add_argument('--headless')
        self.options.add_argument('--disable-gpu')
        self.options.add_argument('--window-size=1920,1080')
        self.options.add_argument('--no-sandbox')
        self.options.add_argument('--disable-dev-shm-usage')
        
        # 启用Chrome开发工具协议
        self.options.set_capability('goog:loggingPrefs', {'performance': 'ALL'})
        
        self.driver = None
        
    def search_image(self, image_url):
        """
        搜索图片并返回原图URL列表
        
        Args:
            image_url: 要搜索的图片URL
            
        Returns:
            list: 原始图片URL列表
        """
        try:
            # 初始化WebDriver
            self.driver = webdriver.Chrome(
                service=Service(ChromeDriverManager().install()),
                options=self.options
            )
            
            # 第一步：获取搜索结果URL
            search_result_url = self._get_search_result_url(image_url)
            if not search_result_url:
                print("无法获取搜索结果URL")
                return []
                
            # 第二步：提取原图URL
            image_urls = self._extract_original_image_urls(search_result_url)
            return image_urls
            
        except Exception as e:
            print(f"搜索图片时出错: {str(e)}")
            return []
        finally:
            # 关闭WebDriver
            if self.driver:
                self.driver.quit()
    
    def _get_search_result_url(self, image_url):
        """
        获取Yandex搜索结果URL
        """
        try:
            # 构建并访问Yandex搜索URL
            url = f"https://yandex.com/images/search?rpt=imageview&url={image_url}"
            print(f"正在访问: {url}")
            
            self.driver.get(url)
            time.sleep(5)  # 等待页面加载
            
            # 从请求日志中提取参数
            logs = self.driver.get_log('performance')
            cbir_id = None
            serpid = None
            
            for log in logs:
                try:
                    log_entry = json.loads(log['message'])['message']
                    if log_entry['method'] == 'Network.requestWillBeSent':
                        req_url = log_entry['params']['request']['url']
                        if 'yandex.com/clck/jclck' in req_url:
                            # 提取serpid
                            serpid_match = re.search(r'serpid=([^/]+)', req_url)
                            if serpid_match:
                                serpid = serpid_match.group(1)
                            
                            # 提取cbir_id
                            if '*' in req_url:
                                after_star = req_url.split('*')[-1]
                                cbir_match = re.search(r'cbir_id=([^&]+)', after_star)
                                if cbir_match:
                                    cbir_id = cbir_match.group(1)
                            
                            if cbir_id and serpid:
                                break
                except:
                    continue
            
            if cbir_id and serpid:
                result_url = f"https://yandex.com/images/search?cbir_id={cbir_id}&cbir_page=similar&rpt=imageview&source-serpid={serpid}&url=https://avatars.mds.yandex.net/get-images-cbir/{cbir_id}/orig"
                print(f"已生成搜索结果URL")
                return result_url
            else:
                return None
                
        except Exception as e:
            print(f"获取搜索结果URL时出错: {str(e)}")
            return None
    
    def _extract_original_image_urls(self, url):
        """
        提取搜索结果页面中的原图URL
        """
        try:
            print(f"正在访问搜索结果页面...")
            self.driver.get(url)
            
            # 等待页面加载
            try:
                WebDriverWait(self.driver, 15).until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, ".serp-item"))
                )
            except:
                print("等待页面元素超时")
            
            # 检查是否有机器人验证
            if "confirm that you and not a robot are sending requests" in self.driver.page_source.lower():
                print("⚠️ 检测到机器人验证页面，需要人工干预")
                return []
            
            # 滚动页面加载更多结果
            self._scroll_page()
            
            # 提取图片URL
            image_urls = []
            
            # 通过data-bem属性精确提取
            serp_items = self.driver.find_elements(By.CSS_SELECTOR, ".serp-item")
            print(f"找到 {len(serp_items)} 个图片元素")
            
            for item in serp_items:
                try:
                    # 获取data-bem属性
                    data_bem = item.get_attribute("data-bem")
                    if data_bem:
                        # 解析JSON
                        data = json.loads(data_bem)
                        if "serp-item" in data and "img_href" in data["serp-item"]:
                            img_url = data["serp-item"]["img_href"]
                            if img_url and img_url not in image_urls:
                                image_urls.append(img_url)
                except:
                    continue
            
            return image_urls
            
        except Exception as e:
            print(f"提取图片URL时出错: {str(e)}")
            return []
    
    def _scroll_page(self):
        """滚动页面以加载更多内容"""
        try:
            # 获取初始高度
            last_height = self.driver.execute_script("return document.body.scrollHeight")
            
            # 滚动3次，或者直到页面底部
            for i in range(3):
                # 滚动到底部
                self.driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
                
                # 等待加载
                time.sleep(2)
                
                # 计算新的滚动高度
                new_height = self.driver.execute_script("return document.body.scrollHeight")
                
                # 如果高度没有变化，说明已经到底部
                if new_height == last_height:
                    break
                    
                last_height = new_height
                
        except:
            pass

def get_similar_images(image_url, headless=True):
    """
    简单的API函数，获取与输入图片相似的原图URL列表
    
    Args:
        image_url: 要搜索的图片URL
        headless: 是否使用无头模式
        
    Returns:
        list: 原始图片URL列表
    """
    search = YandexImageSearchSimple(headless=headless)
    return search.search_image(image_url)

if __name__ == "__main__":
    # 要搜索的图片URL
    image_url = "https://cdn.donmai.us/sample/bc/6e/__claire_harvey_hundred_drawn_by_hellandheaven__sample-bc6eb7dbc6c318418336b73fa6f93a9b.jpg"
    
    # 获取相似图片URL
    print("开始搜索相似图片...")
    result_urls = get_similar_images(image_url)
    
    # 输出结果
    total_count = len(result_urls)
    print(f"\n共找到 {total_count} 张相似图片")
    
    # 打印前10条URL
    if result_urls:
        print("\n前10条图片URL:")
        for i, url in enumerate(result_urls[:10], 1):
            print(f"{i}. {url}")
    else:
        print("没有找到相似图片") 