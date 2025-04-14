package com.zkm.forum.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.mapper.InvitationMapper;
import com.zkm.forum.model.entity.Invitation;
import com.zkm.forum.model.entity.User;
import com.zkm.forum.service.InvitationService;
import com.zkm.forum.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
* @author 张凯铭
* @description 针对表【invitation(用户邀请关系表)】的数据库操作Service实现
* @createDate 2025-04-14 16:41:28
*/
@Service
@Lazy
public class InvitationServiceImpl extends ServiceImpl<InvitationMapper, Invitation>
    implements InvitationService{
//    @Resource
//    UserService userService;
    @Override
    public String getInviteLink(User loginUser) {
        return "https://localhost:8101/register?inviterId=" + loginUser.getId();
    }

    @Override
    public Boolean processInvitation(Long inviteeId, Long inviterId) {
        Invitation invitation = new Invitation();
        invitation.setInviterId(inviterId);
        invitation.setInviteeId(inviteeId);
        boolean result = this.save(invitation);
        if(!result){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"邀请关系添加失败");
        }
        return result;
    }
}




