package com.zkm.forum.model.enums;


public enum IsTagParentEnum {

    YES("yse",1),
    NO("no",0);
    private String judge;
    private int code;
    IsTagParentEnum(String judge, int code){
        this.judge=judge;
        this.code=code;
    }

    public int getCode(){
        return code;
    }
}
