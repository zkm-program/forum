package com.zkm.forum.model.enums;

import lombok.Getter;

public enum SearchModeEnum {
    MYSQL("mysql","postMysqlSearchImpl"),
    ELASTICSEARCH("elasticsearch","postElasticSearchImpl");
    @Getter
    private String mode;
    @Getter
    private String message;

    SearchModeEnum(String mode,String message) {
        this.mode = mode;
        this.message=message;
    }
    public static String getMessageByMode(String mode){
        for (SearchModeEnum searchModeEnum : SearchModeEnum.values()) {
            if (searchModeEnum.getMode().equals(mode)){
                return searchModeEnum.getMessage();
            }
        }
        return null;
    }

}
