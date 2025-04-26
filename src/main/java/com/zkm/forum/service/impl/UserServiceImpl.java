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
import com.zkm.forum.model.dto.user.ReportUserRequest;
import com.zkm.forum.model.dto.user.UserQueryRequest;
import com.zkm.forum.model.dto.user.UserRegisterRequest;
import com.zkm.forum.model.dto.user.UserUpdateMyRequest;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.vo.user.KnowUserVo;
import com.zkm.forum.model.vo.user.LoginUserVO;
import com.zkm.forum.model.vo.user.MatchUserVo;
import com.zkm.forum.service.InvitationService;
import com.zkm.forum.service.UserService;
import com.zkm.forum.utils.AlgorithmUtils;
import com.zkm.forum.utils.MailUtils;
import kotlin.Pair;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zkm.forum.constant.LocalCacheConstant.USERID_USERNAME;
import static com.zkm.forum.constant.RedisConstant.*;

/**
 * @author 张凯铭
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-03-17 17:34:42
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private Cache<String, String> LOCAL_CACHE;
    //加上这个会报循环依赖错误
//    @Resource
//    UserService userService;
    private HashMap<String, Long> codeWithTime = new HashMap<>();
    private static final String SALT = "Masami";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private InvitationService invitationService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Long userRegister(UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册信息有误，请稍后重试");
        }
        String userPassword = userRegisterRequest.getUserPassword();
        String userName = userRegisterRequest.getUserName();
        String userQqEmail = userRegisterRequest.getUserQqEmail();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userCode = userRegisterRequest.getUserCode();
        String gender = userRegisterRequest.getGender();
        Long inviterId = userRegisterRequest.getInviterId();
        Long inviteeId = userRegisterRequest.getInviteeId();
        

        if (userName.length() > 12 || userName.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请填写用户昵称且长度不能超过12");
        }
        if (gender == null || gender.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请填写性别");
        }

        if (userPassword == null || userPassword.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码不能为空");
        }
        if (userPassword.length() < 6 || userPassword.length() > 12) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码位数必须为6到12位");
        }
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
        if(inviteeId!=null&&inviterId!=null){
            Boolean result = invitationService.processInvitation(inviteeId, inviterId);
            if(!result){
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"邀请注册失败,可以自己注册,之后向管理员反应");
            }
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
        LOCAL_CACHE.put(USERID_USERNAME + user.getId(), user.getUserName());
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
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
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
        String userName = userUpdateMyRequest.getUserName();
        String gender = userUpdateMyRequest.getGender();
        User user = new User();
        user.setId(userUpdateMyRequest.getId());
        user.setUserName(userName);
        user.setGender(gender);
        user.setIntroduction(userUpdateMyRequest.getIntroduction());
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

    @Override
    public Boolean reportUser(ReportUserRequest reportUserRequest, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        Long userId = loginUser.getId();
        boolean result;
        synchronized (String.valueOf(userId).intern()) {
            User user = this.getById(reportUserRequest.getUserId());
            if (user.getIsReported() == 1) {
                return true;
            } else {
                user.setIsReported(1);
                user.setReportResults(reportUserRequest.getReportedResults());
                user.setReportUserId(reportUserRequest.getReportUserId());
                result = this.updateById(user);
            }
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统繁忙，请稍后再试");
            }
            return result;
        }
    }

    // todo 可以引入异步rabbitmq，避免用户体验感较差，引入热缓存技术,建表用户匹配到的人表。
    @Override
    public List<MatchUserVo> matchUserByTags(List<String> tags, HttpServletRequest request) {
        if (tags.size() > 3) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "最多选3个标签");
        }
        User loginUser = this.getLoginUser(request);
        if (loginUser.getMatchCount() == 0) {
            throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "匹配次数已用完");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id", "tags");
        userQueryWrapper.last("LIMIT 500");
        List<User> userList = this.list(userQueryWrapper);
        List<MatchUserVo> matchUserVos = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            // todo  可以使用set类型进行存储，这样可以过滤重复的标签，但是要注意，使用不同的序列化工具可能会报错
            List<String> tagList = JSONUtil.toList(userList.get(i).getTags(), String.class);
            for (String tag : tags) {
                if (!tagList.contains(tag)) {
                    break;
                } else {
                    if (matchUserVos.size() <= 1) {
                        matchUserVos.add(getMatchUserVo(userList.get(i)));
                    } else {
                        // todo 可以换成异步避免等太久(先返回再减？)，弹到其他页面先减再返回，避免并发错误
                        synchronized (String.valueOf(loginUser.getId()).intern()) {
                            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                            userUpdateWrapper.eq("id", loginUser.getId());
                            userUpdateWrapper.setSql(true, "matchCount=matchCount-1");
                            boolean result = this.update(userUpdateWrapper);
                            // todo 如果没减成功可通过mq进行再减，添加用户匹配到的人
                        }
                        List<Long> matchUserIdList = matchUserVos.stream().map(MatchUserVo::getId).toList();
                        List<MatchUserVo> matchUserVoList = this.listByIds(matchUserIdList).stream().map(this::getMatchUserVo).toList();
                        for(MatchUserVo matchUserVo:matchUserVoList){
                            matchUserVo.setTags(tags);
                        }
                        return matchUserVoList;
                    }

                }
            }

        }
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "没有符合条件的用户");

//        return List.of();
    }

    @Override
    public MatchUserVo superMatchUser(HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        if (loginUser.getSuperMatchCount() < 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "超级匹配");
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.select("id", "tags");
        List<User> userList = this.list(userQueryWrapper);
        Map<Long, List<User>> map = userList.stream().collect(Collectors.groupingBy(User::getId));
        List<Pair<User, Long>> pairs = new ArrayList<>();
        for (User user : userList) {
            int i = AlgorithmUtils.minDistance(user.getTags(), loginUser.getTags());
            pairs.add(new Pair<User, Long>(user, (long) i));
        }
        // todo 如果 list 很大，排序操作可能会消耗较多时间。在这种情况下，可以考虑优化数据结构或使用并行流（list.parallelStream()）来提高性能。
        Pair<User, Long> pair = pairs.stream().sorted((a, b) -> (int) (a.getSecond() - b.getSecond())).limit(1).toList().get(0);
        synchronized (String.valueOf(loginUser.getId()).intern()) {
            UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
            userUpdateWrapper.eq("id", loginUser.getId());
            userUpdateWrapper.setSql(true, "superMatchCount=superMatchCount-1");
            boolean result = this.update(userUpdateWrapper);
            // todo 如果没减成功可通过mq进行再减，添加用户匹配到的人
        }
        return this.getMatchUserVo(this.getById(pair.getFirst().getId()));


    }

    @Override
    public Map<Long, String> getOwnWithOtherDistance(HttpServletRequest request) {
        HashMap<Long, String> map = new HashMap<>();
        List<User> userList = this.list();
        User loginUser = this.getLoginUser(request);
        for (User user : userList) {
            Distance distance = stringRedisTemplate.opsForGeo().distance(USER_GEO, String.valueOf(loginUser.getId()), String.valueOf(user.getId()), RedisGeoCommands.DistanceUnit.METERS);
            assert distance != null;
            map.put(user.getId(), distance.getValue() + distance.getUnit());
        }
        return map;
    }

    @Override
    public List<LoginUserVO> getOwnCircleDistance(HttpServletRequest request, double distance) {
        User loginUser = this.getLoginUser(request);
        List<Long> userIdList = new ArrayList<>();
        Distance geoRadius = new Distance(distance, RedisGeoCommands.DistanceUnit.METERS);
        Circle circle = new Circle(new Point(loginUser.getLongitude(), loginUser.getDimension()), geoRadius);
        RedisGeoCommands.GeoRadiusCommandArgs geoRadiusCommandArgs = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs().includeCoordinates();
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().radius(USER_GEO, circle, geoRadiusCommandArgs);
        assert results != null;
        for (GeoResult<RedisGeoCommands.GeoLocation<String>> result : results) {
            if (!result.getContent().getName().equals("1")) {
                userIdList.add(Long.valueOf(result.getContent().getName()));
            }
        }

        ;

//        return this.listByIds(userIdList).stream().map(user -> objToVo(user)).toList();
        return this.listByIds(userIdList).stream().map(UserServiceImpl::objToVo).toList();
    }

    @Override
    public Boolean addUserSignIn(Long userId) {
        LocalDate date = LocalDate.now();
        // 获取今天是这一周的第几天（1=星期一，7=星期日）
        int dayOfWeek = date.getDayOfWeek().getValue();
        // 获取今天是这一年的第几周（按照ISO标准，周一为一周的第一天）
        int weekOfYear = date.get(WeekFields.ISO.weekOfYear());
        String key = getRedisUserSignin(date.getYear(), weekOfYear, userId);
        RBitSet bitSet = redissonClient.getBitSet(key);
        if (!bitSet.get(dayOfWeek)) {
           return bitSet.set(dayOfWeek, true);
        }else{
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"请勿重复签到");
        }
    }

    @Override
    public Map<Long, Boolean> getUserThisWeekSign(Long userId) {
        LocalDate date = LocalDate.now();
        int weekOfYear = date.get(WeekFields.ISO.weekOfYear());
        String key = String.format("%s:%s:%s:%s", USER_SIGNIN, date.getYear(), weekOfYear, userId);
        RBitSet bitSet = redissonClient.getBitSet(key);
        Map<Long, Boolean> userSign = new HashMap<>();
        for(int i=0;i<=7;i++){
            userSign.put((long) i,bitSet.get((long) i));
        }
        return userSign;
    }

    @Override
    public KnowUserVo getKnowUserVo(Long userId) {
        User user = this.getById(userId);
        KnowUserVo knowUserVo = new KnowUserVo();
        knowUserVo.setUserName(user.getUserName());
        knowUserVo.setUserRole(user.getUserRole());
        knowUserVo.setCreateTime(user.getCreateTime());
        knowUserVo.setUserAvatar(user.getUserAvatar());
        knowUserVo.setGender(user.getGender());
        knowUserVo.setFollowerCount(user.getFollowerCount());
        return knowUserVo;
    }

    @Override
    public Boolean updateMyTas(List<String> tags,HttpServletRequest request) {
        if(tags.isEmpty()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请填写标签");
        }
        User loginUser = this.getLoginUser(request);
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
        userUpdateWrapper.eq("id",loginUser.getId());
        userUpdateWrapper.set("tags",JSONUtil.toJsonStr(tags));
        boolean result = this.update(userUpdateWrapper);
        if(!result){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"修改失败");
        }
        return result;
    }

    private MatchUserVo getMatchUserVo(User user) {
        MatchUserVo matchUserVo = new MatchUserVo();
        BeanUtils.copyProperties(user, matchUserVo);
        matchUserVo.setTags(JSONUtil.toList(user.getTags(), String.class));
        return matchUserVo;
    }


}




