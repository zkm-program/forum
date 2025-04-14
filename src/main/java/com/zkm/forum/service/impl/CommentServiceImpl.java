package com.zkm.forum.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

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
//    @Resource
//    private CommentService commentService;

    @Override
    public Boolean saveComment(SaveCommentRequest saveCommentRequest, HttpServletRequest request) {
        Long postId = saveCommentRequest.getPostId();
        String commentContent = saveCommentRequest.getCommentContent();
        Long replyUserId = saveCommentRequest.getReplyUserId();
        Long parentId = saveCommentRequest.getParentId();
        Integer isReview = saveCommentRequest.getIsReview();
        Post post = postService.getById(postId);
        User loginUser = userService.getLoginUser(request);
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "找不到评论的帖子");
        }
        Comment comment = new Comment();
        comment.setCommentContent(commentContent);
        comment.setPostId(postId);
        comment.setParentId(parentId);
        comment.setReplyUserId(replyUserId);
        comment.setIsReview(isReview);
        boolean result = this.save(comment);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "发布失败，请稍后再试");
        }
        CompletableFuture.runAsync(() -> notice(loginUser, comment));
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
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("postId", postId).isNull("parentId");
        commentQueryWrapper.orderBy(true,true,"createTime");
        Page<Comment> page = this.page(new Page<>(current, pageSize), commentQueryWrapper);

        List<Comment> commentList = page.getRecords();
        if (commentList == null) {
            return new Page<>();
        }
        List<Long> authorIdList = commentList.stream().map(comment -> comment.getUserId()).toList();
        Map<Long, User> userMap = userService.listByIds(authorIdList).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<CommentVo> commentVoList =  commentList.stream().map(comment -> convertToVo(comment,userMap)).toList();

        Page<CommentVo> commentVoPage = new Page<>(page.getCurrent(),page.getSize(),page.getTotal());
        commentVoPage.setRecords(commentVoList);
        return commentVoPage;
    }

    @Override
    public Page<CommentVo> listChildrenComment(ListCommentRequest request) {
        Long commentId = request.getCommentId();
        Long parentId = request.getParentId();
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq(!Objects.isNull(parentId), "parentId", commentId);
        commentQueryWrapper.orderBy(true,true,"createTime");
        Page<Comment> page = this.page(new Page<>(request.getCurrent(), request.getPageSize()), commentQueryWrapper);
        List<Comment> comments = page.getRecords();
        if (comments == null) {
            return new Page<>();
        }
        List<Long> authorList = comments.stream().map(Comment::getUserId).toList();
        Map<Long, User> userMap = userService.listByIds(authorList).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        List<CommentVo> commentVoList = comments.stream().map(comment -> convertToVo(comment, userMap)).toList();
         Page<CommentVo> pageCommentVo= new Page<>(page.getCurrent(),page.getSize(),page.getTotal());
         pageCommentVo.setRecords(commentVoList);
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
        Long parentId = comment.getParentId();
        // 情况1：用户自己回复自己，不通知
        if (Objects.equals(replyUserId, loginUser.getId())) {
            return;
        }
        // 情况3：回复的用户不是父评论作者且不是自己，发送通知
        if (!Objects.equals(replyUserId, parentId) && !Objects.equals(replyUserId, loginUser.getId())) {
            //构造邮件内容
            Map<String, Object> map = new HashMap<>();
            // todo 增加连接跳转评论区
            map.put("content", "你的好友" + loginUser.getUserName() + "在评论区@了你");
            User replyUser = userService.getById(replyUserId);
            //构造邮件dto
            EmailRequest emailRequest = EmailRequest.builder().email(replyUser.getUserQqEmail())
                    .commentMap(map)
                    .template("common.html")
                    .subject(COMMENT_MENTION)
                    .build();
            rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, "*", new Message(JSON.toJSONBytes(emailRequest), new MessageProperties()));
        }
        //回复的是父评论作者，给父评论作者发通知,否则就给文章作者发通知
        Long emailReplyUserId = null;
        if (Objects.nonNull(parentId)) {
            emailReplyUserId = parentId;
        } else {
            Post replyPost = postService.getById(postId);
            emailReplyUserId = replyPost.getUserId();
        }
        User replyUser = userService.getById(emailReplyUserId);
        HashMap<String, Object> map = new HashMap<>();
        // todo 增加连接跳转评论区
        map.put("content", loginUser.getUserName() + "评论了你,点击此处跳转");
        EmailRequest emailRequest = EmailRequest.builder()
                .commentMap(map)
                .email(replyUser.getUserQqEmail())
                .template("common.html")
                .subject(COMMENT_MENTION).build();
        rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, "*", new Message(JSON.toJSONBytes(emailRequest), new MessageProperties()));

    }

}




