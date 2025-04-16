package com.jiahe.intellipichub.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jiahe.intellipichub.model.dto.picture.*;
import com.jiahe.intellipichub.model.entity.Picture;
import com.jiahe.intellipichub.model.entity.User;
import com.jiahe.intellipichub.model.vo.PictureVO;

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
     * @param inputSource          文件输入源
     * @param pictureUploadRequest
     * @param loginUser
     * @return
     */
    PictureVO uploadPicture(Object inputSource,
                            PictureUploadRequest pictureUploadRequest,
                            User loginUser);

    public QueryWrapper<Picture> getQueryWrapper(PictureQueryRequest pictureQueryRequest);

    /**
     * 获取图片包装类，单条
     *
     * @param picture
     * @param request
     * @return
     */
    public PictureVO getPictureVO(Picture picture, HttpServletRequest request);

    /**
     * 获取图片包装类，分页
     *
     * @param picturePage
     * @param request
     * @return
     */
    public Page<PictureVO> getPictureVOPage(Page<Picture> picturePage, HttpServletRequest request);


    public void validPicture(Picture picture);


    /**
     * @param pictureReviewRequest
     * @param loginUser
     */
    void doPictureReview(PictureReviewRequest pictureReviewRequest, User loginUser);


    /**
     * 填充审核参数
     *
     * @param picture
     * @param loginUser
     */
    void fillReviewParams(Picture picture, User loginUser);

    /**
     * 批量抓取和创建图片
     *
     * @param pictureUploadByBatchRequest
     * @param loginUser
     * @return 返回成功创建的图片数
     */
    Integer uploadPictureByBatch(PictureUploadByBatchRequest pictureUploadByBatchRequest, User loginUser);

    /**
     * 清理图片(删除对象存储中的文件)
     *
     * @param oldPicture
     */
    void clearPictureFile(Picture oldPicture);

    /**
     * 编辑图片
     *
     * @param pictureEditRequest
     * @param loginUser
     */
    void editPicture(PictureEditRequest pictureEditRequest, User loginUser);

    /**
     * 校验空间图片的权限
     *
     * @param loginUser
     * @param picture
     */
    void checkPictureAuth(User loginUser, Picture picture);

    /**
     * 删除图片
     *
     * @param pictureId
     * @param loginUser
     */
    public void deletePicture(long pictureId, User loginUser);

}


