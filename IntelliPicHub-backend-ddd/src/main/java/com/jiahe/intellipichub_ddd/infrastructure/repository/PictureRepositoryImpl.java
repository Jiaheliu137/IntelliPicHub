package com.jiahe.intellipichub_ddd.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiahe.intellipichub_ddd.domain.picture.entity.Picture;
import com.jiahe.intellipichub_ddd.domain.picture.repository.PictureRepository;
import com.jiahe.intellipichub_ddd.infrastructure.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
 * 图片仓储实现
 */
@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture> implements PictureRepository {
}
