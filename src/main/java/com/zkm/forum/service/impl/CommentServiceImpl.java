package com.zkm.forum.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.platform.hotkey.client.callback.JdHotKeyStore;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.CommentMapper;
import com.zkm.forum.model.dto.comment.ListCommentRequest;
import com.zkm.forum.model.dto.comment.SaveCommentRequest;
import com.zkm.forum.model.dto.email.EmailRequest;
import com.zkm.forum.model.entity.Comment;
import com.zkm.forum.model.entity.Post;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.model.enums.UserRoleEnum;
import com.zkm.forum.model.vo.comment.CommentVo;
import com.zkm.forum.service.CommentService;
import com.zkm.forum.service.PostService;
import com.zkm.forum.service.UserService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zkm.forum.constant.JdHotKeyConstant.CHILD_COMMENT;
import static com.zkm.forum.constant.JdHotKeyConstant.PARENT_COMMENT;
import static com.zkm.forum.constant.MentionConstant.COMMENT_MENTION;
import static com.zkm.forum.constant.RabbitMqConstant.EMAIL_EXCHANGE;

/**
 * @author 张凯铭
 * @description 针对表【comment(评论表)】的数据库操作Service实现
 * @createDate 2025-04-12 17:31:09
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment>
        implements CommentService {
    @Resource
    private PostService postService;
    @Resource
    private UserService userService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    //下方注入会导致循环依赖
//    @Resource
//    private CommentService commentService;


    @Override
    public Boolean saveComment(SaveCommentRequest saveCommentRequest, HttpServletRequest request) {
        Long postId = saveCommentRequest.getPostId();
        String commentContent = saveCommentRequest.getCommentContent();
        Long replyUserId = saveCommentRequest.getReplyUserId();
        Long parentId = saveCommentRequest.getParentId();
        Integer isReview = saveCommentRequest.getIsReview();
        Long userId = saveCommentRequest.getUserId();
        Long topCommentId = saveCommentRequest.getTopCommentId();
        Post post = postService.getById(postId);
        User loginUser = userService.getLoginUser(request);

        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "找不到评论的帖子");
        }
        Comment comment = new Comment();
        if(topCommentId!=null){
            comment.setTopCommentId(topCommentId);
        }else{
            comment.setTopCommentId(parentId);
        }
        comment.setCommentContent(commentContent);
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setReplyUserId(replyUserId);
        comment.setUserId(userId);
        comment.setIsReview(isReview);
        boolean result = this.save(comment);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发布失败，请稍后再试");
        }
        CompletableFuture.runAsync(() -> {
            notice(loginUser, comment);
            boolean update = postService.update().eq("id", postId).setSql("commentNum = commentNum + 1").update();
            if(!update){
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "评论数增加失败");
            }
        });
        return result;
    }

    @Override
    public Boolean deleteComment(Long commentId, HttpServletRequest request) {
        if (commentId == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "选择删除的文章");
        }
        Comment comment = this.getById(commentId);
        User loginUser = userService.getLoginUser(request);
        if (!Objects.equals(loginUser.getUserRole(), UserRoleEnum.ADMIN.getValue())) {
            if (!Objects.equals(loginUser.getId(), comment.getUserId())) {
                throw new BusinessException(ErrorCode.NOT_AUTH_ERROR, "无权限");
            }
        }
        boolean result = this.update().eq("id", commentId).set("commentContent", "用户评论已删除").update();
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除失败请稍后再试");
        }
        return result;
    }

    @Override
    public Page<CommentVo> listParentComment(ListCommentRequest request) {
        int current = request.getCurrent();
        int pageSize = request.getPageSize();
        Long postId = request.getPostId();
        String key = PARENT_COMMENT + postId+current;
        if (JdHotKeyStore.isHotKey(key)) {
            Object object = JdHotKeyStore.get(key);
            if (object != null) {
                // 修改：将 Object 转换为 Page<CommentVo> 类型
                Page<CommentVo> cachedPage = (Page<CommentVo>) object;
//                return new Page<>(current, pageSize, cachedPage.getTotal()).setRecords(cachedPage.getRecords());
                return cachedPage;
            }
        }
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq( "isReview", 1);
        commentQueryWrapper.eq("postId", postId).isNull("parentId");
        commentQueryWrapper.orderBy(true, true, "createTime");
        Page<Comment> page = this.page(new Page<>(current, pageSize), commentQueryWrapper);

        List<Comment> commentList = page.getRecords();
        if (commentList == null) {
            return new Page<>();
        }
        List<Long> authorIdList = commentList.stream().map(comment -> comment.getUserId()).toList();
        Map<Long, User> userMap = userService.listByIds(authorIdList).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<CommentVo> commentVoList = commentList.stream().map(comment -> convertToVo(comment, userMap)).toList();

        Page<CommentVo> commentVoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        commentVoPage.setRecords(commentVoList);
        JdHotKeyStore.smartSet(key, commentVoPage);
        return commentVoPage;
    }

    @Override
    public Page<CommentVo> listChildrenComment(ListCommentRequest request) {
        int current = request.getCurrent();
        Long parentId = request.getParentId();
        String key=CHILD_COMMENT+parentId+current;
        if(JdHotKeyStore.isHotKey(key)){
            Object object = JdHotKeyStore.get(key);
            if(object!=null){
                Page<CommentVo> cachedPage = (Page<CommentVo>) object;
                return cachedPage;
            }
        }
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq( "isReview", 1);
        commentQueryWrapper.eq(!Objects.isNull(parentId), "topCommentId", parentId);
        commentQueryWrapper.orderBy(true, true, "createTime");
        Page<Comment> page = this.page(new Page<>(current, 3), commentQueryWrapper);
        List<Comment> comments = page.getRecords();
        if (comments == null) {
            return new Page<>();
        }
        List<Long> authorList = comments.stream().map(Comment::getUserId).toList();
        Map<Long, User> userMap = userService.listByIds(authorList).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<CommentVo> commentVoList = comments.stream().map(comment -> convertToVo(comment, userMap)).toList();
        Page<CommentVo> pageCommentVo = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        pageCommentVo.setRecords(commentVoList);
        JdHotKeyStore.smartSet(key, pageCommentVo);
        return pageCommentVo;

    }


    private CommentVo convertToVo(Comment comment, Map<Long, User> userMap) {
        CommentVo vo = new CommentVo();
        BeanUtils.copyProperties(comment, vo);

        // 填充用户信息
        User user = userMap.get(comment.getUserId());
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setUserAvatar(user.getUserAvatar());
            vo.setGender(user.getGender());
        }

        // 填充回复用户信息（如果有）
        if (comment.getReplyUserId() != null) {
            User replyUser = userMap.get(comment.getReplyUserId());
            if (replyUser != null) {
                vo.setReplyUserName(replyUser.getUserName());
                vo.setReplyUserAvatar(replyUser.getUserAvatar());
            }
        }

        return vo;
    }

    private void notice(User loginUser, Comment comment) {

        Long postId = comment.getPostId();

        Long replyUserId = comment.getReplyUserId();
        /**
         * 父评论id
         */
        Long parentId = comment.getParentId();
        // 情况1：用户自己回复自己，不通知
        if (Objects.equals(replyUserId, loginUser.getId())) {
            return;
        }


