package com.zkm.forum.strategy.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.region.Region;
import com.zkm.forum.common.ErrorCode;
import com.zkm.forum.config.properties.CosConfigProperties;
import com.zkm.forum.exception.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service("cosUploadStrategyImpl")
public class CosUploadStrategyImpl extends AbstractUploadStrategyImpl {

    @Autowired
    private CosConfigProperties cosConfigProperties; // 需要自定义配置类

    @Override
    public Boolean exists(String filePath) {
        return getCosClient().doesObjectExist(cosConfigProperties.getBucketName(), filePath);
    }

    @Override
    public void upload(String path, String fileName, InputStream inputStream) {
        // 创建上传对象的元数据
        try{
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(inputStream.available());

            getCosClient().putObject(
                    cosConfigProperties.getBucketName(),
                    path + fileName,
                    inputStream,
                    metadata
            );
        }catch (Exception e){
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"上传失败请稍后再试");
        }

    }

    @Override
    public String getFileAccessUrl(String filePath) {
        // COS直接返回完整访问URL（需确保Bucket为公有读或已配置CDN）
        return "https://" + cosConfigProperties.getBucketName() + 
               ".cos." + cosConfigProperties.getEndpoint() +
               ".myqcloud.com/" + filePath;
    }

    private COSClient getCosClient() {
        // 1. 初始化用户身份信息
        COSCredentials cred = new BasicCOSCredentials(
            cosConfigProperties.getAccessKeyId(),
            cosConfigProperties.getAccessKeySecret()
        );

        // 2. 设置bucket的地域
        Region region = new Region(cosConfigProperties.getEndpoint());

        // 3. 配置客户端参数
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https); // 使用HTTPS协议

        return new COSClient(cred, clientConfig);
    }
}