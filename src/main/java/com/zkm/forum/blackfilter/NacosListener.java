package com.zkm.forum.blackfilter;

import com.alibaba.nacos.api.annotation.NacosInjected;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Nacos 监听器，nacos配置中心数据发生改变，就会被监听到
 */
@Slf4j
// todo 取消注释开启 Nacos（须先配置 Nacos）
@Component
//继承InitializingBean就会实现单次执行任务
public class NacosListener implements InitializingBean {

    @NacosInjected
    private ConfigService configService;

    @Value("${nacos.config.data-id}")
    private String dataId;

    @Value("${nacos.config.group}")
    private String group;

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("nacos 监听器启动");

        //getConfigAndSignListener获取nacos配置中心配置和注册监听器。下方两个重写是针对于注册监听器的重写
        String config = configService.getConfigAndSignListener(dataId, group, 3000L, new Listener() {

            final ThreadFactory threadFactory = new ThreadFactory() {
                private final AtomicInteger poolNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setName("refresh-ThreadPool" + poolNumber.getAndIncrement());
                    return thread;
                }
            };
            final ExecutorService executorService = Executors.newFixedThreadPool(1, threadFactory);

            // 通过线程池异步处理黑名单变化的逻辑，使用什么方式处理黑名单变化后的逻辑
            @Override
            public Executor getExecutor() {
                return executorService;
            }

            // 监听后续黑名单变化，nacos配置中心发生变化后就会执行下方的方法
            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("监听到配置信息变化：{}", configInfo);
                BlackIpUtils.rebuildBlackIp(configInfo);
            }
        });
        log.info("监听到配置信息变化：{}", config);

        // 初始化黑名单，项目首次启动时也需要获取配置，把黑名单信息从nacos配置中心放到布隆过滤器中
        BlackIpUtils.rebuildBlackIp(config);
    }
}