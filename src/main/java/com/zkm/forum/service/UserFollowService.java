package com.zkm.forum.service;

import com.zkm.forum.model.entity.UserFollow;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.vo.userFollow.ListUserFollowVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 张凯铭
* @description 针对表【user_follow(用户关注关系表)】的数据库操作Service
* @createDate 2025-04-14 19:57:40
*/
public interface UserFollowService extends IService<UserFollow> {
    Integer doFollowUser(Long followerId, HttpServletRequest request);
    Integer doFollowUserInner(Long followerId,Long userId);
    List<ListUserFollowVo> listUserFollowVo(HttpServletRequest request);

}
