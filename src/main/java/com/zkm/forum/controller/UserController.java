package com.zkm.forum.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkm.forum.annotation.AuthCheck;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.user.*;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.user.KnowUserVo;
import com.zkm.forum.model.vo.user.LoginUserVO;
import com.zkm.forum.model.vo.user.MatchUserVo;
import com.zkm.forum.service.UserService;
import com.zkm.forum.strategy.context.UploadStrategyContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Api(tags = "用户模块")
@RequestMapping("/user")
@RestController
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private UploadStrategyContext uploadStrategyContext;

    /**
     * 注册
     *
     * @param userRegisterRequest
     * @return
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public BaseResponse<Long> register(@RequestBody UserRegisterRequest userRegisterRequest) {

        Long id = userService.userRegister(userRegisterRequest);
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册失败请稍后重试");
        }
        return ResultUtils.success(id);

    }

    /**
     * 登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {

        String userQqEmail = userLoginRequest.getUserQqEmail();
        String userPassword = userLoginRequest.getUserPassword();
        if (userQqEmail == null || userQqEmail.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请填写邮箱");
        }
        if (userPassword == null || userPassword.length() < 6 || userPassword.length() > 12) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请填写密码");
        }
        return ResultUtils.success(userService.login(userQqEmail, userPassword, request));

    }

    /**
     * 发送邮箱验证码
     *
     * @param userQqEmail
     */
    @ApiOperation("发送验证码")
    @GetMapping("/sendcode/{userQqEmail}")
    public BaseResponse<Boolean> sendCode(@PathVariable("userQqEmail") String userQqEmail) {
        if (userQqEmail == null || userQqEmail.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "qq邮箱不能为空");
        }
      return ResultUtils.success(userService.sendCode(userQqEmail));
    }

    /**
     * 管理员分页获取用户信息
     *
     * @param userQueryRequest
     * @return
     */
    @ApiOperation("管理员:获取用户信息")
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        int current = userQueryRequest.getCurrent();
        int pageSize = userQueryRequest.getPageSize();
        Page<User> page = new Page<>(current, pageSize);
        Page<User> userPage = userService.page(page, userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 管理员获取用户信息
     *
     * @param userQqEmail
     * @return
     */
    @ApiOperation("管理员通过邮箱获取用户信息")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/get")
    public BaseResponse<User> getUserByEmail(String userQqEmail) {
        if (StringUtils.isBlank(userQqEmail)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入邮箱进行查询");
        }
        User user = userService.getUserByEmail(userQqEmail);
        return ResultUtils.success(user);
    }

    /**
     * 用户修改自己信息
     *
     * @param userUpdateMyRequest
     * @param httpServletRequest
     * @return
     */
    @ApiOperation("用户修改基本信息")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    @PostMapping("/updatemy")
    public BaseResponse<Boolean> userUpdateMy(@RequestBody UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest httpServletRequest) {
        User loginuser = userService.getLoginUser(httpServletRequest);
        if (!loginuser.getUserRole().equals(UserConstant.ADMIN_ROLE)) {
            if (!loginuser.getId().equals(userUpdateMyRequest.getId())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "没有权限修改信息");
            }
        }
        return ResultUtils.success(userService.userUpdateMy(userUpdateMyRequest));
    }

    /**
     * 管理员修改用户信息
     *
     * @param updateForAdminRequest
     * @return
     */
    @ApiOperation("管理员修改基本信息")
    @PostMapping("/updateForAdmin")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateForAdmin(@RequestBody UpdateForAdminRequest updateForAdminRequest) {
        String userRole = updateForAdminRequest.getUserRole();
        int matchCount = updateForAdminRequest.getMatchCount();
        String userQqEmail = updateForAdminRequest.getUserQqEmail();
        User oldUser = userService.getUserByEmail(userQqEmail);

        if ((matchCount - oldUser.getMatchCount()) > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "修改数过大，请换个较小的数");
        }
        return ResultUtils.success(userService.updateForAdmin(matchCount, userRole, userQqEmail));
    }

    @ApiOperation("用户退出登录")
    @PostMapping("/logout")
    public BaseResponse<Boolean> logout(HttpServletRequest httpServletRequest) {
        httpServletRequest.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return ResultUtils.success(true);
    }

    @ApiOperation("举报用户")
    @PostMapping("/report/post")
    public BaseResponse<Boolean> reportUser(ReportUserRequest reportUserRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.reportUser(reportUserRequest, request));
    }

    @ApiOperation("上传图片")
    @PostMapping("/upload")
    public BaseResponse<String> upload(MultipartFile multipartFile,HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String path="avatar/"+loginUser.getId()+"/";
        return ResultUtils.success(uploadStrategyContext.executeUploadStrategy(multipartFile, path));
    }

    // todo 普通匹配和超级匹配加个elasticsearch
    @ApiOperation("基础根据标签匹配用户")
    @GetMapping("/matchuser/bytags")
    public BaseResponse<List<MatchUserVo>> matchUserByTags(@RequestParam(required = false) List<String> tagList, HttpServletRequest request) {
        return ResultUtils.success(userService.matchUserByTags(tagList, request));

    }
    @ApiOperation("修改自己的标签")
    @GetMapping("/updateMyTas")
    public BaseResponse<Boolean> updateMyTas(@RequestParam(required = false) List<String> tagList, HttpServletRequest request) {
        return ResultUtils.success(userService.updateMyTas(tagList, request));
    }

    @ApiOperation("超级根据标签匹配用户")
    @GetMapping("/super/match")
    public BaseResponse<MatchUserVo> superMatchUser(HttpServletRequest request) {
        return ResultUtils.success(userService.superMatchUser(request));
    }

    @ApiOperation("获得自己与所有用户之间的距离")
    @GetMapping("/getown/distance")
    public BaseResponse<Map<Long, String>> getOwnWithOtherDistance(HttpServletRequest request) {
        return ResultUtils.success(userService.getOwnWithOtherDistance(request));
    }

    @ApiOperation("获得距离自己1500米的用户信息")
    @PostMapping("/get/circle")
    public BaseResponse<List<LoginUserVO>> getOwnCircleDistance(HttpServletRequest request, @RequestParam("true") double distance) {
        return ResultUtils.success(userService.getOwnCircleDistance(request, distance));
    }

    @ApiOperation("用户签到")
    @GetMapping("/signin")
    public BaseResponse<Boolean> addUserSignIn(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Boolean result = userService.addUserSignIn(loginUser.getId());
        if (result) {
            return ResultUtils.success(result);
        } else {
            return ResultUtils.error(ErrorCode.OPERATION_ERROR, "签到失败");
        }
    }

    @ApiOperation("获得用户签到情况")
    @PostMapping("/getuserthisweeksign")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Map<Long, Boolean>> getUserThisWeekSign(Long userId) {
        return ResultUtils.success(userService.getUserThisWeekSign(userId));

    }

    @ApiOperation("产看用户基本信息详情页")
    @GetMapping("/getKnowUserVo/{userId}")
    public BaseResponse<KnowUserVo> getKnowUserVo(@PathVariable("userId") Long userId) {
        return ResultUtils.success(userService.getKnowUserVo(userId));

    }

    @GetMapping("/current")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(loginUser, loginUserVO);
        loginUserVO.setTagList(JSONUtil.toList(loginUser.getTags(), String.class));
        return ResultUtils.success(loginUserVO);
    }


}
