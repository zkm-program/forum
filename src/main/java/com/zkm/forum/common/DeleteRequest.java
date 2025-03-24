package com.zkm.forum.common;

import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {
    Long id;

    private static final long serialVersionUID = 1L;
}
