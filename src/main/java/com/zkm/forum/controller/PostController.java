package com.zkm.forum.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zkm.forum.annotation.AuthCheck;
import com.zkm.forum.common.BaseResponse;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.common.ResultUtils;
import com.zkm.forum.config.properties.NacosProperties;
import com.zkm.forum.constant.UserConstant;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.model.dto.post.*;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.post.PostSearchVo;
import com.zkm.forum.model.vo.post.PostVo;
import com.zkm.forum.service.PostService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.CounterUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
//import org.springframework.data.redis.core.RedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.zkm.forum.constant.RedisConstant.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Api(tags = "帖子模块")
@RestController
@RequestMapping("/post")
public class PostController {
    private static final Logger log = LoggerFactory.getLogger(PostController.class);
    @Resource
    PostService postService;
    @Resource
    StringRedisTemplate stringredisTemplate;
    @Resource
    private CounterUtils counterUtils;
    @Resource
    private UserService userService;
    @Resource
    private NacosProperties nacosProperties;

    @ApiOperation("发布或修改帖子")
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> addPost(@RequestBody AddPostRequest addPostRequest, HttpServletRequest httpServletRequest) {
        if (ObjectUtils.isEmpty(addPostRequest)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "信息填写完整再发布");
        }
        return ResultUtils.success(postService.addPost(addPostRequest, httpServletRequest));
    }

    @ApiOperation("逻辑删除帖子")
    @PostMapping("/updatedelete/mypost")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Boolean> updatePostDeletForMy(UpdatePostDeleteForMy updatePostDeleteForMy, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(postService.updatePostDeletForMy(updatePostDeleteForMy, httpServletRequest));
    }

    @ApiOperation("搜索帖子")
    @PostMapping("/searchPost")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<PostSearchVo>> searchPost(@RequestBody PostSearchRequest postSearchRequest, HttpServletRequest request) {
//        crawlerDetect(request);
        return ResultUtils.success(postService.searchPost(postSearchRequest));
    }

    @ApiOperation("ES搜索帖子")
    @PostMapping("/searchEsPost")
    public BaseResponse<Page<PostSearchVo>> searchEsPost(@RequestBody PostQueryRequest postQueryRequest, HttpServletRequest request) {
//        crawlerDetect(request);
        return ResultUtils.success(postService.searchEsPost(postQueryRequest));
    }

    @ApiOperation("查看帖子详情")
    @GetMapping("/{postId}")
    public BaseResponse<PostVo> getPostById(@PathVariable("postId") Long postId, HttpServletRequest httpServletRequest) {
        return ResultUtils.success(postService.getPostById(postId, httpServletRequest));
    }


    /**
     * 首页的公共推荐(登录看到的)
     *
     * @return
     */
    @ApiOperation("热缓存推荐登录")
    @GetMapping("/public/login/recommend")
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<List<PostVo>> getLoginPublicRecommend() {
        return ResultUtils.success(Objects.requireNonNull(stringredisTemplate
                        .opsForList().range(PRE_CACHE_POST_LOGIN, 0, 14))
                .stream().map(str -> JSONUtil.toBean(str, PostVo.class)).toList());
    }

    //    @SentinelResource(value = "getLogoutRecommend",
//            blockHandler = "handleBlockException",
//            fallback = "handleFallback")
    @SentinelResource(value = "getLogoutRecommend")
    @ApiOperation("热缓存推荐未登录")
    @GetMapping("/logout/recommend")
    // todo redis缓存时间要设置一下
    public BaseResponse<List<PostVo>> getLogoutRecommend() {
        return ResultUtils.success(Objects.requireNonNull(stringredisTemplate
                        .opsForList().range(PRE_CACHE_POST_LOGOUT, 0, 14))
                .stream().map(str -> JSONUtil.toBean(str, PostVo.class)).toList());
    }

    /**
     * listQuestionBankVOByPage 流控操作（此处为了方便演示，写在同一个类中）
     * 限流：提示“系统压力过大，请耐心等待”
     * 熔断：执行降级操作
     * BlockException处理限流异常和熔断后降级的异常
     */
