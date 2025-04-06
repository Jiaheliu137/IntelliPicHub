package generator.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jiahe.intellipichub.model.entity.Picture;
import generator.service.IntellipicService;
import com.jiahe.intellipichub.mapper.PictureMapper;
import org.springframework.stereotype.Service;

/**
* @author jiahe
* @description 针对表【intellipic(Image)】的数据库操作Service实现
* @createDate 2025-04-05 09:54:35
*/
@Service
public class IntellipicServiceImpl extends ServiceImpl<PictureMapper, Picture>
    implements IntellipicService{

}




