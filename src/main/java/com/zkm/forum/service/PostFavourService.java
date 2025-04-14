package com.zkm.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.dto.postfavour.PostFavourRequest;
import com.zkm.forum.model.entity.PostFavour;
import com.zkm.forum.model.vo.postFavour.ListMyPostFavourVo;


import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 张凯铭
* @description 针对表【post_favour(帖子收藏)】的数据库操作Service
* @createDate 2025-03-29 18:04:53
*/
public interface PostFavourService extends IService<PostFavour> {
Integer doPostFavour(PostFavourRequest postFavourRequest, HttpServletRequest request);
Integer doPostFavourInner(Long userId,Long postId);
List<ListMyPostFavourVo> listMyPostFavourVo(HttpServletRequest request);
}
