package com.zkm.forum.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.dto.user.*;
import com.zkm.forum.model.entity.MatchTags;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.user.KnowUserVo;
import com.zkm.forum.model.vo.user.LoginUserVO;
import com.zkm.forum.model.vo.user.MatchUserVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * @author 张凯铭
 * @description 针对表【user(用户表)】的数据库操作Service
 * @createDate 2025-03-17 17:34:42
 */
public interface UserService extends IService<User> {
    Long userRegister(UserRegisterRequest userRegisterRequest);

    Boolean sendCode(String userQqEmail);

    User getLoginUser(HttpServletRequest request);

    LoginUserVO login(String userQqEmail, String userPassword, HttpServletRequest request);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    User getUserByEmail(String userQqEmail);

    Boolean userUpdateMy(UserUpdateMyRequest userUpdateMyRequest);

    Boolean updateForAdmin(int matchCount, String userRole, String userQqEmail);

    Boolean reportUser(ReportUserRequest reportUserRequest, HttpServletRequest request);

    List<MatchTags> matchUserByTags(List<String> tags, HttpServletRequest request);

    MatchTags superMatchUser(HttpServletRequest request);

    Map<Long, String> getOwnWithOtherDistance(HttpServletRequest request);

    List<LoginUserVO> getOwnCircleDistance(HttpServletRequest request, double distance);
    Boolean addUserSignIn(Long userId);
    Map<Long, Boolean> getUserThisWeekSign(Long userId);
    KnowUserVo getKnowUserVo(Long userId);
    Boolean updateMyTas(List<String> tags,HttpServletRequest request);
    String uploadCosWanXiang(UploadCosWanXiangRequest uploadCosWanXiangRequest);
    Boolean getReward(User loginUser);
    List<String> checkMyTags(HttpServletRequest request);
}

