package com.zkm.forum.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.DeleteObjectsResult;
import com.qcloud.cos.region.Region;
import com.zkm.forum.config.properties.CosConfigProperties;
import com.zkm.forum.model.entity.FitnessImage;
import com.zkm.forum.service.FitnessImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
//@Component
public class DeleteFailureCosJob {
    @Resource
    private FitnessImageService fitnessImageService;

    @Resource
    private CosConfigProperties cosConfigProperties; // 腾讯云COS配置

    // 每天的早上5点，上午11点，下午17点各执行一次
    @Scheduled(cron = "0 0 5,11,17 * * ?")
    public void work() {
        // 1. 查询需要删除的图片记录
        QueryWrapper<FitnessImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("type", 3)
                .select("id", "pictureUrl");
        List<FitnessImage> fitnessImageList = fitnessImageService.list(queryWrapper);

        // 2. 提取ID列表和文件路径
        List<Long> idList = fitnessImageList.stream()
                .map(FitnessImage::getId)
                .collect(Collectors.toList());

        List<String> filePaths = fitnessImageList.stream()
                .map(fitnessImage -> extractFilePathFromUrl(
                        fitnessImage.getPictureUrl(),
                        cosConfigProperties.getUrl()))
                .collect(Collectors.toList());

        // 3. 从COS删除文件
        deleteFileFromCOS(filePaths);

        // 4. 从数据库删除记录
        fitnessImageService.removeByIds(idList);
    }

    /**
     * 从URL中提取文件路径
     */
    private String extractFilePathFromUrl(String url, String baseUrl) {
        if (url.contains(baseUrl)) {
            int startIndex = url.indexOf(baseUrl) + baseUrl.length();
            return url.substring(startIndex);
        } else {
            throw new IllegalArgumentException("提供的URL不匹配指定的COS域名: " + baseUrl);
        }
    }

    /**
     * 从腾讯云COS删除多个文件
     */

    /**
     * 从腾讯云COS删除多个文件
     */
    private void deleteFileFromCOS(List<String> filePaths) {
        // 1. 初始化COS客户端
        COSCredentials cred = new BasicCOSCredentials(
                cosConfigProperties.getAccessKeyId(),
                cosConfigProperties.getAccessKeySecret());

        ClientConfig clientConfig = new ClientConfig(new Region(cosConfigProperties.getEndpoint()));
        clientConfig.setHttpProtocol(HttpProtocol.https); // 使用HTTPS协议

        COSClient cosClient = new COSClient(cred, clientConfig);

        try {
            // 2. 批量删除请求
            DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(cosConfigProperties.getBucketName());

            // 将 filePaths 转换为 List<KeyVersion>
            List<DeleteObjectsRequest.KeyVersion> keyVersions = filePaths.stream()
                    .map(filePath -> new DeleteObjectsRequest.KeyVersion(filePath))
                    .collect(Collectors.toList());

            deleteRequest.setKeys(keyVersions);

            // 3. 执行删除
            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteRequest);
            List<DeleteObjectsResult.DeletedObject> deletedObjects = deleteObjectsResult.getDeletedObjects();

            log.info("成功删除 {} 个文件", deletedObjects.size());
        } catch (Exception e) {
            log.error("删除COS文件失败: {}", e.getMessage(), e);
        } finally {
            // 4. 关闭客户端
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }


}
