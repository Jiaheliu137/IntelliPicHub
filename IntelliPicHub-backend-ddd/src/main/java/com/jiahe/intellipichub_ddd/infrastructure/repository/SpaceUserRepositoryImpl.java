package com.jiahe.intellipichub_ddd.infrastructure.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiahe.intellipichub_ddd.domain.space.entity.SpaceUser;
import com.jiahe.intellipichub_ddd.domain.space.repository.SpaceUserRepository;
import com.jiahe.intellipichub_ddd.infrastructure.mapper.SpaceUserMapper;
import org.springframework.stereotype.Service;

/**
 * 空间成员仓储实现
 */
@Service
public class SpaceUserRepositoryImpl extends ServiceImpl<SpaceUserMapper, SpaceUser> implements SpaceUserRepository {
}
