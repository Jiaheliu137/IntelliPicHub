package com.jiahe.intellipichub_ddd.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiahe.intellipichub_ddd.domain.space.entity.Space;
import com.jiahe.intellipichub_ddd.domain.space.repository.SpaceRepository;
import com.jiahe.intellipichub_ddd.infrastructure.mapper.SpaceMapper;
import org.springframework.stereotype.Service;

/**
 * 空间仓储实现
 */
@Service
public class SpaceRepositoryImpl extends ServiceImpl<SpaceMapper, Space> implements SpaceRepository {

}
