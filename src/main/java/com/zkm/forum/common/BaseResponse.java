package com.zkm.forum.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class BaseResponse<T> implements Serializable {
    private int code;
    private T data;
    private String message;

   public BaseResponse(int code,T data,String message){
       this.code=code;
       this.message=message;
       this.data=data;
    }
//    public BaseResponse(int code,T data ){
//        this(code,data,"");
//    }
    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(),null, errorCode.getMessage());
    }


}
