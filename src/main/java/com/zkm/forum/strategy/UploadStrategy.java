package com.zkm.forum.strategy;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
@Component
public interface UploadStrategy {

    String uploadFile(MultipartFile multipartFile,String path);
    String uploadFile(String fileName, String path, InputStream inputStream);
}
