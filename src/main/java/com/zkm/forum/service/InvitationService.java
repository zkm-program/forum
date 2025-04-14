package com.zkm.forum.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zkm.forum.model.entity.Invitation;
import com.zkm.forum.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 张凯铭
 * @description 针对表【invitation(用户邀请关系表)】的数据库操作Service
 * @createDate 2025-04-14 16:41:28
 */
public interface InvitationService extends IService<Invitation> {
    String getInviteLink(User loginUser);

    /**
     *
     * @param inviteeId 被邀请人id
     * @param inviterId 邀请人id
     * @return
     */
    Boolean processInvitation(Long inviteeId, Long inviterId);

}
