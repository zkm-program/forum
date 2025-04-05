package com.zkm.forum.strategy.context;

import com.zkm.forum.model.enums.UploadModeEnum;
import com.zkm.forum.strategy.UploadStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Map;
@Component
public class UploadStrategyContext {
    @Resource
    private Map<String, UploadStrategy> map;
    @Value("${upload.mode}")
    private String uploadMode;
//    @Resource
//    private UploadModeEnum uploadModeEnum;

    public String executeUploadStrategy(MultipartFile multipartFile, String path) {
        return map.get(UploadModeEnum.getStrategy(uploadMode)).uploadFile(multipartFile, path);
    }

    public String executeUploadStrategy(String fileName, String path, InputStream inputStream){
        return map.get(UploadModeEnum.getStrategy(uploadMode)).uploadFile(fileName,path,inputStream);
    }

}
