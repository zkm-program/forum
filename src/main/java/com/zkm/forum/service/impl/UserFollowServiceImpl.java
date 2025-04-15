package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.UserMapper;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.entity.UserFollow;
import com.zkm.forum.model.vo.userFollow.ListUserFollowVo;
import com.zkm.forum.service.UserFollowService;
import com.zkm.forum.mapper.UserFollowMapper;
import com.zkm.forum.service.UserService;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 张凯铭
 * @description 针对表【user_follow(用户关注关系表)】的数据库操作Service实现
 * @createDate 2025-04-14 19:57:40
 */
@Service
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow>
        implements UserFollowService {
    @Resource
    private UserService userService;


    @Override
    public Integer doFollowUser(Long userId, HttpServletRequest request) {
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请选择关注的用户");
        }
        User loginUser = userService.getLoginUser(request);
        Long followerId = loginUser.getId();
        UserFollowService userFollowService = (UserFollowService) AopContext.currentProxy();
        synchronized (String.valueOf(userId).intern()) {
            return userFollowService.doFollowUserInner(followerId, userId);
        }
    }

    @Override
    public Integer doFollowUserInner(Long followerId, Long userId) {
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setUserId(userId);
        QueryWrapper<UserFollow> userFollowQueryWrapper = new QueryWrapper<>(userFollow);
        UserFollow follow = this.getOne(userFollowQueryWrapper);
        if (follow == null) {
            boolean save = this.save(userFollow);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "关注失败");
            }
            //被关注用户
            boolean result = userService.update().eq("id", userId).setSql("followerCount=followerCount+1").update();
            return result ? 1 : 0;
        } else {
            boolean remove = this.remove(userFollowQueryWrapper);
            if (!remove) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "取消关注失败");
            }
            boolean result = userService.update().eq("id", userId).setSql("followerCount=followerCount-1").update();
            return result ? 1 : 0;
        }

    }

    @Override
    public List<ListUserFollowVo> listUserFollowVo(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserFollow> userFollowQueryWrapper = new QueryWrapper<>();
        userFollowQueryWrapper.select("userId");
        userFollowQueryWrapper.eq("followerId", loginUser.getId());
        List<UserFollow> userFollowList = this.list(userFollowQueryWrapper);
        List<Long> userIdList = userFollowList.stream().map(UserFollow::getUserId).toList();

        return convertToVo(userIdList);
    }

    private List<ListUserFollowVo> convertToVo(List<Long> userIdList) {
        List<User> userList = userService.listByIds(userIdList);
        List<ListUserFollowVo> listUserFollowVos = new ArrayList<>();
        for (User user : userList) {
            ListUserFollowVo userFollowVo = new ListUserFollowVo();
            userFollowVo.setUserId(user.getId());
            userFollowVo.setUserName(user.getUserName());
            userFollowVo.setUserAvatar(user.getUserAvatar());
            userFollowVo.setIntroduction(user.getIntroduction());
            listUserFollowVos.add(userFollowVo);
        }
        return listUserFollowVos;
    }
}




