package com.zkm.forum.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public enum UploadModeEnum {
    OSS("oss","ossUploadStrategyImpl"),
    COS("cos","cosUploadStrategyImpl"),
    MINIO("minio","minioUploadStrategyImpl");
    private String mode;
    private String strategy;
    UploadModeEnum(String mode,String strategy){
        this.mode=mode;
        this.strategy=strategy;
    }
    private String getStrategy(String mode){
        for (UploadModeEnum uploadModeEnum:UploadModeEnum.values()){
            if(uploadModeEnum.getMode().equals(mode)){
                return uploadModeEnum.getStrategy();
            }
        }
        return null;

    }


}
