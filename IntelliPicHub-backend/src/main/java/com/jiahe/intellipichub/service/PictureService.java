package com.jiahe.intellipichub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiahe.intellipichub.model.dto.picture.PictureQueryRequest;
import com.jiahe.intellipichub.model.dto.picture.PictureUploadRequest;
import com.jiahe.intellipichub.model.entity.Picture;
import com.jiahe.intellipichub.model.entity.User;
import com.jiahe.intellipichub.model.vo.PictureVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jiahe
 * @description 针对表【intellipic(Image)】的数据库操作Service
 * @createDate 2025-04-05 09:54:35
 */
public interface PictureService extends IService<Picture> {

    /**
     * 上传图片
     *
     * @param multipartFile
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(MultipartFile multipartFile,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类，单条
     * @param picture
     * @param request
     * @return
     */
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类，分页
     * @param picturePage
     * @param request
     * @return
     */
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);


    public void validPicture(Picture picture);

}


