package com.zkm.forum.strategy.impl;

import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.exception.BusinessException;
import com.zkm.forum.strategy.UploadStrategy;
import com.zkm.forum.utils.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;

public abstract class AbstractUploadStrategyImpl implements UploadStrategy {
    @Resource
    private FileUtils fileUtils;

    @Override
    public String uploadFile(MultipartFile multipartFile, String path) {
        try {
            String md5 = fileUtils.getMd5(multipartFile.getInputStream());
            String exName = fileUtils.getExName(multipartFile.getOriginalFilename());
            String fileName = md5 + exName;
            if (!exists(path + fileName)) {
                upload(path, fileName, multipartFile.getInputStream());
            }
            return getFileAccessUrl(path + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传失败，请稍后再试");
        }
    }

    @Override
    public String uploadFile(String fileName, String path, InputStream inputStream) {
        try {
            upload(path,fileName,inputStream);
            return getFileAccessUrl(path+fileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败，请稍后再试");
        }

    }


    public abstract Boolean exists(String filePath);

    public abstract void upload(String path, String fileName, InputStream inputStream);

    public abstract String getFileAccessUrl(String filePath);
}
