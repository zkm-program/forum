package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.constant.CommonConstant;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.UserMapper;
import com.zkm.forum.model.dto.user.UserQueryRequest;
import com.zkm.forum.model.dto.user.UserUpdateMyRequest;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.user.LoginUserVO;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.MailUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.zkm.forum.constant.LocalCacheConstant.USERID_USERNAME;

/**
 * @author 张凯铭
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-03-17 17:34:42
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private Cache<String, String> LOCAL_CACHE;
    private HashMap<String, Long> codeWithTime = new HashMap<>();
    private static final String SALT = "Masami";


    @Override
    public Long userRegister(String userPassword, String checkPassword, String userQqEmail, String userCode, String userName, String gender) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        QueryWrapper<User> userQueryWrapper = queryWrapper.eq("userQqEmail", userQqEmail);
        long count = this.count(userQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已经注册过");
        }
        Long oldTime = codeWithTime.get(userCode);
        long newTime = System.currentTimeMillis();

        if (oldTime == null || (newTime - oldTime) >= TimeUnit.MINUTES.toMillis(1)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码过期");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }
        String encryptPassword = DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());
        User user = new User();
        user.setUserName(userName);
        user.setUserQqEmail(userQqEmail);
        user.setGender(gender);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败，请稍后再试");
        }
        return user.getId();
    }

    public LoginUserVO login(String userQqEmail, String userPassword, HttpServletRequest request) {
        String encryptPassword = DigestUtils.md5DigestAsHex((userPassword + SALT).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userPassword", encryptPassword);
        queryWrapper.eq("userQqEmail", userQqEmail);
        User user = this.baseMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱或密码错误或账户不存在");
        }
        if (user.getUserRole().equals(UserConstant.BAN_ROLE)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已被封，禁止登录！");
        }
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        LOCAL_CACHE.put(USERID_USERNAME+user.getId(),user.getUserName());
        return objToVo(user);
    }


    @Override
    public void sendCode(String userQqEmail) {
        Set<String> userQqSet = new HashSet<>();
        Random random = new Random();
        String code = String.valueOf(random.nextInt(8999) + 1000);// 0-9的随机数
        codeWithTime.put(code, System.currentTimeMillis());
        userQqSet.add(userQqEmail);
        boolean condition = MailUtils.sendEmail(userQqSet, "商师守夜人验证码", "有效期为60秒，请勿将验证码泄露给他人" + code);
        if (!condition) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "出错了请稍等一会再尝试发送验证码");
        }
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object attribute = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) attribute;
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        Long userId = user.getId();
        user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return user;
    }


    /**
     * 每30分钟清理一次内存，就是验证码30分钟过期
     */
    public void codeWithTimeClear() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(() -> {
            long newTime = System.currentTimeMillis();
            codeWithTime.entrySet().removeIf(entry -> (newTime - entry.getValue()) > TimeUnit.MINUTES.toMillis(30));
            // todo 下方三个参数什么意思没明白
        }, 1, 1, TimeUnit.MINUTES);
    }


    public static User voToObj(LoginUserVO loginUserVO) {
        User user = new User();
        BeanUtils.copyProperties(loginUserVO, user);
        List<String> tagList = loginUserVO.getTagList();
        user.setTags(JSONUtil.toJsonStr(tagList));
        return user;
    }

    public static LoginUserVO objToVo(User user) {
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        List<String> list = JSONUtil.toList(JSONUtil.parseArray(user.getTags()), String.class);
        loginUserVO.setTagList(list);
        return loginUserVO;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        String gender = userQueryRequest.getGender();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        String isReported = userQueryRequest.getIsReported();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(gender), "gender", gender);
        queryWrapper.eq(StringUtils.isNotBlank(isReported), "isReported", isReported);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    @Override
    public User getUserByEmail(String userQqEmail) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(StringUtils.isNotBlank(userQqEmail), "userQqEmail", userQqEmail);
        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        return user;
    }

    @Override
    public Boolean userUpdateMy(UserUpdateMyRequest userUpdateMyRequest) {
        List<String> tagList = userUpdateMyRequest.getTags();
        String userName = userUpdateMyRequest.getUserName();
        String gender = userUpdateMyRequest.getGender();
        User user = new User();
        user.setId(userUpdateMyRequest.getId());
        user.setUserName(userName);
        user.setGender(gender);
        String tags = JSONUtil.toJsonStr(tagList);
        user.setTags(tags);
        return this.updateById(user);
    }

    @Override
    public Boolean updateForAdmin(int matchCount, String userRole, String userQqEmail) {
        User olduser = this.getUserByEmail(userQqEmail);
        User newuser = new User();
        newuser.setUserRole(userRole);
        newuser.setMatchCount(matchCount);
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userQqEmail", userQqEmail);
        boolean result = this.update(newuser, updateWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "修改失败请稍后再试！");
        }
        return result;
    }


}




