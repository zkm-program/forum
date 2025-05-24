package com.zkm.forum.utils;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.zkm.forum.config.properties.CosConfigProperties;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片上传腾讯云cos，加上了压缩图和缩略图的处理
 */
@Component
public class CosUploadUtils {
    @Resource
    private CosConfigProperties cosConfigProperties;
    @Resource
    private COSClient cosClient;

    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfigProperties.getBucketName(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 1. 图片压缩（转成 webp 格式），从一串图片地址【https:555/taobao/shupian.png】中拿到图片的名字【不带后缀shupian】并且给图片加上后缀【.webp】，
        // 对于这种换文件名后缀的压缩，采用的是上传文件时进行压缩，并且原图和换了后缀的图都会保留在cos中
//        String webpKey = FileUtil.mainName(key) + ".webp";
//        PicOperations.Rule compressRule = new PicOperations.Rule();
//        compressRule.setFileId(webpKey);
//        compressRule.setBucket(cosConfigProperties.getBucketName());
        //这是按照腾讯云文档写的
//        compressRule.setRule("imageMogr2/format/webp");
//        rules.add(compressRule);
        // 缩略图处理
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosConfigProperties.getBucketName());
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
            rules.add(thumbnailRule);
        }
        // 构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

}