//    public BaseResponse<List<PostVo>> handleBlockException(BlockException ex) {
//        // 降级操作，if进行判断如果是降级操作，我就执行handleFallback方法，
//        // 因为实际情况下不管是限流还是降级都会走handleBlockException方法
//        if (ex instanceof DegradeException) {
//            return handleFallback(ex);
//        }
//        System.out.println("限流操作");
//        // 限流操作
//        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统压力过大，请耐心等待");
//    }
//
//    /**
//     * listQuestionBankVOByPage 降级操作：直接返回本地数据（此处为了方便演示，写在同一个类中）
//     * 处理业务异常
//     */
//    public BaseResponse<List<PostVo>> handleFallback(Throwable ex) {
//        System.out.println("熔断操作");
//        // 可以返回本地数据或空数据
//        return ResultUtils.success(null);
//    }

    /**
     * 每个用户看文章下滑会触发私人推荐文章
     *
     * @param userId
     * @param getPrivateRecommendRequest
     * @return
     */
    @ApiOperation("热缓存私人推荐文章")
    @PostMapping("/private/recommend/{userId}")
    public BaseResponse<List<PostVo>> getPrivateRecommend(@PathVariable("userId") Long userId, @RequestBody GetPrivateRecommendRequest getPrivateRecommendRequest) {

        return ResultUtils.success(postService.getPrivateRecommend(userId, getPrivateRecommendRequest));
    }

    //    @SentinelResource(value = "listPostForAdmin",
//            blockHandler = "handleBlockException",
//            fallback = "handleFallback")
    @SentinelResource(value = "listPostForAdmin")
    @ApiOperation("管理员:发布文章列表")
    @PostMapping("/list/post")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    BaseResponse<Page<Post>> listPostForAdmin(@RequestBody PostQueryRequest postQueryRequest) {
        return ResultUtils.success(postService.listPostForAdmin(postQueryRequest));
    }
//    public BaseResponse<Page<Post>> handleBlockException(@RequestBody PostQueryRequest postQueryRequest,BlockException ex) {
//        // 降级操作，if进行判断如果是降级操作，我就执行handleFallback方法，
//        // 因为实际情况下不管是限流还是降级都会走handleBlockException方法
//        if (ex instanceof DegradeException) {
//            return handleFallback(postQueryRequest,ex);
//        }
//        System.out.println("限流操作");
//        // 限流操作
//        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统压力过大，请耐心等待");
//    }
//    public BaseResponse<Page<Post>> handleFallback(@RequestBody PostQueryRequest postQueryRequest,Throwable ex) {
//        System.out.println("熔断操作");
//        // 可以返回本地数据或空数据
//        return ResultUtils.success(null);
//    }

    @ApiOperation("举报帖子")
    @PostMapping("/report/post")
    public BaseResponse<Boolean> reportPost(@RequestBody ReportPostRequest reportPostRequest, HttpServletRequest request) {
        return ResultUtils.success(postService.reportPost(reportPostRequest, request));
    }

    /**
     * 检测爬虫
     *
     * @param request
     */
    private void crawlerDetect(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long loginUserId = loginUser.getId();
        // 调用多少次时告警
        final int WARN_COUNT = 10;
        // 超过多少次封号
        final int BAN_COUNT = 1;
        // 拼接访问 key
        String key = String.format("user:access:%s", loginUserId);
        // 一分钟内访问次数，180 秒过期
        long count = counterUtils.incrAndGetCounter(key, 1, TimeUnit.MINUTES, 180);
        // 是否封号
        if (count > BAN_COUNT) {
            // 强制退出登录
            request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
            // 封号
            User updateUser = new User();
            updateUser.setId(loginUserId);
            updateUser.setUserRole(UserConstant.BAN_ROLE);
            userService.updateById(updateUser);
            log.error("用户 {} 访问太频繁，已被封号", loginUserId);
            throw new BusinessException(ErrorCode.Many_Times, "访问太频繁，已被封号");
        }
        // 是否告警
        if (count == WARN_COUNT) {
            // 可以改为向管理员发送邮件通知
            throw new BusinessException(110, "警告访问太频繁");
        }
    }

    /**
     * 添加IP到黑名单
     *
     * @param ip 要添加的IP地址
     */
    public void addIpToBlacklist(String ip) throws NacosException {
        NamingService namingService = NamingFactory.createNamingService(nacosProperties.getServerAddr());

        // 构造黑名单实例
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(0); // 黑名单不需要端口
        instance.setEnabled(false); // 禁用表示加入黑名单

        // 注册实例到黑名单服务
        namingService.registerInstance("nacos.ips.blacklist", "DEFAULT_GROUP", instance);
    }

}
