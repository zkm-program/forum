package com.zkm.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.dto.user.UserQueryRequest;
import com.zkm.forum.model.dto.user.UserUpdateMyRequest;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.user.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 张凯铭
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-03-17 17:34:42
 */
public interface UserService extends IService<User> {
    Long userRegister(String userPassword, String checkPassword, String userQqEmail, String userCode, String userName, String gender);

    void sendCode(String userQqEmail);

    User getLoginuser(HttpServletRequest request);

    LoginUserVO login(String userQqEmail, String userPassword, HttpServletRequest request);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    User getUserByEmail(String userQqEmail);

    Boolean userUpdateMy(UserUpdateMyRequest userUpdateMyRequest);

    Boolean updateForAdmin(int matchCount, String userRole,String userQqEmail);

}
