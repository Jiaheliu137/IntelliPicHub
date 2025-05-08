package com.jiahe.intellipichub_ddd.application.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiahe.intellipichub_ddd.interfaces.dto.space.SpaceAddRequest;
import com.jiahe.intellipichub_ddd.interfaces.dto.space.SpaceQueryRequest;
import com.jiahe.intellipichub_ddd.domain.space.entity.Space;
import com.jiahe.intellipichub_ddd.domain.user.entity.User;
import com.jiahe.intellipichub_ddd.interfaces.vo.space.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author jiahe
* @description 针对表【space(Space)】的数据库操作Service
* @createDate 2025-04-15 14:48:07
*/
public interface SpaceApplicationService extends IService<Space> {

    /**
     * 创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);


    /**
     * 获取空间包装类，单条
     * @param space
     * @param request
     * @return
     */
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间包装类，分页
     * @param spacePage
     * @param request
     * @return
     */
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);


    /**
     * 根据space空间级别填充空间对象
     * @param space
     */
    public void fillSpaceBySpaceLevel(Space space);

    /**
     * 获取查询对象
     * @param spaceQueryRequest
     * @return
     */
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 校验空间权限
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);

}