//        {
//            "commentContent": "什么时候见面",
//                "isReview": 0,
//                "parentId": null,
//                "postId": 1,
//                "replyUserId": null,
//                "userId": 1902558628941598722
//        }
        //回复的是父评论作者，给父评论作者发通知,否则就给文章作者发通知
        // todo 增加连接跳转评论区
        if ((replyUserId == null && parentId == null) || Objects.equals(this.getById(parentId).getUserId(), replyUserId)) {
            Long emailReplyUserId = null;
            if (Objects.nonNull(parentId)) {
                emailReplyUserId = replyUserId;
            } else {
                Post replyPost = postService.getById(postId);
                emailReplyUserId = replyPost.getUserId();
            }
            User replyUser = userService.getById(emailReplyUserId);
            HashMap<String, Object> map = new HashMap<>();
            map.put("content", "用户" + "【" + loginUser.getUserName() + "】" + "评论了你,点击此处跳转");
            EmailRequest emailRequest = EmailRequest.builder()
                    .commentMap(map)
                    .email(replyUser.getUserQqEmail())
                    .template("common.html")
                    .subject(COMMENT_MENTION).build();
            rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, "*", new Message(JSON.toJSONBytes(emailRequest), new MessageProperties()));
        } else {
            // 情况3：回复的用户不是父评论作者且不是自己，发送通知
            //构造邮件内容
            Map<String, Object> map = new HashMap<>();
            // todo 增加连接跳转评论区
            map.put("content", "你的好友" + "【" + loginUser.getUserName() + "】" + "在评论区@了你");
            User replyUser = userService.getById(replyUserId);
            //构造邮件dto
            EmailRequest emailRequest = EmailRequest.builder().email(replyUser.getUserQqEmail())
                    .commentMap(map)
                    .template("common.html")
                    .subject(COMMENT_MENTION)
                    .build();
            rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, "*", new Message(JSON.toJSONBytes(emailRequest), new MessageProperties()));
        }
    }

}




