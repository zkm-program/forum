package com.zkm.forum.model.dto.user;

import com.zkm.forum.model.entity.User;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadCosWanXiangRequest {
    private MultipartFile multipartFile;
    private User loginUser;
    private String path;
}
